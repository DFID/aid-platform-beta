module CountryHelpers
  def dfid_total_budget
    # aggregates the budgets from all projects
    @cms_db['projects'].aggregate([{ 
      "$group" => { 
        "_id"   => nil,  
        "total" => { 
          "$sum" => "$currentFYBudget"  
        }  
      } 
    }]).first['total'].to_f
  end

  def active_projects(countryCode)
    @cms_db['projects'].find({  'projectType' => "country",
                                'recipient' => countryCode,
                                'status' => { '$lt'=> 3 }
                              }).count()
  end

  def sector_groups(countryCode) 
    firstSeven = @cms_db['sector-breakdowns'].find({'country' => countryCode}).sort({'total' => -1}).limit(7).to_a
    others = @cms_db['sector-breakdowns'].aggregate([{ 
      "$match" => {"country" => countryCode } 
    }, { 
      "$skip" => 7 
    }, {
     "$group" => {
        "_id" => nil,
        "total" => {
         "$sum" => "$total"
        } 
      } 
    }])

    firstSeven + others
  end

  
  def country_project_budgets(country_code)
    projects = @cms_db['projects'].find({"projectType" => "country", "recipient" => country_code})
    startDate = "#{(financial_year-3)} 04-01"
    endDate = "#{(financial_year+2)} 03-31"

    projects.inject([]) { |graph, project|

      project_budgets = @cms_db['project-budgets'].find({
          'id' => project['iatiId'],
          'date' => {
            '$gte' => startDate,
            '$lte' => endDate
          }}).to_a

      graph + project_budgets.inject({}) { |results, budget| 
        fy = financial_year_formatter budget['date']
        results[fy] = (results[fy] || 0) + budget['value']
        results
      }.map { |fy, budget| [fy, budget] }
      }.inject({}) { |graph, group|
        graph[group.first] = (graph[group.first] || 0) + group.last
        graph
      }.map { |fy, budget| [fy, budget] }.sort

  end

  def financial_year_formatter(dateStr)
    date = Date.parse dateStr
    if date.month < 4
      "FY" + (date.year-1).to_s
    else
      "FY" + date.year.to_s
    end
  end

  def financial_year 
    now = Time.new
    if(now.month < 4)
      now.year-1
    else
      now.year
    end
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

  def country_name(countryCode)
    result = @cms_db['countries'].find({
      'code' => countryCode
    })
    (result.first || { 'name' => '' })['name']
  end
end