package lib.traits

import concurrent.Future


trait FrontPageManagedContentApi {
  def getWhatWeDo: Future[List[(String, String)]]
  def getWhatWeAchieve:Future[List[(String, String)]]
  def saveWhatWeDo(entries: List[(String, String)])
  def saveWhatWeAchieve(entries: List[(String, String)])
}

