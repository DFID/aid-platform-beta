package controllers

import play.api.mvc.{AnyContent, Action}

object WebJars extends AssetsBuilder {
  def at(file: String): Action[AnyContent] = at("/META-INF/resources/webjars", file)
}
