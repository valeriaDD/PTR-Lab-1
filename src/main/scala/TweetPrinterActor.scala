import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}

import scala.io.Source
import scala.util.Random

class TweetPrinterActor(emotionsMap: Map[String, Int]) extends Actor with ActorLogging {
  val batcher: ActorRef = context.actorOf(Props(new BatcherActor(3)), "child")
  override def preStart(): Unit = {
    log.info(s"Printer Actor ${self.path.name} restarted! :)")
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
        val blurredTweetText = this.blurBadWords(tweetText)

        batcher !  blurredTweetText
//        log.info(s"Actor ${self.path.name} from ${sender().path.name}: $blurredTweetText");

        Thread.sleep(Random.nextInt(46) + 5)
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

class EngagementRatioCalculator(emotionsMap: Map[String, Int]) extends Actor with ActorLogging {
  override def preStart(): Unit = {
    log.info(s"Engagement Actor ${self.path.name} restarted! :)")
  }

  override def receive: Receive = {
    case event: SSEEvent => {
      val tweet = event.data
      val regexFavorites = "\"favourites_count\":(\\d+)".r.unanchored
      val regexFollowers = "\"followers_count\":(\\d+)".r.unanchored
      val regexRetweets = "\"retweet_count\":(\\d+)".r.unanchored
      val favouritesCount = regexFavorites.findFirstMatchIn(tweet).map(_.group(1)).getOrElse("")
      val followersCount = regexFollowers.findFirstMatchIn(tweet).map(_.group(1)).getOrElse("")
      val retweetsCount = regexRetweets.findFirstMatchIn(tweet).map(_.group(1)).getOrElse("")

      val engagementRatio = (favouritesCount.toInt + retweetsCount.toInt).toDouble / followersCount.toInt

      log.info(s"Engagement Ratio: $engagementRatio")
    }
  }
}

class SentimentalScoreActor(emotionsMap: Map[String, Int]) extends Actor with ActorLogging {
  override def preStart(): Unit = {
    log.info(s"Sentimental Actor ${self.path.name} restarted! :)")
  }
  override def receive: Receive = {
    case event: SSEEvent => {
      val sentimentScore = event.data
        .split("\\s+")
        .map(word => emotionsMap.getOrElse(word.toLowerCase, 0))
        .sum
        .toDouble / event.data.split("\\s+").length

      log.info(s"Sentiment Score: $sentimentScore")
    }
  }
}

