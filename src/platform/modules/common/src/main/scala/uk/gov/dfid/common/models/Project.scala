package uk.gov.dfid.common.models

import reactivemongo.bson._
import reactivemongo.bson.handlers.{BSONWriter, BSONReader}
import reactivemongo.bson.BSONLong
import reactivemongo.bson.BSONString

case class Project(
  id:                Option[BSONObjectID],
  iatiId:            String,
  title:             String,
  description:       String,
  projectType:       String,
  reportingOrg:      String,
  recipient:         Option[String],
  allRecipients:     List[String],
  status:            Int,
  budget:            Option[Long],
  participatingOrgs: List[String])

object Project {

  implicit object ProjectReader extends BSONReader[Project]{
    def fromBSON(doc: BSONDocument): Project = {
      val document = doc.toTraversable

      Project(
        document.getAs[BSONObjectID]("_id"),
        document.getAs[BSONString]("iatiId").map(_.value).get,
        document.getAs[BSONString]("title").map(_.value).get,
        document.getAs[BSONString]("description").map(_.value).get,
        document.getAs[BSONString]("projectType").map(_.value).get,
        document.getAs[BSONString]("reportingOrg").map(_.value).get,
        document.getAs[BSONString]("recipient").map(_.value),
        document.getAs[BSONArray]("allRecipients").map { values =>
          values.values.toList.flatMap { case value =>
            value match {
              case v: BSONString => Some(v.value)
              case _ => None
            }
          }
        }.getOrElse(List.empty),
        document.getAs[BSONInteger]("status").map(_.value).get,
        document.getAs[BSONLong]("budget").map(_.value),
        document.getAs[BSONArray]("participatingOrgs").map { values =>
          values.values.toList.flatMap { case value =>
            value match {
              case v: BSONString => Some(v.value)
              case _ => None
            }
          }
        }.getOrElse(List.empty)
      )
    }
  }

  implicit object ProjectWriter extends BSONWriter[Project]{
    def toBSON(project: Project): BSONDocument = {
      BSONDocument(
        "_id"               -> project.id.getOrElse(BSONObjectID.generate),
        "iatiId"            -> BSONString(project.iatiId),
        "title"             -> BSONString(project.title),
        "description"       -> BSONString(project.description),
        "projectType"       -> BSONString(project.projectType),
        "reportingOrg"       -> BSONString(project.reportingOrg),
        "status"            -> BSONInteger(project.status),
        "allRecipients"     -> BSONArray(project.allRecipients.map(BSONString(_)): _*),
        "participatingOrgs" -> BSONArray(project.participatingOrgs.map(BSONString(_)): _*)
      ).append(Seq(
        project.budget.map(b => "budget" -> BSONLong(b)),
        project.recipient.map(r => "recipient" -> BSONString(r))
      ).flatten:_*)
    }
  }
}