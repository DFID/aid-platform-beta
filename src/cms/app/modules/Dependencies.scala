package modules

import com.tzavellas.sse.guice.ScalaModule
import lib._
import impl.{MongoBackedConfigurationGenerator, Deployinator, SimpleAuthenticator, IatiDataSourceSelector}


class Dependencies extends ScalaModule {
  def configure() {
    bind[Authenticator].to[SimpleAuthenticator]
    bind[SourceSelector].to[IatiDataSourceSelector]
    bind[Deployer].to[Deployinator]
    bind[ConfigurationGenerator].to[MongoBackedConfigurationGenerator]
  }
}


