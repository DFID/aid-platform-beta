package uk.gov.dfid.loader

import org.neo4j.graphdb.{Transaction, Node, GraphDatabaseService}
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.neo4j.graphdb.index.{Index, IndexManager}

class MapperSpec extends Specification with Mockito {

  "The parsers parse(elem) method" should {

    "create a new node for the root element" in {
      // arrange
      val db = mockDb
      val node = mock[Node]

      db.createNode returns node

      // act
      new Mapper(db).map(<my-node></my-node>)

      // assert
      there was one(db).createNode
      there was one(node).setProperty("label", "my-node")
    }

    "add attribtues as properties with munged types" in {
      // arrange
      val db   = mockDb
      val node = mock[Node]

      db.createNode returns node

      // act
      new Mapper(db).map(
        <my-node name="arnold" age="45" is-human="true">
        </my-node>)

      // assert
      there was one(node).setProperty("name", "arnold")
      there was one(node).setProperty("age", 45)
      there was one(node).setProperty("is-human", true)
    }

    "add textual children as properties" in {
      // arrange
      val db   = mockDb
      val node = mock[Node]

      db.createNode returns node

      // act
      new Mapper(db).map(<my-node><my-title>Super Title</my-title></my-node>)

      there was one(db).createNode
      there was one(node).setProperty("my-title", "Super Title")
    }

    "add non-textual children as new entities" in {
      // arrange
      val db   = mockDb
      val root = mock[Node]
      val child = mock[Node]

      db.createNode returns root thenReturn child

      // act
      new Mapper(db).map(<my-node><my-title name="arnold" /></my-node>)

      // assert
      there was two(db).createNode
      there was one(root).setProperty(anyString, anyString)
      there was one(child).setProperty("name", "arnold")
    }
  }

  def mockDb = {
    val db   = mock[GraphDatabaseService]
    val txn  = mock[Transaction]
    val idm  = mock[IndexManager]
    val idx  = mock[Index[Node]]
    val node = mock[Node]

    db.beginTx               returns txn
    db.createNode            returns node
    db.index                 returns idm
    idm.forNodes("entities") returns idx

    db
  }
}
