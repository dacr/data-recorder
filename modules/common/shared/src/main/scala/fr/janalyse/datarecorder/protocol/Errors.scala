package fr.janalyse.datarecorder.protocol

import zio.json.*
import java.util.UUID

case class UnknownRecorderError(
  recorderUUID: UUID
)

object UnknownRecorderError {
  given JsonCodec[UnknownRecorderError] = DeriveJsonCodec.gen
}
