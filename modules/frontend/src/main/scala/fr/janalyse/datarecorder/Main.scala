package fr.janalyse.datarecorder

import scala.scalajs.js
import org.scalajs.dom
import fr.janalyse.datarecorder.protocol.*
import com.raquo.laminar.api.L.*
import org.scalajs.dom.Event
import sttp.client3.impl.zio.FetchZioBackend

object App {

  val runtime = zio.Runtime.default

  //val backend = FetchZioBackend()

  def root = div(
    h1("test")
  )

  def initialize(onEvent: Event): Unit = {
    val appContainer = dom.document.querySelector("#app")
    appContainer.innerHTML = ""
    val _            = render(appContainer, root)
  }

  def main(args: Array[String]): Unit = {
    val _ = documentEvents.onDomContentLoaded.foreach(initialize)(unsafeWindowOwner)
  }
}
