package uk.gov.dfid.loader

import javax.xml.validation.SchemaFactory
import javax.xml.transform.stream.StreamSource
import util.Try
import java.io.InputStream

/**
 * Concrete Implementation of a validator that uses the remote IATI Standard XSDs
 * to validate a given source for an Organisation file
 */
class Validator {

  def validate(source: InputStream, version: String, sourceType: String) = {

    val plural = sourceType match {
      case "organisation" => "organisations"
      case "activity" => "activities"
    }

    val xsd = s"http://iatistandard.org/downloads/$version/iati-$plural-schema.xsd"
    val schema = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema").newSchema(new StreamSource(xsd))

    Try(schema.newValidator.validate(new StreamSource(source))).isSuccess
  }
 }
