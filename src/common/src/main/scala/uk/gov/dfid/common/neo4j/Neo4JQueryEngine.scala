package uk.gov.dfid.common.neo4j

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.kernel.impl.util.StringLogger


class Neo4JQueryEngine(db: GraphDatabaseService) extends QueryEngine {

   private lazy val engine = new ExecutionEngine(db, StringLogger.DEV_NULL)

   def execute(cypher: String) = {
     engine.execute(cypher)
   }
 }
