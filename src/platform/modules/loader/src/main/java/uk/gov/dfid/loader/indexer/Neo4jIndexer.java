package uk.gov.dfid.loader.indexer;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import uk.gov.dfid.common.search.ElasticSearch;
import uk.gov.dfid.loader.indexer.helper.Country;
import uk.gov.dfid.loader.util.Sectors;

public class Neo4jIndexer {

    private static Sectors sectorsHelper;

	/**
	 * Indexing neo4jDatabse, if grpaphDB is not provided - creating new one, and shutting down, when done
	 *
	 * @param databaseLocation - optional
	 * @param elasticSearchNodeLocation
	 * @param graphDb - optional
	 */
	public static void index(
            String databaseLocation,
            String elasticSearchNodeLocation,
            GraphDatabaseService graphDb,
            Sectors sectors) {
        sectorsHelper = sectors;
		Long counter = System.currentTimeMillis();
		boolean isGraphDBprovided = true;
		if(graphDb == null){
			isGraphDBprovided = false;
			graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databaseLocation);
		}
		try {
			ElasticSearch es = new ElasticSearch();
			ExecutionEngine engine = new ExecutionEngine(graphDb);
			Map<String, IndexBean> structure = createBasicIndexStructureBasedOnHierarchy(engine);
			Map<String,Country> budgetsForCountries = getBudgetsForCountries(engine);
			es.deleteAll(elasticSearchNodeLocation);
			indexDfidSpecificDataToElasticSearch(es,structure,elasticSearchNodeLocation);
			indexCountrySugestionsDataToElasticSearch(es,budgetsForCountries,elasticSearchNodeLocation);

			System.out.println("Done in : "	+ (System.currentTimeMillis() - counter));

		} catch (Exception e) {
			e.printStackTrace();
			if(!isGraphDBprovided) graphDb.shutdown();
		}
		if(!isGraphDBprovided) graphDb.shutdown();
	}


	private static void indexCountrySugestionsDataToElasticSearch(ElasticSearch es,Map<String,Country> elementsToindex, String elasticSearchNodeLocation) {
		System.out.println("Getting sugestions");
		for (String element : elementsToindex.keySet()) {
			Map<String, Object> forES = new HashMap<String, Object>();
			forES.put("sugestion","CountriesSugestion");
			forES.put("countryName",elementsToindex.get(element).getCountryName());
			forES.put("countryCode",elementsToindex.get(element).getCountryCode());
			forES.put("countryBudget",elementsToindex.get(element).getCountryBudget());
			es.putIndex(forES, "aid" ,elasticSearchNodeLocation);
		}
	}

	private static void indexDfidSpecificDataToElasticSearch(ElasticSearch es, Map<String, IndexBean> elementsToindex, String elasticSearchNodeLocation) {
		for (IndexBean ib : elementsToindex.values()) {
			Map<String, Object> forES = new HashMap<String, Object>();

			StringBuilder sbSubs = new StringBuilder();
			for (String sub : ib.getSubProjects()) {
				sbSubs.append(sub);
				sbSubs.append("#");
			}
			StringBuilder sbOrgganization = new StringBuilder();
			for (String org : ib.getOrganizations()) {
				sbOrgganization.append(org);
				sbOrgganization.append("#");
			}
			StringBuilder sbCountry = new StringBuilder();
			for (String country : ib.getCountry()) {
				sbCountry.append(country);
				sbCountry.append("#");
			}
			StringBuilder sbSector = new StringBuilder();
			for (String sector : ib.getSector()) {
				sbSector.append(sector);
				sbSector.append("#");
			}
			StringBuilder sbRegion = new StringBuilder();
			for (String region : ib.getRegion()) {
				sbRegion.append(region);
				sbRegion.append("#");
			}

			forES.put("id", ib.getIatiId());
			forES.put("title", ib.getTitle());
			forES.put("description", ib.getDescription());
			forES.put("status", ib.getStatus());
			forES.put("budget",  ib.getBudget());
			String formated = NumberFormat.getCurrencyInstance(Locale.UK).format(ib.getBudget());
			forES.put("formatedBudget",  formated.substring(0, formated.length()-3));
			forES.put("organizations", sbOrgganization.toString());
			forES.put("subActivities", sbSubs.toString());
			forES.put("countries", sbCountry.toString());
			forES.put("sectors", sbSector.toString());
			forES.put("regions", sbRegion.toString());
			es.putIndex(forES, "aid" ,elasticSearchNodeLocation);
		}

	}

	private static Map<String,Country> getBudgetsForCountries(ExecutionEngine engine) {
		System.out.println("Getting budgets for countries");
		Map<String,Country> countries = new HashMap<String,Country>();
		try {
			String countryBudgets = "START  n=node:entities(type=\"iati-activity\") MATCH  n-[:`recipient-country`]-c, n-[:budget]-b-[:value]-v RETURN v.value as value, c.`recipient-country`? as country, c.code? as code";
			ExecutionResult result = engine.execute(countryBudgets);
			Iterator<Map<String, Object>> it = result.iterator();
			while (it.hasNext()) {
				Map<String, Object> item = it.next();
				if(item.get("country") == null || item.get("code") == null || item.get("value") == null){
					continue;
				}
				String countryName = (String) item.get("country");
				String countryCode = (String) item.get("code");
				Long countryBudget = (Long) item.get("value");
				Country country = countries.get(countryName);
				if (country == null) {
					country = new Country();
					country.setCountryBudget(countryBudget);
					country.setCountryCode(countryCode);
					country.setCountryName(countryName);
				} else {
					country.setCountryBudget(country.getCountryBudget() + countryBudget);
				}
				countries.put(countryName, country);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return countries;
	}

	private static void aquireDataFromRealatedActivityNodes(Map<String, String> structure, Map<String, IndexBean> elementsToindex, ExecutionEngine engine) {
			System.out.println("Getting data from activity realted nodes");

		try {
			String secondaryActivities = "START n=node:entities(type=\"iati-activity\")	MATCH n-[:`recipient-country`|`recipient-region`]-region,n-[:`reporting-org`]-org, n-[:`sector`]-sector WHERE n.`hierarchy` = 2 AND org.ref=\"GB-1\" RETURN n.`iati-identifier`?, region.`recipient-region`?, sector.`sector`?, region.`recipient-country`?, sector.code?";
			String budgets = "START n=node:entities(type=\"iati-activity\") MATCH  n-[:`related-activity`]-a, n-[:`reporting-org`]-org, n-[:budget]-b-[:value]-v WHERE  a.type = 1 AND n.hierarchy = 2 AND org.ref=\"GB-1\" RETURN a.ref as id, v.value as value";
			ExecutionResult result = engine.execute(secondaryActivities);
			ExecutionResult budgetsResults = engine.execute(budgets);
			Iterator<Map<String, Object>> bit = budgetsResults.iterator();
			Iterator<Map<String, Object>> it = result.iterator();

			while (it.hasNext()) {


				Map<String, Object> item = it.next();
				String id = (String) item.get("n.iati-identifier?");
				String region = (String) ((item.get("region.recipient-region?") == null) ? "" : item.get("region.recipient-region?"));
				String country = (String) ((item.get("region.recipient-country?") == null) ? "" : item.get("region.recipient-country?"));
				String sector = (String) item.get("sector.sector?");
				Long sectorCode = (Long) item.get("sector.code?");

                final String highLevelSector = sectorsHelper.getHighLevelSector(sectorCode);

				String primaryAcitivity = structure.get(id);
				IndexBean indexBean = elementsToindex.get(primaryAcitivity);
				if (indexBean != null) {
					indexBean.getRegion().add(region);
					indexBean.getCountry().add(country);
					indexBean.getSector().add(highLevelSector);
					elementsToindex.put(primaryAcitivity, indexBean);
				}
			}

			while (bit.hasNext()) {
				Map<String, Object> item = bit.next();
				Long budget = (Long) item.get("value");
				String id = (String) item.get("id");
				IndexBean indexBean = elementsToindex.get(id);
				indexBean.setBudget(indexBean.getBudget()+budget);
				elementsToindex.put(id, indexBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static Map<String, IndexBean> createBasicIndexStructureBasedOnHierarchy(ExecutionEngine engine) {
		System.out.println("Creating basic structure");
		HashMap<String, IndexBean> elementsToindex = new HashMap<String, IndexBean>();

		String primaryActivities = "START n=node:entities(type=\"iati-activity\") MATCH n-[:`related-activity`]->r, n-[:`activity-status`]->x, n-[:`participating-org`]->org, n-[:`reporting-org`]-o WHERE n.`hierarchy` = 1  AND o.ref=\"GB-1\" RETURN  n.`iati-identifier`? ,r.`ref`?,n.`title`?, x.`activity-status`?, org.`participating-org`? ,n.`description`?";
		try{
		ExecutionResult result = engine.execute(primaryActivities);
		Iterator<Map<String, Object>> it = result.iterator();
		Map<String, HashSet<String>> hierarhyRelations = new HashMap<String, HashSet<String>>();

		while (it.hasNext()) {
			Map<String, Object> item = it.next();
			if(item.get("r.ref?") == null || item.get("n.iati-identifier?") == null || item.get("n.title?") == null || item.get("x.activity-status?")==null){
				// in case is not what we are looking for (eg wrong data in xml during import)
				System.out.println("Wrong node, not indexing: "+item);
				continue;
			}

			String ref = (String) item.get("r.ref?");
			String id = (String) item.get("n.iati-identifier?");

			IndexBean indexBean = elementsToindex.get(id);
			if (indexBean == null) {
				indexBean = new IndexBean();
			}
			indexBean.setIatiId(id);
			indexBean.setDescription((String) item.get("n.description?"));
			indexBean.setTitle((String) item.get("n.title?"));
			indexBean.setStatus((String) item.get("x.activity-status?"));
			if (indexBean.getOrganizations() == null) {
				Set<String> orgs = new HashSet<String>();
				orgs.add((String) item.get("org.participating-org?"));
				indexBean.setOrganizations(orgs);
			} else {
				indexBean.getOrganizations().add((String) item.get("org.participating-org?"));
			}
			if (indexBean.getSubProjects() == null) {
				Set<String> subs = new HashSet<String>();
				subs.add(ref);
				indexBean.setSubProjects(subs);
			} else {
				indexBean.getSubProjects().add(ref);
			}

			indexBean.setCountry(new HashSet<String>());
			indexBean.setRegion(new HashSet<String>());
			indexBean.setSector(new HashSet<String>());
			indexBean.setBudget(0l);

			HashSet<String> references = hierarhyRelations.get(id);
			if (references != null) {
				references.add(ref);
			} else {
				references = new HashSet<String>();
				references.add(ref);
			}

			elementsToindex.put(id, indexBean);
			hierarhyRelations.put(id, references);
		}

		aquireDataFromRealatedActivityNodes(reverseRelatedActivietes(hierarhyRelations), elementsToindex, engine);

		}catch (Exception e) {
			e.printStackTrace();
		}

		return elementsToindex;
	}

	private static Map<String, String> reverseRelatedActivietes(
			Map<String, HashSet<String>> hierarhyRelations) {
		Map<String, String> secondaryActivities = new HashMap<String, String>();
		for (String primaryActivity : hierarhyRelations.keySet()) {
			Iterator<String> it = hierarhyRelations.get(primaryActivity).iterator();
			while (it.hasNext()) {
				secondaryActivities.put(it.next(), primaryActivity);
			}
		}
		return secondaryActivities;
	}
}
