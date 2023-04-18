
import slick.jdbc.PostgresProfile.profile.api._

object Connection {
  val db = Database.forConfig("postgres")
}
