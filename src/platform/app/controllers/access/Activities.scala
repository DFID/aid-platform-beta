package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import collection.JavaConversions._
import com.google.inject.Inject
import org.neo4j.graphdb.{Node, GraphDatabaseService}
import lib.JsonWriters._
import org.neo4j.cypher.ExecutionEngine

class Activities @Inject()(db: GraphDatabaseService )extends Controller {

  def index = Action { request =>

    val whereClause = request.queryString.map { case (key, values) =>
      s"n.$key=${values.head}"
    }.mkString("WHERE ", " AND ", "")

    val results = new ExecutionEngine(db).execute(
      s"""
        | START n=node:entities(type="iati-activity")
        | ${ if(request.queryString.isEmpty) "" else whereClause }
        | RETURN n
      """.stripMargin).columnAs[Node]("n")
    Ok(Json.toJson(results.toSeq))
  }

}

