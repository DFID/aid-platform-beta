package uk.gov.dfid.loader

import scala.util.Try
import org.neo4j.tooling.GlobalGraphOperations
import org.neo4j.graphdb.{NotFoundException, GraphDatabaseService}
import collection.JavaConversions._

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

    /**
     * Gets a node property using an Option[T] rather
     * than returning null - also performs type coercion
     * @param name
     * @tparam T
     * @return
     */
    def getPropertySafe[T](name: String): Option[T] = {
      try{
        n.getProperty(name) match {
          case null => None
          case prop => Some(prop.asInstanceOf[T])
        }
      } catch{
        // if the property isn't found we can just return a none
        case e: NotFoundException => None
      }
    }

    def toMap = {
      n.getPropertyKeys.map { key =>
        (key, n.getProperty(key) match {
          case v: java.lang.String  => v
          case v: java.lang.Integer => v.toInt
          case v: java.lang.Long    => v.toLong
          case v: java.lang.Double  => v.toDouble
          case v: java.lang.Boolean => !(!v)
        })
      }
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
      Try(stringValue.toLong) orElse
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

  implicit class SuperGraphDatabaseService(db: GraphDatabaseService) {
    def withTransaction[T](f: => T): T = {
      val tx = db.beginTx
      try {
        val result = f
        tx.success()
        result
      } catch {
        case e: Throwable =>
          println(e.getMessage)
          tx.failure()
          throw e
      } finally {
        tx.finish()
      }
    }

    def clearDb = {
      val tx = db.beginTx
      try {
        // kill the indexes
        db.index.nodeIndexNames.foreach(db.index.forNodes(_).delete)
        db.index.relationshipIndexNames.foreach(db.index.forRelationships(_).delete)

        // kill the nodes and relationships
        val ops = GlobalGraphOperations.at(db)
        ops.getAllRelationships.foreach(_.delete)
        ops.getAllNodes.foreach(_.delete)

        // complete the transaction
        tx.success
      } catch {
        case e: Throwable => {
          tx.failure
          throw e
        }
      } finally {
        tx.finish
      }
    }
  }
}
