package uk.gov.dfid.iati.parser

class BudgetSpec extends TestBase {
  describe("Base Budget Object") {
    it("should handle the full data set") {
      val budget = Budget(
        <total-budget>
          <period-start iso-date="2012-01-01">2012</period-start>
          <period-end iso-date="2012-12-31">2012</period-end>
          <value currency="USD" value-date="2012-01-01">250000000</value>
        </total-budget>)

      budget.periodStart should not be(null)
      budget.periodEnd should not be(null)
      budget.value should not be(null)
    }

    it("should handle the minimum set of data") {
      val budget = Budget(
        <total-budget>
          <period-start iso-date="2012-01-01"/>
          <period-end iso-date="2012-12-31"/>
          <value value-date="2012-01-01">250000000</value>
        </total-budget>)

      budget.periodStart should not be(null)
      budget.periodEnd should not be(null)
      budget.value should not be(null)
    }
  }

  describe("Recipient Organisation Budget") {

    it("should handle the full set of data"){
      val budget = RecipentOrganisationBudget(
        <recipient-org-budget>
          <recipient-org ref="44001">Banque internationale pour la reconstruction et le développement</recipient-org>
          <period-start iso-date="2013-10-01">2013/14</period-start>
          <period-end iso-date="2014-09-30">2013/14</period-end>
          <value currency="USD" value-date="2013-10-01">300000000</value>
        </recipient-org-budget>)


      budget.recipientOrg should not be(null)
      budget.periodStart should not be(null)
      budget.periodEnd should not be(null)
      budget.value should not be(null)
    }

    it("should handle the minimum set of data"){
      val budget = RecipentOrganisationBudget(
        <recipient-org-budget>
          <recipient-org ref="41114"/>
          <period-start iso-date="2012-04-01"/>
          <period-end iso-date="2013-03-31"/>
          <value value-date="2012-04-01">250000000</value>
        </recipient-org-budget>)


      budget.recipientOrg should not be(null)
      budget.periodStart should not be(null)
      budget.periodEnd should not be(null)
      budget.value should not be(null)
    }
  }

  describe("Recipient Country Budget") {
    it("should handle a maximum set of data") {
      val budget = RecipientCountryBudget(
        <recipient-country-budget>
          <recipient-country code="IV" xml:lang="en">Ivory Coast</recipient-country>
          <period-start iso-date="2012-04-01">2012/13</period-start>
          <period-end iso-date="2013-03-31">2012/13</period-end>
          <value currency="USD" value-date="2012-04-01">250000000</value>
        </recipient-country-budget>)

      budget.recipientCountry should not be(null)
      budget.periodStart should not be(null)
      budget.periodEnd should not be(null)
      budget.value should not be(null)
    }

    it("should handle a minimum set of data") {
      val budget = RecipientCountryBudget(
        <recipient-country-budget>
          <recipient-country code="IV"/>
          <period-start iso-date="2012-04-01"/>
          <period-end iso-date="2013-03-31"/>
          <value value-date="2012-04-01">250000000</value>
        </recipient-country-budget>)

      budget.recipientCountry should not be(null)
      budget.periodStart should not be(null)
      budget.periodEnd should not be(null)
      budget.value should not be(null)
    }
  }
}
