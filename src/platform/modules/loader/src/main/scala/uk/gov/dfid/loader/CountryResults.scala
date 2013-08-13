package uk.gov.dfid.loader

import org.neo4j.cypher.ExecutionEngine
import reactivemongo.api.DefaultDB
import uk.gov.dfid.common.{Auditor, DataLoadAuditor}
import reactivemongo.bson.{BSONArray, BSONString, BSONDocument}
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import concurrent.ExecutionContext.Implicits.global
import concurrent.Await
import concurrent.duration.Duration
import io.Source
import scala.util.parsing.combinator.RegexParsers

class CountryResults(engine: ExecutionEngine, db: DefaultDB, auditor: Auditor) {

  def loadCountryResults = {
    val country_results = db.collection("country-results")
    val country_results_src = Source.fromURL(getClass.getResource("/country_results.csv"))
    
    Await.ready(country_results.drop(), Duration.Inf)

    val source = country_results_src.getLines.drop(1).mkString("\n")
    val results = CSV.parse(source)
    results.foreach { result =>
      val document = BSONDocument(
        "country" -> BSONString(result(0)),
        "code" -> BSONString(result(1)),
        "pillar" -> BSONString(result(2)),
        "results" -> BSONString(result(3)),
        "total" -> BSONString(result(5))
      )
      //val label = BSONString(result(0))

      Await.ready(country_results.insert(document), Duration.Inf)
      //auditor.info(s"Inserted results for " + label.toString)
    }
  }

  object CSV extends RegexParsers {
    override val skipWhitespace = false   // meaningful spaces in CSV

    def COMMA   = ","
    def DQUOTE  = "\""
    def DQUOTE2 = "\"\"" ^^ { case _ => "\"" }  // combine 2 dquotes into 1
    def CRLF    = "\r\n" | "\n"
    def TXT     = "[^\",\r\n]".r
    def SPACES  = "[ \t]+".r

    def file: Parser[List[List[String]]] = repsep(record, CRLF) <~ (CRLF?)

    def record: Parser[List[String]] = repsep(field, COMMA)

    def field: Parser[String] = escaped|nonescaped

    def escaped: Parser[String] = {
      ((SPACES?)~>DQUOTE~>((TXT|COMMA|CRLF|DQUOTE2)*)<~DQUOTE<~(SPACES?)) ^^ {
        case ls => ls.mkString("")
      }
    }

    def nonescaped: Parser[String] = (TXT*) ^^ { case ls => ls.mkString("") }

    def parse(s: String) = parseAll(file, s) match {
      case Success(res, _) => res
      case e => throw new Exception(e.toString)
    }
  }
}
