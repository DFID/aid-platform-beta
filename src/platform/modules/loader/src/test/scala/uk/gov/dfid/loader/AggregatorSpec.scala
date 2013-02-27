package uk.gov.dfid.loader

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.neo4j.cypher.{QueryStatistics, ExecutionResult, ExecutionEngine}
import reactivemongo.api.{DefaultCollection, DefaultDB}
import uk.gov.dfid.common.models.Project
import uk.gov.dfid.common.api.Api
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import concurrent.ExecutionContext.Implicits.global
import java.io.PrintWriter
import java.util

class AggregatorSpec extends Specification with Mockito  {

  class FakeExecutionResult(internal: Iterator[Map[String, Any]]) extends ExecutionResult{
    def hasNext: Boolean = internal.hasNext
    def next(): Map[String, Any] = internal.next()
    def columns: List[String] = ???
    def javaColumns: util.List[String] = ???
    def javaColumnAs[T](column: String): util.Iterator[T] = ???
    def columnAs[T](column: String): Iterator[T] = ???
    def javaIterator: util.Iterator[util.Map[String, Any]] = ???
    def dumpToString(writer: PrintWriter) {}
    def dumpToString(): String = ???
    def queryStatistics(): QueryStatistics = ???
  }

  "rollupProjectBudgets" should {
    "should rollup any budgets found" in {

      // arrange
      val engine = mock[ExecutionEngine]
      val result = mock[ExecutionResult]
      val db     = mock[DefaultDB]
      val coll   = mock[DefaultCollection]
      val api    = mock[Api[Project]]

      db.collection(anyString) returns coll

      engine.execute(anyString) returns new FakeExecutionResult(
        Iterator(
          Map("id" -> "123456","value" -> 123456L),
          Map("id" -> "789101","value" -> 6)
        )
      )

      val aggregator = new Aggregator(engine, db, api)

      // act
      aggregator.rollupProjectBudgets

      // assert
      there was two(coll).update(BSONDocument(), BSONDocument())
    }
  }
}
