import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.pattern.ask
import akka.stream.scaladsl.Source

import scala.concurrent.Future
import scala.util.{Failure, Success}

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
    import akka.util.Timeout

    import scala.concurrent.duration._

    implicit val timeout: Timeout = Timeout(5.seconds)
    implicit val system: ActorSystem = ActorSystem("SSESystem")

    val emotions = system.actorOf(Props(new EmotionSseReader("http://localhost:50/emotion_values")))

    emotions ! "emotions"
    import system.dispatcher
    Thread.sleep(5000);
    val response = (emotions ? "getEmotions").mapTo[Map[String, Int]]

    response.onComplete {
      case Success(emotionsMap) =>

        val poolSupervisor = system.actorOf(Props(new PoolSupervisor(3, classOf[TweetPrinterActor], emotionsMap)), "supervisor")
        val poolEmotionsSupervisor = system.actorOf(Props(new PoolSupervisor(3, classOf[EngagementRatioCalculator], emotionsMap)), "supervisorEngagement")
        val poolSentimentalSupervisor = system.actorOf(Props(new PoolSupervisor(3, classOf[SentimentalScoreActor], emotionsMap)), "supervisorSentimental")

        val sseActor = system.actorOf(Props(new SseReader("http://localhost:50/tweets/1", poolSupervisor)), "sseReader1")
        val sseActor1 = system.actorOf(Props(new SseReader("http://localhost:50/tweets/1", poolEmotionsSupervisor)), "sseReader2")
        val sseActor2 = system.actorOf(Props(new SseReader("http://localhost:50/tweets/1", poolSentimentalSupervisor)), "sseReader3")

        sseActor ! "start"
        sseActor1 ! "start"
        sseActor2 ! "start"

      case Failure(ex) => println(s"Failed to get emotions: ${ex.getMessage}")
    }
  }
}