package uk.gov.dfid.loader.util

object Converter {

	def toDouble(input: Any) : Double = {

		input match {
            case v: java.lang.String => try { v.toDouble } catch { case _ : Throwable => 0.0 }
            case v: java.lang.Long    => v.toDouble
            case v: java.lang.Double    => v.toDouble
            case _ => 0.0
        }
	}

	def toLong(input: Any) : Long = {

		input match {
            case v: java.lang.String => try { v.toLong } catch { case _ : Throwable => 0 }
            case v: java.lang.Long    => v.toLong
            case v: java.lang.Double    => v.toLong
            case _ => 0
        }
	}
}