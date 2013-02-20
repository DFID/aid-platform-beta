package uk.gov.dfid.common.neo4j

import org.neo4j.cypher.ExecutionResult

trait QueryEngine {
  def execute(cypher: String): ExecutionResult
}
