package controllers

import play.api.mvc._
import play.api.libs.json._

object Projects extends Controller {

  val projects: JsValue = Json.parse("""
                                      {
                                        "projects" : {
                                        "GB-1-202990" : {
                                                   "name" : "Ethiopia: Support for the Ethiopian Health Sector Development Programme",
                                                   "summary" : "Increased access to and improved quality of health services.",
                                                   "budget-set" : 275000000,
                                                   "budget-spent" : 120000000,
                                                   "start-date" : "1 Dec 2011",
                                                   "end-date" : "31 Mar 2015",
                                                   "funding" : "Conditional",
                                                   "status" : "Implementation",
                                                   "sector-groups" : [{
                                                                     "name" : "Research",
                                                                     "percentage" : 100
                                                                     }],
                                                  "partner-split" : [{
                                                                          "name" : "National NGO",
                                                                         "percentage" : 50
                                                                     },
                                                                     {
                                                                         "name" : "Multilateral",
                                                                         "percentage" : 50
                                                                     }]
                                                },
                                        "GB-1-113492" : {

                                                    "name" : "Ethiopia: Water, Sanitation and Hygiene Programme",
                                                    "summary" : "Increase access to affordable and sustainable water supply and sanitation services.",
                                                    "budget-set" : 66000000,
                                                    "budget-spent" : 660000000,
                                                    "start-date" : "4 Feb 2008",
                                                    "end-date" : "31 Mar 2013",
                                                    "funding" : "Unconditional",
                                                    "status" : "Completed",
                                                    "sector-groups" : [{
                                                                      "name" : "Livestock",
                                                                      "percentage" : 90
                                                                      },
                                                                      {
                                                                      "name" : "Social Welfare",
                                                                      "percentage" : 10
                                                                      }],
                                                    "partner-split" : [{
                                                                         "name" : "National NGO",
                                                                        "percentage" : 33
                                                                      },
                                                                      {
                                                                          "name" : "Multilateral",
                                                                          "percentage" : 33
                                                                      },
                                                                      {
                                                                          "name" : "Government",
                                                                          "percentage" : 33
                                                                      }]
                                                    },
                                        "GB-1-200094" : {
                                                   "name" : "Africa Enterprise Challenge Fund (AECF)",
                                                   "summary" : "To catalyse private sector entrepreneurs in Africa to innovate and find profitable ways of improving market access and functioning for the poor- especially in rural areas. Its focus will be on agricultural, targeting agribusiness, and access to finance working with providers of financial services",
                                                   "budget-set" : 10000000,
                                                   "budget-spent" : 791534,
                                                   "start-date" : "17 July 2008",
                                                   "end-date" : "17 Aug 2018",
                                                   "funding" : "Conditional",
                                                   "status" : "Implementation",
                                                   "sector-groups" : [{
                                                                          "name" : "Livestock",
                                                                          "percentage" : 90
                                                                       },
                                                                       {
                                                                          "name" : "Social Welfare",
                                                                          "percentage" : 10
                                                                       }],
                                                   "partner-split" : [{
                                                                          "name" : "National NGO",
                                                                          "percentage" : 25
                                                                        },
                                                                        {
                                                                           "name" : "Multilateral",
                                                                           "percentage" : 25
                                                                        },
                                                                        {
                                                                           "name" : "Government",
                                                                           "percentage" : 25
                                                                        },
                                                                        {
                                                                           "name" : "Foundation",
                                                                           "percentage" : 25
                                                                        }]
                                               }
                                      }

                                     }
                                     """)

  def allProjects = Action {
    Ok (
        (projects \ "projects")
      )
  }

  def project(code: String) = Action {
    Ok (
        (projects \ "projects" \ code)
    )
  }


}