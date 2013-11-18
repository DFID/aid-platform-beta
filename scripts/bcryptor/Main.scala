
import org.mindrot.jbcrypt.BCrypt

object Main extends App {
  args.headOption.map { unencrypted =>
    println(BCrypt.hashpw(unencrypted, BCrypt.gensalt()))
  }
}