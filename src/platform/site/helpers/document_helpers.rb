require "rubygems"
require "json"
require "net/http"
require "uri"
 
def r4DApiDocFetch(projectId)
	
	begin
	
		uri = URI.parse("http://linked-development.org/openapi/r4d/get/research_outputs/"+projectId+"?per_project=5&format=json")
 
		http = Net::HTTP.new(uri.host, uri.port)
		request = Net::HTTP::Get.new(uri.request_uri)
 
		response = http.request(request)
 
		if response.code == "200"
			result = JSON.parse(response.body)['results']		
		else
			''
		end

	rescue SystemCallError
		''
	end
end

def getR4DSearchLink(link)
	
	uri = URI.parse(link)
	proj_id = uri.path.gsub!(/\D/, "")

	search_uri = "http://r4d.dfid.gov.uk/Search/SearchResults.aspx?search=advancedsearch&SearchType=3&Projects=false&Documents=true&DocumentsOnly=true&ProjectID="+proj_id

	search_uri
end

def getR4DDocsCountNotShowing(output_count)
		output_count.to_i - 5 
end