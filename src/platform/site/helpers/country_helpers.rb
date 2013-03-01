module CountryHelpers
  def dfid_total_budget
    # aggregates the budgets from all projects
    @cms_db['country-stats'].aggregate([{ 
      "$group" => { 
        "_id"   => nil,  
        "total" => { 
          "$sum" => "$totalBudget"  
        }  
      } 
    }]).first['total']
  end

  def active_projects(countryCode)
    @cms_db['projects'].find({  'projectType' => "country",
                                'recipient' => countryCode,
                                'status' => { '$lt'=> 3 }
                              }).count()
  end

  def sector_groups(countryCode) 
    @cms_db['sector-breakdowns'].find({'country' => countryCode}).sort({'total' => -1})
  end

  def top_5_countries
    @cms_db['country-stats'].aggregate([{
      "$sort" => {
        "totalBudget" => -1
      }
    },{
      "$limit" => 5
    }]).map do |totals|  
      name = @cms_db['countries'].find_one({
        'code' => totals['code']
      })['name']

      {
        'code'        => totals['code'],
        'name'        => name,
        'totalBudget' => totals['totalBudget']
      }
    end
  end
end