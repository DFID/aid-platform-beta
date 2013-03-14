import com.google.inject.Guice
import play.api.{Application, GlobalSettings}
import modules.Dependencies

object Global  extends GlobalSettings {

  override def beforeStart(app: Application) {
    System.setProperty("actors.corePoolSize","20")
  }

  lazy private val injector = Guice.createInjector(new Dependencies)

  override def getControllerInstance[A](controllerClass: Class[A]) = {
    injector.getInstance(controllerClass)
  }

}
