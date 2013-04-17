module RegionHelpers

  # returns an alphabetically sorted list of all regions
  def region_list

    # get all regions
    all_regions = @cms_db['regions'].find({})

    # sort them alphabetically (irrespective of case)
    all_regions.sort_by { |region| region['name'][0].upcase }
  end

end