package uk.gov.dfid.loader.indexer.helper;

public class Country {
	private String countryName;
	private Long countryBudget;
	private String countryCode;

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public Long getCountryBudget() {
		return countryBudget;
	}

	public void setCountryBudget(Long countryBudget) {
		this.countryBudget = countryBudget;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
}
