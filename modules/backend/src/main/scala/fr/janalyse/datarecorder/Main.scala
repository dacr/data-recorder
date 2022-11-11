package fr.janalyse.datarecorder

import fr.janalyse.datarecorder.protocol.*
import sttp.apispec.openapi.Info
import sttp.capabilities.WebSockets
import sttp.model.StatusCode
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.*
import zio.*
import zio.json.*
import zio.stream.*
import sttp.capabilities.zio.ZioStreams

object Main extends ZIOAppDefault {

  type DataRecorderEnv = DataRecorderService

  import DataRecorderEndPoints.*

  // -------------------------------------------------------------------------------------------------------------------
  val serviceStatusLogic = for {
    dataRecorderService <- ZIO.service[DataRecorderService]
    serviceStatus       <- dataRecorderService.serviceStatus
  } yield serviceStatus

  val serviceStatusEndpointImpl =
    serviceStatusEndpoint
      .zServerLogic[DataRecorderEnv](_ => serviceStatusLogic)

  // -------------------------------------------------------------------------------------------------------------------
  val serviceEventsEndpointLogic =
    ZIO.succeed((clientMessageStream: Stream[Throwable, ClientMessage]) =>
      ZStream
        .tick(500.millis)
        .zipWith(ZStream("A", "B", "C", "D").repeat(Schedule.forever))((_, c) => ServerMessage(c))
    )

  val serviceEventsEndpointImpl =
    serviceEventsEndpoint
      .zServerLogic[DataRecorderEnv](_ => serviceEventsEndpointLogic)

  // -------------------------------------------------------------------------------------------------------------------
  val apiRoutes = List(
    serviceStatusEndpointImpl,
    serviceEventsEndpointImpl
  )

  def apiDocRoutes =
    SwaggerInterpreter()
      .fromServerEndpoints(
        apiRoutes,
        Info(title = "ZWORDS Game API", version = "2.0", description = Some("A wordle like game as an API by @BriossantC and @crodav"))
      )

  // -------------------------------------------------------------------------------------------------------------------

  def webService = for {
    _        <- ZIO.logInfo("Starting webService")
    routes    = apiRoutes ++ apiDocRoutes
    httpApp   = ZioHttpInterpreter().toHttp(routes)
    zservice <- zhttp.service.Server.start(8080, httpApp)
  } yield zservice

  override def run =
    webService.provide(DataRecorderService.live)

}
