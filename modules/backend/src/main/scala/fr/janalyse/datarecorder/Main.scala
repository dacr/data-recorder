package fr.janalyse.datarecorder

import fr.janalyse.datarecorder.protocol.*
import sttp.apispec.openapi.Info
import sttp.capabilities.WebSockets
import sttp.model.StatusCode
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.*
import zio.*
import zio.json.*
import zio.stream.*
import zio.interop.catz.*
import cats.syntax.all.*
import cats.implicits.*
import sttp.capabilities.zio.ZioStreams
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
      .from(pingEndpoint.zServerLogic(_ => ZIO.succeed("pong")))
      .toRoutes

  // -------------------------------------------------------------------------------------------------------------------
  val serviceStatusLogic = for {
    dataRecorderService <- ZIO.service[DataRecorderService]
    serviceStatus       <- dataRecorderService.serviceStatus
  } yield serviceStatus

  val serviceStatusRoutes: HttpRoutes[DataRecorderTask] =
    ZHttp4sServerInterpreter()
      .from(serviceStatusEndpoint.zServerLogic[DataRecorderEnv](_ => serviceStatusLogic))
      .toRoutes

  // -------------------------------------------------------------------------------------------------------------------
//  val serviceEventsEndpointLogic =
//    ZIO.succeed((clientMessageStream: Stream[Throwable, ClientMessage]) =>
//      ZStream
//        .tick(500.millis)
//        .zipWith(ZStream("A", "B", "C", "D").repeat(Schedule.forever))((_, c) => ServerMessage(c))
//    )
//
//  val serviceEventsEndpointImpl =
//    serviceEventsEndpoint
//      .zServerLogic[DataRecorderEnv](_ => serviceEventsEndpointLogic)

  // -------------------------------------------------------------------------------------------------------------------
  val apiRoutes = List(
    pingEndpoint,
    serviceStatusEndpoint
  )

  def swaggerRoutes: HttpRoutes[DataRecorderTask] =
    ZHttp4sServerInterpreter()
      .from(
        SwaggerInterpreter().fromEndpoints[DataRecorderTask](
          apiRoutes,
          Info(title = "ZWORDS Game API", version = "2.0", description = Some("A wordle like game as an API by @BriossantC and @crodav"))
        )
      )
      .toRoutes

  // -------------------------------------------------------------------------------------------------------------------

  def webService = {
    val routes = pingRoutes <+> serviceStatusRoutes <+> swaggerRoutes

    // Starting the server
    val serverApp: ZIO[DataRecorderEnv, Throwable, Unit] = {
      ZIO.executor.flatMap(executor =>
        BlazeServerBuilder[DataRecorderTask]
          .withExecutionContext(executor.asExecutionContext)
          .bindHttp(8080, "localhost")
          .withHttpApp(Router("/" -> routes).orNotFound)
          .serve
          .compile
          .drain
      )
    }

    serverApp
  }

  override def run = webService.provide(DataRecorderService.live)

}
