package uk.gov.dfid.iati

import javax.xml.validation.SchemaFactory
import javax.xml.transform.stream.StreamSource
import org.xml.sax.SAXException

class Validator {
  def validate(xmlFile: String, xsdFile: String): Boolean = {
    try {
      val schemaLang = "http://www.w3.org/2001/XMLSchema"
      val factory = SchemaFactory.newInstance(schemaLang)
      val schema = factory.newSchema(new StreamSource(xsdFile))
      val validator = schema.newValidator()
      validator.validate(new StreamSource(xmlFile))
    } catch {
      case ex: SAXException => println(ex.getMessage()); return false
      case ex: Exception => ex.printStackTrace()
    }
    true
  }
}