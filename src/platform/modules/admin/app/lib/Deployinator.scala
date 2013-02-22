package lib

import traits.Deployer
import uk.gov.dfid.loader.Loader
import com.google.inject.Inject
import sys.process._
import concurrent.ExecutionContext.Implicits.global

class Deployinator @Inject()(loader: Loader) extends Deployer {
  def deploy {
    loader.load.onSuccess { case _ =>
      "./build-site.sh".!
    }
  }
}
