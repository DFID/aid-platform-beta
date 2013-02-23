require "rubygems"
require "json"
require "helpers/formatters"
require "middleman-smusher"

#------------------------------------------------------------------------------
# CONFIGURATION VARIABLES
#------------------------------------------------------------------------------
@api_access_url = ENV['DFID_API_URL'] || 'http://localhost:9000/access'
@cms_client     = Mongo::MongoClient.new('localhost', 27017)
@cms_db         = @cms_client['dfid']

#------------------------------------------------------------------------------
# GENERATE COUNTRIES
#------------------------------------------------------------------------------
ignore "/countries/index.html"
countriesJSON = HTTParty.get("#{@api_access_url}/countries")
parsedJSON = JSON.parse(countriesJSON.body)
parsedJSON.each do |country|
  proxy "/countries/#{country['code']}/index.html", "/countries/index.html", :locals => {
    :country => country, 
    :code => country['code']
  }
end

#------------------------------------------------------------------------------
# DEFINE HELPERS - Import from modules to avoid bloat
#------------------------------------------------------------------------------
helpers do

  include Formatters

  def top_5_countries
    response = HTTParty.get("#{@api_access_url}/countries")
    body     = JSON.parse(response.body)

    body.sort_by! { |c| c['totalBudget'] }.take 5
  end

  def what_we_do
    @cms_db['whatwedo'].find({})
  end

  def what_we_achieve
    @cms_db['whatweachieve'].find({})
  end

  def countries_helper
    countriesJSON = HTTParty.get("#{@api_access_url}/countries") #make sure test-api is running
    parsedJSON = JSON.parse(countriesJSON.body) #gets the json for all countries
  end
end

#------------------------------------------------------------------------------
# CONFIGURE DIRECTORIES
#------------------------------------------------------------------------------
set :css_dir   , 'stylesheets'
set :js_dir    , 'javascripts'
set :images_dir, 'images'

activate :livereload

#------------------------------------------------------------------------------
# BUILD SPECIFIC CONFIGURATION
#------------------------------------------------------------------------------
configure :build do
  activate :minify_css
  activate :minify_javascript
  activate :cache_buster
  activate :smusher
end