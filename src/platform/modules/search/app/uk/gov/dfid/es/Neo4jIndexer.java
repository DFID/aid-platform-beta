package uk.gov.dfid.es;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class Neo4jIndexer {

	GraphDatabaseService graphDb;
	ExecutionEngine engine;
	Map<String, IndexBean> elementsToindex;
	Long counter;

	public Neo4jIndexer(String databaseLocation){
		neo4jStartup(databaseLocation);
	}
	
	private void neo4jStartup(String databaseLocation) {
		graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabase(databaseLocation);
		engine = new ExecutionEngine(graphDb);
	}

	public void index(String elasticSearchNodeLocation) {
		try{
		counter = System.currentTimeMillis();
		Map<String, List<String>> structure = createBasicIndexStructureBasedOnHierarchy();
		indexToElasticSearch(elasticSearchNodeLocation);
		//aquireDataFromRealatedActivityNodes(structure);
		System.out.println("Done in : " + (System.currentTimeMillis() - counter));
		}catch (Exception e) {
			e.printStackTrace();
			neo4jShutdown();
		}
	}
	
	private void indexToElasticSearch(String elasticSearchNodeLocation){
		ElasticSearch es = new ElasticSearch();
		es.connectToESNode(elasticSearchNodeLocation); // System.getenv("DFID_ELASTICSEARCH_PATH");
		
		for(IndexBean ib : elementsToindex.values()){
			Map<String,Object> forES = new HashMap<String,Object>();
			forES.put("id", ib.getIatiId());
			forES.put("title", ib.getTitle());
			forES.put("description", ib.getDescription());
			forES.put("status", ib.getStatus());
			es.putIndex(forES, "aid");
		}
	}
	
	private void aquireDataFromRealatedActivityNodes(Map<String, List<String>> structure) {
		
		System.out.println("Getting data from activity realted nodes");
		for (String iatiId : structure.keySet()) {
			System.out.println("Getting data from related nodes, for activity: " + iatiId);
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
					ib.getRegion().add((String) item.get("region.recipient-region"));
					ib.getSector().add((String) item.get("sector.sector"));
					elementsToindex.put(iatiId, ib);
				}
			}

		}
		System.out.println("Done getting data from activity realted nodes");
	}

	private Map<String, List<String>> createBasicIndexStructureBasedOnHierarchy() {
		System.out.println("Creating basic structure");
		elementsToindex = new HashMap<String, IndexBean>();

		String relatedActivities = "START n=node:entities(type=\"iati-activity\")MATCH n-[:`related-activity`]->r, n-[:`activity-status`]->x WHERE n.`hierarchy` = 1 RETURN  n.`iati-identifier`,r.`ref`,n.`title`, x.`activity-status`, n.`description`?";
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
		System.out.println("Done creating basic structure");
		return hierarhyRelations;
	}

	public void neo4jShutdown() {
		graphDb.shutdown();
	}
}
