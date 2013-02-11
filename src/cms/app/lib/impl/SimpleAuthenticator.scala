package lib.impl

import lib.Authenticator
import play.api.Play
import org.mindrot.jbcrypt.BCrypt

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
