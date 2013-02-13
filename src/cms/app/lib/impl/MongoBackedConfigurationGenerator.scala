package lib.impl

import lib.{SourceSelector, ConfigurationGenerator}
import com.google.inject.Inject
import play.api.libs.json.{JsArray, JsString, Json}
import concurrent.ExecutionContext.Implicits.global

class MongoBackedConfigurationGenerator @Inject()(val sources: SourceSelector) extends ConfigurationGenerator {
  def generate = {
    for (
      organisations <- sources.get("organisation", activeOnly = true);
      activities    <- sources.get("activity", activeOnly = true)
    ) yield {
      val sb = new StringBuilder

      if (!organisations.isEmpty) {
        sb.append(organisations.map(_.url).mkString("organisations=[\n  \"", "\",\n  \"", "\"\n]\n"))
      }

      if (!activities.isEmpty) {
        sb.append(activities.map(_.url).mkString("activities=[\n  \"", "\",\n  \"", "\"\n]\n"))
      }

      sb.toString
    }
  }
}
