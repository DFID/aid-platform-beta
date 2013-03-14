package uk.gov.dfid.iati.parser


class DocumentLinkSpec extends TestBase {
  describe("Documents") {
    it("should accept the full data set"){
      val doc = DocumentLink(
        <document-link url="http:\\organisation.com\document.txt" format="text/plain">
          <language>en</language>
          <category code="B02" xml:lang="en">Strategy Paper</category>
          <title xml:lang="en">White Paper on Aid</title>
        </document-link>)


      doc.url should be("""http:\\organisation.com\document.txt""")
      doc.format should be(Some("text/plain"))
      doc.category should not be(null)
      doc.language should be(Some("en"))
      doc.title should be("White Paper on Aid")
    }

    it("should accept the minimum data set"){
      val doc = DocumentLink(
        <document-link url="http:\\organisation.com\document.txt" format="text/plain">
          <category code="B02" />
          <title>White Paper on Aid</title>
        </document-link>)

      doc.url should be("""http:\\organisation.com\document.txt""")
      doc.format should be(Some("text/plain"))
      doc.category should not be(null)
      doc.language should be(None)
      doc.title should be("White Paper on Aid")
    }
  }
}
