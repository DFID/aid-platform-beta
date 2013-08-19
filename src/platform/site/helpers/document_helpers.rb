require "rubygems"
require "json"
require "net/http"
require "uri"
 
def r4DApiDocFetch(projectId)
	
	uri = URI.parse("http://linked-development.org/openapi/r4d/get/research_outputs/"+projectId+"?per_project=5&format=json")
 
	http = Net::HTTP.new(uri.host, uri.port)
	request = Net::HTTP::Get.new(uri.request_uri)
 
	response = http.request(request)
 
	if response.code == "200"
		result = JSON.parse(response.body)['results']		
	else
		''
	end	
end


 



