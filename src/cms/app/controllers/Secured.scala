package controllers

import play.api.mvc._

trait Secured {

  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Authentication.login)

  def SecuredAction(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  def SecuredAction[A](parser: BodyParser[A])(f: => String => Request[A] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(parser)(request => f(user)(request))
    }
  }
}