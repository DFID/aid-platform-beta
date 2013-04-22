package uk.gov.dfid.loader.util

object Statuses extends Map[Long, String] {

  private val inner = Map(
    1L -> "Pipeline/identification",
    2L -> "Implementation",
    3L -> "Completion",
    4L -> "Post-completion",
    5L -> "Cancelled"
  )

  def get(key: Long): Option[String] = inner.get(key)

  def iterator: Iterator[(Long, String)] = inner.iterator

  def -(key: Long): Map[Long, String] = inner.-(key)

  def +[B1 >: String](kv: (Long, B1)): Map[Long, B1] = inner.+(kv)
}
