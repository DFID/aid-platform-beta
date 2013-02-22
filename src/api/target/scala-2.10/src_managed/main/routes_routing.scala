// @SOURCE:/Users/kyledavidson/aid-platform-beta/src/api/conf/routes
// @HASH:16713f9ff4b6f9a18a28f6f4b62450cc60b0e7d7
// @DATE:Thu Feb 21 09:16:31 GMT 2013


import play.core._
import play.core.Router._
import play.core.j._

import play.api.mvc._


import Router.queryString

object Routes extends Router.Routes {

private var _prefix = "/"

def setPrefix(prefix: String) {
  _prefix = prefix  
  List[(String,Routes)]().foreach {
    case (p, router) => router.setPrefix(prefix + (if(prefix.endsWith("/")) "" else "/") + p)
  }
}

def prefix = _prefix

lazy val defaultPrefix = { if(Routes.prefix.endsWith("/")) "" else "/" } 


// @LINE:2
private[this] lazy val controllers_Application_index0 = Route("GET", PathPattern(List(StaticPart(Routes.prefix))))
        

// @LINE:4
private[this] lazy val controllers_Access_index1 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("access"))))
        

// @LINE:6
private[this] lazy val controllers_Aggregate_index2 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("aggregate"))))
        

// @LINE:8
private[this] lazy val controllers_Organisations_index3 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("access/organisations"))))
        

// @LINE:10
private[this] lazy val controllers_Activities_index4 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("access/activities"))))
        

// @LINE:12
private[this] lazy val controllers_Countries_index5 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("access/countries"))))
        

// @LINE:13
private[this] lazy val controllers_Countries_view6 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("access/countries/"),DynamicPart("code", """[^/]+"""))))
        
def documentation = List(("""GET""", prefix,"""@controllers.Application@.index"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """access""","""@controllers.Access@.index"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """aggregate""","""@controllers.Aggregate@.index"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """access/organisations""","""@controllers.Organisations@.index"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """access/activities""","""@controllers.Activities@.index"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """access/countries""","""@controllers.Countries@.index"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """access/countries/$code<[^/]+>""","""@controllers.Countries@.view(code:String)""")).foldLeft(List.empty[(String,String,String)]) { (s,e) => e match {
  case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
  case l => s ++ l.asInstanceOf[List[(String,String,String)]] 
}}
       
    
def routes:PartialFunction[RequestHeader,Handler] = {        

// @LINE:2
case controllers_Application_index0(params) => {
   call { 
        invokeHandler(play.api.Play.maybeApplication.map(_.global).getOrElse(play.api.DefaultGlobal).getControllerInstance(classOf[controllers.Application]).index, HandlerDef(this, "controllers.Application", "index", Nil,"GET", """""", Routes.prefix + """"""))
   }
}
        

// @LINE:4
case controllers_Access_index1(params) => {
   call { 
        invokeHandler(play.api.Play.maybeApplication.map(_.global).getOrElse(play.api.DefaultGlobal).getControllerInstance(classOf[controllers.Access]).index, HandlerDef(this, "controllers.Access", "index", Nil,"GET", """""", Routes.prefix + """access"""))
   }
}
        

// @LINE:6
case controllers_Aggregate_index2(params) => {
   call { 
        invokeHandler(play.api.Play.maybeApplication.map(_.global).getOrElse(play.api.DefaultGlobal).getControllerInstance(classOf[controllers.Aggregate]).index, HandlerDef(this, "controllers.Aggregate", "index", Nil,"GET", """""", Routes.prefix + """aggregate"""))
   }
}
        

// @LINE:8
case controllers_Organisations_index3(params) => {
   call { 
        invokeHandler(play.api.Play.maybeApplication.map(_.global).getOrElse(play.api.DefaultGlobal).getControllerInstance(classOf[controllers.Organisations]).index, HandlerDef(this, "controllers.Organisations", "index", Nil,"GET", """""", Routes.prefix + """access/organisations"""))
   }
}
        

// @LINE:10
case controllers_Activities_index4(params) => {
   call { 
        invokeHandler(play.api.Play.maybeApplication.map(_.global).getOrElse(play.api.DefaultGlobal).getControllerInstance(classOf[controllers.Activities]).index, HandlerDef(this, "controllers.Activities", "index", Nil,"GET", """""", Routes.prefix + """access/activities"""))
   }
}
        

// @LINE:12
case controllers_Countries_index5(params) => {
   call { 
        invokeHandler(play.api.Play.maybeApplication.map(_.global).getOrElse(play.api.DefaultGlobal).getControllerInstance(classOf[controllers.Countries]).index, HandlerDef(this, "controllers.Countries", "index", Nil,"GET", """""", Routes.prefix + """access/countries"""))
   }
}
        

// @LINE:13
case controllers_Countries_view6(params) => {
   call(params.fromPath[String]("code", None)) { (code) =>
        invokeHandler(play.api.Play.maybeApplication.map(_.global).getOrElse(play.api.DefaultGlobal).getControllerInstance(classOf[controllers.Countries]).view(code), HandlerDef(this, "controllers.Countries", "view", Seq(classOf[String]),"GET", """""", Routes.prefix + """access/countries/$code<[^/]+>"""))
   }
}
        
}
    
}
        