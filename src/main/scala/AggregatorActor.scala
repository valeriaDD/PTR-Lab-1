import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import models.Tweet

class AggregatorActor extends Actor with ActorLogging {

  override def preStart(): Unit = {
    log.info(s"Agregator ${self.path.name} started!")
  }

  private var tweetsMap: Map[String, Map[String, String]] = Map.empty

  val batcher: ActorRef = context.actorOf(Props(new BatcherActor(3)), "batcher")

  override def receive: Receive = {
    case tweetInfo: ProcessedTweet =>
      tweetsMap.get(tweetInfo.id) match {
        case Some(value) =>
          val newValue = value + (tweetInfo.key -> tweetInfo.value);
          tweetsMap = tweetsMap.updated(tweetInfo.id, newValue)

          if (isCompleted(tweetsMap(tweetInfo.id))) {
            val tweetItem = tweetsMap(tweetInfo.id)
            println(tweetItem)

            batcher ! Tweet(
              tweetInfo.id,
              tweetItem("user_id"),
              "a",
              tweetItem("engagementRatio"),
              tweetItem("sentimentalScore")
            )

            println("Tweet send to batcher")
            tweetsMap.removed(tweetInfo.id)
          }

        case None =>
          tweetsMap += (tweetInfo.id -> Map(tweetInfo.key -> tweetInfo.value))
      }
  }

  private def isCompleted(map: Map[String, String]): Boolean = {
    map.contains("user_id") &&
      map.contains("text") &&
      map.contains("engagementRatio") &&
      map.contains("sentimentalScore")
  }
}