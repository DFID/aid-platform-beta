module ResultsHelpers

	def country_results(countryCode)
		@cms_db['country-results'].aggregate([{ 
	    	"$match" => {"code" => countryCode}
	    	}, {
		     "$group" => {
		        "_id" => "$pillar",
		        "countryResult" => {
		        	"$addToSet" => "$results"
		        },
		        "resultTotal" => {
		        	"$addToSet" => "$total"
		        }
		  	} 
	    }])
	end
end