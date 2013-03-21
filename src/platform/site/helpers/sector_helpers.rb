module SectorHelpers

	def level_1_sectors
		aggregate_sectors_hierarchy_level("highLevelCode", "highLevelName")
	end

	def aggregate_sectors_hierarchy_level(sectorLevelCodeField, sectorsLevelNameField)
		sectors = @cms_db['sector-hierarchies'].aggregate([{ 
			"$group" => { 
				"_id"  => "$" + sectorLevelCodeField, 
				"name" => {
					"$first" => "$" + sectorsLevelNameField
				}, 
				"sectorCodes" => {
					"$addToSet" => "$sectorCode"
				} 
			} 
		}]).map { |l| {
			:code => l['_id'],
			:name => l['name'],
			:budget => calculate_total_sector_level_budget(l['sectorCodes'])
		}}

		totalSectorsBudget = Float(sectors.map { |s| s[:budget] }.inject(:+))

		sectors.sort_by { |s| s[:code] }.map { |s| s.merge({
			:percentage => s[:budget] / totalSectorsBudget * 100.0
		})}.select { |s| s[:percentage] >= 0.01 }
	end

	def calculate_total_sector_level_budget(sectorCodes)
		result = @cms_db['project-sector-budgets'].aggregate([
			{
				"$match" => {
					"sectorCode" => {
						"$in" => sectorCodes
					}
				}
			}, {
				"$group" => {
					"_id" => nil, 
					"total" => {
						"$sum" => "$sectorBudget"}
				}
			}
		])
		(result.first || { 'total' => 0 })['total']
	end	
end
