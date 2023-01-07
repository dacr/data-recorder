package fr.janalyse.datarecorder.protocol

import zio.json.*
import java.util.UUID

case class UnknownRecorderError(
  recorderUUID: UUID
)

object UnknownRecorderError {
  given JsonCodec[UnknownRecorderError] = DeriveJsonCodec.gen
}

case class UnknownWebsocketError(
  recorderUUID: UUID,
  websocketUUID: UUID
)

object UnknownWebsocketError {
  given JsonCodec[UnknownWebsocketError] = DeriveJsonCodec.gen
}

case class UnknownWebhookError(
  recorderUUID: UUID,
  webhookUUID: UUID
)

object UnknownWebhookError {
  given JsonCodec[UnknownWebhookError] = DeriveJsonCodec.gen
}
