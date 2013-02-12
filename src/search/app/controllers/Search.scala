package controllers

import org.elasticsearch.node.Node
import com.google.inject.Inject
import play.api.mvc.{Controller, Action}

class Search @Inject()(val searcher: Node) extends Controller {

  def index(query: String) = TODO
}
