package uk.gov.dfid.iati.parser

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.joda.time.DateTime
import xml.XML
import java.net.URL

class OrganisationFileSpec extends FunSpec with ShouldMatchers {

  describe("File Header") {
    it("should return none for optional values") {
      val org = OrganisationFile(<iati-organisations></iati-organisations>)

      org.generatedDateTime should be(None)
      org.iatiVersion should be(None)
    }

    it("should return values where they exist") {
      val org = OrganisationFile(<iati-organisations iati-version="1.0" generated-datetime="2011-04-22T06:00"></iati-organisations>)

      org.generatedDateTime should be(Some(DateTime.parse("2011-04-22T06:00")))
      org.iatiVersion should be(Some("1.0"))
    }
  }

  describe("Organisation List") {
    it("should handle a single organisations") {
      val org = OrganisationFile(
        <iati-organisations>
        <iati-organisation></iati-organisation>
        </iati-organisations>)

      org.organisations.size should be(1)
    }

    it("should handle a multiple organisation") {
      val org = OrganisationFile(
        <iati-organisations>
          <iati-organisation></iati-organisation>
          <iati-organisation></iati-organisation>
        </iati-organisations>)

      org.organisations.size should be(2)
    }
  }

  describe("integration with IATI file") {
    it("Should consume the current AIDS filfe") {
      val xml = XML.load(new URL("http://maps.aidsalliance.org/iati/organisation.xml"))
      val org= OrganisationFile(xml)

      org.organisations.size should be(1)
      org.organisations.head.iatiIdentifier should be("21020")
      org.organisations.head.documents.size should be(5)
    }
  }

}


