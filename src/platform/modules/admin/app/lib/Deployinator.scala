package lib

import traits.Deployer
import uk.gov.dfid.loader.Loader
import com.google.inject.Inject
import sys.process._
import concurrent.ExecutionContext.Implicits.global
import play.api.{Mode, Play}

class Deployinator @Inject()(loader: Loader) extends Deployer {
  def deploy {
    loader.load.onComplete { case _ =>
      println("Building Site")

      Play.current.mode match {
        case Mode.Dev | Mode.Test => println("Not building site in dev mode. Run it yourself")
        case Mode.Prod => Process.apply("./build-site.sh").run
      }
    }
  }
}
