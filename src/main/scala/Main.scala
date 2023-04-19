import models.{Tweet, User, dbTables}

import scala.concurrent.{Future}
import scala.util.{Failure, Success}


object Main {
  import slick.jdbc.PostgresProfile.api._
  import PrivateExecutionContext._

  val tweet: Tweet = Tweet("1", "2", "test", "score", "engagement")

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