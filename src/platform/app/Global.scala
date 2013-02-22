import com.google.inject.Guice
import play.api.GlobalSettings
import modules.Dependencies

object Global  extends GlobalSettings {

  lazy private val injector = Guice.createInjector(new Dependencies)

  override def getControllerInstance[A](controllerClass: Class[A]) = {
    injector.getInstance(controllerClass)
  }

}
