package lib

import org.mindrot.jbcrypt.BCrypt
import play.api.Play


trait Authenticator {
  def authenticate(username: String, password: String): Boolean
  def authenticate(password: String): Boolean
}

class SimpleAuthenticator extends Authenticator{

  def config(key: String) = Play.current.configuration.getString(key).get

  def authenticate(username: String, password: String) = {
    BCrypt.checkpw(username, config("credentials.username")) &&
    BCrypt.checkpw(password, config("credentials.password"))
  }

  def authenticate(password: String) = {
    BCrypt.checkpw(password, config("credentials.password"))
  }
}
