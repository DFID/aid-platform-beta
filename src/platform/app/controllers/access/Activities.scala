package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import com.google.inject.Inject
import lib.JsonWriters._
import uk.gov.dfid.common.lib.ProjectService

class Activities @Inject()(projectService: ProjectService )extends Controller {

  def index = Action { request =>

    val whereClause = request.queryString.map { case (key, values) =>
      s"n.$key=${values.head}"
    }.mkString("WHERE ", " AND ", "")

    val result = projectService.getIatiActivityNodes(request.queryString.isEmpty, whereClause)

    Ok(Json.toJson(result.toSeq))
  }

  def getFundedProjectsForActivity (iatiId: String) = Action  {

    val result = projectService.getFundedProjectsForActivity(iatiId)
    Ok(Json.toJson(result.toSeq))
  }

}

