package uk.gov.dfid.es.helper;

public class Organization {

	public static String resolveOrganizationCode(int code) {
		switch (code) {
		case 10:
			return "Government";
		case 15:
			return "Other Public Sector";
		case 21:
			return "International NGO";
		case 22:
			return "National NGO";
		case 23:
			return "Regional NGO";
		case 30:
			return "Public Private Partnership";
		case 40:
			return "Multilateral";
		case 60:
			return "Foundation";
		case 70:
			return "Private Sector";
		case 80:
			return "Academic, Training and Research";
		default:
			return "";
		}

	}
}
