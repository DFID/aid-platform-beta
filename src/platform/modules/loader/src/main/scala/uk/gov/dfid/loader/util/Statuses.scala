package uk.gov.dfid.loader.util

object Statuses extends Map[Int, String] {

  private val inner = Map(
    1 -> "Pipeline/identification",
    2 -> "Implementation",
    3 -> "Completion",
    4 -> "Post-completion",
    5 -> "Cancelled"
  )

  def get(key: Int): Option[String] = inner.get(key)

  def iterator: Iterator[(Int, String)] = inner.iterator

  def -(key: Int): Map[Int, String] = inner.-(key)

  def +[B1 >: String](kv: (Int, B1)): Map[Int, B1] = inner.+(kv)
}
