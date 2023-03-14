import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.scaladsl.Source

import scala.concurrent.Future

case class SSEEvent(data: String)

class SseReader(uri: String, printerActorSupervisor: ActorRef)(implicit system: ActorSystem) extends Actor {

  private val request = HttpRequest(uri = Uri(uri))

  override def receive: Receive = {
    case "start" =>
      val responseFuture: Future[HttpResponse] = Http().singleRequest(request)

      val source: Source[String, Any] = Source.future(responseFuture)
        .flatMapConcat(response => response.entity.dataBytes.map(_.utf8String))

      val sseStream: Source[SSEEvent, Any] = source.map(SSEEvent)

      sseStream.runForeach { event =>
        printerActorSupervisor ! event
      }
  }
}

object SseReader {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("SSESystem")

    val supervisor = system.actorOf(Props[TweetPrinterPool], "supervisor")

    val sseActor = system.actorOf(Props(new SseReader("http://localhost:50/tweets/1", supervisor)), "sseReader1")
    val sseActor2 = system.actorOf(Props(new SseReader("http://localhost:50/tweets/2", supervisor)), "sseReader2")
//
    sseActor ! "start"
//    sseActor2 ! "start"
  }
}