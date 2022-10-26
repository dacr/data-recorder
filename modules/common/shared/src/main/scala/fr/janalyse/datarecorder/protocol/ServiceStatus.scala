package fr.janalyse.datarecorder.protocol

import zio.json.*

case class ServiceStatus(
  version: String,
  alive: Boolean
)

object ServiceStatus {
  given JsonCodec[ServiceStatus] = DeriveJsonCodec.gen
}