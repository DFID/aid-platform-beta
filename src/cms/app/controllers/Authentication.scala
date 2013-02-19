package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import views.html
import com.google.inject.Inject
import lib.traits.Authenticator

class Authentication @Inject()(val auth: Authenticator) extends Controller {

  val loginForm = Form(
    tuple(
      "username" -> text,
      "password" -> text
    ) verifying ("Invalid email or password", result => result match {
      case (username, password) => auth.authenticate(username, password)
    })
  )

  def login = Action { implicit request =>
    Ok(html.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => Redirect(routes.Application.index).withSession(Security.username -> user._1)
    )
  }

  def logout = Action {
    Redirect(routes.Authentication.login).withNewSession
  }
}


