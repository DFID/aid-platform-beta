package lib.impl

import lib.Deployer
import play.api.Logger
import sys.process._

class Deployinator extends Deployer {

  val logger = Logger.logger

  def deploy {
    logger.debug("Calling deploy script")
    "./deploy.sh".!
    logger.debug("Deploy script executed successfully")
  }

}
