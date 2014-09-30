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
                                           
                      "receipients" => { "$addToSet" => { 'name' =>  "$receiver-activity-id", 'receiver-org' => '$receiver-org', 'value' => '$value'} },
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
                  "_id" => { "id" => "$id", "rec-id" => "$receipients.name", "total" => "$total" }, 
                  'sub-sum' => { "$sum" => "$receipients.value"}
                  }
              }, { 
                "$group" => {
                  "_id" => { "id" => '$_id.id', "total" => "$_id.total"}, 
                  "children" => { "$addToSet" => { "name" => '$_id.rec-id', "org" => '$rec-org', "value" => "$sub-sum"}}}
              }, { 
                "$project" => { 
                  "_id" => 0, 
                  "name" => "$_id.id", 
                  "value" => "$_id.total", 
                  "children" => 1} 
              }]
          )[0].to_json

	
	result
		
end

def get_trace_data(project_id, parent = nil, level = 0)
  
  return_resutl = nil
  result = get_funded_child_projects(project_id) || ''

  return_resutl = Hash.new

  if !parent.nil? then     
    return_resutl['name'] = parent['name']
    return_resutl['value'] = parent['value']
  end
  
  level += 1
  if level == 7 then    
    return return_resutl
  end
  

  if !result.nil? && !result['children'].nil? && result['children'].length > 0 then
    result = JSON.parse(result)

    return_resutl = Hash.new
    return_resutl['name'] = result['name']
    return_resutl['value'] = result['value']           
    return_resutl['children'] = []
        

    result['children'].each do |child|

      return_resutl['children'] << get_trace_data(child['name'], child, level)

    end
  end

  return_resutl

end