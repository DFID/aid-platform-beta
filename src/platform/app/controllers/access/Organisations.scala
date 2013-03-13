package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import com.google.inject.Inject
import org.neo4j.graphdb.{Node, GraphDatabaseService}
import lib.JsonWriters._
import lib.ApiController

class Organisations @Inject()(implicit db: GraphDatabaseService )extends Controller with ApiController {

  def index = Action { implicit request =>

    val (start, limit) = paging.bindFromRequest.get

    val result = engine.execute(
      s"""
        | START    organisation = node:entities(type="iati-organisation")
        | RETURN   organisation
        | ORDER BY organisation.`iati-identifier`
        | SKIP     $start
        | LIMIT    $limit
      """.stripMargin).columnAs[Node]("organisation")

    Ok(Json.toJson(
      Json.obj(
        "options" -> Json.obj(
          "start" -> start,
          "limit" -> limit
        ),
        "results" -> result.toSeq
      )
    ))
  }

  def view(id: String) = Action {

    val result = engine.execute(
      s"""
        | START    organisation = node:entities(type="iati-organisation")
        | WHERE    organisation.`iati-identifier` = $id
        | RETURN   organisation
      """.stripMargin).columnAs[Node]("organisation")

    Ok(Json.toJson(result.toSeq.headOption))
  }
}

