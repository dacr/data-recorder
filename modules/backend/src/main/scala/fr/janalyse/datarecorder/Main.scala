package fr.janalyse.datarecorder

import fr.janalyse.datarecorder.protocol.*

import sttp.apispec.openapi.Info
import sttp.model.StatusCode
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.{oneOfVariant, *}
import zio.*
import zio.json.*

object Main extends ZIOAppDefault {

  type DataRecorderEnv = DataRecorderService

  import DataRecorderEndPoints.*

  val serviceStatusLogic = for {
    dataRecorderService <- ZIO.service[DataRecorderService]
    serviceStatus       <- dataRecorderService.serviceStatus
  } yield serviceStatus

  val serviceStatusEndpointImpl =
    serviceStatusEndpoint
      .zServerLogic[DataRecorderEnv](_ => serviceStatusLogic)

  val apiRoutes = List(
    serviceStatusEndpointImpl
  )

  def apiDocRoutes =
    SwaggerInterpreter()
      .fromServerEndpoints(
        apiRoutes,
        Info(title = "ZWORDS Game API", version = "2.0", description = Some("A wordle like game as an API by @BriossantC and @crodav"))
      )

  def webService = for {
    _        <- ZIO.logInfo("Starting webService")
    httpApp   = ZioHttpInterpreter().toHttp(apiRoutes ++ apiDocRoutes)
    zservice <- zhttp.service.Server.start(8080, httpApp)
  } yield zservice

  override def run =
    webService.provide(DataRecorderService.live)

}
