package uk.gov.dfid.iati.validators

import javax.xml.validation.SchemaFactory
import javax.xml.transform.stream.StreamSource
import util.Try

/**
 * Concrete Implementation of a validator that uses the remote IATI Standard XSDs
 * to validate a given source for an Activities file
 */
class IATIActivitiesFileValidator extends IATIValidator {
   def validate(source: String, version: String) = {
     val source = s"http://iatistandard.org/downloads/$version/iati-activitites-schema.xsd"
     val schema = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema").newSchema(new StreamSource(source))

     Try(schema.newValidator.validate(new StreamSource(source))).isSuccess
   }
 }
