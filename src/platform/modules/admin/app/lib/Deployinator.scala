package lib

import traits.Deployer
import uk.gov.dfid.loader.Loader
import com.google.inject.Inject
import sys.process._
import concurrent.ExecutionContext.Implicits.global
import concurrent.Future
import akka.actor.Actor

class Deployinator @Inject()(loader: Loader) extends Deployer {
  def deploy {
    loader.load.onComplete { case _ =>
      println("Building Site")

      Future {
        println(Process.apply("./build-site.sh").!)
        println("Built Site")
      }
    }
  }
}
