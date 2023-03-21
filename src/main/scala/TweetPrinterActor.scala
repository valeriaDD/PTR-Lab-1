import akka.actor.{Actor, ActorLogging, PoisonPill}

import scala.io.Source
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
        val blurredTweetText = this.blurBadWords(tweetText);

        log.info(s"Actor ${self.path.name} from ${sender().path.name}: $blurredTweetText");

        val sleepTime = Random.nextInt(46) + 5
        Thread.sleep(1000)
      }
    case _ => log.error(s" ${self.path.name}: I don't know how to process it")
  }

  private val badWords: Set[String] = {
    val badWordsFile = Source.fromFile("C:\\Users\\dubin\\Uni_sem_6\\PTR\\Labs\\Lab1\\src\\main\\scala\\data\\bad-words.txt")
    val words = badWordsFile.getLines().toSet
    badWordsFile.close()
    words
  }

  private def blurBadWords(text: String): String = {
    val badWords = this.badWords

    text.split(" ").map { word =>
      if (badWords.contains(word.toLowerCase)) {
        "*" * word.length
      } else {
        word
      }
    }.mkString(" ")
  }
}
