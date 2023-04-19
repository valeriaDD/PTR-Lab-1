import PrivateExecutionContext._
import akka.actor.Actor
import models.{Tweet, User, dbTables}
import slick.jdbc.PostgresProfile.api._

import java.util.concurrent.Executors
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object PrivateExecutionContext {
  private val executor = Executors.newFixedThreadPool(3)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executor)
}

class BatcherActor(batchSize: Int = 5) extends Actor {
  private val buffer = new ListBuffer[Tweet]()


  override def receive: Receive = {
    case tweet: Tweet =>
      if (tweet.userId.nonEmpty) {
        buffer += tweet
      }
      if (buffer.length >= batchSize) {
        insertTweet()
      }
  }

  private def insertTweet(): Unit = {
    buffer.foreach(tweet => {
      this.insertUser(tweet);

      val tweetQueryDescription = dbTables.tweetTable.insertOrUpdate(tweet)
      val futureTweetId: Future[Int] = Connection.db.run(tweetQueryDescription)

      futureTweetId.onComplete {
        case Success(id) => println(s"Success tweet insert, id: $id")
        case Failure(ex) => println(s"Fail tweet insert $ex")
      }
      Thread.sleep(1000)

    })


    println(s"Batch of size ${buffer.length}: ${buffer.mkString(", ")}")
    buffer.clear()
  }

  private def insertUser(tweet: Tweet): Unit = {
    val userQueryDescription = dbTables.userTable.insertOrUpdate(User(tweet.userId, null))
    val futureUserId: Future[Int] = Connection.db.run(userQueryDescription)

    futureUserId.onComplete {
      case Success(id) => println(s"Success user insert, id: $id")
      case Failure(ex) => println(s"Fail user insert $ex")
    }

    Thread.sleep(1000)
  }
}