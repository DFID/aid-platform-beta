package controllers

import play.api._
import play.api.mvc._
import uk.gov.dfid.es._
import scala.collection.JavaConversions._
import scala.collection.mutable.Map


object Application extends Controller {

  def search(query: String) = Action {
    play.api.Logger.info("Executing search for: "+query)
    val results = ElasticSearch.search(query,"/dfid/aid-platform-beta/data/elasticsearch")
     
    Ok(views.html.search(query,null))
  }
  
}