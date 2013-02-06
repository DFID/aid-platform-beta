package modules

import com.tzavellas.sse.guice.ScalaModule
import lib._
import api.{IatiDataSourcesApi, SourceSelector}

class Dependencies extends ScalaModule {
  def configure() {
    bind[Authenticator].to[SimpleAuthenticator]
    bind[SourceSelector].to[IatiDataSourcesApi]
  }
}


