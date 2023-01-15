package fr.janalyse.datarecorder.protocol

import zio.json.*

import java.time.OffsetDateTime

case class TestJsonOutput(
  timestamp: OffsetDateTime,
  message: String
)

object TestJsonOutput {
  given JsonCodec[TestJsonOutput] = DeriveJsonCodec.gen
}