package fr.janalyse.datarecorder.protocol

import zio.json.*
import sttp.tapir.Schema.annotations.*

@description("Generic message coming from the backend")
case class ServerMessage(
  message: String
)

object ServerMessage {
  given JsonCodec[ServerMessage] = DeriveJsonCodec.gen
}
