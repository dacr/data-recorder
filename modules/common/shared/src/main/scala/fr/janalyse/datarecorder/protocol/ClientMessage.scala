package fr.janalyse.datarecorder.protocol
import sttp.tapir.Schema.annotations.*
import zio.json.*

@description("Generic message coming from the client")
case class ClientMessage(
  message: String
)

object ClientMessage {
  given JsonCodec[ClientMessage] = DeriveJsonCodec.gen
}