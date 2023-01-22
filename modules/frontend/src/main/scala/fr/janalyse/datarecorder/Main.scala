package fr.janalyse.datarecorder

import scala.scalajs.js
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import org.scalajs.dom
import fr.janalyse.datarecorder.protocol.*
import com.raquo.laminar.api.L.*
import org.scalajs.dom.Event
import sttp.capabilities
import sttp.tapir.client.sttp.SttpClientInterpreter
import sttp.capabilities.zio.ZioStreams
import sttp.client3.impl.zio.FetchZioBackend
import sttp.tapir.client.sttp.ws.zio.*
import sttp.client3.*
import zio.*
import zio.stream.*
import sttp.ws.*

import sttp.tapir.client.sttp.*

type Backend = SttpBackend[Task, ZioStreams & capabilities.WebSockets]

case class DataRecorderService(backend: Backend) {
  import fr.janalyse.datarecorder.protocol.DataRecorderEndPoints.*

  val baseUri   = Some(uri"http://127.0.0.1:3000")
  val wsBaseUri = Some(uri"ws://127.0.0.1:3000")

  val ping: Task[Response[Either[Unit, String]]] =
    SttpClientInterpreter()
      .toRequestThrowDecodeFailures(systemPingEndpoint, baseUri = baseUri)
      .apply(())
      .send(backend)
      .tapError(err => ZIO.attempt(err.printStackTrace()) *> Console.printLine("ping =====>" + err.toString))

  val serviceStatus: Task[Response[Either[Unit, ServiceStatus]]] =
    SttpClientInterpreter()
      .toRequestThrowDecodeFailures(systemStatusEndpoint, baseUri = baseUri)
      .apply(())
      .send(backend)
      .tapError(err => ZIO.attempt(err.printStackTrace()) *> Console.printLine("serviceStatus =====>" + err.toString))

  val events: Task[Either[Unit, Stream[Throwable, ClientEvent] => Stream[Throwable, ServerEvent]]] =
    SttpClientInterpreter()
      .toClientThrowDecodeFailures(serviceEventsEndpoint, baseUri = wsBaseUri, backend)
      .apply(())
      .tapError(err => ZIO.attempt(err.printStackTrace()) *> Console.printLine("events =====>" + err.toString))
}

object App {

  val runtime = zio.Runtime.default

  val events: Var[Vector[String]] = Var(Vector.empty)

  val backend = FetchZioBackend()

  val dataRecorderService = DataRecorderService(backend)

  def processWebsocket(ws: WebSocket[Task]): Task[Unit] = {
    val receiveOne = ws.receiveText().flatMap(res => Console.printLine(s"received $res"))
    receiveOne.forever
  }

  def byHandWebsocketCall() = {
    val request =
      basicRequest
        .get(uri"ws://127.0.0.1:3000/ws/system/events")
        // .response(asWebSocketAlways(processWebsocket))
        .response(asWebSocket(processWebsocket))

    val response =
      request
        .send(backend)

    Unsafe.unsafe { implicit u =>
      runtime.unsafe.fork {
        response
          .tap(result => Console.printLine(result.toString))
          .tapError(err => Console.printLine(s"--------> $err"))
      }
    }
  }

  val beginStream: Modifier[Element] = onMountCallback { _ =>
    Unsafe.unsafe { implicit u =>
      runtime.unsafe.fork {
        val consumeLogic = for {
          inputQueue  <- Queue.unbounded[ClientEvent]
          response    <- dataRecorderService.events.retry(Schedule.spaced(5.second))
          fromFCT     <- ZIO.fromEither(response)
          inputStream  = ZStream.fromQueue(inputQueue)
          outputStream = fromFCT(inputStream)
          _           <- outputStream.runFoldZIO(0)((n, event) =>
                           Console.printLine(s"#$n - $event") *>
                             ZIO.succeed(events.update(_.appended(event.toString))) *>
                             ZIO.succeed(n + 1)
                         )
        } yield ()
        consumeLogic
          .tapError(err => Console.printLine(s"consumer global error $err"))
          .retry(Schedule.exponential(100.millis, 2d))
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

    // byHandWebsocketCall()

    Unsafe.unsafe { implicit u =>
      runtime.unsafe.fork {
        ZIO.succeed(events.update(_.appended("init"))) *>
          dataRecorderService.serviceStatus.map(res => events.update(_.appended(res.body.map(_.version).toString))) *>
          dataRecorderService.ping.map(res => events.update(_.appended(res.body.toString)))
      }
    }

    val _ = render(appContainer, root)
  }

  def main(args: Array[String]): Unit = {
    val _ = documentEvents.onDomContentLoaded.foreach(initialize)(unsafeWindowOwner)
  }
}
