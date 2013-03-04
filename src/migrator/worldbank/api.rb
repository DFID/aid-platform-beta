require "httparty"
require "json"
require "mongo"

cms_client = Mongo::MongoClient.new('localhost', 27017)
cms_db     = cms_client['dfid']
countries  = cms_db['countries']
base_url   = "http://api.worldbank.org/countries/all"

# make the requests and get the responses
population_response      = HTTParty.get("#{base_url}/indicators/SP.POP.TOTL?date=2011&format=json&per_page=5000")
income_level_response    = HTTParty.get("#{base_url}?format=json&per_page=5000")
life_expectancy_response = HTTParty.get("#{base_url}/indicators/SP.DYN.LE00.IN?date=2010&format=json&per_page=5000")

# parse the responses into JSON
population_body          = JSON.parse population_response.body     
income_level_body        = JSON.parse income_level_response.body  
life_expectancy_body     = JSON.parse life_expectancy_response.body

# map the json into the values we need
population_body[1].map { |country| 
  [country['country']['id'], country['value']] 
}.each { |code, population|
  countries.update({
    "code" => code
  }, { 
    "$set" => { 
      "population" => population 
    }
  })
}

income_level_body[1].map { |country| 
  [country['iso2Code'], country['incomeLevel']['value']] 
}.each { |code, income_level|
  countries.update({
    "code" => code
  }, { 
    "$set" => { 
      "incomeLevel" => income_level 
    }
  })
}

life_expectancy_body[1].map { |country| 
  [country['country']['id'], country['value']] 
}.each { |code, life_expectancy|
  countries.update({
    "code" => code
  }, { 
    "$set" => { 
      "lifeExpectancy" => life_expectancy 
    }
  })
}



