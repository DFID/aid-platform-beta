package modules

import com.tzavellas.sse.guice.ScalaModule
import lib._
import impl.{Deployinator, SimpleAuthenticator, IatiDataSourceSelector}
import uk.gov.dfid.iati.{XmlToNeo4JMapper, SourceMapper}
import org.neo4j.graphdb.{Node => GraphNode, GraphDatabaseService}
import xml.{Node => XmlNode}
import org.neo4j.kernel.EmbeddedGraphDatabase
import play.api.Play.current
import play.api.Play
import uk.gov.dfid.iati.validators.{RemoteIATIValidator, IATIValidator}

class Dependencies extends ScalaModule {
  def configure() {
    bind[Authenticator].to[SimpleAuthenticator]
    bind[SourceSelector].to[IatiDataSourceSelector]
    bind[Deployer].to[Deployinator]
    bind[SourceMapper[XmlNode, GraphNode]].to[XmlToNeo4JMapper]
    bind[IATIValidator].to[RemoteIATIValidator]
    bind[GraphDatabaseService].toInstance({

      val path = Play.configuration.getString("neo4j.path").get
      val db = new EmbeddedGraphDatabase(path)
      db
    })
  }
}


