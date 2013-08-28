package controllers

import play.api.mvc.{Controller, Action}
import play.api.libs.json.{JsArray, JsObject, Json}
import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source

/**
 * Created with IntelliJ IDEA.
 * User: endam
 * Date: 28/08/2013
 * Time: 11:02
 * To change this template use File | Settings | File Templates.
 */
object RSS extends Controller{

  val apiRootURL = "http://localhost:9000/api/"

  def countryProjectRss(code:String) = Action {
    Async(
      Future{
        val country = getJsonFromUrl("country/" + code).asInstanceOf[JsObject]
        val projects = getJsonFromUrl("projects/" + code).asInstanceOf[JsArray]

        val myRss =
          <rss version="2.0">
            <channel>
              <title>{ country.\("name").as[String] }</title>
              <description>{ country.\("description").as[String] }</description>
              <link>http://devtracker.dfid.gov.uk/countries/{ country.\("code").as[String] }/</link>

              {
              for (project <- projects.value) yield {
                <item>
                  <title>{project.\("title").as[String]}</title>
                  <description>{project.\("description").as[String]}</description>
                  <link>http://devtracker.dfid.gov.uk/projects/{project.\("id").as[String].dropRight(4)}</link>
                  <guid isPermaLink="false">{UUID.randomUUID()}</guid>
                  <pubDate>Tue, 06 Oct 2012 13:00:00 +0100</pubDate>
                </item>
              }
              }
            </channel>
          </rss>

        Ok(myRss).as("text/XML")
      }
    )
  }

  def getJsonFromUrl(apiExtention: String) = {
    val url = new java.net.URL(apiRootURL + apiExtention)
    Json.parse(Source.fromInputStream(url.openStream).getLines.mkString("\n"))
  }

}
