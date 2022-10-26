package fr.janalyse.datarecorder.protocol

import zio.json.*

case class Status(
  version: String,
  alive: Boolean
)
