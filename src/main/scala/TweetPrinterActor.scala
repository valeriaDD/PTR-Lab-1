import akka.actor.{Actor, ActorLogging, PoisonPill}

import scala.util.Random

class TweetPrinterActor extends Actor with ActorLogging {
  override def preStart(): Unit = {
    log.info(s"Actor ${self.path.name} restarted! :)")
  }

  override def postStop(): Unit = {
    log.info(s"Actor ${self.path.name} panicked and died :(")
  }

  override def receive: Receive = {
    case sseEvent: SSEEvent =>
      if (sseEvent.data contains "panic") {
        throw new Exception(s"${self.path.name}: PANIC, I die! X( ")
        self ! PoisonPill
      } else {
        val pattern = "\"text\":\"(.*?)\"".r.unanchored
        val tweetText = pattern.findFirstMatchIn(sseEvent.data).map(_.group(1)).getOrElse("")
        log.info(s"Actor ${self.path.name} from ${sender().path.name}: $tweetText");

        val sleepTime = Random.nextInt(46) + 5
        Thread.sleep(1000)
      }
    case _ => log.error(s" ${self.path.name}: I don't know how to process it")
  }
}
