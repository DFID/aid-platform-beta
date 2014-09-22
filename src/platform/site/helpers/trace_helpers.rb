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

# def get_children(project_id, data=nil, children=nil)
#   result = get_funded_child_projects(project_id) || ''

#   if !result.nil? && result.length > 0 && !result['children'].nil? && result['children'].length > 0 then
#     result = JSON.parse(result)

#     if !children.nil? then
#       data['children'] = children
#     else
#       data = result
#     end

#     result['children'].each do |child|
#       get_children(child['id'], child, result['children'])

#     end

#   end

#   return data

# end

def get_child_funded_projects_recursive(project_id, parent=nil)

	result = get_funded_child_projects(project_id) || ''

  if result['children'].nil? then
    return parent
  end

  if !result.nil? && result.length > 0 && !result['children'].nil? && result['children'].length > 0 then
    result = JSON.parse(result)

    if !parent.nil? then
      parent['children'] = result['children']
    end
    
    result['children'].each do |child|

      data = get_child_funded_projects_recursive(child['id'], child) || ''

      # if !data.nil? && data.length > 0 && !data['children'].nil? then

      #   data = JSON.parse(data)
      #   child['children'] = data['children']

      # end

    end  
  end

	result
end


def get_child_funded_projects_by_level(project_id, result = nil, depth = 1, current_level = 0)

  current_level += 1
  result = get_funded_child_projects(project_id) || ''

  if depth == current_level then
    return result
  end

    
  if !result.nil? && result.length > 0 && !result['children'].nil? && result['children'].length > 0 then
    result = JSON.parse(result)
      
      
      result['children'].each do |child|

        data = get_child_funded_projects_by_level(child['id'], result['children'], depth, current_level)

        if !data.nil? && data.length > 0 && !data['children'].nil? then

          data = JSON.parse(data)
          child['children'] = data['children']

        end

      end

  end

  result
end