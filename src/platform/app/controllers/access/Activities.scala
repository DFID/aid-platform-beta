package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import com.google.inject.Inject
import org.neo4j.graphdb.{GraphDatabaseService, Node}
import lib.JsonWriters._
import lib.ApiController


class Activities @Inject()(implicit graph: GraphDatabaseService) extends Controller with ApiController {

  def index = Action { implicit request =>

    val (start, limit) = paging.bindFromRequest.get

    val result = engine.execute(
      s"""
        | START    activity = node:entities(type="iati-activity")
        | RETURN   activity
        | ORDER BY activity.`iati-identifier`
        | SKIP     $start
        | LIMIT    $limit
      """.stripMargin).columnAs[Node]("activity")

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
        | START    activity = node:entities(type="iati-activity")
        | WHERE    activity.`iati-identifier` = '$id'
        | RETURN   activity
      """.stripMargin).columnAs[Node]("activity")

    Ok(Json.toJson(result.toSeq.headOption))
  }
}

