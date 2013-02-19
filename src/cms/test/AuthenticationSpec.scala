package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import controllers.Authentication
import org.specs2.mock._
import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import lib.traits.Authenticator


class AuthenticationSpec extends Specification with Mockito  {

  private val unwantedPlugins = Seq("play.modules.reactivemongo.ReactiveMongoPlugin")
  private val dummyRequest = FakeRequest().withFormUrlEncodedBody(
    "username" -> "username",
    "password" -> "password"
  )

  "Authentication Controller" should {
    "allow the user to login with correct username and password" in {
      running(FakeApplication(withoutPlugins = unwantedPlugins)){
        val auth = mock[Authenticator]
        auth.authenticate(anyString, anyString) returns true

        val controller = new Authentication(auth)
        val response = controller.authenticate()(dummyRequest)

        status(response) must equalTo(SEE_OTHER)
      }
    }

    "prevent users with the wrong username and password form logging in" in {
      val auth = mock[Authenticator]
      auth.authenticate(anyString, anyString) returns false

      val controller = new Authentication(auth)
      val response = controller.authenticate()(dummyRequest)

      status(response) must equalTo(BAD_REQUEST)
    }

    "allow a logged in user to logout" in {
      running(FakeApplication(withoutPlugins = unwantedPlugins)){
        val controller = new Authentication(mock[Authenticator])
        val response = controller.logout()(FakeRequest().withSession("username" -> "someone"))
        session(response) must beEmpty
      }
    }
  }
}