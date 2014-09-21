require "rubygems"
require "json"
require "net/http"

def get_funded_child_projects(project_id)

	result = @cms_db['transactions'].aggregate([{
                  "$match" => {
                      "provider-activity-id" => project_id, # filter with Project Id
                      "receiver-activity-id" => { "$ne" => ""} # filter out rec without any activity id or with empty ones
                  }
              }, {
                  "$group" => { 
                      "_id" => "$provider-activity-id", 
                                           
                      "receipients" => { "$addToSet" => { 'id' =>  "$receiver-activity-id", 'receiver-org' => '$receiver-org', 'value' => '$value'} },
                      "total" => {
                          "$sum" => "$value"
                      }
                  }
              }, {
                  "$project" => {                      
                    "_id" => 0,
                    "id" => '$_id',
                    "receipients" => 1,
                    "total" => 1
                  }
              },{ 
                "$unwind" => "$receipients" 
              }, { 
                "$group" => { 
                  "_id" => { "id" => "$id", "rec-id" => "$receipients.id", "total" => "$total" }, 
                  'sub-sum' => { "$sum" => "$receipients.value"}
                  }
              }, { 
                "$group" => {
                  "_id" => { "id" => '$_id.id', "total" => "$_id.total"}, 
                  "children" => { "$addToSet" => { "id" => '$_id.rec-id', "org" => '$rec-org', "value" => "$sub-sum"}}}
              }, { 
                "$project" => { 
                  "_id" => 0, 
                  "id" => "$_id.id", 
                  "total" => "$_id.total", 
                  "children" => 1} 
              }]
          )[0].to_json

	
	result
		
end

def get_child_funded_projects_recursive(project_id)

	result = get_funded_child_projects(project_id) || ''

  if !result.nil? && result.length > 0 && !result['children'].nil? then
    result = JSON.parse(result)
    result['children'].each do |child|

      secLeveData = get_funded_child_projects(child['id']) || ''

      if !secLeveData.nil? && secLeveData.length > 0 && !secLeveData['children'].nil? then

        secLeveData = JSON.parse(secLeveData)
        child['children'] = secLeveData['children']

      end

    end

  end

	result
end