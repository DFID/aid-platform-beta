import com.google.inject.Guice
import play.api.{Play, Application, GlobalSettings}
import modules.Dependencies
import uk.gov.dfid.common.neo4j.SingletonEmbeddedNeo4JDatabaseHasALongName

object Global  extends GlobalSettings {

  lazy private val injector = Guice.createInjector(new Dependencies)

  override def getControllerInstance[A](controllerClass: Class[A]) = {
    injector.getInstance(controllerClass)
  }

}
