package uk.gov.dfid.loader.indexer;

import java.util.Set;

public class IndexBean {
	private String iatiId;
	private String description;
	private String status;
	private String title;
	private Long budget;
	private Set<String> organizations;
	private Set<String> subProjects;
	private Set<String> sector;
	private Set<String> country;
	private Set<String> region;

	public Set<String> getSubProjects() {
		return subProjects;
	}

	public void setSubProjects(Set<String> subProjects) {
		this.subProjects = subProjects;
	}

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

	public Long getBudget() {
		return budget;
	}

	public void setBudget(Long budget) {
		this.budget = budget;
	}

	public Set<String> getSector() {
		return sector;
	}

	public void setSector(Set<String> sector) {
		this.sector = sector;
	}

	public Set<String> getCountry() {
		return country;
	}

	public void setCountry(Set<String> country) {
		this.country = country;
	}

	public Set<String> getRegion() {
		return region;
	}

	public void setRegion(Set<String> region) {
		this.region = region;
	}

	public Set<String> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(Set<String> organizations) {
		this.organizations = organizations;
	}

}
