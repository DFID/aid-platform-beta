module ResultsHelpers

	def country_results(countryCode)
		@cms_db['country-results'].find({
			'code' => countryCode
			}).to_a
	end
end