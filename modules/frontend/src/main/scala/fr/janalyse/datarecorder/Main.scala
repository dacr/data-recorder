package fr.janalyse.datarecorder

import scala.scalajs.js
import scala.concurrent.Future
import org.scalajs.dom
import fr.janalyse.datarecorder.protocol.*
import com.raquo.laminar.api.L.*
import org.scalajs.dom.Event
import sttp.tapir.client.sttp.SttpClientInterpreter
import sttp.client3.*
import zio.*

object DataRecorderService {
  import fr.janalyse.datarecorder.protocol.DataRecorderEndPoints.*

  val backend = FetchBackend()

  val serviceStatusRequest: Request[Either[Unit, ServiceStatus], Any] =
    SttpClientInterpreter()
      .toRequestThrowDecodeFailures(serviceStatusEndpoint, baseUri = None)
      .apply(())

  def serviceStatus():Task[Response[Either[Unit,ServiceStatus]]] =
    val response = serviceStatusRequest.send(backend)
    ZIO.fromFuture(implicit ec => response)
}

object App {

  val runtime = zio.Runtime.default

  def root = div(
    h1("Test")
  )

  def initialize(onEvent: Event): Unit = {
    val appContainer = dom.document.querySelector("#app")
    appContainer.innerHTML = ""

    Unsafe.unsafe { implicit u =>
      runtime.unsafe.fork {
        DataRecorderService.serviceStatus().tap(res => Console.printLine(res.body.map(_.version)))
      }
    }

    val _ = render(appContainer, root)
  }

  def main(args: Array[String]): Unit = {
    val _ = documentEvents.onDomContentLoaded.foreach(initialize)(unsafeWindowOwner)
  }
}
