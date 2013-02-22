package controllers.admin

import play.api.mvc.Controller
import models.IatiDataSource
import lib.traits.SourceSelector
import controllers.traits.admin.{IatiDataSourceActions, Secured}
import com.google.inject.Inject

class OrganisationSources @Inject()(val sources: SourceSelector) extends Controller with Secured with IatiDataSourceActions {
  val sourceType = "organisation"

  def view(sources: List[IatiDataSource]) = {
    views.html.admin.organisations(sources)
  }
}


