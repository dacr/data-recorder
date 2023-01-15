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
  val websocketTestEndPoint =
    endpoint
      .tag("Test")
      .name("Example websocket generating random json events every 2 seconds")
      .summary("Receive application service events")
      .description("Example websocket endpoint which can be used to test a recorder")
      .out(webSocketBody[String, CodecFormat.TextPlain, TestJsonOutput, CodecFormat.Json](ZioStreams))
      .get
      .in("ws")
      .in("test")
      .in("json-random-stream")

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

  val statusForUnknownRecorderIssue   = oneOfVariant(StatusCode.NotFound, jsonBody[ErrorUnknownRecorder].description("Recorder does not exist"))
  val statusForUnknownWebhookIssue    = oneOfVariant(StatusCode.NotFound, jsonBody[ErrorUnknownWebhook].description("Unknown webhook for given recorder"))
  val statusForUnknownEchoIssue       = oneOfVariant(StatusCode.NotFound, jsonBody[ErrorUnknownWebsocket].description("Unknown echo for given recorder"))
  val statusForExpiredRecorderIssue   = oneOfVariant(StatusCode(480), jsonBody[ErrorExpiredRecorder].description("Recorder has expired, it is now read only"))
  val statusForForbiddenRecorderIssue = oneOfVariant(StatusCode.Forbidden, jsonBody[ErrorForbiddenRecorder].description("Recorder access denied, a valid secret token is mandatory"))

  val pathRecorderUUID = path[UUID]("recorderUUID").example(UUID.fromString("522f7400-2a1d-4820-9328-286203f07940"))
  val pathWebhookUUID  = path[UUID]("webhookUUID").example(UUID.fromString("534a83e7-2767-4783-8b55-048e45388825"))
  val pathEchoUUID     = path[UUID]("echoUUID").example(UUID.fromString("034f7765-68b7-4700-81ea-f5377fe9ee19"))

  private val recorderEndpoint =
    endpoint
      .tag("Recorder management")
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

  // -------------------------------------------------------------------------------------------------------------------

  private val recorderEchoEndpoint =
    endpoint
      .tag("Recorder echoes management")
      .in("api")
      .in("recorder")
      .in(pathRecorderUUID)
      .in("echo")

  val recorderEchoListEndpoint =
    recorderEchoEndpoint
      .name("List recorder echoes")
      .summary("List all echoes attached to this recorder")
      .description("Returns the list of all attached echoes to the given recorder")
      .get
      .errorOut(oneOf(statusForUnknownRecorderIssue))
      .out(jsonBody[List[Echo]])

  val recorderEchoWebsocketCreateEndpoint =
    recorderEchoEndpoint
      .name("Create websocket echo")
      .summary("Attach a new echo to the given recorder")
      .description("")
      .post
      .in("websocket")
      .out(jsonBody[EchoWebsocket])
      .errorOut(oneOf(statusForUnknownRecorderIssue, statusForUnknownEchoIssue))

  val recorderEchoWebhookCreateEndpoint =
    recorderEchoEndpoint
      .name("Create webhook echo")
      .summary("Attach a new echo to the given recorder")
      .description("")
      .post
      .in("webhook")
      .out(jsonBody[EchoWebhook])
      .errorOut(oneOf(statusForUnknownRecorderIssue, statusForUnknownEchoIssue))

  val recorderEchoGetEndpoint =
    recorderEchoEndpoint
      .name("Get echo")
      .summary("")
      .description("Returns all echo information for the given recorder")
      .get
      .in(pathEchoUUID)
      .errorOut(oneOf(statusForUnknownRecorderIssue, statusForUnknownEchoIssue))

  val recorderEchoDeleteEndpoint =
    recorderEchoEndpoint
      .name("Delete attached echo")
      .summary("Delete the attached echo for the selected recorder")
      .description("Delete immediately the given echo for the selected recorder")
      .delete
      .in(pathEchoUUID)
      .errorOut(oneOf(statusForUnknownRecorderIssue, statusForUnknownEchoIssue))

  // ===================================================================================================================

  private val echoesEndpoint =
    endpoint
      .tag("Echoes query")
      .in("api")
      .in("echo")
      .in(pathEchoUUID)

  val echoesDataGetEndpoint =
    echoesEndpoint
      .name("Get recorded data")
      .summary("Get all recorded data for the given recorder")
      .description("Returns a stream of all recorded data in chronological order")
      .get
      .errorOut(oneOf(statusForUnknownRecorderIssue, statusForForbiddenRecorderIssue))
    // TODO add secret token

  // -------------------------------------------------------------------------------------------------------------------

  private val echoesWebhookWriteEndpoint =
    endpoint
      .tag("Echoes webhooks")
      .in("api")
      .in("echo")
      .in(pathEchoUUID)
      .in("webhook")
      .in("data")

  val echoesWebhookAddDataGetEndpoint =
    echoesWebhookWriteEndpoint
      .name("Send data")
      .summary("Send data to this webhook endpoint using simple GET request and query params")
      .description("Send any data as a HTTP GET request")
      .get
      .errorOut(oneOf(statusForUnknownEchoIssue, statusForExpiredRecorderIssue))

  val echoesWebhookAddDataPostEndpoint =
    echoesWebhookWriteEndpoint
      .name("Send data")
      .summary("Send data to this webhook endpoint")
      .description("Send any data as a HTTP POST request")
      .post
      .errorOut(oneOf(statusForUnknownEchoIssue, statusForExpiredRecorderIssue))

  val echoesWebhookAddDataPutEndpoint =
    echoesWebhookWriteEndpoint
      .name("Send data")
      .summary("Send data to this webhook endpoint")
      .description("Send any data as a HTTP PUT request")
      .put
      .errorOut(oneOf(statusForUnknownEchoIssue, statusForExpiredRecorderIssue))

  // ===================================================================================================================

  val apiEndpoints = List(
    // -------------------------------
    recorderCreateEndpoint,
    recorderGetEndpoint,
    recorderDeleteEndPoint,
    // -------------------------------
    recorderEchoListEndpoint,
    recorderEchoWebsocketCreateEndpoint,
    recorderEchoWebhookCreateEndpoint,
    recorderEchoGetEndpoint,
    recorderEchoDeleteEndpoint,
    // -------------------------------
    echoesWebhookAddDataPostEndpoint,
    echoesWebhookAddDataPutEndpoint,
    echoesWebhookAddDataGetEndpoint,
    // -------------------------------
    echoesDataGetEndpoint,
    // -------------------------------
    serviceEventsEndpoint,
    // -------------------------------
    websocketTestEndPoint,
    // -------------------------------
    systemPingEndpoint,
    systemStatusEndpoint
    // -------------------------------
  )

}
