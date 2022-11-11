package fr.janalyse.datarecorder.protocol

import zio.json.*

case class ServerMessage(message: String)
object ServerMessage {
  given JsonCodec[ServerMessage] = DeriveJsonCodec.gen
}
