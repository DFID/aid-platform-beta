require "rubygems"
require "json"
require "helpers/formatters"
require "helpers/country_helpers"
require "helpers/frontpage_helpers"
require "helpers/project_helpers"
require "helpers/codelists"
require "helpers/lookups"
require "helpers/sector_helpers"
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
ignore "/countries/results.html"
ignore "/projects/summary.html"
ignore "/projects/documents.html"
ignore "/projects/transactions.html"
ignore "/projects/partners.html"
ignore "/sector/categories.html"
ignore "/sector/sectors.html"
ignore "/sector/projects.html"

#------------------------------------------------------------------------------
# GENERATE COUNTRIES
#------------------------------------------------------------------------------
@cms_db['countries'].find({}).each do |country|
  stats    = @cms_db['country-stats'].find_one({ "code" => country["code"] })
  projects = @cms_db['projects'].find({ "recipient" => country['code'] }, :sort => ['totalBudget', Mongo::DESCENDING]).to_a
  results = @cms_db['country-results'].aggregate([{ 
        "$match" => {"code" => country["code"]}
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
  
  proxy "/countries/#{country['code']}/index.html",          "/countries/country.html",  :locals => { :country => country, :stats    => stats,    :projects => projects }
  proxy "/countries/#{country['code']}/results/index.html",  "/countries/results.html",  :locals => { :country => country, :projects => projects, :results  => results }
  proxy "/countries/#{country['code']}/projects/index.html", "/countries/projects.html", :locals => { :country => country, :projects => projects }
end

#------------------------------------------------------------------------------
# GENERATE REGION PROJECT LIST
#------------------------------------------------------------------------------
@cms_db['regions'].find({}).each do |region|
  projects = @cms_db['projects'].find({"projectType" => "regional", "recipient" => region['code']}, :sort => ['totalBudget', Mongo::DESCENDING]).to_a
  proxy "/regions/#{region['code']}/projects/index.html", "/projectList.html", :locals => {:projects => projects, :name => region['name']}
end

#------------------------------------------------------------------------------
# GENERATE GLOBAL PROJECT LIST
#------------------------------------------------------------------------------
  projects = @cms_db['projects'].find({"projectType" => "global"}, :sort => ['totalBudget', Mongo::DESCENDING]).to_a
  proxy "/global/projects/index.html", "/projectList.html", :locals => {:projects => projects, :name => "Global"}


#------------------------------------------------------------------------------
# GENERATE PROJECTS
#------------------------------------------------------------------------------
@cms_db['projects'].find({}).each do |project|

  id                  = project['iatiId']
  funded_projects     = @cms_db['funded-projects'].find({ 'funding' => id }).to_a
  has_funded_projects = funded_projects.size > 0
  documents           = @cms_db['documents'].find({ 'project' => id}).to_a
  transaction_groups  = @cms_db['transactions'].aggregate([{
    "$match" => {
      "project" => id
    }
  }, {
    "$group" => {
      "_id" => "$type",
      "total" => {
        "$sum" => "$value"
      },
      "transactions" => {
        "$addToSet" => {
          "description" => "$description",
          "component"   => "$component",
          "date"        => "$date",
          "value"       => "$value",
        }
      }
    }
  }, {
    "$sort" => {
      "_id" => 1
    }
  }])

  proxy "/projects/#{id}/index.html",              '/projects/summary.html',      :locals => { :project => project, :has_funded_projects => has_funded_projects }
  proxy "/projects/#{id}/documents/index.html",    '/projects/documents.html',    :locals => { :project => project, :has_funded_projects => has_funded_projects, :documents => documents }
  proxy "/projects/#{id}/transactions/index.html", '/projects/transactions.html', :locals => { :project => project, :has_funded_projects => has_funded_projects, :transaction_groups => transaction_groups }

  if has_funded_projects then
    proxy "/projects/#{id}/partners/index.html",   '/projects/partners.html',     :locals => { :project => project, :funded_projects => funded_projects }
  end
end

#------------------------------------------------------------------------------
# GENERATE OTHER PROJECTS
#------------------------------------------------------------------------------
@cms_db['other-org-projects'].find({}).each do |project|

  id                  = project['iatiId']
  documents           = @cms_db['documents'].find({ 'project' => id }).to_a
  transaction_groups  = @cms_db['transactions'].aggregate([{
    "$match" => {
      "project" => id
    }
  },{
    "$group" => {
      "_id" => "$type",
      "total" => {
        "$sum" => "$value"
      },
      "transactions" => {
        "$addToSet" => {
          "description" => "$description",
          "component"   => "$component",
          "date"        => "$date",
          "value"       => "$value",
        }
      }
    }
  }])

  proxy "/projects/#{id}/index.html",              '/projects/summary.html',      :locals => { :project => project, :has_funded_projects => false }
  proxy "/projects/#{id}/documents/index.html",    '/projects/documents.html',    :locals => { :project => project, :has_funded_projects => false, :documents => documents }
  proxy "/projects/#{id}/transactions/index.html", '/projects/transactions.html', :locals => { :project => project, :has_funded_projects => false, :transaction_groups => transaction_groups }

end

#------------------------------------------------------------------------------
# GENERATE FUNDED PROJECT PAGES
#------------------------------------------------------------------------------
@cms_db['funded-projects'].find({}).to_a.each do |funded_project|

  # format the project model to suit the project templates
  project = {
    'iatiId'            => funded_project['funded'],
    'title'             => funded_project['title'],
    'description'       => funded_project['description'],
    'funds'             => funded_project['funds'],
    'totalBudget'       => funded_project['totalBudget'],    
    'totalProjectSpend' => funded_project['totalSpend'],
    'end-actual'        => funded_project['end-actual'],
    'end-planned'       => funded_project['end-planned'],
    'start-actual'      => funded_project['start-actual'],
    'start-planned'     => funded_project['start-planned'],
    'status'            => funded_project['status']
  }

  # get the other funded projects
  funded_projects = @cms_db['funded-projects'].find({ 
    'funding' => funded_project['funding'],
    'funded'  => { '$ne' => funded_project['funded'] } 
  }).to_a

  # get the parent project
  funding_project    = @cms_db['projects'].find_one({ 'iatiId' =>  funded_project['funding'] })
  documents          = @cms_db['documents'].find({ 'project' => funded_project['funded'] }).to_a
  transaction_groups = @cms_db['transactions'].aggregate([{
    "$match" => {
      "project" => funded_project['funded']
    }
  },{
    "$group" => {
      "_id" => "$type",
      "total" => {
        "$sum" => "$value"
      },
      "transactions" => {
        "$addToSet" => {
          "description" => "$description",
          "component"   => "$component",
          "date"        => "$date",
          "value"       => "$value",
        }
      }
    }
  }])

  proxy "/projects/#{project['iatiId']}/index.html",              '/projects/summary.html',      :locals => { :project => project, :has_funded_projects => true }
  proxy "/projects/#{project['iatiId']}/documents/index.html",    '/projects/documents.html',    :locals => { :project => project, :has_funded_projects => true, :documents => documents }
  proxy "/projects/#{project['iatiId']}/transactions/index.html", '/projects/transactions.html', :locals => { :project => project, :has_funded_projects => true, :transaction_groups => transaction_groups }
  proxy "/projects/#{project['iatiId']}/partners/index.html",     '/projects/partners.html',     :locals => { :project => project, :has_funded_projects => true, :funded_projects => funded_projects, :funding_project => funding_project }

end

#------------------------------------------------------------------------------
# GENERATE SECTOR HIERARCHIES
#------------------------------------------------------------------------------
@cms_db['sector-hierarchies'].aggregate([{ "$group" => { 
    "_id"  => "$highLevelCode", 
    "sectorName" => {"$first" => "$highLevelName"} } }]).each do |sector|

  sectorCode = sector['_id']
  proxy "/sector/#{sectorCode}/index.html", '/sector/categories.html', :locals => { :sector => sector }

end

@cms_db['sector-hierarchies'].aggregate([{ "$group" => { 
    "_id"          => "$categoryCode", 
    "sectorCode"   => {"$first" => "$highLevelCode"}, 
    "sectorName"   => {"$first" => "$highLevelName"}, 
    "categoryName" => {"$first" => "$categoryName"} } }]).each do |sector|

  categoryCode = sector['_id']
  sectorCode   = sector['sectorCode']
  proxy "/sector/#{sectorCode}/categories/#{categoryCode}/index.html", '/sector/sectors.html', :locals => { :sector => sector }

end

@cms_db['sector-hierarchies'].find({}).to_a.each do |sector|
  highLevelCode = sector['highLevelCode']
  categoryCode  = sector['categoryCode']
  sectorCode    = sector['sectorCode']
  
  proxy "/sector/#{highLevelCode}/categories/#{categoryCode}/projects/#{sectorCode}/index.html", 'sector/projects.html', :locals => { :sector => sector }  
end

#------------------------------------------------------------------------------
# DEFINE HELPERS - Import from modules to avoid bloat
#------------------------------------------------------------------------------
helpers do

  include Formatters
  include CountryHelpers
  include FrontPageHelpers
  include Lookups
  include ProjectHelpers
  include CodeLists
  include SectorHelpers

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