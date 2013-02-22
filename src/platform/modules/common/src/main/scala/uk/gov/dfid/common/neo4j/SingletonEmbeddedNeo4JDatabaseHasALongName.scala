package uk.gov.dfid.common.neo4j

import org.neo4j.kernel.EmbeddedGraphDatabase
import org.neo4j.kernel.EmbeddedReadOnlyGraphDatabase
import com.typesafe.config.ConfigFactory

object SingletonEmbeddedNeo4JDatabaseHasALongName {

  private lazy val path = ConfigFactory.load.getString("neo4j.path")
  lazy val db = new EmbeddedGraphDatabase(path)
}
