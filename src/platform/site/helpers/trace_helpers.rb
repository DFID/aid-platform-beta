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

	
	#return JSON.parse(result)	
	result
		
end

def get_child_funded_projects_recursive(project_id, is_top, parent)

	result = get_funded_child_projects(project_id)

	if !result.empty? && !result['children'].nil? then
	
		
		children = result[0]['children']
		
	
		# if( !children.empty? && children.length > 0) then

			children = JSON.parse(children)

		# 	if !is_top && !parent.nil? then
		# 		parent['children'] = children
		# 	end

		# 	children.each do |child|
		# 		#get_child_funded_projects_recursive(child['id'], false, child)
		# 	end		
		# end

	end

	# if result != 'null' then
	# 	result = JSON.parse(result)
	# end

	result
end