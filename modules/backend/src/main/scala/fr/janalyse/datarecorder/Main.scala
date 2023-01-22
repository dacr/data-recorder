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
import org.http4s.server.websocket.WebSocketBuilder2

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
    ZIO.succeed((clientMessageStream: Stream[Throwable, ClientEvent]) =>
      ZStream
        .tick(2.seconds)
        .zipWith(ZStream("A", "B", "C", "D").repeat(Schedule.forever))((_, c) => ServerEvent(c))
    )

  val serviceEventsRoutes =
    ZHttp4sServerInterpreter()
      .fromWebSocket(serviceEventsEndpoint.zServerLogic[DataRecorderEnv](_ => serviceEventsEndpointLogic))
      .toRoutes

  // -------------------------------------------------------------------------------------------------------------------
  val websocketTestBroadcastEndpointLogic =
    ZIO.succeed((clientMessageStream: Stream[Throwable, String]) =>
      ZStream
        .tick(5.seconds)
        .mapZIO(_ => Clock.currentDateTime)
        .zipWith(ZStream("A", "B", "C", "D").repeat(Schedule.forever))((timestamp, message) => TestJsonOutput(timestamp, message))
    )

  val websocketTestBroadcastRoutes =
    ZHttp4sServerInterpreter()
      .fromWebSocket(websocketTestBroadcastEndPoint.zServerLogic[DataRecorderEnv](_ => websocketTestBroadcastEndpointLogic))
      .toRoutes

  // -------------------------------------------------------------------------------------------------------------------
  val websocketTestEchoEndpointLogic =
    ZIO.succeed((clientMessageStream: Stream[Throwable, String]) => clientMessageStream.zipWithIndex.map { case (input, index) => s"$index: $input" })

  val websocketTestEchoRoutes =
    ZHttp4sServerInterpreter()
      .fromWebSocket(websocketTestEchoEndPoint.zServerLogic[DataRecorderEnv](_ => websocketTestEchoEndpointLogic))
      .toRoutes

  // -------------------------------------------------------------------------------------------------------------------

  val apiName = "DATA RECORDER API"
  val apiVersion = "1.0.0"
  val apiDescription = Some("Data recorder by @crodav")

  def swaggerDocumentationRoutes: HttpRoutes[DataRecorderTask] = {
    import sttp.tapir.swagger.bundle.SwaggerInterpreter
    import sttp.apispec.openapi.Info
    import sttp.tapir.generic.auto.* // MANDATORY TO GENERATE CASE SCHEMA SCHEMA DOCUMENTATION
    ZHttp4sServerInterpreter()
      .from(
        SwaggerInterpreter().fromEndpoints[DataRecorderTask](
          apiEndpoints,
          Info(title = apiName, version = apiVersion, description = apiDescription)
        )
      )
      .toRoutes
  }

  val asyncapiDocumentation = {
    import sttp.tapir.docs.asyncapi.AsyncAPIInterpreter
    import sttp.apispec.asyncapi.Info
    import sttp.tapir.generic.auto.* // MANDATORY TO GENERATE CASE SCHEMA SCHEMA DOCUMENTATION
    import sttp.apispec.asyncapi.circe.yaml._

    val docs = AsyncAPIInterpreter().toAsyncAPI(
      apiEndpoints,
      Info(title = apiName, version = apiVersion, description = apiDescription)
    )

    println(docs.toYaml)

    docs
  }

  val asyncapiDocumentationRoutes: HttpRoutes[DataRecorderTask] = {
    ZHttp4sServerInterpreter().from(asyncapiDocumentation)
  }


  // -------------------------------------------------------------------------------------------------------------------

  def webService = {
    import zio.interop.catz.*
    import cats.syntax.all.*
    import cats.implicits.*

    val routes =
      pingRoutes <+>
        serviceStatusRoutes <+>
        swaggerDocumentationRoutes

    def websocketRoutes(wsb: WebSocketBuilder2[DataRecorderTask]) =
      serviceEventsRoutes(wsb) <+>
        websocketTestEchoRoutes(wsb) <+>
        websocketTestBroadcastRoutes(wsb)

    // Starting the server
    val serverApp: ZIO[DataRecorderEnv, Throwable, Unit] = {
      ZIO.executor.flatMap(executor =>
        BlazeServerBuilder[DataRecorderTask]
          .withExecutionContext(executor.asExecutionContext)
          .bindHttp(8080, "localhost")
          // .withHttpApp(Router("/" -> routes).orNotFound)
          .withHttpWebSocketApp(wsb => Router("/" -> (websocketRoutes(wsb) <+> routes)).orNotFound)
          .serve
          .compile
          .drain
      )
    }

    serverApp
  }

  override def run = webService.provideLayer(DataRecorderService.live)

}
