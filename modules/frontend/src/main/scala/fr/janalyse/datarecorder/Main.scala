package fr.janalyse.datarecorder

import scala.scalajs.js
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import org.scalajs.dom
import fr.janalyse.datarecorder.protocol.*
import com.raquo.laminar.api.L.*
import org.scalajs.dom.Event
import sttp.tapir.client.sttp.SttpClientInterpreter
import sttp.capabilities.zio.ZioStreams
import sttp.client3.impl.zio.FetchZioBackend
import sttp.client3.*
import zio.*
import zio.stream.*

object DataRecorderService {
  import fr.janalyse.datarecorder.protocol.DataRecorderEndPoints.*

  val backend = FetchZioBackend()

  val ping: Task[Response[Either[Unit, String]]] =
    SttpClientInterpreter()
      .toRequestThrowDecodeFailures(pingEndpoint, baseUri = None)
      .apply(())
      .send(backend)

  val serviceStatus: Task[Response[Either[Unit, ServiceStatus]]] =
    SttpClientInterpreter()
      .toRequestThrowDecodeFailures(serviceStatusEndpoint, baseUri = None)
      .apply(())
      .send(backend)

  val eventStream: Task[Response[Either[Unit, Stream[Throwable, ClientMessage] => Stream[Throwable, ServerMessage]]]] =
    SttpClientInterpreter()
      .toRequestThrowDecodeFailures(serviceEventsEndpoint, baseUri = None)
      .apply(())
      .send(backend)
}

object App {

  val runtime = zio.Runtime.default

  val events: Var[Vector[String]] = Var(Vector.empty)

  val beginStream: Modifier[Element] = onMountCallback { _ =>
    Unsafe.unsafe { implicit u =>
      runtime.unsafe.fork {
        DataRecorderService.eventStream
          .retry(Schedule.spaced(1.second))
          .map { response =>
            response.body match {
              case Left(err)      => Console.printLine("Can't establish websocket")
              case Right(fromFct) =>
                val inputStream  = ZStream.empty
                val outputStream = fromFct(inputStream)
                outputStream.runFoldZIO(0)((n, event) =>
                  Console.printLine(s"#$n - $event") *>
                    ZIO.succeed(events.update(_.appended(event.toString))) *>
                    ZIO.succeed(n + 1)
                )
            }
          }
      }
    }
  }

  def root = div(
    beginStream, // To trigger the onMountCallback for the stream
    h1("Test websockets"),
    p("received events :"),
    children <-- events.signal.map(_.zipWithIndex.reverse).split(_._2) { (_, event, _) =>
      div(event._1)
    }
  )

  def initialize(onEvent: Event): Unit = {
    val appContainer = dom.document.querySelector("#app")
    appContainer.innerHTML = ""

    Unsafe.unsafe { implicit u =>
      runtime.unsafe.fork {
        ZIO.succeed(events.update(_.appended("init"))) *>
          DataRecorderService.serviceStatus.tap(res => Console.printLine(res.body.map(_.version)))
      }
    }

    val _ = render(appContainer, root)
  }

  def main(args: Array[String]): Unit = {
    val _ = documentEvents.onDomContentLoaded.foreach(initialize)(unsafeWindowOwner)
  }
}
