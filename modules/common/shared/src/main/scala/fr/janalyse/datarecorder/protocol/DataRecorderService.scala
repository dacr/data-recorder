package fr.janalyse.datarecorder.protocol

import zio.*

trait DataRecorderService {
  def serviceStatus:UIO[ServiceStatus]
}
