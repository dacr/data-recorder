package fr.janalyse.datarecorder.protocol

import zio.*
import zio.stream.*
import sttp.client3.*

import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

import sttp.capabilities.zio.ZioStreams

import sttp.model.HeaderNames

import java.nio.charset.StandardCharsets

object DataRecorderEndPoints {

  private val systemEndpoint = endpoint.in("api").in("system").tag("System")

  val pingEndpoint =
    systemEndpoint
      .name("Ping application service")
      .summary("Just get a very simple pong response")
      .description("Returns pong, this is the faster and simplesdt backend health check")
      .in("ping")
      .get
      .out(stringBody)

  val serviceStatusEndpoint =
    systemEndpoint
      .name("Application service status")
      .summary("Get the application service status")
      .description("Returns the service status, can also be used as a health check end point for monitoring purposes")
      .in("status")
      .get
      .out(jsonBody[ServiceStatus])

  val serviceEventsEndpoint =
    endpoint
      .in("ws")
      .in("system")
      .in("events")
      .tag("System")
      .name("Application service events")
      .summary("Receive application service events")
      .description("Receive broadcasted application service events")
      .out(webSocketBody[ClientMessage, CodecFormat.Json, ServerMessage, CodecFormat.Json](ZioStreams))
}
