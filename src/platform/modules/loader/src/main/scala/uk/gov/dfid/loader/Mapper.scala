package uk.gov.dfid.loader

import Implicits._
import org.neo4j.graphdb.{DynamicRelationshipType, Node, GraphDatabaseService}
import org.neo4j.graphdb.index.Index
import uk.gov.dfid.common.DataLoadAuditor

class Mapper(val db: GraphDatabaseService, auditor: DataLoadAuditor) {

  /**
   * Parses XML elements into Graph structures
   * @param el
   * @return
   */
  def map(el: xml.Node) = {
    db.withTransaction {
      // get/create an index for getting entity types
      implicit val index = db.index.forNodes("entities")

      performMap(el)

      auditor.info("Mapping of source complete")
    }
  }

  private def performMap(el: xml.Node)(implicit index: Index[Node]) : Node = {
    val node = db.createNode

    // add this entity/node to the index we use to get types of nodes
    index.add(node, "type", el.label)

    // update this node with the label
    node + ("label" -> el.label)

    // if we only have a text body we set this as a property
    if (el.nonEmptyChildren.size == 1 && el.nonEmptyChildren.head.isAtom) {
      node + (el.label -> el.text.mungeToType)
    }

    // iterate over each attribute and fill it with data
    // derived from the attributes
    fillProperties(node, el.attributes)

    // loop over each child, if its a plain <tag>text</tag>
    // style tag then push it in as a property
    (el \ "_").collect { case child if(!child.isTextNode) =>
      val children = child.nonEmptyChildren

      // if it has a single child that is an atom then its a property
      val hasTextProperty = children.size == 1 && children.head.isAtom
      val shouldBeProperty = hasTextProperty && child.attributes.isEmpty

      // if this child has no descendants then it should be treated as a new entity.
      // if it has a child that is not an atom is an entity
      val isNewEntity = children.isEmpty || !shouldBeProperty || children.size > 1

      if (isNewEntity) {
        val childNode = performMap(child)
        val relationship = DynamicRelationshipType.withName(child.label)

        // create a relationship to the current node to the child
        // based on the label of the child
        node.createRelationshipTo(childNode, relationship)
      } else {

        // if the node should be a property we can push it into the original
        // node as a simple property
        if (shouldBeProperty) {
          node + (child.label -> child.text)
        }

        // fill the properties of the parent node
        fillProperties(node, child.attributes)
      }
    }

    node
  }

  /**
   * Fills a neo4j node with properties from attributes
   * @param node
   * @param attrs
   */
  private def fillProperties(node: Node, attrs: xml.MetaData) {
    attrs.foreach { attr =>
      node + (attr.key -> attr.value.head.text.mungeToType)
    }
  }
}