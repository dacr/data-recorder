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
      // .out(statusCode(StatusCode.SwitchingProtocols)) // not needed, this is the default with webSocketbody
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

  val statusForUnknownRecorderIssue  = oneOfVariant(StatusCode.NotFound, jsonBody[UnknownRecorderError].description("Recorder does not exist"))
  val statusForUnknownWebhookIssue   = oneOfVariant(StatusCode.NotFound, jsonBody[UnknownWebhookError].description("Unknown webhook for given recorder"))
  val statusForUnknownWebsocketIssue = oneOfVariant(StatusCode.NotFound, jsonBody[UnknownWebsocketError].description("Unknown websocket for given recorder"))

  val pathRecorderUUID  = path[UUID]("recorderUUID").example(UUID.fromString("522f7400-2a1d-4820-9328-286203f07940"))
  val pathWebhookUUID   = path[UUID]("webhookUUID").example(UUID.fromString("534a83e7-2767-4783-8b55-048e45388825"))
  val pathWebsocketUUID = path[UUID]("websocketUUID").example(UUID.fromString("034f7765-68b7-4700-81ea-f5377fe9ee19"))

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
      .in(pathRecorderUUID)

  private val echoesWebhookEndpoint =
    endpoint
      .tag("Echoes Webhook")
      .in("api")
      .in("echoes")
      .in(pathRecorderUUID)
      .in("webhook")

  private val echoesWebsocketEndpoint =
    endpoint
      .tag("Echoes Websocket")
      .in("api")
      .in("echoes")
      .in(pathRecorderUUID)
      .in("websocket")

  val echoesDataGetEndpoint =
    echoesEndpoint
      .name("Get recorded data")
      .summary("Get all recorded data for the given recorder")
      .description("Returns a stream of all recorded data in chronological order")
      .get
      .errorOut(oneOf(statusForUnknownRecorderIssue))

  // -------------------------------------------------------------------------------------------------------------------

  val echoesWebhookAddDataGetEndpoint =
    echoesWebhookEndpoint
      .name("Send data")
      .summary("Send data to this webhook endpoint using simple GET request and query params")
      .description("Send any data as a HTTP GET request")
      .get
      .in(pathWebhookUUID)
      .in("data")
      .errorOut(oneOf(statusForUnknownRecorderIssue, statusForUnknownWebhookIssue))

  val echoesWebhookAddDataPostEndpoint =
    echoesWebhookEndpoint
      .name("Send data")
      .summary("Send data to this webhook endpoint")
      .description("Send any data as a HTTP POST request")
      .post
      .in(pathWebhookUUID)
      .in("data")
      .errorOut(oneOf(statusForUnknownRecorderIssue, statusForUnknownWebhookIssue))

  val echoesWebhookAddDataPutEndpoint =
    echoesWebhookEndpoint
      .name("Send data")
      .summary("Send data to this webhook endpoint")
      .description("Send any data as a HTTP PUT request")
      .put
      .in(pathWebhookUUID)
      .in("data")
      .errorOut(oneOf(statusForUnknownRecorderIssue, statusForUnknownWebhookIssue))

  // -------------------------------------------------------------------------------------------------------------------

  val echoesWebsocketListEndpoint =
    echoesWebsocketEndpoint
      .name("List recorder websockets")
      .summary("List all websockets attached to this recorder")
      .description("Returns the list of all attached websockets to the given recorder")
      .get
      .errorOut(oneOf(statusForUnknownRecorderIssue))

  val echoesWebsocketCreateEndpoint =
    echoesWebsocketEndpoint
      .name("Attach websocket")
      .summary("Attach a new websocket to the given recorder")
      .description("")
      .post
      .errorOut(oneOf(statusForUnknownRecorderIssue, statusForUnknownWebsocketIssue))

  val echoesWebSocketGetEndpoint =
    echoesWebsocketEndpoint
      .name("Get websocket")
      .summary("")
      .description("Returns all websocket information for the given recorder")
      .get
      .in(pathWebsocketUUID)
      .errorOut(oneOf(statusForUnknownRecorderIssue, statusForUnknownWebsocketIssue))

  val echoesWebsocketDeleteEndpoint =
    echoesWebsocketEndpoint
      .name("Delete attached websocket")
      .summary("Delete the attached websocket for the selected recorder")
      .description("Delete immediately the given websocket for the selected recorder")
      .delete
      .in(pathWebsocketUUID)
      .errorOut(oneOf(statusForUnknownRecorderIssue, statusForUnknownWebsocketIssue))

  // ===================================================================================================================

  val apiEndpoints = List(
    // -------------------------------
    recorderCreateEndpoint,
    recorderGetEndpoint,
    recorderDeleteEndPoint,
    // -------------------------------
    echoesDataGetEndpoint,
    // -------------------------------
    echoesWebhookAddDataPostEndpoint,
    echoesWebhookAddDataPutEndpoint,
    echoesWebhookAddDataGetEndpoint,
    // -------------------------------
    echoesWebsocketListEndpoint,
    echoesWebsocketCreateEndpoint,
    echoesWebSocketGetEndpoint,
    echoesWebsocketDeleteEndpoint,
    // -------------------------------
    serviceEventsEndpoint,
    // -------------------------------
    systemPingEndpoint,
    systemStatusEndpoint
    // -------------------------------
  )

}
