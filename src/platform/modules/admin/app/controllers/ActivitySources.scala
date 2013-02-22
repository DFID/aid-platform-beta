package controllers.admin

import play.api.mvc.Controller
import models.IatiDataSource
import lib.traits.SourceSelector
import controllers.traits.admin.{IatiDataSourceActions, Secured}
import com.google.inject.Inject

class ActivitySources @Inject()(val sources: SourceSelector) extends Controller with Secured with IatiDataSourceActions {
  val sourceType = "activity"

  def view(sources: List[IatiDataSource]) = {
    views.html.admin.activities(sources)
  }
}
