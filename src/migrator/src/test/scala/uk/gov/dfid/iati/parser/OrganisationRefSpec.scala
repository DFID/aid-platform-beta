package uk.gov.dfid.iati.parser

class OrganisationRefSpec extends TestBase {

  describe("Reporting Org Node") {
    it("should handle the minimum defintion") {
      val ele = OrganisationRef(<reporting-org ref="US-EIN-12345" />)

      ele.orgName should be(None)
      ele.orgRef should be("US-EIN-12345")
      ele.orgType should be(None)
    }

    it("should handle the maximum defintion") {
      val ele = OrganisationRef(<reporting-org ref="US-EIN-12345" type="60" xml:lang="en">The XYZ Foundation</reporting-org>)

      ele.orgName should be(Some("The XYZ Foundation"))
      ele.orgRef should be("US-EIN-12345")
      ele.orgType should be(Some("60"))
    }
  }
}
