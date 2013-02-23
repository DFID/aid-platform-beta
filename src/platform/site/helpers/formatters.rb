require "kramdown"

module Formatters
  def format_million_stg(v)
    "&pound;#{v/1000000}M"
  end 

  def markdown_to_html(md)
    Kramdown::Document.new(md).to_html
  end
end