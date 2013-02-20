package modules

import com.tzavellas.sse.guice.ScalaModule
import org.neo4j.graphdb.GraphDatabaseService
import play.api.Play
import play.api.Play.current
import uk.gov.dfid.common.models.Country
import uk.gov.dfid.common.api.{ReadOnlyApi, ReadOnlyCountriesApi}
import reactivemongo.api.DefaultDB
import play.modules.reactivemongo.ReactiveMongoPlugin
import org.neo4j.kernel.EmbeddedReadOnlyGraphDatabase

class Dependencies extends ScalaModule {
  def configure() {
    bind[GraphDatabaseService].toInstance({
      val path = Play.configuration.getString("neo4j.path").getOrElse(throw new Exception("Neo4J Path not set.  Check you have environment variable $DFID_DATA_PATH configured"))
      val db = new EmbeddedReadOnlyGraphDatabase(path)
      db
    })

    bind[DefaultDB].toInstance(ReactiveMongoPlugin.db)

    bind[ReadOnlyApi[Country]].to[ReadOnlyCountriesApi]
  }
}
