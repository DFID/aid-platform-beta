package lib.traits

import concurrent.Future


trait FrontPageManagedContentApi {
  def getWhatWeAchieve:Future[List[(String, String)]]
  def saveWhatWeAchieve(entries: List[(String, String)])
}

