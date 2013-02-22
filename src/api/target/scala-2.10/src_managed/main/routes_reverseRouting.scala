// @SOURCE:/Users/kyledavidson/aid-platform-beta/src/api/conf/routes
// @HASH:16713f9ff4b6f9a18a28f6f4b62450cc60b0e7d7
// @DATE:Thu Feb 21 09:16:31 GMT 2013

import Routes.{prefix => _prefix, defaultPrefix => _defaultPrefix}
import play.core._
import play.core.Router._
import play.core.j._

import play.api.mvc._


import Router.queryString


// @LINE:13
// @LINE:12
// @LINE:10
// @LINE:8
// @LINE:6
// @LINE:4
// @LINE:2
package controllers {

// @LINE:6
class ReverseAggregate {
    

// @LINE:6
def index(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "aggregate")
}
                                                
    
}
                          

// @LINE:10
class ReverseActivities {
    

// @LINE:10
def index(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "access/activities")
}
                                                
    
}
                          

// @LINE:13
// @LINE:12
class ReverseCountries {
    

// @LINE:13
def view(code:String): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "access/countries/" + implicitly[PathBindable[String]].unbind("code", code))
}
                                                

// @LINE:12
def index(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "access/countries")
}
                                                
    
}
                          

// @LINE:2
class ReverseApplication {
    

// @LINE:2
def index(): Call = {
   Call("GET", _prefix)
}
                                                
    
}
                          

// @LINE:8
class ReverseOrganisations {
    

// @LINE:8
def index(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "access/organisations")
}
                                                
    
}
                          

// @LINE:4
class ReverseAccess {
    

// @LINE:4
def index(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "access")
}
                                                
    
}
                          
}
                  


// @LINE:13
// @LINE:12
// @LINE:10
// @LINE:8
// @LINE:6
// @LINE:4
// @LINE:2
package controllers.javascript {

// @LINE:6
class ReverseAggregate {
    

// @LINE:6
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Aggregate.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "aggregate"})
      }
   """
)
                        
    
}
              

// @LINE:10
class ReverseActivities {
    

// @LINE:10
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Activities.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "access/activities"})
      }
   """
)
                        
    
}
              

// @LINE:13
// @LINE:12
class ReverseCountries {
    

// @LINE:13
def view : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Countries.view",
   """
      function(code) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "access/countries/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("code", code)})
      }
   """
)
                        

// @LINE:12
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Countries.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "access/countries"})
      }
   """
)
                        
    
}
              

// @LINE:2
class ReverseApplication {
    

// @LINE:2
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Application.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + """"})
      }
   """
)
                        
    
}
              

// @LINE:8
class ReverseOrganisations {
    

// @LINE:8
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Organisations.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "access/organisations"})
      }
   """
)
                        
    
}
              

// @LINE:4
class ReverseAccess {
    

// @LINE:4
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Access.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "access"})
      }
   """
)
                        
    
}
              
}
        


// @LINE:13
// @LINE:12
// @LINE:10
// @LINE:8
// @LINE:6
// @LINE:4
// @LINE:2
package controllers.ref {

// @LINE:6
class ReverseAggregate {
    

// @LINE:6
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   play.api.Play.maybeApplication.map(_.global).getOrElse(play.api.DefaultGlobal).getControllerInstance(classOf[controllers.Aggregate]).index(), HandlerDef(this, "controllers.Aggregate", "index", Seq(), "GET", """""", _prefix + """aggregate""")
)
                      
    
}
                          

// @LINE:10
class ReverseActivities {
    

// @LINE:10
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   play.api.Play.maybeApplication.map(_.global).getOrElse(play.api.DefaultGlobal).getControllerInstance(classOf[controllers.Activities]).index(), HandlerDef(this, "controllers.Activities", "index", Seq(), "GET", """""", _prefix + """access/activities""")
)
                      
    
}
                          

// @LINE:13
// @LINE:12
class ReverseCountries {
    

// @LINE:13
def view(code:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   play.api.Play.maybeApplication.map(_.global).getOrElse(play.api.DefaultGlobal).getControllerInstance(classOf[controllers.Countries]).view(code), HandlerDef(this, "controllers.Countries", "view", Seq(classOf[String]), "GET", """""", _prefix + """access/countries/$code<[^/]+>""")
)
                      

// @LINE:12
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   play.api.Play.maybeApplication.map(_.global).getOrElse(play.api.DefaultGlobal).getControllerInstance(classOf[controllers.Countries]).index(), HandlerDef(this, "controllers.Countries", "index", Seq(), "GET", """""", _prefix + """access/countries""")
)
                      
    
}
                          

// @LINE:2
class ReverseApplication {
    

// @LINE:2
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   play.api.Play.maybeApplication.map(_.global).getOrElse(play.api.DefaultGlobal).getControllerInstance(classOf[controllers.Application]).index(), HandlerDef(this, "controllers.Application", "index", Seq(), "GET", """""", _prefix + """""")
)
                      
    
}
                          

// @LINE:8
class ReverseOrganisations {
    

// @LINE:8
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   play.api.Play.maybeApplication.map(_.global).getOrElse(play.api.DefaultGlobal).getControllerInstance(classOf[controllers.Organisations]).index(), HandlerDef(this, "controllers.Organisations", "index", Seq(), "GET", """""", _prefix + """access/organisations""")
)
                      
    
}
                          

// @LINE:4
class ReverseAccess {
    

// @LINE:4
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   play.api.Play.maybeApplication.map(_.global).getOrElse(play.api.DefaultGlobal).getControllerInstance(classOf[controllers.Access]).index(), HandlerDef(this, "controllers.Access", "index", Seq(), "GET", """""", _prefix + """access""")
)
                      
    
}
                          
}
                  
      