require "rubygems"
require "json"

###
# Compass
###

# Susy grids in Compass
# First: gem install susy --pre
# require 'susy'

# Change Compass configuration
# compass_config do |config|
#   config.output_style = :compact
# end

###
# Page options, layouts, aliases and proxies
###

# Per-page layout changes:
#
# With no layout
# page "/path/to/file.html", :layout => false
#
# With alternative layout
# page "/path/to/file.html", :layout => :otherlayout
#
# A path which all have the same layout
# with_layout :admin do
#   page "/admin/*"
# end

#This will use data from db, but for now will test a few different project codes
projects = ["GB-1-203600", "GB-1-113939", "GB-1-112068"]
projects.each do |projectCode|
  page "/projects/#{projectCode}", :proxy => "/projects/index.html", :locals => {:project => projectCode}
end

countries = ["ET", "UK", "IR"]
countries.each do |countryCode|
  page "/countries/#{countryCode}", :proxy => "/countries/index.html", :locals => {:country => countryCode}
end

# Proxy (fake) files
# page "/this-page-has-no-template.html", :proxy => "/template-file.html" do
#   @which_fake_page = "Rendering a fake page with a variable"
# end

###
# Helpers
###

# Automatic image dimensions on image_tag helper
# activate :automatic_image_sizes

# Methods defined in the helpers block are available in templates
helpers do
   def countries_helper
     countriesJSON = HTTParty.get("http://0.0.0.0:9000/access/countries") #make sure test-api is running
     parsedJSON = JSON.parse(countriesJSON.body) #gets the json for all countries
   end

   def sectors_helper
      sectors = [
        ["Economic and development policy/planning", "£1917m"],
        ["Education policy and administrative management", "£1687m"],
        ["Primary education", "£1687m"],
        ["Social/ welfare services", "£1402m"],
        ["Bio-diversity", "£1391m"]
      ]
   end

   def projects_helper
      projects = [
        ["Malaria bed nets distributed","£12.2m"],
        ["Children vaccinated","£12m"],
        ["Access to financial services","£11.9m"],
        ["Hygiene conditions improved","£7.4m"],
        ["Emergency food assistance","£6m"]
      ]
   end
end

set :css_dir, 'stylesheets'

set :js_dir, 'javascripts'

set :images_dir, 'images'

# Build-specific configuration
configure :build do
  # For example, change the Compass output style for deployment
  # activate :minify_css

  # Minify Javascript on build
  # activate :minify_javascript

  # Enable cache buster
  # activate :cache_buster

  # Use relative URLs
  # activate :relative_assets

  # Compress PNGs after build
  # First: gem install middleman-smusher
  # require "middleman-smusher"
  # activate :smusher

  # Or use a different image path
  # set :http_path, "/Content/images/"
end