package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import com.typesafe.plugin._
import play.api.data.Forms._
import play.api.data.Form
import play.api.Play
import play.api.Play.current
import lib.Mailer

/**
 * Reporter controller that is used to send email to the correct parties
 */
class Reporter @Inject()(mailer: Mailer) extends Controller {

  /**
   * Encapsulates all the data necessary to represent a case of fraud
   * @param country The country to be reported (optional)
   * @param project The project to reported (optional)
   * @param description The description of the fraud being committed
   * @param name The submitters name (optional)
   * @param email The email address of the submitter (optional)
   * @param telno The telephone number of the submitter (optional)
   */
  case class FraudForm(
    country     : Option[String],
    project     : Option[String],
    description : String,
    name        : Option[String],
    email       : Option[String],
    telno       : Option[String]) {

    /**
     * Builds up a plain text email body from the supplied fields
     * @return The body
     */
    def body =
      (country.getOrElse("") :: project.getOrElse("") ::
          description ::
          name.getOrElse("") ::
          email.getOrElse("") ::
          telno.getOrElse("") :: Nil).mkString("<p>","","</p>")
  }

  /**
   * Encapsualtes a unit of feedback from the site
   * @param description The feedback
   * @param name The name of the submtter
   * @param email The email address of the submitter.
   */
  case class FeedbackForm(description: String, name: String, email: String)

  /**
   * Provides a mapping between a request and the FraudForm case class
   */
  val fraudForm = Form(
    mapping(
      "country" -> optional(text),
      "project" -> optional(text),
      "description" -> nonEmptyText,
      "name" -> optional(text),
      "email" -> optional(text),
      "telno" -> optional(text)
    )(FraudForm.apply)(FraudForm.unapply)
  )

  /**
   * Provides a mapping between the request and the FeedbackForm case class
   */
  val feedbackForm = Form(
    mapping(
      "description" -> nonEmptyText,
      "name" -> nonEmptyText,
      "email" -> nonEmptyText
    )(FeedbackForm.apply)(FeedbackForm.unapply)
  )

  /**
   *
   * @return
   */
  def fraud = Action { implicit request =>
    fraudForm.bindFromRequest.fold(
      errors => Redirect("/"),
      form => {
        val to = Play.application.configuration.getString("address.fraud")
          .getOrElse(throw new Exception("address.fraud not configured"))
        val subject = s"Report Fraud: ${form.country.getOrElse(form.project.getOrElse(""))}"

        mailer.send("fraud@dfid.gov.uk", to, subject, form.body)

        // redirect back to the main page of the site
        Redirect("/")
      }
    )
  }

  /**
   * Extracts feedback data from a request and
   * @return
   */
  def feedback = Action { implicit request =>
    feedbackForm.bindFromRequest.fold(
      errors => Redirect("/"),
      form => {
        val to = Play.application.configuration.getString("address.feedback").getOrElse(throw new Exception("address.feedback not configured"))
        mailer.send("ukaidtracker-feedback@dfid.gov.uk", to, "Aid Platform Feedback", form.description)

        // redirect back to the main page of the site
        Redirect("/")
      }
    )
  }
}
