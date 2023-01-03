package fr.janalyse.datarecorder.protocol

import zio.*
import zio.stream.*
import sttp.client3.*
import java.util.UUID

import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

import sttp.capabilities.zio.ZioStreams

import sttp.model.StatusCode
import sttp.model.HeaderNames

import java.nio.charset.StandardCharsets

object DataRecorderEndPoints {

  // ===================================================================================================================

  val serviceEventsEndpoint =
    endpoint
      .tag("System")
      .name("Application service events")
      .summary("Receive application service events")
      .description("Receive broadcasted application service events")
      .out(webSocketBody[ClientMessage, CodecFormat.Json, ServerMessage, CodecFormat.Json](ZioStreams))
      .get
      .in("ws")
      .in("system")
      .in("events")

  // ===================================================================================================================

  private val systemEndpoint =
    endpoint
      .tag("System")
      .in("api")
      .in("system")

  val systemPingEndpoint =
    systemEndpoint
      .name("Ping application service")
      .summary("Get simple pong response")
      .description("Returns pong, this is the faster and simplest backend health check, but it won't check feature are running fine")
      .get
      .in("ping")
      .out(stringBody)

  val systemStatusEndpoint =
    systemEndpoint
      .name("Application service status")
      .summary("Get application service status")
      .description("Returns the service status, can also be used as an accurate health check end point for monitoring purposes")
      .get
      .in("status")
      .out(jsonBody[ServiceStatus])

  // ===================================================================================================================

  val statusForUnknownRecorderIssue = oneOfVariant(StatusCode.NotFound, jsonBody[UnknownRecorderError].description("Recorder does not exist"))

  val pathRecorderUUID = path[UUID]("recorderUUID").example(UUID.fromString("522f7400-2a1d-4820-9328-286203f07940"))

  private val recorderEndpoint =
    endpoint
      .tag("Recorder")
      .in("api")
      .in("recorder")

  val recorderCreateEndpoint =
    recorderEndpoint
      .name("Create an anonymous recorder")
      .summary("Create an anonymous data recorder with limited life time")
      .description("Returns the recorder identifier as well as URLs to be used for both read and write operations, a webhook endpoint is created.")
      .post
      .out(jsonBody[Recorder])

  val recorderGetEndpoint =
    recorderEndpoint
      .name("Get recorder")
      .summary("Get recorder information")
      .description("Returns recorder information")
      .get
      .in(pathRecorderUUID)
      .out(jsonBody[Recorder])
      .errorOut(oneOf(statusForUnknownRecorderIssue))

  val recorderDeleteEndPoint =
    recorderEndpoint
      .name("Delete recorder")
      .summary("Delete recorder given its identifier")
      .delete
      .in(pathRecorderUUID)
      .errorOut(oneOf(statusForUnknownRecorderIssue))

  // ===================================================================================================================

  private val echoesEndpoint =
    endpoint
      .tag("Echoes")
      .in("api")
      .in("echoes")

  val echoesGetEndpoint =
    echoesEndpoint
      .name("Get recorded data")
      .summary("Get all recorded data for the given recorder")
      .description("Returns a stream of all recorded data in chronological order")
      .in(pathRecorderUUID)
      .errorOut(oneOf(statusForUnknownRecorderIssue))

  // ===================================================================================================================

  val apiEndpoints = List(
    // -------------------------------
    recorderCreateEndpoint,
    recorderGetEndpoint,
    recorderDeleteEndPoint,
    // -------------------------------
    echoesGetEndpoint,
    // -------------------------------
    serviceEventsEndpoint,
    // -------------------------------
    systemPingEndpoint,
    systemStatusEndpoint
    // -------------------------------
  )

}
