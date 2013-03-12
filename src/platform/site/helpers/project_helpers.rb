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
end