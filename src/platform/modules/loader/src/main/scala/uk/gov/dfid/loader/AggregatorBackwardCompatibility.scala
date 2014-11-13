package uk.gov.dfid.loader

import org.neo4j.cypher.ExecutionEngine
import uk.gov.dfid.common.DataLoadAuditor
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import reactivemongo.bson._
import concurrent.Await
import concurrent.duration.Duration
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson.BSONLong
import reactivemongo.bson.BSONDateTime
import reactivemongo.api.DefaultDB
import reactivemongo.bson.BSONString
import uk.gov.dfid.loader.util.SupportedOrgRefsForPartners
import uk.gov.dfid.loader.util.Converter

/**
 * To aggregate data before version 1.05
 */
class AggregatorBackwardCompatibility(engine: ExecutionEngine, db: DefaultDB, auditor: DataLoadAuditor) {

	// Use this method to load location data in versions before 1.04
	def collectProjectLocationsForVersionBefore104(){

		try{

		      engine.execute(
		      s"""
		        | START  location=node:entities(type='location')
		        | MATCH  org-[:`reporting-org`]-project-[:location]-location-[:coordinates]-coordinates,
		        |        location-[:`location-type`]-type
		        | WHERE  HAS(org.ref) AND org.ref IN ${SupportedOrgRefsForPartners.Reporting.mkString("['","','","']")}
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

		      db.collection("locations").insert(BSONDocument(
		        "id"        -> BSONString(id),
		        "title"     -> BSONString(title),
		        "name"      -> BSONString(name),
		        "precision" -> BSONLong(precision),
		        "longitude" -> BSONDouble(longitude),
		        "latitude"  -> BSONDouble(latitude),
		        "type"      -> BSONString(locationType)
		      ))
		    }

    }
    catch{
      case e: Throwable => println(e.getMessage); e.printStackTrace()
    }

    }	

}