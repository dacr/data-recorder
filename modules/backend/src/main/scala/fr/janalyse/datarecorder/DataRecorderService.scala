package fr.janalyse.datarecorder

import zio.*
import fr.janalyse.datarecorder.protocol.*

case class DataRecorderServiceLive() extends DataRecorderService {
  override def serviceStatus: UIO[ServiceStatus] = ZIO.succeed(ServiceStatus("0.1.0", true))
}

object DataRecorderServiceLive {
  val layer: ULayer[DataRecorderServiceLive] = ZLayer.succeed(DataRecorderServiceLive())
}
