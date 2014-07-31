package uk.gov.dfid.loader

import org.neo4j.cypher.ExecutionEngine
import reactivemongo.api.DefaultDB
import uk.gov.dfid.common.{Auditor, DataLoadAuditor}
import reactivemongo.bson.{BSONArray, BSONString, BSONDocument, BSONLong}
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import concurrent.ExecutionContext.Implicits.global
import concurrent.Await
import concurrent.duration.Duration
import io.Source
import scala.util.parsing.combinator.RegexParsers

class SectorHierarchies(engine: ExecutionEngine, db: DefaultDB, auditor: Auditor) {

  def loadSectorHierarchies = {

    auditor.info("Loading sector hierarchies")

    val sector_hierarchies = db.collection("sector-hierarchies")
    val sector_hierarchies_src = Source.fromURL(getClass.getResource("/sector_hierarchies.csv"))
    
    Await.ready(sector_hierarchies.drop(), Duration.Inf)

    val source = sector_hierarchies_src.getLines.drop(1).mkString("\n")
    val sectors = CSV.parse(source)
    sectors.foreach(sector => {
      val highLevelCode = sector(1).toLong
      val categoryCode  = sector(5).toLong
      val sectorCode    = sector(0).toLong

      val document = BSONDocument(
        "highLevelCode" -> BSONLong(highLevelCode),
        "highLevelName" -> BSONString(sector(2)),
        "categoryCode"  -> BSONLong(categoryCode),
        "categoryName"  -> BSONString(sector(6)),
        "sectorCode"    -> BSONLong(sectorCode),
        "sectorName"    -> BSONString(sector(3)),
        "sectorDesc"    -> BSONString(sector(4))
      )

      Await.ready(sector_hierarchies.insert(document), Duration.Inf)
    })
    
    auditor.info("Finished loading sector hierarchies")
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
