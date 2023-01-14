package fr.janalyse.datarecorder.protocol

import zio.json.*

import java.time.OffsetDateTime
import java.util.UUID

case class Recorder(
  uuid: UUID,
  webhookURL: String,
  readURL: String,
  expireDate: Option[OffsetDateTime],
  owner: Option[User],
  secretToken: String // Mandatory in order to to get access to recorder data
)

object Recorder {
  given JsonCodec[Recorder] = DeriveJsonCodec.gen
}
