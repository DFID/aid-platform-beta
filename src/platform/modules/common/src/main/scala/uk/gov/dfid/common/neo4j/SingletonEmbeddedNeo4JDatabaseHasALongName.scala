package uk.gov.dfid.common.neo4j

import org.neo4j.kernel.EmbeddedGraphDatabase
import com.typesafe.config.ConfigFactory
import com.google.inject.Provider
import org.neo4j.graphdb.GraphDatabaseService
import sys.process._
import concurrent.Lock

object SingletonEmbeddedNeo4JDatabaseHasALongName extends Provider[GraphDatabaseService] {

  private lazy val path = ConfigFactory.load.getString("neo4j.path")
  private var db : EmbeddedGraphDatabase = _
  private val lock = new Lock

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run {
      shutdown
    }
  })

  def get(): GraphDatabaseService = {

    if (db == null) {
      lock.acquire
      db = new EmbeddedGraphDatabase(path)
      lock.release
    }

    db
  }

  def restart(flush: Boolean = false) =  {
    lock.acquire
    shutdown
    if(flush) {
      s"rm -rf $path".!
    }
    lock.release
    get
  }

  private def shutdown = {
    if (db != null) {
      db.shutdown
      db = null
    }
  }
}
