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

  def serviceStatus() =
    val response: Future[Response[Either[Unit,ServiceStatus]]] =
      SttpClientInterpreter()
        .toRequestThrowDecodeFailures(serviceStatusEndpoint, baseUri = None)
        .apply(())
        .send(backend)
    //ZIO.fromFuture(response)
    response
}

object App {

  val runtime = zio.Runtime.default

  def root = div(
    h1("Test")
  )

  def initialize(onEvent: Event): Unit = {
    val appContainer = dom.document.querySelector("#app")
    appContainer.innerHTML = ""

    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
    DataRecorderService.serviceStatus().map(res => println(res.body.map(_.version.toString())))

    val _ = render(appContainer, root)
  }

  def main(args: Array[String]): Unit = {
    val _ = documentEvents.onDomContentLoaded.foreach(initialize)(unsafeWindowOwner)
  }
}
