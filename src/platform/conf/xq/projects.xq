declare variable $country_code external;
let $db        := db:open('iati')

let $db := $db//iati-activity[reporting-org/@ref='GB-1'][recipient-country/@code=$country_code]
let $projects := for $project in $db
                  let $title := data($project//title)
                  let $description := data($project//description)
                  let $code := data($project//iati-identifier)
                  let $status := data($project//activity-status/code)
                  let $par := for $partOrg in $project//participating-org
                            return for $partOrgData in $partOrg
                                    where data($partOrgData) != 'UNITED KINGDOM'
                                   return  <participatingName>{data($partOrg)}</participatingName>                           
                                          
                return  <project>
                          <id>{$code}</id>
                          <status>{$status}</status>
                          <title>{$title}</title>
                          <description>{$description}</description>
                          <participating>{$par}</participating>
                        </project>

return json:serialize( 
          <json arrays="json participating" objects="project" strings="code status title description"> 
            { $projects } 
          </json>)
