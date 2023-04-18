import models.{Tweet, User, dbTables}

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


object PrivateExecutionContext {
  private val executor = Executors.newFixedThreadPool(4)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executor)
}

object Main {
  import slick.jdbc.PostgresProfile.api._
  import PrivateExecutionContext._

  val tweet: Tweet = Tweet(1L, "2", "test", "score", "engagement")

  def insertTweet(): Unit = {
    val queryDescription = dbTables.tweetTable += tweet
    val futureId: Future[Int] = Connection.db.run(queryDescription)

    futureId.onComplete {
      case Success(id) => println(s"Success $id")
      case Failure(ex) => println(s"Fail $ex")
    }

    Thread.sleep(100)
  }

  def insertUser(): Unit = {
    val user: User = User("524", "new name")
    val queryDescription = dbTables.userTable += user
    val futureId: Future[Int] = Connection.db.run(queryDescription)

    futureId.onComplete {
      case Success(id) => println(s"Success user $id")
      case Failure(ex) => println(s"Fail user $ex")
    }

    Thread.sleep(1000)
  }

  def main(args: Array[String]): Unit = {
    insertUser()
  }
}