import com.google.inject.Guice
import java.io.File
import play.api.{Mode, Configuration, GlobalSettings}
import modules.Dependencies

object Global extends GlobalSettings {

  lazy private val injector = Guice.createInjector(new Dependencies)
  lazy private val overrides = Map(
    Mode.Dev -> Configuration.from(Map("session.secure" -> false))
  )

  override def getControllerInstance[A](controllerClass: Class[A]) = {
    injector.getInstance(controllerClass)
  }

  override def onLoadConfig(original: Configuration, path: File, classloader: ClassLoader, mode: Mode.Mode): Configuration = {
    val config = original ++ overrides.getOrElse(Mode.Dev, Configuration.empty)
    super.onLoadConfig(config, path, classloader, mode)
  }
}
