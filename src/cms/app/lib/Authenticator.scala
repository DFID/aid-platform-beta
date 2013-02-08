package lib

import org.mindrot.jbcrypt.BCrypt
import play.api.Play


trait Authenticator {
  def authenticate(username: String, password: String): Boolean
  def authenticate(password: String): Boolean
}
