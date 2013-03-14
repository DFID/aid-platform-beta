package lib

import play.api.libs.json._
import org.neo4j.graphdb.{DynamicRelationshipType, Direction, Node}
import play.api.libs.json.JsString
import play.api.libs.json.JsBoolean
import play.api.libs.json.JsNumber
import collection.JavaConversions._
import uk.gov.dfid.loader.Implicits._
import org.neo4j.cypher.internal.symbols.RelationshipType
import org.json.JSONArray

object JsonWriters {

  implicit object DefaultNodeWrites extends Writes[Node] {

    def writes(node: Node): JsValue = {
      val properties = nodeToSeq(node)
      JsObject(properties)
    }
  }

  implicit object DeepNodeWrites extends Writes[Node]{
    def writes(node: Node): JsValue = {

      val properties = nodeToSeq(node)

      val childer = node.getRelationships(Direction.OUTGOING).map(_.getType.name).toList.distinct.map { name =>
        val rels = node.getRelationships(DynamicRelationshipType.withName(name)).toSeq

        if(rels.size == 1){
          name ->  writes(rels.head.getEndNode)
        } else {
          name -> JsArray(
            rels.map(r => writes(r.getEndNode)).toSeq
          )
        }
      }

      JsObject(properties ++ childer)
    }
  }

  private def nodeToSeq(node: Node) = {
    node.getPropertyKeys.flatMap { case key =>
      if(key != "label") {
        val value : JsValue = node.getProperty(key) match {
          case v: java.lang.String  => JsString(v)
          case v: java.lang.Integer => JsNumber(BigDecimal(v))
          case v: java.lang.Long    => JsNumber(BigDecimal(v))
          case v: java.lang.Double  => JsNumber(BigDecimal(v))
          case v: java.lang.Boolean => JsBoolean(v)
        }
      Some(key -> value)
      } else{
        None
      }
    }.toSeq
  }
}
