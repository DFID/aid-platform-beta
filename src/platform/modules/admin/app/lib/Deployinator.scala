package lib

import traits.Deployer
import sys.process._
import play.api.{Mode, Play}

class Deployinator extends Deployer {
  def deploy {
    Play.current.mode match {
      case Mode.Dev | Mode.Test => println("Not building site in dev mode. Run it yourself")
      case Mode.Prod => Process.apply("./build-site.sh").run
    }
  }
}
