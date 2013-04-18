package modules

import com.tzavellas.sse.guice.ScalaModule
import lib._
import traits._
import uk.gov.dfid.common.models._
import uk.gov.dfid.common.api._
import reactivemongo.api.MongoConnection
import play.api.Play
import collection.JavaConversions._
import concurrent.ExecutionContext.Implicits.global
import org.neo4j.graphdb.GraphDatabaseService
import uk.gov.dfid.common.neo4j.{GraphDatabaseManager, SingletonEmbeddedNeo4JDatabaseHasALongName}
import uk.gov.dfid.loader.{DataLoader, Loader}
import uk.gov.dfid.common.{DataLoadAuditor, Auditor}
import reactivemongo.api.DefaultDB
import org.neo4j.cypher.ExecutionEngine

class Dependencies extends ScalaModule {
   def configure() {
     bind[Authenticator].to[SimpleAuthenticator]
     bind[SourceSelector].to[IatiDataSourceSelector]
     bind[Deployer].to[Deployinator]

     bind[DefaultDB].toInstance({
       val connection = MongoConnection(Play.current.configuration.getStringList("mongodb.servers").get.toList)
       val db = connection.db(Play.current.configuration.getString("mongodb.db").get)
       db
     })

     bind[Api[Country]].to[CountriesApi]
     bind[ReadOnlyApi[Country]].to[ReadOnlyCountriesApi]
     bind[ReadOnlyApi[CountryStats]].to[ReadonlyCountryStatsApi]

     bind[Api[Region]].to[RegionsApi]
     bind[ReadOnlyApi[Region]].to[ReadOnlyRegionsApi]

     bind[Api[Project]].to[ProjectsApi]
     bind[ReadOnlyApi[Project]].to[ReadOnlyProjectsApi]

     bind[GraphDatabaseService].toProvider(SingletonEmbeddedNeo4JDatabaseHasALongName)
     bind[GraphDatabaseManager].toInstance(SingletonEmbeddedNeo4JDatabaseHasALongName)

     bind[DataLoader].to[Loader]
     bind[FrontPageManagedContentApi].to[MongoBackedFrontPageManagedContentApi]

     bind[Auditor].to[DataLoadAuditor]
     bind[ReadOnlyApi[AuditLog]].to[ReadOnlyDataLoaderAuditLogsApi]

     bind[Mailer].to[MailerPluginBackedMailer]
   }
 }


