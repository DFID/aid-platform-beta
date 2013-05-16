package controllers.traits.admin

import play.api.mvc._
import controllers.admin.routes
import scala.util.Random
import scala.concurrent.duration._


trait Secured {

  /**
   * Returns the value of the username that is currently active on the system
   * also checks the activity-time and can timeout a request if it has been idle for
   * 15 minutes or more
   * @param request
   * @return
   */
  def username(request: RequestHeader) = {
    val username = request.session.get(Security.username)

    request.session.get("activity-time").map { time =>
      val timespan = System.currentTimeMillis - time.toLong
      if(timespan < 15.minutes.toMillis) {
        username
      } else {
        None
      }
    } getOrElse username
  }

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Authentication.login)

  def SecuredAction(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request).withSession(request.session + ("activity-time" -> System.currentTimeMillis().toString)))
    }
  }

  def SecuredAction[A](parser: BodyParser[A])(f: => String => Request[A] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(parser)(request => f(user)(request).withSession(request.session + ("activity-time" -> System.currentTimeMillis().toString)))
    }
  }
}