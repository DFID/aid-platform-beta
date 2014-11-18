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
import scala.Some
import uk.gov.dfid.loader.util.Converter
import uk.gov.dfid.loader.util.OtherOrganisations

import play.api.libs.iteratee._ 
import play.api.libs.iteratee.Iteratee
import reactivemongo.api._
import reactivemongo.bson.handlers.DefaultBSONHandlers._




class SeparateDataAggregator(engine: ExecutionEngine, db: DefaultDB, auditor: DataLoadAuditor)  {

	private val format = DateTimeFormat.forPattern("yyyy-MM-ddd")

  	def collectProjects = {

    auditor.info("Collecting projects for FCO etc")

    Await.ready(db.collection("other-org-projects-separate").drop(), Duration.Inf)

    try {
      engine.execute(
        s"""
          | START  activity = node:entities(type="iati-activity")
          | MATCH  status-[?:`activity-status`]-activity-[:`reporting-org`]-org,
          |        activity-[?:title]-titleNode,
          |        activity-[?:description]-d,
          |        activity-[?:`iati-identifier`]-identifier
          | WHERE  HAS(org.ref) AND org.ref IN ${OtherOrganisations.SupportedForSeparateLoading.mkString("['","','","']")}
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
              db.collection("other-org-projects-separate").insert(
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

                    db.collection("project-budgets-separate").insert(
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

                    db.collection("project-sector-budgets-separate").insert(
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

    auditor.info("Collected projects for FCO etc")
  }

  def collectTransactions = {

    auditor.info("Collecting Transactions for FCO etc")

    Await.ready(db.collection("transactions-separate").drop(), Duration.Inf)

    try { 
      engine.execute(
      s"""
        | START  txn = node:entities(type="transaction")
        | MATCH  org-[:`reporting-org`]-project-[:transaction]-txn,
        |        txn-[:value]-value,
        |        txn-[:`transaction-date`]-date,
        |        txn-[:`transaction-type`]-type,
        |        project-[?:`iati-identifier`]-id
        | WHERE  HAS(org.ref) AND org.ref IN ${OtherOrganisations.SupportedForSeparateLoading.mkString("['","','","']")}
        | RETURN COALESCE(project.`iati-identifier`?, id.`iati-identifier`?, "") as projectIatiId,
        |        COALESCE(txn.description?, "")                              as description,
        |        COALESCE(value.value?, 0)                                   as valueReturn,
        |        date.`iso-date`                                             as dateReturn,
        |        COALESCE(type.code?, "")                                    as typeReturn
      """.stripMargin).foreach { row =>

      val project     = row("projectIatiId").asInstanceOf[String]
      //auditor.info(s"$project")

      if(project!=""){
          val value       = Converter.toDouble(row("valueReturn"))
          val date        = try { DateTime.parse(row("dateReturn").asInstanceOf[String], format).getMillis } catch { case _ : Throwable => 0 }
          val transaction = row("typeReturn").asInstanceOf[String]
          val description = Converter.toString(row("description"))

        //auditor.info(s"transactions insert: $project, $description, $date, $transaction")

        db.collection("transactions-separate").insert(
          BSONDocument(
            "project"     -> BSONString(project),
            "description" -> BSONString(description),
            "component"   -> BSONString(""),
            "value"       -> BSONDouble(value), 
            "date"        -> BSONDateTime(date),
            "type"        -> BSONString(transaction)
          )
        )
      }
    }
    } catch {
      case e: Throwable => println(e.getMessage); e.printStackTrace()
    }    

    auditor.success("Collected Transactions for FCO etc")
  }

  def collectProjectDocuments = {
    try { 

    auditor.info("Dropping documents collection")
    // drop the collection and start up
    Await.ready(db.collection("documents-separate").drop, Duration.Inf)
    auditor.info("Documents collection dropped")

    auditor.info("Collecting Documents for FCO etc")

    engine.execute(
      s"""
        |START  doc = node:entities(type = "document-link")
        |MATCH  category-[:category]-doc<-[:`document-link`]-project-[?:`iati-identifier`]-id,
        |       project-[:`reporting-org`]-org
        |WHERE  HAS(org.ref) AND org.ref IN ${OtherOrganisations.SupportedForSeparateLoading.mkString("['","','","']")}
        |RETURN COALESCE(project.`iati-identifier`?, id.`iati-identifier`?) as id,
        |       doc.title!                                                  as title,
        |       COALESCE(doc.format?, "text/plain")                         as format,
        |       doc.url                                                     as url,
        |       COLLECT(COALESCE(category.category?, ""))                   as categories
      """.stripMargin).foreach { row =>

      val projectId  = row("id").asInstanceOf[String]
      val format     = row("format").asInstanceOf[String]
      val url        = row("url").asInstanceOf[String]
      val categories = row("categories").asInstanceOf[Seq[String]]
      val title      = row("title") match {
        case null => ""
        case t    => t.asInstanceOf[String]
      }

      db.collection("documents-separate").insert(
        BSONDocument(
          "project"    -> BSONString(projectId),
          "title"      -> BSONString(title),
          "format"     -> BSONString(format),
          "url"        -> BSONString(url),
          "categories" -> BSONArray(
            categories.map(c => BSONString(c)): _*
          )
        )
      )
    }

    auditor.success("Aggregated all project documents for FCO etc")

   } catch {
      case e: Throwable => println(e.getMessage); e.printStackTrace()
    }  
  }

  def collectProjectLocations = {

    try { 

    auditor.info("Collecting project locations for FCO etc")

    // drop the collection as we will build is all from scratch here
    Await.ready(db.collection("locations-separate").drop, Duration.Inf)

    engine.execute(
      s"""
        | START  location=node:entities(type='location')
        | MATCH  org-[:`reporting-org`]-project-[:location]-location-[:coordinates]-coordinates,
        |        location-[:`location-type`]-type
        | WHERE  HAS(org.ref) AND org.ref IN ${OtherOrganisations.SupportedForSeparateLoading.mkString("['","','","']")}
        | RETURN project.`iati-identifier`? as id,
        |        project.title             as title,
        |        location.name             as name,
        |        coordinates.precision     as precision,
        |        coordinates.longitude     as longitude,
        |        coordinates.latitude      as latitude,
        |        type.code                 as type
      """.stripMargin).foreach { row =>

      val id           = { if ( row("id").isInstanceOf[String] ) row("id").asInstanceOf[String] else "" }
      val title        = { if ( row("title").isInstanceOf[String] ) row("title").asInstanceOf[String] else "" }
      val name         = { if ( row("name").isInstanceOf[String] ) row("name").asInstanceOf[String] else "" }
      val precision    = { if ( row("precision").isInstanceOf[Long] ) row("precision").asInstanceOf[Long] else 0 }
      val longitude    = Converter.toDouble(row("longitude"))
      val latitude     = Converter.toDouble(row("latitude"))
      val locationType = { if ( row("type").isInstanceOf[String] ) row("type").asInstanceOf[String] else "" }

      db.collection("locations-separate").insert(BSONDocument(
        "id"        -> BSONString(id),
        "title"     -> BSONString(title),
        "name"      -> BSONString(name),
        "precision" -> BSONLong(precision),
        "longitude" -> BSONDouble(longitude),
        "latitude"  -> BSONDouble(latitude),
        "type"      -> BSONString(locationType)
      ))
    }


    auditor.success("Collected all project locations for FCO etc")
    } catch {
      case e: Throwable => println(e.getMessage); e.printStackTrace()
    } 
  }

  def mergeSeparatelyLoadedProjects = {

    auditor.info("Preparing to merge separately loaded data")

    try {

    val query = BSONDocument()

    //auditor.info("Preparing to merge: other-org-projects-separate")
 
    //auditor.info("Preparing to merge: project-budgets-separate")

    //auditor.info("Preparing to merge: project-sector-budgets-separate")

    //auditor.info("Preparing to merge: transactions-separate")  

    //auditor.info("Preparing to merge: documents-separate")      

    //auditor.info("Preparing to merge: locations-separate")  

    val collection = db("locations-separated")
    val cursor = collection.find(query)
    cursor.enumerate.apply(Iteratee.foreach { doc =>
       db.collection("locations").insert(doc)
    })



     } catch {
      case e: Throwable => println(e.getMessage); e.printStackTrace()
    } 
  }

//  def mergeSeparatelyLoadedTransactions = {

//    auditor.info("Preparing to merge separately loaded data")

 //   try {
 //       auditor.info("Preparing to merge: transactions-separate")  
 //   } catch {
 //     case e: Throwable => println(e.getMessage); e.printStackTrace()
//    } 
//  }


}