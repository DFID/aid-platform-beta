package modules

import com.tzavellas.sse.guice.ScalaModule
import lib._
import traits._
import uk.gov.dfid.common.models.{CountryStats, Country}
import uk.gov.dfid.common.api._
import reactivemongo.api.MongoConnection
import models.WhatWeDoEntry
import play.api.Play
import collection.JavaConversions._
import concurrent.ExecutionContext.Implicits.global
import org.neo4j.graphdb.GraphDatabaseService
import uk.gov.dfid.common.neo4j.SingletonEmbeddedNeo4JDatabaseHasALongName
import uk.gov.dfid.loader.{DataLoader, Loader}
import reactivemongo.api.DefaultDB

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
     bind[Api[WhatWeDoEntry]].to[WhatWeDoApi]
     bind[GraphDatabaseService].toProvider(SingletonEmbeddedNeo4JDatabaseHasALongName)
     bind[ReadOnlyApi[Country]].to[ReadOnlyCountriesApi]
     bind[ReadOnlyApi[CountryStats]].to[ReadonlyCountryStatsApi]
     bind[DataLoader].to[Loader]
     bind[FrontPageManagedContentApi].to[MongoBackedFrontPageManagedContentApi]
   }
 }


