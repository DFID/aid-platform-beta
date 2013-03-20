module ProjectHelpers
    def dfid_total_projects_budget(projectType)
        # aggregates a total budget of all the dfid projects for a given type (global, coutry, regional)
        dfid_projects_budget(projectType).first['total']
    end

    def dfid_projects_budget(projectType)
        @cms_db['projects'].aggregate([{
            "$group" => {
                "_id"   => "$projectType",
                "total" => {
                    "$sum" => "$currentFYBudget"
                }
            } }, {
                "$match" => {
                    "_id" => projectType
                }
            }]
        )
    end

    def is_dfid_project(projectCode)   
        projectCode[0, 4] == "GB-1"
    end

    def dfid_region_projects_budget(regionCode)
        # aggregates a total budget of all the regional projects for the given region code
        result = @cms_db['projects'].aggregate([{
            "$match" => {
                "projectType" => "regional",
                "recipient" => regionCode
                }
            }, {
                "$group" => {
                    "_id" => "$recipient",
                    "total" => {
                        "$sum" => "$currentFYBudget"
                    }
                }
            }]
        )
        (result.first || { 'total' => 0 })['total']
    end

    def dfid_country_projects_data
        result = @cms_db['projects'].aggregate([{
                "$match" => {
                    "projectType" => "country",
                    "status" => {
                        "$lt" => 3
                    }
                }
            }, {
                "$group" => { 
                    "_id" => "$recipient", 
                    "total" => {
                        "$sum" => "$currentFYBudget"
                    }
                }
            }]
        )
        result.map { |country| {
            country['_id'] => {
                :country =>  @cms_db['countries'].find({ "code" => country['_id'] }).first['name'],
                :id => country['_id'],
                :projects => @cms_db['projects'].find({ "recipient" => country['_id'], "projectType" => "country"}).count(),
                :budget => country['total'],
                :flag => '/images/flags/' + country['_id'].downcase + '.png'
            }
        }}.inject({}) { |obj, entry| 
            obj.merge! entry
        }.to_json
    end

    def dfid_regional_projects_data
        # aggregates budgets of the dfid regional projects grouping them by regions
        @cms_db['regions'].find().map { |region| {
            :region => region['name'],
            :budget => dfid_region_projects_budget(region['code']) || 0
        }}.to_json
    end

    def choose_better_date(actual, planned)
        # determines project actual start/end date - use actual date, planned date as a fallback
        unless actual.nil? || actual == ''
            return (Time.at(actual).to_f * 1000.0).to_i
        end

        unless planned.nil? || planned == ''
            return (Time.at(planned).to_f * 1000.0).to_i
        end

        return 0
    end

    def project_budgets(projectId)
        project_budgets = @cms_db['project-budgets'].find({
              'id' => projectId
            }).to_a
        graph = []
        graph + project_budgets.inject({}) { |results, budget| 
          fy = financial_year_formatter budget['date']
          results[fy] = (results[fy] || 0) + budget['value']
          results
        }.map { |fy, budget| [fy, budget] }.inject({}) { |graph, group|
          graph[group.first] = (graph[group.first] || 0) + group.last
          graph
        }.map { |fy, budget| [fy, budget] }.sort
    end

    def project_budget_per_fy(projectId)
        # aggregates the project budgets and budgets spend per financial years for given project
        spends = @cms_db['transactions'].find({
            "project" => projectId,
            "$or"     => [{ "type" => "C"}, {"type" => "D"}]
        }).map { |t| {
            "fy" => financial_year_formatter(t['date'].strftime("%Y-%m-%d")),
            "value" => t['value']
        }}.group_by { |year| year["fy"] }.map do |fy, v|
            total = v.map { |year| year["value"] }.inject(:+)
            {'fy' => fy, 'value' => total}
        end.map { |year| {
            year['fy'] => year['value']
        }}.reduce(Hash.new, :merge)

        @cms_db['project-budgets'].find({
            "id"    => projectId,
            "value" => { "$gt" => 0 }
        }).sort({
            "date" => 1
        }).map { |budget| [ financial_year_formatter(budget['date']),
                            budget['value'],
                            spends[financial_year_formatter(budget['date'])] || 0 ]
        }
    end

    def project_sector_groups(projectId)
    	projectData = @cms_db['projects'].find({
    		"iatiId" => projectId
    	})
    	sectorGroups = (projectData.first || { 'sectorGroups' => [] })['sectorGroups']
    	if sectorGroups.any? then
	    	sectorGroups = sectorGroups.group_by { |s| 
	    		s['code'] }.map do |code, sectors| { 
	    		"code" => code, "name" => sectors[0]["name"], "budget" => sectors.map { |sec| sec["budget"] }.inject(:+)
	    	} end.sort_by{ |sg| -sg["budget"]}
	    	sectorsTotalBudget = Float(sectorGroups.map {|s| s["budget"]}.inject(:+))

	    	sectorGroups.map { |sg| {
	    		:sector => sg['name'],
	    		:budget => sg['budget'] / sectorsTotalBudget * 100.0,
	    		:formatted => format_percentage(sg['budget'] / sectorsTotalBudget * 100)
			}}
		else
			return sectorGroups
		end
    end
end
