package fr.janalyse.datarecorder.protocol

import zio.*
import sttp.tapir.*
import sttp.client3.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

object DataRecorderEndPoints {

  private val systemEndpoint = endpoint.in("api").in("system").tag("System")

  val serviceStatusEndpoint =
    systemEndpoint
      .name("Game service status")
      .summary("Get the game service status")
      .description("Returns the service status, can also be used as a health check end point for monitoring purposes")
      .get
      .in("status")
      .out(jsonBody[ServiceStatus])
}
