package uk.gov.dfid.loader

import org.neo4j.cypher.ExecutionEngine
import reactivemongo.api.DefaultDB
import uk.gov.dfid.common.DataLoadAuditor
import concurrent.duration.Duration
import concurrent.Await
import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import concurrent.ExecutionContext.Implicits.global
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import reactivemongo.bson.BSONLong
import reactivemongo.bson.BSONDateTime
import reactivemongo.api.DefaultDB
import reactivemongo.bson.BSONString
import scala.Some
import uk.gov.dfid.loader.util.OtherOrganisations

class OtherOrgAggregator(engine: ExecutionEngine, db: DefaultDB, auditor: DataLoadAuditor)  {

  private val format = DateTimeFormat.forPattern("yyyy-MM-ddd")

  def collectOtherOrganisationProjects = {

    auditor.info("Collecting other Organisation projects")

    Await.ready(db.collection("other-org-projects").drop(), Duration.Inf)

    try {
      engine.execute(
        s"""
          | START  activity = node:entities(type="iati-activity")
          | MATCH  status-[?:`activity-status`]-activity-[:`reporting-org`]-org,
          |        activity-[?:title]-titleNode,
          |        activity-[?:description]-d,
          |        activity-[?:`iati-identifier`]-identifier
          | WHERE  HAS(org.ref) AND org.ref IN ${OtherOrganisations.Supported.mkString("['","','","']")}
          | RETURN COALESCE(activity.title?, titleNode.title?, "")                       AS title,
          |        COALESCE(activity.description?, d.description?, "")     AS description,
          |        COALESCE(activity.`iati-identifier`?, identifier.`iati-identifier`?, "") AS id,
          |        COALESCE(activity.`reporting-org`?,org.`reporting-org`?, "")   AS organisation,
          |        COALESCE(status.code?, 0)                                             AS stat
        """.stripMargin).foreach { row =>

        val title        = Option(row("title").asInstanceOf[String]).getOrElse("")
        val description  = Option(row("description").asInstanceOf[String]).getOrElse("")
        val id           = row("id").asInstanceOf[String]
        val status       = row("stat") match {
            case v: java.lang.String => try { v.toLong } catch { case _ : Throwable => 0 }
            case v: java.lang.Long    => v.toLong
            case v: java.lang.Double  => v.toLong
            case _ => 0
        }
        val organisation = row("organisation").asInstanceOf[String]

        //auditor.info(s"id: $id")

        // some data generation results in bad data being spat out.  If there is no IATI ID
        // then we are going to ignore this.
        if(id != "" && organisation != "") {

          try{
              // now we need to sum up the project budgets and spend.
              val totalBudget = engine.execute(
                s"""
                 | START  funded=node:entities(type="iati-activity")
                 | MATCH  id-[r?:`iati-identifier`]-funded-[:budget]-budget-[:value]-budget_value
                 | WHERE  (funded.`iati-identifier` = '$id' OR  id.`iati-identifier` = '$id')
                 | RETURN SUM(budget_value.value) as totalBudget
              """.stripMargin).toSeq.head("totalBudget") match {
                case v: java.lang.Integer => v.toDouble
                case v: java.lang.Long    => v.toDouble
                case v: java.lang.Double    => v.toDouble
                case _ => 0.0
              }

              val totalSpend = engine.execute(
                s"""
                 |START  funded=node:entities(type="iati-activity")
                 |MATCH  id-[r?:`iati-identifier`]-funded-[:transaction]-transaction-[:value]-transaction_value,
                 |       transaction-[:`transaction-type`]-type
                 |WHERE  (  funded.`iati-identifier` = '$id'  OR id.`iati-identifier` = '$id' )
                 |       AND    HAS(type.`code`)
                 |       AND    (type.`code` = 'D' OR type.`code` = 'E')
                 |RETURN SUM(transaction_value.value) as totalSpend
              """.stripMargin).toSeq.head("totalSpend") match {
                case v: java.lang.Integer => v.toDouble
                case v: java.lang.Long    => v.toDouble
                case v: java.lang.Double    => v.toDouble
                case _ => 0.0
              }

              // then we need to get the dates as well
              val dates = engine.execute(
                s"""
                | START  n=node:entities(type="iati-activity")
                | MATCH  d-[:`activity-date`]-n-[:`activity-status`]-a,
                | id-[r?:`iati-identifier`]-n
                | WHERE  (HAS(n.`iati-identifier`) OR HAS(id.`iati-identifier`) ) AND (n.`iati-identifier` = '$id' OR id.`iati-identifier` = '$id')
                | RETURN COALESCE(d.type?, "") as type, 
                |       COALESCE(d.`iso-date`?, d.`activity-date`?) as date
              """.stripMargin).toSeq.map { row =>

                val dateType = { if( row("type").isInstanceOf[String]) row("type").asInstanceOf[String] else ""}

                if( dateType != "" && row("date") != null)
                {
                  val date     = DateTime.parse(row("date").asInstanceOf[String], format)
                  dateType -> BSONDateTime(date.getMillis)
                }
                else
                  dateType -> BSONDateTime(0)              
              }

              //auditor.info(s"other-org-projects insert: $title, $description, $id, $status, $totalBudget")
              db.collection("other-org-projects").insert(
                BSONDocument(
                    "title"             -> BSONString(title),
                    "description"       -> BSONString(description),
                    "iatiId"            -> BSONString(id),
                    "status"            -> BSONLong(status),
                    "totalBudget"       -> BSONDouble(totalBudget),
                    "organisation"      -> BSONString(organisation),
                    "totalProjectSpend" -> BSONDouble(totalSpend)
                  ).append(dates:_*)              
              )

              // put the project budgets in
              engine.execute(
                s"""
                |  START b=node:entities(type="budget")
                |  MATCH  v-[:value]-b-[:budget]-n-[r?:`iati-identifier`]-id,
                |         b-[:`period-start`]-p
                |  WHERE  (n.`iati-identifier` = '$id' OR id.`iati-identifier` = '$id')
                |  AND HAS(v.value)
                |  RETURN COALESCE(v.value?, 0.0)      as value,
                |         COALESCE(p.`iso-date`?, "") as date
              """.stripMargin).foreach { row =>              

                  val value = row("value") match {
                    case v: java.lang.String => try { v.toDouble } catch { case _ : Throwable => 0.0 }
                    case v: java.lang.Long    => v.toDouble
                    case v: java.lang.Double    => v.toDouble
                    case _ => 0.0
                  }
                  if( value != 0.0){
                    val date = row("date").asInstanceOf[String]

                    //println(s"project-budgets insert: $id, $value, $date")

                    db.collection("project-budgets").insert(
                      BSONDocument(
                        "id"    -> BSONString(id),
                        "value" -> BSONDouble(value),
                        "date"  -> BSONString(date)
                      )
                    )
                  }              
              }

              engine.execute(
                s"""
              | START  n=node:entities(type="iati-activity")
              | MATCH  s-[:`sector`]-n-[:`budget`]-b-[:`value`]-v
              | WHERE  n.`iati-identifier`? = '$id'
              |        AND HAS(s.code) AND s.code <> ""
              | RETURN COALESCE(s.code?, 0)                                  as code,
              |        s.sector?                                             as name,
              |        COALESCE(s.percentage?, 100)                          as percentage,
              |        (COALESCE(s.percentage?, 100) / 100.0 * sum(v.value)) as total
            """.stripMargin).foreach { row =>              

                  val sectorCode        = row("code") match {
                    case v: java.lang.String => try { v.toLong } catch { case _ : Throwable => 0 }
                    case v: java.lang.Long    => v.toLong
                    case v: java.lang.Double  => v.toLong
                    case _ => 0
                  }
                  if( sectorCode != 0){
                    val sectorName = row("name") match {
                      case null          => None
                      case value: String => Some(value)
                    }
                    

                    val total       = row("total")  match {
                      case v: java.lang.String => try { v.toDouble } catch { case _ : Throwable => 0.0 }
                      case v: java.lang.Long    => v.toDouble
                      case v: java.lang.Double  => v.toDouble
                    }

                    //println(s"project-sector-budgets insert: $id, $sectorCode, $total")

                    db.collection("project-sector-budgets").insert(
                      BSONDocument(
                        "projectIatiId" -> BSONString(id),
                        "sectorCode"  -> BSONLong(sectorCode),
                        "sectorBudget"  -> BSONDouble(total)
                      ).append(
                        Seq(
                          sectorName.map("sectorName" -> BSONString(_))
                        ).flatten:_*
                      )
                    )
                  }              
              }

            }catch{
              case e: Throwable => println(e.getMessage); e.printStackTrace()
            }
        }
      }
  } catch {
    case e: Throwable => println(e.getMessage); e.printStackTrace()
  }

    auditor.info("Collected other Organisation projects")
  }

  def collectTransactions = {

    auditor.info("Collecting other Organisation Project Transactions")

    try { 
      engine.execute(
      s"""
        | START  txn = node:entities(type="transaction")
        | MATCH  org-[:`reporting-org`]-project-[:transaction]-txn,
        |        txn-[:value]-value,
        |        txn-[:`transaction-date`]-date,
        |        txn-[:`transaction-type`]-type,
        |        project-[?:`iati-identifier`]-id
        | WHERE  HAS(org.ref) AND org.ref IN ${OtherOrganisations.Supported.mkString("['","','","']")}
        | RETURN COALESCE(project.`iati-identifier`?, id.`iati-identifier`?, "") as projectIatiId,
        |        COALESCE(txn.description?, "")                              as description,
        |        COALESCE(value.value?, 0)                                   as valueReturn,
        |        date.`iso-date`                                             as dateReturn,
        |        COALESCE(type.code?, "")                                    as typeReturn
      """.stripMargin).foreach { row =>

      val project     = row("projectIatiId").asInstanceOf[String]
      //auditor.info(s"$project")

      if(project!=""){
        val value       = row("valueReturn") match {            
            case v: java.lang.Long    => v.toLong
            case v: java.lang.Double  => v.toLong
            case v: java.lang.String => try { v.toLong } catch { case _ : Throwable => 0 }
            case _ => 0
          }
        val date        = try { DateTime.parse(row("dateReturn").asInstanceOf[String], format).getMillis } catch { case _ : Throwable => 0 }
        val transaction = row("typeReturn").asInstanceOf[String]
        val description = row("description").asInstanceOf[String]

        //auditor.info(s"transactions insert: $project, $description, $date, $transaction")

        db.collection("transactions").insert(
          BSONDocument(
            "project"     -> BSONString(project),
            "description" -> BSONString(description),
            "component"   -> BSONString(""),
            "value"       -> BSONLong(value),
            "date"        -> BSONDateTime(date),
            "type"        -> BSONString(transaction)
          )
        )
      }
    }
    } catch {
      case e: Throwable => println(e.getMessage); e.printStackTrace()
    }    

    auditor.success("Collected other Organisation Project Transactions")
  }
}
