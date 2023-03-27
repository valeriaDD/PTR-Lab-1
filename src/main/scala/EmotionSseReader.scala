import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.scaladsl.Source

import scala.concurrent.{ExecutionContext, Future}

class EmotionSseReader(uri: String)(implicit system: ActorSystem) extends Actor with ActorLogging {
  private val request = HttpRequest(uri = Uri(uri))
  var emotionsMap: Map[String, Int] = Map.empty
  implicit val ec: ExecutionContext = ExecutionContext.global

  override def receive: Receive = {
    case "emotions" =>
      val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
      val source: Source[String, Any] = Source.future(responseFuture)
        .flatMapConcat(response => response.entity.dataBytes.map(_.utf8String))

      val sseStream: Source[SSEEvent, Any] = source.map(SSEEvent)

      sseStream.runForeach { event =>
        val dataArray = event.data.split("\r\n")
        dataArray.foreach { data =>
          val splitData = data.split("\t")
          if (splitData.length == 2) {
            val (emotion, score) = (splitData(0), splitData(1).toInt)
            emotionsMap += (emotion -> score)
          } else {
            log.warning(s"Invalid SSE event data: $data")
          }
        }
      }.onComplete { _ =>
        sender() ! emotionsMap
      }
    case "getEmotions" =>
      sender() ! emotionsMap
  }
}
