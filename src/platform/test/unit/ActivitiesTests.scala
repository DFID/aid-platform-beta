package unit

import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dfid.common.lib.ProjectService
import play.api.GlobalSettings
import com.google.inject.Guice
import play.api.test.Helpers._
import scala.Some
import play.api.test.{FakeRequest, FakeApplication}
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.neo4j.graphdb.Node
import collection.JavaConversions._

class ActivitiesTests extends Specification with Mockito {

  class TestDependencies extends ScalaModule {
    def configure() {
      val mockProjectService = mock[ProjectService]
      val mockNode = mock[Node]
      mockNode.getPropertyKeys returns List()
      mockProjectService.getFundedProjectsForActivity("testEmpty") returns Seq.empty[Node]
      mockProjectService.getFundedProjectsForActivity("testOne") returns List(mockNode)
      mockProjectService.getFundedProjectsForActivity("testMultiple") returns List(mockNode, mockNode, mockNode)
      bind[ProjectService].toInstance(mockProjectService)
    }
  }

  object TestGlobal extends GlobalSettings  {
    lazy private val injector = Guice.createInjector(new TestDependencies)

    override def getControllerInstance[A](controllerClass: Class[A]) = {
      injector.getInstance(controllerClass)
    }
  }

  "Activites REST API calls " should {
    "successfully return empty array when there are no results for funded projects" in {

      //  }
      running(FakeApplication(withGlobal = Some(TestGlobal))) {

        val Some(result) = route(
          FakeRequest(
            GET,
            "/access/activities/testEmpty/funded"
          )
        )
        contentAsString(result) must beEqualTo("[]")
      }
    }

    "should successfully return correct data when there is one result for funded projects" in {
      running(FakeApplication(withGlobal = Some(TestGlobal))) {

        val Some(result) = route(
          FakeRequest(
            GET,
            "/access/activities/testOne/funded"
          )
        )
        contentAsString(result) must beEqualTo("[{}]")
      }
    }

    "should successfully return correct data when there is more than one result for funded projects" in {
      running(FakeApplication(withGlobal = Some(TestGlobal))) {

        val Some(result) = route(
          FakeRequest(
            GET,
            "/access/activities/testMultiple/funded"
          )
        )
        contentAsString(result) must beEqualTo("[{},{},{}]")
      }
    }
  }
}
