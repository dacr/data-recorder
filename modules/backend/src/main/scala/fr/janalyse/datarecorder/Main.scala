package fr.janalyse.datarecorder

import fr.janalyse.datarecorder.protocol.*
import sttp.capabilities.WebSockets
import sttp.model.StatusCode
import sttp.tapir.json.zio.*
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir.*
import zio.*
import zio.json.*
import zio.stream.*
import org.http4s.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router

object Main extends ZIOAppDefault {

  import DataRecorderEndPoints.*

  type DataRecorderEnv = DataRecorderService

  type DataRecorderTask[A] = RIO[DataRecorderEnv, A]

  // -------------------------------------------------------------------------------------------------------------------

  val pingRoutes: HttpRoutes[DataRecorderTask] =
    ZHttp4sServerInterpreter()
      .from(systemPingEndpoint.zServerLogic(_ => ZIO.succeed("pong")))
      .toRoutes

  // -------------------------------------------------------------------------------------------------------------------
  val serviceStatusLogic = for {
    dataRecorderService <- ZIO.service[DataRecorderService]
    serviceStatus       <- dataRecorderService.serviceStatus
  } yield serviceStatus

  val serviceStatusRoutes: HttpRoutes[DataRecorderTask] =
    ZHttp4sServerInterpreter()
      .from(systemStatusEndpoint.zServerLogic[DataRecorderEnv](_ => serviceStatusLogic))
      .toRoutes

  // -------------------------------------------------------------------------------------------------------------------
  val serviceEventsEndpointLogic =
    ZIO.succeed((clientMessageStream: Stream[Throwable, ClientMessage]) =>
      ZStream
        .tick(500.millis)
        .zipWith(ZStream("A", "B", "C", "D").repeat(Schedule.forever))((_, c) => ServerMessage(c))
    )

  val serviceEventsRoutes =
    ZHttp4sServerInterpreter()
      .fromWebSocket(serviceEventsEndpoint.zServerLogic[DataRecorderEnv](_ => serviceEventsEndpointLogic))
      .toRoutes

  // -------------------------------------------------------------------------------------------------------------------

  def swaggerRoutes: HttpRoutes[DataRecorderTask] = {
    import sttp.tapir.swagger.bundle.SwaggerInterpreter
    import sttp.apispec.openapi.Info
    ZHttp4sServerInterpreter()
      .from(
        SwaggerInterpreter().fromEndpoints[DataRecorderTask](
          apiEndpoints,
          Info(title = "DATA RECORDER API", version = "1.0.0", description = Some("Data recorder by @crodav"))
        )
      )
      .toRoutes
  }

  //def asyncapiRoute: HttpRoutes[DataRecorderTask] = {
  val docs = {
    import sttp.tapir.docs.asyncapi.AsyncAPIInterpreter
    import sttp.apispec.asyncapi.Info
    import sttp.tapir.generic.auto._
    //import sttp.tapir.json.circe._
    import io.circe.generic.auto._

    val docs = AsyncAPIInterpreter().toAsyncAPI(
      apiEndpoints,
      Info(title = "DATA RECORDER API", version = "1.0.0", description = Some("Data recorder by @crodav"))
    )

    import sttp.apispec.asyncapi.circe.yaml._

    println(docs.toYaml)

    //ZHttp4sServerInterpreter().from(docs)
    docs
  }

  // -------------------------------------------------------------------------------------------------------------------

  def webService = {
    import zio.interop.catz.*
    import cats.syntax.all.*
    import cats.implicits.*

    //val routes = pingRoutes <+> serviceStatusRoutes <+> asyncapiRoute
    val routes = pingRoutes <+> serviceStatusRoutes <+> swaggerRoutes

    // Starting the server
    val serverApp: ZIO[DataRecorderEnv, Throwable, Unit] = {
      ZIO.executor.flatMap(executor =>
        BlazeServerBuilder[DataRecorderTask]
          .withExecutionContext(executor.asExecutionContext)
          .bindHttp(8080, "localhost")
          // .withHttpApp(Router("/" -> routes).orNotFound)
          .withHttpWebSocketApp(wsb => Router("/" -> (serviceEventsRoutes(wsb) <+> routes)).orNotFound)
          .serve
          .compile
          .drain
      )
    }

    serverApp
  }

  override def run = webService.provideLayer(DataRecorderService.live)

}
