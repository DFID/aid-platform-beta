require "kramdown"

module Formatters
  def format_million_stg(v)
    "&pound;#{v/1000000}M"
  end 

  def format_billion_stg(v)
    "&pound;#{v/1000000000}bn"
  end

  def markdown_to_html(md)
    Kramdown::Document.new(md).to_html
  end

  def current_financial_year
    now = Time.new
    if(now.month < 4)
      "#{now.year-1}/#{now.year}"
    else
      "#{now.year}/#{now.year +1}"
    end
  end
end