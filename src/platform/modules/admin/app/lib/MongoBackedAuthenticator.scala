package lib

import play.api.Play
import org.mindrot.jbcrypt.BCrypt
import traits.Authenticator
import com.google.inject.Inject
import reactivemongo.api.DefaultDB
import reactivemongo.bson.{BSONInteger, BSONString, BSONDocument}
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import concurrent.ExecutionContext.Implicits.global

class MongoBackedAuthenticator @Inject()(db: DefaultDB) extends Authenticator {

  private def config(key: String) = Play.current.configuration.getString(key).get
  private val users = db.collection("users")

  def authenticate(username: String, password: String): Boolean = {

    val query = BSONDocument("username" -> BSONString(username))
    val maybeUser = Await.result(users.find(query).headOption, Duration.Inf)

    maybeUser map { user =>
      val lockout = user.getAs[BSONInteger]("retryCount").map(_.value).getOrElse(0)

      if(lockout > 5) {
        false
      } else {
        val authn = user.getAs[BSONString]("password").map(_.value).get
        val valid = BCrypt.checkpw(password, authn)

        // increment or reset the lockout
        users.update(
          BSONDocument("_id" -> user.get("_id")),
          BSONDocument(
            "$set" -> BSONDocument(
              "retryCount" -> BSONInteger(if(valid) 0 else { lockout +1 })
            )
          )
        )

        valid
      }
    } getOrElse false
  }

  def authenticate(password: String): Boolean =
    BCrypt.checkpw(password, config("credentials.password"))
}
