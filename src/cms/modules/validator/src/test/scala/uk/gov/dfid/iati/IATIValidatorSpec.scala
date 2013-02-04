package uk.gov.dfid.iati

import validators.IATIValidator
import org.specs2.mutable.Specification

class IATIValidatorSpec extends Specification {
  "IATI Validator" should {
    "have a validate method that must be implemented" in {
      object TestValidator extends IATIValidator {
        def validate(source: String, version: String) = true
      }

      TestValidator.validate("", "") must equalTo(true)
    }
  }
}