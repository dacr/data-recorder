package fr.janalyse.datarecorder.protocol

import sttp.tapir.Schema.annotations.*
import zio.json.*

@description("Generic feedback message coming from the client")
case class ClientEvent(
  message: String
)

object ClientEvent {
  given JsonCodec[ClientEvent] = DeriveJsonCodec.gen
}

@description("Generic broadcasted message coming from the backend")
case class ServerEvent(
  message: String
)

object ServerEvent {
  given JsonCodec[ServerEvent] = DeriveJsonCodec.gen
}
