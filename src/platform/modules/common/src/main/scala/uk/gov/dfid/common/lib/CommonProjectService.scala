package uk.gov.dfid.common.lib

import com.google.inject.Inject
import org.neo4j.graphdb.{Node, GraphDatabaseService}
import org.neo4j.cypher.{ExecutionResult, ExecutionEngine}
import org.neo4j.kernel.impl.util.StringLogger

class CommonProjectService @Inject()(db: GraphDatabaseService ) extends ProjectService {

  lazy val executionEngine = new ExecutionEngine(db, StringLogger.DEV_NULL)

  def getIatiActivityNodes(includeWhereClause : Boolean, whereClause : String) : Seq[Node] = {
    executionEngine.execute(s"""
        | START n=node:entities(type="iati-activity")
        | ${ if(includeWhereClause) whereClause else "" }
        | RETURN n
      """.stripMargin).columnAs[Node]("n").toSeq
  }

  def getFundedProjectsForActivity(iatiId: String) : Seq[Node] = {
    executionEngine.execute(s"""
       | START n=node:entities(type="provider-org")
       | MATCH n-[`provider-org`]-t-[`transaction`]-activity
       | WHERE activity.label = "iati-activity"
       | AND has(n.`provider-activity-id`)
       | AND n.`provider-activity-id` = "$iatiId"
       | RETURN activity""".stripMargin).columnAs[Node]("activity").toSeq
  }
}
