package fr.janalyse.datarecorder.protocol

import zio.json.*

import java.time.OffsetDateTime
import sttp.tapir.Schema.annotations.*

@description("Websocket debug json message")
case class TestJsonOutput(
  @description("When this message has been forged")
  timestamp: OffsetDateTime,
  @description("Message echo or automatically generated content")
  message: String
)

object TestJsonOutput {
  given JsonCodec[TestJsonOutput] = DeriveJsonCodec.gen
}
