package lib

import com.typesafe.plugin._
import play.api.Play.current

/**
 * Mailer trait that supports the sending of simple emails
 */
trait Mailer {
  def send(from: String, to: String, subject: String, body: String)
}

/**
 * Mailer implementation that uses the Play 2 Mailer plugin to send mails
 */
class MailerPluginBackedMailer extends Mailer{
  def send(from: String, to: String, subject: String, body: String) {
    val mail = use[MailerPlugin].email

    mail.setSubject(subject)
    mail.addRecipient(to)
    mail.addFrom(from)
    mail.send(body)
  }
}