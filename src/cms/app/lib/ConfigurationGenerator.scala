package lib

import concurrent.Future


trait ConfigurationGenerator {
  def generate: Future[String]
}
