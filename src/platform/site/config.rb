require "rubygems"
require "json"
require "helpers/formatters"
require "middleman-smusher"

#------------------------------------------------------------------------------
# CONFIGURATION VARIABLES
#------------------------------------------------------------------------------
@api_root_url   = ENV['DFID_API_URL'] || "http://localhost:9000"
@api_access_url = "#{@api_root_url}/access"
@cms_client     = Mongo::MongoClient.new('localhost', 27017)
@cms_db         = @cms_client['dfid']

#------------------------------------------------------------------------------
# IGNORE TEMPLATES AND PARTIALS
#------------------------------------------------------------------------------
ignore "/countries/country.html"
ignore "/countries/projects.html"

ignore "/projects/summary.html"
ignore "/projects/documents.html"
ignore "/projects/transactions.html"
ignore "/projects/partners.html"

#------------------------------------------------------------------------------
# GENERATE COUNTRIES
#------------------------------------------------------------------------------

countriesJSON = HTTParty.get("#{@api_access_url}/countries")
parsedJSON = JSON.parse(countriesJSON.body)
parsedJSON.each do |country|
countryCMS = @cms_db['countries'].find_one({"code" => country['code']})

  proxy "/countries/#{country['code']}/index.html", "/countries/country.html", :locals => {
    :country => country,
    :code    => country['code'],
    :countryCMS => countryCMS
  }

  proxy "/countries/#{country['code']}/projects/index.html", "/countries/projects.html", :locals => { :country => country }
end

#------------------------------------------------------------------------------
# GENERATE PROJECTS
#------------------------------------------------------------------------------
projects_response = HTTParty.get("#{@api_access_url}/activities?hierarchy=1")
projects_json     = JSON.parse(projects_response.body)

projects_json.each do |project|
  proxy "/projects/#{project['iati-identifier']}/index.html", '/projects/summary.html', :locals => { :project => project }
  proxy "/projects/#{project['iati-identifier']}/documents/index.html", '/projects/documents.html', :locals => { :project => project }
  proxy "/projects/#{project['iati-identifier']}/transactions/index.html", '/projects/transactions.html', :locals => { :project => project }
  proxy "/projects/#{project['iati-identifier']}/partners/index.html", '/projects/partners.html', :locals => { :project => project }
end

#------------------------------------------------------------------------------
# DEFINE HELPERS - Import from modules to avoid bloat
#------------------------------------------------------------------------------
helpers do

  include Formatters

  def current_financial_year
    now = Time.new

    if(now.month < 4)
      "#{now.year-1}/#{now.year}"
    else
      "#{now.year}/#{now.year +1}"
    end
  end

  def dfid_total_budget
    response = HTTParty.get("#{@api_access_url}/countries")
    body     = JSON.parse(response.body)

    body.inject(0) {|sum, c| sum + c['totalBudget'] }
  end

  def top_5_countries
    response = HTTParty.get("#{@api_access_url}/countries")
    body     = JSON.parse(response.body)

    body.sort_by! { |c| -c['totalBudget'] }.take 5
  end

  def what_we_do
    @cms_db['whatwedo'].find({})
  end

  def what_we_achieve
    @cms_db['whatweachieve'].find({})
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
  
  # this takes time, be careful
  # activate :smusher
end