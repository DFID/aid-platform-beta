require "kramdown"
require 'uri'


module Formatters

  def format_million_stg(v)
    "&pound;#{(v/1000000.0).round(2)}M"
  end 

  def format_round_million(v)
    "#{(v/1000000.0).round(2)} million"
  end

  def format_round_m(v)
    "#{(v/1000000.0).round(1)}m"
  end

  def format_billion_stg(v)
    "&pound;#{(v/1000000000.0).round(2)}bn"
  end

  def markdown_to_html(md)
    Kramdown::Document.new(md || '').to_html
  end

  def current_financial_year
    now = Time.new
    if(now.month < 4)
      "#{now.year-1}/#{now.year}"
    else
      "#{now.year}/#{now.year + 1}"
    end
  end

  def format_date(d)
    if (d > 0)
        # formats date in miliseconds as '%d %b %Y', eg. "11 Mar 2008"
        Time.at(d/1000.0).strftime("%d %b %Y")
    else
        ""
    end
  end

  def format_percentage(v)
    "%.2f" % v + "%"
  end

  def format_query_string(s)
    URI.escape(s)
  end
end