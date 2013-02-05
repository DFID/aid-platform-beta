package uk.gov.dfid.iati

import xml.{MetaData, Elem, XML}
import java.net.URL
import util.Try
import org.neo4j.kernel.EmbeddedGraphDatabase
import Implicits._
import org.neo4j.graphdb
import graphdb.{Node, GraphDatabaseService, DynamicRelationshipType}


class OrganisationLoader {
  def load(src: String) {
    implicit val db = new EmbeddedGraphDatabase("/tmp/data/neo4j")
    val tx = db.beginTx
    try {
      new XMLToNeo4JParser().parse("http://projects.dfid.gov.uk/iatixmlfiles/organisation.xml")
      tx.success
    }catch{
      case e: Throwable => tx.failure; println(e)
    }finally {
      tx.finish
      db.shutdown()
    }
  }
}

class XMLToNeo4JParser {

  def parse(uri: String)(implicit db: GraphDatabaseService) {
    parse(XML.load(new URL(uri)))
  }

  def parse(el: xml.Node)(implicit db: GraphDatabaseService) : Node = {
    // create a new node for the el
    val node = db.createNode
    // update this node with the label
    node + ("label" -> el.label)
    println(s"Creating node ${el.label}")
    // iterate over each attribute and fill it with data
    // derived from the attributes
    fillProperties(node, el.attributes)
    // loop over each child, if its a plain <tag>text</tag>
    // style tag then push it in as a property
    for(child <- el \ "_"){
      println(s"Working on child ${child.label}")
      // if this child has no descendants then it should be treated as a new entity.
      // if it has a single child that is an atom then its a property
      // it it has a child that is not an atom is an entity
      val children = child.nonEmptyChildren
      val shouldBeProperty = children.size == 1 && children.head.isAtom && child.attributes.isEmpty
      val isNewEntity = children.isEmpty || !shouldBeProperty || children.size > 1
      val isTextNode = child.label.equals("#PCDATA")

      if(!isTextNode) {
        if (isNewEntity) {
          // recursive call to parse to handle the children
          println(s"Creating child node ${child.label}")
          node.createRelationshipTo(parse(child), DynamicRelationshipType.withName(child.label))
        } else {
          if (shouldBeProperty) {
            println(s"\tSetting ${child.label} as ${child.text}")
            node + (child.label -> child.text)
          }

          // fill the properties of the parent node
          fillProperties(node, child.attributes)
        }
      }
    }
    node
  }

  def fillProperties(node: Node, attrs: xml.MetaData) {
    attrs.foreach { attr =>
      println(s"\tPumping attrs ${attr.key}")
      node + (attr.key -> attr.value.head.text.mungeToType)
    }
  }
}