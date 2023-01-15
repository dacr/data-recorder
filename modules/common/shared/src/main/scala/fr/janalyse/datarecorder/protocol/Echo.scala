package fr.janalyse.datarecorder.protocol

import zio.json.*

import java.time.OffsetDateTime
import java.util.UUID
import sttp.tapir.Schema.annotations.*

sealed trait Echo {
  @description("Echo unique identifier")
  def uuid: UUID
  @description("Echo name")
  def name: String
}

object Echo {
  given JsonCodec[Echo] = DeriveJsonCodec.gen
}

// -----------------------------------------------------------------------------------------------

@description("Webhook to be used by clients to send data using GET/PUT/POST http requests")
case class EchoWebhook(
  @description("Echo unique identifier")
  uuid: UUID,
  @description("Echo name")
  name: String,
  @description("Webhook generated URL")
  webhookURL: String
) extends Echo

object EchoWebhook {
  val defaultEchoName          = "default-webhook"
  given JsonCodec[EchoWebhook] = DeriveJsonCodec.gen
}

// -----------------------------------------------------------------------------------------------

@description("Websocket to extract data from")
case class EchoWebsocket(
  @description("Echo unique identifier")
  uuid: UUID,
  @description("Echo name")
  name: String,
  @description("Given websocket to connect to")
  @encodedExample("wss://ws.postman-echo.com/raw")
  websocketURL: String
) extends Echo

object EchoWebsocket {
  given JsonCodec[EchoWebsocket] = DeriveJsonCodec.gen
}
