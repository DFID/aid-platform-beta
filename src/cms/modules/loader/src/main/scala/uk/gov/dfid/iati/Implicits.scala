package uk.gov.dfid.iati

import org.neo4j.graphdb.Node
import util.Try

object Implicits {
  implicit class SuperNode(n: Node) {
    def +(property: (String, Any)) {
      n.setProperty(property._1, property._2)
    }
  }

  implicit class SuperString(stringValue : String) {
    def mungeToType =
      Try(stringValue.toInt) orElse
        Try(stringValue.toDouble) orElse
        Try(stringValue.toBoolean) getOrElse
        stringValue
  }

  implicit class SuperXmlNode(node: xml.Node) {
    def isTextNode = node.label.equals("#PCDATA")
  }
}
