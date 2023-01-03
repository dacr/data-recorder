package fr.janalyse.datarecorder.protocol

import zio.json.*
import java.util.UUID

case class User(
  uuid: UUID,
  pseudo: String
)

object User {
  given JsonCodec[User] = DeriveJsonCodec.gen
}
