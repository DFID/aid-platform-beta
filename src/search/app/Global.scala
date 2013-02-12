import com.google.inject.Guice
import play.api.GlobalSettings

object Global extends GlobalSettings {

  lazy val container = Guice.createInjector()

  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    container.getInstance(controllerClass)
  }
}
