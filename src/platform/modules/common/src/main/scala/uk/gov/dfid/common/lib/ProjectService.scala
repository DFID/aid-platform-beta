package uk.gov.dfid.common.lib

import org.neo4j.graphdb.Node

trait ProjectService {

  def getIatiActivityNodes(includeWhereClause : Boolean, whereClause : String) : Seq[Node]

  def getFundedProjectsForActivity(iatiId: String) : Seq[Node]

}
