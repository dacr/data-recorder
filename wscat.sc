// summary : Simple websocket example
// keywords : scala, zio, sttp, websocket
// publish : gist
// authors : David Crosson
// license : Apache BUT Machine Learning models training is not allowed by the author
// id : 5642b2ef-3f21-4a10-8924-f435bb69306d
// created-on : 2022-12-30T09:25:55+01:00s
// managed-by : https://github.com/dacr/code-examples-manager
// run-with : scala-cli $file

// ---------------------
//> using scala  "3.2.1"
//> using lib "dev.zio::zio:2.0.5"
//> using lib "fr.janalyse::zio-worksheet:2.0.4.0"
//> using lib "com.softwaremill.sttp.client3::zio:3.8.5"
// ---------------------

import zio.*, zio.worksheet.*
import sttp.client3.*, sttp.client3.basicRequest.*
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.ws.*

def processWebsocket(ws: WebSocket[Task]): Task[Unit] = {
  val receiveOne = ws.receiveText().flatMap(res => Console.printLine(s"received $res"))
  val sendOne = Console.readLine.flatMap(line => ws.sendText(s"""{"message":"$line"}"""))
  receiveOne.forever.race(sendOne.forever)
}

def run(): Unit = {
  val request =
    basicRequest
      .get(uri"ws://127.0.0.1:3000/ws/system/events")
      .response(asWebSocket(processWebsocket))

  val logic =
    for {
      backend <- HttpClientZioBackend()
      response <- request.send(backend)
    } yield response

  logic.unsafeRun
}

run()
