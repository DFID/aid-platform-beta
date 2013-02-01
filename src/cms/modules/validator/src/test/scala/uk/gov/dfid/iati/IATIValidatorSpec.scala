package uk.gov.dfid.iati

import validators.IATIValidator
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class IATIValidatorSpec extends FunSpec with ShouldMatchers{
  describe("IATI Validator") {
    it("should have a validate method that must be implemented") {
      object TestValidator extends IATIValidator {
        def validate(source: String, version: String) = true
      }

      TestValidator.validate("", "") should be(true)
    }
  }
}