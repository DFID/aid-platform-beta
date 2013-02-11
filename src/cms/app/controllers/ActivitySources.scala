package controllers

import play.api.mvc.Controller
import com.google.inject.Inject
import traits.{IatiDataSourceActions, Secured}
import models.IatiDataSource
import lib.SourceSelector

class ActivitySources @Inject()(val sources: SourceSelector) extends Controller with Secured with IatiDataSourceActions {
  val sourceType = "activity"

  def view(sources: List[IatiDataSource]) = {
    views.html.activities(sources)
  }
}
