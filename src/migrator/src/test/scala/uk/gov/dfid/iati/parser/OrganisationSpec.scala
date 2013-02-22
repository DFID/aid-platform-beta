package uk.gov.dfid.iati.parser

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.joda.time.DateTime



class OrganisationSpec extends TestBase {

  describe("Record Header") {
    it("should return none for optional values") {
      val org = Organisation(<iati-organisation></iati-organisation>)

      org.defaultCurrency should be(None)
      org.defaultLanguage should be(None)
      org.lastUpdatedDatetime should be(None)
    }

    it("should return values where they exist") {
      val org = Organisation(<iati-organisation last-updated-datetime="2011-04-22T06:00" default-language="en" default-currency="GBP"></iati-organisation>)

      org.lastUpdatedDatetime should be(Some(DateTime.parse("2011-04-22T06:00")))
      org.defaultCurrency should be(Some("GBP"))
      org.defaultLanguage should be(Some("en"))
    }

    it("should have a reporting organistation") {
      val org = Organisation(<iati-organisation><reporting-org ref="US-EIN-12345"/></iati-organisation>)

      org.reportingOrg should not be(null)
    }
  }

  describe("Iati Identifier") {
    it("must have an identifer") {
      val org = Organisation(<iati-organisation><iati-identifier>Test</iati-identifier></iati-organisation>)

      org.iatiIdentifier should be("Test")
    }
  }

  describe("Name") {
    it("may have a name") {
      val org = Organisation(<iati-organisation><name>Test</name></iati-organisation>)
      org.name should be(Some("Test"))
    }

    it("may have more than one name but we ignore all but the first") {
      val org = Organisation(<iati-organisation><name>Test</name><name>Test 2</name></iati-organisation>)
      org.name should be(Some("Test"))
    }

     it("may have no names") {
      val org = Organisation(<iati-organisation></iati-organisation>)
      org.name should be(None)
    }
  }

}






