package uk.gov.dfid.loader

import org.specs2.mutable.{BeforeAfter, Specification}
import io.Source
import java.io.File
import org.joda.time.DateTime
import org.neo4j.kernel.{EmbeddedReadOnlyGraphDatabase, EmbeddedGraphDatabase}
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.cypher.ExecutionEngine
import reactivemongo.api.MongoConnection
import uk.gov.dfid.common.DataLoadAuditor
import org.specs2.mock.Mockito
import concurrent.ExecutionContext.Implicits.global


class ProjectAggregatorSpec extends Specification with Mockito {

  // CURRENTLY A USELESS WORK IN PROGRESS
  /*
  "" should {
    "" in new sandbox {

    }
  }

  trait sandbox extends BeforeAfter {

    private val tmp = System.getProperty("java.io.tmpdir")
    private val dir = DateTime.now.getMillis

    var db: GraphDatabaseService = _

    def after: Any = {
      db.shutdown
    }

    def before: Any = {
      db = new EmbeddedGraphDatabase(s"/$tmp/$dir")
    }
  }
  */

}
