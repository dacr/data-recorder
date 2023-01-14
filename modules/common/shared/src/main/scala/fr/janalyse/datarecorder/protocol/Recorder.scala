package fr.janalyse.datarecorder.protocol

import zio.json.*

import java.time.OffsetDateTime
import java.util.UUID
import sttp.tapir.Schema.annotations.*

@description("Webhook to be use by clients to send data using GET/PUT/POST http requests")
case class Webhook(
  @description("Webhook generated URL")
  webhookURL: Option[String],
  @description("Webhook unique identifier")
  webhookUUID: Option[UUID]
)

object Webhook {
  given JsonCodec[Webhook] = DeriveJsonCodec.gen
}

@description("Recorder to store and query easily any kind of data")
case class Recorder(
  @description("Record unique identifier")
  uuid: UUID,
  @description("URL of the endpoint to retrieve the recorded data")
  echoesDataURL: String,
  @description("Default webhook automatically created when this recorder has been created, if it hasn't been deleted")
  webhook: Option[Webhook],
  @description("The secret token required to get access to recorder data, only given when the recorder is create")
  secretToken: Option[String],
  @description("When webhooks and websockets will become disabled, only data read operations will remain possible")
  expireDate: Option[OffsetDateTime]
)

object Recorder {
  given JsonCodec[Recorder] = DeriveJsonCodec.gen
}
