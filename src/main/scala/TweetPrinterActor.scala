import akka.actor.Actor

import scala.util.Random

class TweetPrinterActor extends Actor {
  override def receive: Receive = {
    case sseEvent: SSEEvent =>
      val pattern = "\"text\":\"(.*?)\"".r.unanchored
      val tweetText = pattern.findFirstMatchIn(sseEvent.data).map(_.group(1)).getOrElse("")
      println(s"Actor ${self.path.name} from ${sender().path.name}: $tweetText");

      val sleepTime = Random.nextInt(46) + 5
      Thread.sleep(sleepTime)

    case _ => println("I don't know how to process it")
  }
}
