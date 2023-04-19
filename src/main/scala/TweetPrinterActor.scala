import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill}

import scala.io.Source

case class ProcessedTweet(id: String, key: String, value: String);

class TweetPrinterActor(emotionsMap: Map[String, Int], aggregator: ActorRef) extends Actor with ActorLogging {
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

        val idPattern = "\"id_str\":\"(.*?)\"".r.unanchored
        val id = idPattern.findFirstMatchIn(sseEvent.data).map(_.group(1)).getOrElse("")

        val blurredTweetText = this.blurBadWords(tweetText)

        aggregator !  ProcessedTweet(id, "text", blurredTweetText)

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

class EngagementRatioCalculator(emotionsMap: Map[String, Int], aggregator: ActorRef) extends Actor with ActorLogging {
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

      val idPattern = "\"id_str\":\"(.*?)\"".r.unanchored
      val id = idPattern.findFirstMatchIn(event.data).map(_.group(1)).getOrElse("")

      val engagementRatio = (favouritesCount.toInt + retweetsCount.toInt).toDouble / followersCount.toInt
      aggregator !  ProcessedTweet(id, "engagementRatio", engagementRatio.toString)

//      log.info(s"Engagement Ratio: $engagementRatio")
    }
  }
}

class SentimentalScoreActor(emotionsMap: Map[String, Int], aggregator: ActorRef) extends Actor with ActorLogging {
  override def preStart(): Unit = {
    log.info(s"Sentimental Actor ${self.path.name} restarted! :)")
  }
  override def receive: Receive = {
    case event: SSEEvent => {
      val idPattern = "\"id_str\":\"(.*?)\"".r.unanchored
      val id = idPattern.findFirstMatchIn(event.data).map(_.group(1)).getOrElse("")

      val sentimentScore = event.data
        .split("\\s+")
        .map(word => emotionsMap.getOrElse(word.toLowerCase, 0))
        .sum
        .toDouble / event.data.split("\\s+").length

      aggregator !  ProcessedTweet(id, "sentimentalScore", sentimentScore.toString)
      //      log.info(s"Sentiment Score: $sentimentScore")
    }
  }
}

class UserExtractorActor(emotionsMap: Map[String, Int], aggregator: ActorRef) extends Actor with ActorLogging {
  override def preStart(): Unit = {
    log.info(s"User Extractor Actor ${self.path.name} restarted! :)")
  }

  override def receive: Receive = {
    case event: SSEEvent =>
      if (event.data contains "panic") {
        throw new Exception(s"${self.path.name}: PANIC, I die! X( ")
        self ! PoisonPill
      } else {
        val idPattern = "\"id_str\":\"(.*?)\"".r.unanchored
        val userPattern = "\"source_user_id_str\":\"(.*?)\"".r.unanchored

        val userId = userPattern.findFirstMatchIn(event.data).map(_.group(1)).getOrElse("")
        val id = idPattern.findFirstMatchIn(event.data).map(_.group(1)).getOrElse("")

        aggregator ! ProcessedTweet(id, "user_id", userId)
      }
  }
}

