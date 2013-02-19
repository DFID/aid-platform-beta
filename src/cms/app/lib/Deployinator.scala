package lib

import play.api.Logger
import sys.process._
import traits.Deployer

class Deployinator extends Deployer {

  val logger = Logger.logger

  def deploy {
    logger.debug("Calling deploy script")
    "./deploy.sh".!
    logger.debug("Deploy script executed successfully")
  }

}
