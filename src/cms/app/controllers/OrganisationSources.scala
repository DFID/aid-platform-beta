package controllers

import play.api.mvc.Controller
import com.google.inject.Inject
import traits.{IatiDataSourceActions, Secured}
import models.IatiDataSource
import lib.traits.SourceSelector

class OrganisationSources @Inject()(val sources: SourceSelector) extends Controller with Secured with IatiDataSourceActions {
  val sourceType = "organisation"

  def view(sources: List[IatiDataSource]) = {
    views.html.organisations(sources)
  }
}


