package uk.gov.dfid.es;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import uk.gov.dfid.es.helper.Organization;

public class Neo4jIndexer {

	public static void index(String databaseLocation, String elasticSearchNodeLocation) {
		Long counter = System.currentTimeMillis();
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databaseLocation);
		try {
			
			ExecutionEngine engine = new ExecutionEngine(graphDb);
			Map<String, IndexBean> structure = createBasicIndexStructureBasedOnHierarchy(engine);
			indexToElasticSearch(structure,elasticSearchNodeLocation);
			System.out.println("Done in : "	+ (System.currentTimeMillis() - counter));
		
		} catch (Exception e) {
			graphDb.shutdown();
		}
		graphDb.shutdown();
	}

	private static void indexToElasticSearch(Map<String, IndexBean> elementsToindex, String elasticSearchNodeLocation) {
		ElasticSearch es = new ElasticSearch();
		for (IndexBean ib : elementsToindex.values()) {
			Map<String, Object> forES = new HashMap<String, Object>();

			StringBuilder sb = new StringBuilder();
			for (String sub : ib.getSubProjects()) {
				sb.append(sub);
				sb.append(" ");
			}
			StringBuilder sborg = new StringBuilder();
			for (String org : ib.getOrganizations()) {
				sborg.append(org);
				sborg.append(" ");
			}
			
			forES.put("id", ib.getIatiId());
			forES.put("title", ib.getTitle());
			forES.put("description", ib.getDescription());
			forES.put("status", ib.getStatus());
			forES.put("organizations", sborg.toString());
			forES.put("subActivities", sb.toString());

			es.putIndex(forES, "aid" ,elasticSearchNodeLocation);
		}
	}

	private static void aquireDataFromRealatedActivityNodes(Map<String, List<String>> structure, Map<String, IndexBean> elementsToindex, ExecutionEngine engine) {

		System.out.println("Getting data from activity realted nodes");
		for (String iatiId : structure.keySet()) {
			System.out
					.println("Getting data from related nodes, for activity: "
							+ iatiId);
			for (String subIatiId : structure.get(iatiId)) {
				String relatedActivities = "START n=node:entities(type=\"iati-activity\") MATCH n-[:`recipient-region`]-region, n-[:`sector`]-sector "
						+ "WHERE n.`iati-identifier` = \""
						+ subIatiId
						+ "\" RETURN region.`recipient-region`, sector.`sector`";

				ExecutionResult result = engine.execute(relatedActivities);
				Iterator<Map<String, Object>> it = result.iterator();

				while (it.hasNext()) {
					Map<String, Object> item = it.next();
					IndexBean ib = elementsToindex.get(iatiId);
					ib.getRegion().add(
							(String) item.get("region.recipient-region"));
					ib.getSector().add((String) item.get("sector.sector"));
					elementsToindex.put(iatiId, ib);
				}
			}

		}
		System.out.println("Done getting data from activity realted nodes");
	}
	 
	
	private static Map<String, IndexBean> createBasicIndexStructureBasedOnHierarchy(ExecutionEngine engine) {
		System.out.println("Creating basic structure");
		HashMap<String, IndexBean> elementsToindex = new HashMap<String, IndexBean>();

		String relatedActivities = "START n=node:entities(type=\"iati-activity\")MATCH n-[:`related-activity`]->r, n-[:`activity-status`]->x, n-[:`participating-org`]->org WHERE n.`hierarchy` = 1 RETURN  n.`iati-identifier`,r.`ref`,n.`title`, x.`activity-status`, org.`type`? ,n.`description`?";
		ExecutionResult result = engine.execute(relatedActivities);
		Iterator<Map<String, Object>> it = result.iterator();
		Map<String, List<String>> hierarhyRelations = new HashMap<String, List<String>>();

		while (it.hasNext()) {
			Map<String, Object> item = it.next();

			String ref = (String) item.get("r.ref");
			String id = (String) item.get("n.iati-identifier");

			IndexBean indexBean = elementsToindex.get(id);
			if (indexBean == null) {
				indexBean = new IndexBean();
			}
			indexBean.setIatiId(id);
			indexBean.setDescription((String) item.get("n.description?"));
			indexBean.setTitle((String) item.get("n.title"));
			indexBean.setStatus((String) item.get("x.activity-status"));
			if (indexBean.getOrganizations() == null) {
				Set<String> orgs = new HashSet<String>();
				orgs.add(Organization.resolveOrganizationCode((int) (long) item.get("org.type?")));
				indexBean.setOrganizations(orgs);
			} else {
				indexBean.getOrganizations().add(
						Organization.resolveOrganizationCode((int) (long) item.get("org.type?")));
			}
			if (indexBean.getSubProjects() == null) {
				Set<String> subs = new HashSet<String>();
				subs.add(ref);
				indexBean.setSubProjects(subs);
			} else {
				indexBean.getSubProjects().add(ref);
			}

			indexBean.setCountry(new ArrayList<String>());
			indexBean.setRegion(new ArrayList<String>());
			indexBean.setSector(new ArrayList<String>());

			List<String> references = hierarhyRelations.get(id);
			if (references != null) {
				references.add(ref);
			} else {
				references = new ArrayList<String>();
				references.add(ref);
			}

			elementsToindex.put(id, indexBean);
			hierarhyRelations.put(id, references);
			
		}
		
		// TODO: optimize aquireDataFromRealatedActivityNodes - when filtering is needed
		//aquireDataFromRealatedActivityNodes(hierarhyRelations, elementsToindex, engine);
		System.out.println("Done creating basic structure");
		return elementsToindex;
	}
}
