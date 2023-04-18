package models

case class Tweet(id: Long, userId: String, text: String, engagementRatio: String, sentimentalScore: String)
case class User(id: String, name: String)

object dbTables {

  import slick.jdbc.PostgresProfile.api._

  lazy val tweetTable = TableQuery[TweetTable]
  lazy val userTable = TableQuery[UserTable]

  class TweetTable(tag: Tag) extends Table[Tweet](tag, Some("tweets"), "Tweet") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[String]("user_id")

    def text = column[String]("text")

    def engagementRatio = column[String]("engagement_ratio")

    def sentimentalScore = column[String]("sentimental_score")

    override def * = (id, userId, text, engagementRatio, sentimentalScore) <> (Tweet.tupled, Tweet.unapply)
  }

  class UserTable(tag: Tag) extends Table[User](tag, Some("tweets"), "User") {
    def id = column[String]("id", O.PrimaryKey)
    def name = column[String]("name")

    override def * = (id, name) <> (User.tupled, User.unapply)
  }
}
