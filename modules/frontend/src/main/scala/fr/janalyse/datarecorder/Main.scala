package fr.janalyse.datarecorder

import scala.scalajs.js
import org.scalajs.dom
import fr.janalyse.datarecorder.protocol.*
import com.raquo.laminar.api.L.*
import org.scalajs.dom.Event
import sttp.tapir.client.sttp.SttpClientInterpreter
import sttp.client3.*

object DataRecorderService {
  import fr.janalyse.datarecorder.protocol.DataRecorderEndPoints.*

  val backend = FetchBackend()

  def serviceStatus() =
    SttpClientInterpreter()
      .toRequestThrowErrors(serviceStatusEndpoint, baseUri = None)
      .apply(())
      .send(backend)
}

object App {

  val runtime = zio.Runtime.default


  def root = div(
    h1("test")
  )

  def initialize(onEvent: Event): Unit = {
    val appContainer = dom.document.querySelector("#app")
    appContainer.innerHTML = ""
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
    println("ICI")
    DataRecorderService.serviceStatus().map(res => println(res.body.version.toString()))
    val _            = render(appContainer, root)
  }

  def main(args: Array[String]): Unit = {
    val _ = documentEvents.onDomContentLoaded.foreach(initialize)(unsafeWindowOwner)
  }
}
