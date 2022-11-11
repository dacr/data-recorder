package fr.janalyse.datarecorder.protocol
import zio.json.*

case class ClientMessage(message: String)
object ClientMessage {
  given JsonCodec[ClientMessage] = DeriveJsonCodec.gen
}