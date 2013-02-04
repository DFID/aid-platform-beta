package modules

import com.tzavellas.sse.guice.ScalaModule
import lib._

class Dependencies extends ScalaModule {
  def configure() {

    // Wire up the rest
    bind[Authenticator].to[SimpleAuthenticator]
  }
}


