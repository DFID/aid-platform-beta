package lib.traits

import play.api.Play.current
import play.modules.reactivemongo.ReactiveMongoPlugin

trait MongoAccess {
  lazy val database = ReactiveMongoPlugin.db
}
