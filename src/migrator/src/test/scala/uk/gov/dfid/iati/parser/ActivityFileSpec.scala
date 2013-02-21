package uk.gov.dfid.iati.parser

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.joda.time.DateTime
import xml.XML
import java.net.URL
import collection.immutable.Seq
import io.Source

class ActivityFileSpec extends FunSpec with ShouldMatchers {

  describe("integration with IATI file") {

    val xml = XML.load(getClass.getResource("/test.xml"))
    val org = ActivityFile(xml)
    val firstActivity = org.activities.head

    it("should consume all activites") {
      org.activities.size should be(14)
    }

    it("should extract the base properties correctly"){
      firstActivity.iatiIdentifier should be("GB-1-102580")
      firstActivity.title should be("Nile Basin Initiative")
      firstActivity.description.get.text should be("Increased regional cooperation in the management of the Nile waters to deliver sustainable development")
      firstActivity.description.get.descriptionType should be(None)
      firstActivity.status.code.get should be("2")
      firstActivity.status.description should be("Implementation")
    }

    it("should parse the reporting org correctly") {
      val reportingOrg = firstActivity.reportingOrg.head

      firstActivity.reportingOrg.size should be(1)

      reportingOrg.orgName.get should be("Department for International Development")
      reportingOrg.orgRef should be("GB-1")
      reportingOrg.orgType.get should be("10")
    }

    it("should parse the dates correctly") {
      val dates = org.activities.head.dates

      dates.size should be (4)

      dates.find(_.dateType.equals("end-actual")).get.date should be(DateTime.parse("2013-06-30"))
      dates.find(_.dateType.equals("end-planned")).get.date should be(DateTime.parse("2013-06-30"))
      dates.find(_.dateType.equals("start-actual")).get.date should be(DateTime.parse("2004-03-29"))
      dates.find(_.dateType.equals("start-planned")).get.date should be(DateTime.parse("2003-11-01"))
    }

    it("should extract the contact details correctly") {
      val details = firstActivity.contacts.head

      firstActivity.contacts.size should be(1)

      details.organisation.get should be("Department for International Development")
      details.email.get should be("enquiry@dfid.gov.uk")
      details.mailingAddress.get should be("Public Enquiry Point, Abercrombie House, Eaglesham Road, East Kilbride, Glasgow G75 8EA")
      details.personName should be(None)
      details.telephone.get should be("+44 (0) 1355 84 3132")
    }

    it("should parse the participating orgs correctly") {
      val firstOrg = firstActivity.participatingOrgs.head

      firstActivity.participatingOrgs.size should be(2)

      firstOrg.orgRef should be("GB")
      firstOrg.orgType.get should be("10")
      firstOrg.role should be("Funding")
      firstOrg.orgName.get should be("UNITED KINGDOM")
    }

    it("should parse the recipients correctly") {
      firstActivity.recipientCountries.size should be(0)
      firstActivity.recipientRegions.size should be(0)
    }

    it("should parse budgets correctly") {
      firstActivity.budgets.size should be(0)

      val secondActivityBugets = org.activities.tail.head.budgets

      secondActivityBugets.size should be(6)

      val budget = secondActivityBugets.head

      budget.budgetType should be("Original")
      budget.periodStart.isoDate should be(DateTime.parse("2003-04-01"))
      budget.periodEnd.isoDate should be(DateTime.parse("2004-03-31"))
      budget.value.valueDate should be(DateTime.parse("2003-04-01"))
      budget.value.currency.get should be("GBP")
      budget.value.amount should be(20000)
    }
  }

}


