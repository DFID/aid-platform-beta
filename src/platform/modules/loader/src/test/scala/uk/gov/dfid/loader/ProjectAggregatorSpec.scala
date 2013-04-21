package uk.gov.dfid.loader

import org.specs2.mutable.Specification
import org.neo4j.kernel.EmbeddedReadOnlyGraphDatabase
import org.neo4j.cypher.ExecutionEngine
import reactivemongo.api.MongoConnection
import org.specs2.mock.Mockito
import concurrent.ExecutionContext.Implicits.global
import uk.gov.dfid.loader.util.Sectors


class ProjectAggregatorSpec extends Specification with Mockito {

  "Indexer" should {
    "load up all the other projects" in {
      val db = MongoConnection("localhost:27017" :: Nil).db("dfid")
      val a = new Indexer(
        db,
        new ExecutionEngine(new EmbeddedReadOnlyGraphDatabase("/Users/james/Projects/dfid/data/neo4j")),
        new Sectors(db)
      )

      a.index
    }
  }
}
