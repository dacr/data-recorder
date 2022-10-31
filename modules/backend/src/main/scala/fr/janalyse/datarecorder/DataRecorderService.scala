package fr.janalyse.datarecorder

import zio.*
import fr.janalyse.datarecorder.protocol.*

trait DataRecorderService {
  def serviceStatus: UIO[ServiceStatus]
}

case class DataRecorderServiceLive() extends DataRecorderService {
  override def serviceStatus: UIO[ServiceStatus] = ZIO.succeed(ServiceStatus("0.1.0", true))
}

object DataRecorderService {
  val live: ULayer[DataRecorderServiceLive] = ZLayer.succeed(DataRecorderServiceLive())
}
