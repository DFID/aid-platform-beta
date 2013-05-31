package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import com.google.inject.Inject
import org.neo4j.graphdb.{GraphDatabaseService, Node}
import lib.ApiController


object ActivityQueryBuilder {

  val whitelist = Map(
    "status"    -> "status.code",
    "hierarchy" -> "node.hierarchy"
  )

  def activities = new StringBuilder("""
    | START node = node:entities(type='iati-activity')
    | MATCH node-[:`activity-status`]-status
  """.stripMargin)

  def where(query: Map[String, Seq[String]]) = {
    val result = whitelist.flatMap { case (key, property) =>
      query.get(key).map { parameter =>
        parameter.mkString(s"$property IN [", ",", "]")
      }
    }

    if (result.isEmpty){
      ""
    }else {
      result.mkString("WHERE ", " AND ", "")
    }
  }

  def count(query: StringBuilder) = new StringBuilder(query.toString) ++= "RETURN COUNT(node) as total"

  def page(query: StringBuilder, start: Long, limit: Long) = {
    (new StringBuilder(query.toString) ++= s"""
        | RETURN   node
        | ORDER BY node.`iati-identifier`?
        | SKIP     $start
        | LIMIT    $limit
      """.stripMargin).toString
  }
}

class Activities @Inject()(val graph: GraphDatabaseService) extends Controller with ApiController {

  def index = Action { implicit request =>

    import ActivityQueryBuilder._

    implicit val writes = lib.JsonWriters.DefaultNodeWrites

    val (start, limit) = paging.bindFromRequest.get
    val totalizer = count(activities ++= where(request.queryString))

    val (options, results) = Map(
      "start" -> start,
      "limit" -> limit,
      "total" -> engine.execute(totalizer.toString).columnAs[Long]("total").toSeq.head
    ) -> (
      engine.execute(
        page(
          activities ++= where(request.queryString), start, limit
        )
      ).columnAs[Node]("node").toSeq
    )

    Ok(Json.obj(
      "options" -> options,
      "results" -> results
    ))
  }

  def view(id: String) = Action {
    implicit val writes = lib.JsonWriters.DeepNodeWrites
    Ok(Json.toJson(single("iati-activity", id)))
  }
}

class Transactions @Inject()(val graph: GraphDatabaseService) extends Controller with ApiController {

  def index = Action { implicit request =>
    implicit val writes = lib.JsonWriters.DeepNodeWrites
    val (options, results) = list("transaction", "label")
    Ok(Json.obj(
      "options" -> options,
      "results" -> results
    ))
  }
}

class Organisations @Inject()(val graph: GraphDatabaseService) extends Controller with ApiController {

  def index = Action { implicit request =>
    implicit val writes = lib.JsonWriters.DefaultNodeWrites
    val (options, results) = list("iati-organisation")
    Ok(Json.obj(
      "options" -> options,
      "results" -> results
    ))
  }

  def view(id: String) = Action {
    implicit val writes = lib.JsonWriters.DeepNodeWrites
    Ok(Json.toJson(single("iati-organisation", id)))
  }
}

class ParticipatingOrgs @Inject()(val graph: GraphDatabaseService) extends Controller with ApiController {

  def index = Action { implicit request =>

    implicit val writes = lib.JsonWriters.DefaultNodeWrites
    val (options, results) = list("participating-org", "ref")
    Ok(Json.obj(
      "options" -> options,
      "results" -> results
    ))
  }
}

