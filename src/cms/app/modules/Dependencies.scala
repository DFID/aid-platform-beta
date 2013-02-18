package modules

import com.tzavellas.sse.guice.ScalaModule
import lib._
import traits.{Api, SourceSelector, Deployer, Authenticator}
import models.Country


class Dependencies extends ScalaModule {
  def configure() {
    bind[Authenticator].to[SimpleAuthenticator]
    bind[SourceSelector].to[IatiDataSourceSelector]
    bind[Deployer].to[Deployinator]
    bind[Api[Country]].to[CountriesApi]
  }
}


