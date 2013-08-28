package basex

import scala.sys.process._
import scala.concurrent.Future
import concurrent.future
import concurrent.ExecutionContext.Implicits.global

trait BaseXSupport {

  implicit class BetterizedQuery(client: BaseXClient) {

    private val XQ_PATH =  s"${"pwd".!!.trim}/conf/xq"

    def run(filename: String) = client.execute(s"RUN $XQ_PATH/$filename.xq")

    def bind(args: (String, Any)*) = {
      val vars = args.map { case (name, value) =>
        s"$name=$value"
      }.mkString(",")

      client.execute(s"SET BINDINGS $vars")
    }
  }

  def withSession[T](action: BaseXClient => T) = time {
    future {
      val client = BaseXClient("localhost", 1984, "admin", "admin")
      try {
        action(client)
      } finally {
        client.close()
      }
    }
  }


  private def time[R](block: => Future[R]) = {
    val t0 = System.nanoTime
    block.andThen { case _ =>
      val t1 = System.nanoTime
      println("Elapsed time: " + ("%1.9f" format ((t1 - t0)/1E9)) + "s")
    }
  }
}
