package fr.janalyse.datarecorder.protocol

import zio.json.*
import java.util.UUID

case class ErrorUnknownRecorder(
  recorderUUID: UUID
)

object ErrorUnknownRecorder {
  given JsonCodec[ErrorUnknownRecorder] = DeriveJsonCodec.gen
}

case class ErrorForbiddenRecorder(
  recorderUUID: UUID
)

object ErrorForbiddenRecorder {
  given JsonCodec[ErrorForbiddenRecorder] = DeriveJsonCodec.gen
}

case class ErrorExpiredRecorder(
  recorderUUID: UUID
)
object ErrorExpiredRecorder   {
  given JsonCodec[ErrorExpiredRecorder] = DeriveJsonCodec.gen
}

case class ErrorUnknownWebsocket(
  recorderUUID: UUID,
  websocketUUID: UUID
)

object ErrorUnknownWebsocket {
  given JsonCodec[ErrorUnknownWebsocket] = DeriveJsonCodec.gen
}

case class ErrorUnknownWebhook(
  recorderUUID: UUID,
  webhookUUID: UUID
)

object ErrorUnknownWebhook {
  given JsonCodec[ErrorUnknownWebhook] = DeriveJsonCodec.gen
}
