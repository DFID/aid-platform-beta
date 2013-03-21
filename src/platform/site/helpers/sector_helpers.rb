module SectorHelpers

	def high_level_sectors_structure
		sectors = @cms_db['sector-hierarchies'].aggregate([{ 
			"$group" => { 
				"_id"  => "$highLevelCode",
				"name" => {
					"$first" => "$highLevelName"
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

		calculate_hierarchy_structure(sectors)
	end

	def sector_categories_structure(highLevelSectorCode)
		sectors = @cms_db['sector-hierarchies'].aggregate([{
			"$match" => {
				"highLevelCode" => highLevelSectorCode
			}			
		},{ 
			"$group" => { 
				"_id"  => "$categoryCode",
				"name" => {
					"$first" => "$categoryName"
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

		calculate_hierarchy_structure(sectors)
	end

	def calculate_hierarchy_structure(sectors)	
		totalSectorsBudget = Float(sectors.map { |s| s[:budget] }.inject(:+))
		maxBudget = Float(sectors.max_by { |s| s[:budget] }[:budget])

		sectors.sort_by { |s| s[:code] }.map { |s| s.merge({
			:percentage => s[:budget] / totalSectorsBudget * 100.0,
			:cellWidth	=> s[:budget] / maxBudget * 60.0
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
