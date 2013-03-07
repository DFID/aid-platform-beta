package uk.gov.dfid.es;

import java.util.List;

public class IndexBean {
	private String iatiId;
	private String description;
	private String status;
	private String title;
	private List<String> sector;
	private List<String> country;
	private List<String> region;

	public String getIatiId() {
		return iatiId;
	}

	public void setIatiId(String iatiId) {
		this.iatiId = iatiId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getSector() {
		return sector;
	}

	public void setSector(List<String> sector) {
		this.sector = sector;
	}

	public List<String> getCountry() {
		return country;
	}

	public void setCountry(List<String> country) {
		this.country = country;
	}

	public List<String> getRegion() {
		return region;
	}

	public void setRegion(List<String> region) {
		this.region = region;
	}

}
