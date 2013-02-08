package uk.gov.dfid.iati

import validators.{IATIValidator}
import org.specs2.mutable.Specification
import java.io.InputStream

class IATIValidatorSpec extends Specification {
  "IATI Validator" should {
    "have a validate method that must be implemented" in {
      object TestValidator extends IATIValidator {
        def validate(source: InputStream, version: String, sourceType: String) = true
      }

      TestValidator.validate(null, "", "") must equalTo(true)
    }
  }
}

