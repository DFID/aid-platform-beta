package uk.gov.dfid.iati

import util.Try

/**
 * Sugar for the loading process
 */
object Implicits {

  /**
   * Implicit wrapper for some Neo4j Node Sugar
   * @param n node
   */
  implicit class SuperNeo4JNode(n: org.neo4j.graphdb.Node) {

    /**
     * Thin wrapper around the Node.setProperty call
     * @param property
     */
    def +(property: (String, Any)) {
      n.setProperty(property._1, property._2)
    }
  }

  /**
   * Sugar for working with string
   * @param stringValue
   */
  implicit class SuperString(stringValue : String) {

    /**
     * Super cludge that attempts to parse a string of essentially anything
     * into something else.  Tries Int, Double and Boolean
     * @return Something, anything, magic
     */
    def mungeToType =
      Try(stringValue.toInt) orElse
        Try(stringValue.toDouble) orElse
        Try(stringValue.toBoolean) getOrElse
        stringValue
  }

  /**
   * Sugar for working with XML Nodes
   * @param node
   */
  implicit class SuperXmlNode(node: xml.Node) {

    /**
     * Simple check on a nod to determine if it is a simple text node
     * @return
     */
    def isTextNode = node.label.equals("#PCDATA")
  }
}
