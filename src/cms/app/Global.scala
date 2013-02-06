import com.google.inject.Guice
import modules.Dependencies
import play.api.GlobalSettings

object Global  extends GlobalSettings {

  lazy private val injector = Guice.createInjector(new Dependencies)

  override def getControllerInstance[A](controllerClass: Class[A]) = {
    injector.getInstance(controllerClass)
  }

}
