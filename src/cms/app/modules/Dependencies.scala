package modules

import com.tzavellas.sse.guice.ScalaModule
import lib._
import traits.{SourceSelector, Deployer, Authenticator}
import uk.gov.dfid.common.models.Country
import uk.gov.dfid.common.api.{Api, CountriesApi}
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.DefaultDB
import play.api.Play.current


class Dependencies extends ScalaModule {
  def configure() {
    bind[Authenticator].to[SimpleAuthenticator]
    bind[SourceSelector].to[IatiDataSourceSelector]
    bind[Deployer].to[Deployinator]
    bind[DefaultDB].toInstance(ReactiveMongoPlugin.db)
    bind[Api[Country]].to[CountriesApi]
  }
}


