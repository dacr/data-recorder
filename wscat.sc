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
//> using lib "dev.zio::zio:2.0.6"
//> using lib "com.softwaremill.sttp.client3::zio:3.8.8"
// ---------------------

import zio.*
import sttp.client3.*, sttp.client3.basicRequest.*, sttp.ws.*, sttp.model.*

object WebSocketCat extends ZIOAppDefault {

  // val defaultTarget = "wss://ws.postman-echo.com/raw" // This is an echo service also
  // val defaultTarget = "ws://127.0.0.1:3000/ws/test/echo"
  val defaultTarget = "ws://127.0.0.1:3000/ws/test/stream"

  def processWebsocket(ws: WebSocket[Task]): Task[Unit] = {
    val receiveOne = ws.receiveText().flatMap(res => Console.printLine(s"$res"))
    val sendOne    = Console.readLine.flatMap(line => ws.sendText(line))
    receiveOne.forever.race(sendOne.forever)
  }

  def run =
    for {
      args      <- getArgs
      target     = args.headOption.getOrElse(defaultTarget)
      targetURI <- ZIO.fromEither(Uri.parse(target))
      backend   <- sttp.client3.httpclient.zio.HttpClientZioBackend()
      response  <- basicRequest
                     .get(targetURI)
                     .response(asWebSocket(processWebsocket))
                     .send(backend)
    } yield response
}

WebSocketCat.main(args)
