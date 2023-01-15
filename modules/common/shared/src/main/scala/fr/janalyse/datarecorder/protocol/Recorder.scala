package fr.janalyse.datarecorder.protocol

import zio.json.*

import java.time.OffsetDateTime
import java.util.UUID
import sttp.tapir.Schema.annotations.*

@description("Recorder to store and query easily any kind of data")
case class Recorder(
  @description("Record unique identifier")
  uuid: UUID,
  @description("URL of the endpoint to retrieve the recorded data")
  echoesDataURL: String,
  @description("Default webhook echo automatically created when this recorder has been created")
  defaultWebhook: Option[EchoWebhook],
  @description("The secret token required to get access to recorder data, only given when the recorder is create")
  secretToken: Option[String],
  @description("When webhooks and websockets will become disabled, only data read operations will remain possible")
  expireDate: Option[OffsetDateTime]
)

object Recorder {
  given JsonCodec[Recorder] = DeriveJsonCodec.gen
}
