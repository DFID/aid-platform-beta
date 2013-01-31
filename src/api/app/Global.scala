import com.google.inject.Guice
import modules.Dependencies
import play.api.GlobalSettings

object Global extends GlobalSettings {

  val container = Guice.createInjector(new Dependencies)

  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    container.getInstance(controllerClass)
  }
}
