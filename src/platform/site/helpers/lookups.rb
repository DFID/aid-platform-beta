module Lookups

  def country_name(code)
    (@cms_db['countries'].find_one({ 'code' => code }) || { "name" => "" })["name"]
  end

  def region_name(code)
    (@cms_db['regions'].find_one({ 'code' => code }) || { "name" => "" })["name"]
  end

end