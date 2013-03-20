package uk.gov.dfid.es;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;

public class ElasticSearch {

	private static Client client;
	private static Node node;


	public static void connectToESNode(String dataLocation) {

		node = NodeBuilder
				.nodeBuilder()
				.local(true)
				.data(true)
				.settings(ImmutableSettings.settingsBuilder().put("path.data", dataLocation)).node();
		node.client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
		client = node.client();

	}

	/**
	 * Shut down all connections
	 */
	public void shutdown() {
		if (node != null)
			node.close();
		if (client != null)
			client.close();
	}

	public IndexResponse putIndex(Map<String, Object> indexMap, String indexName, String dataLocation) {
		if(client == null){
			connectToESNode(dataLocation);
		}
		IndexRequest indexRequest = new IndexRequest(indexName);
		indexRequest.type("index");
		indexRequest.source(indexMap);
		return client.index(indexRequest).actionGet();

	}

	public static List<Map<String, String>> search(String search, String dataLocation) {
		if(client == null){
			System.out.println("Launching ES client");
			connectToESNode(dataLocation);
			System.out.println("ES client launched");
		}
		
		Long counter = System.currentTimeMillis();
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		
		SearchResponse response = client.prepareSearch()
				.setQuery(QueryBuilders.queryString(search)).setSize(300).execute()
				.actionGet();
		SearchHit[] hits = response.getHits().getHits();
		
		for (SearchHit hit : hits) {
			Map<String, String> hitMap = (HashMap<String, String>)(Object)hit.getSource();
			results.add(hitMap);
		}
		
		System.out.println(new StringBuilder().append("Keyword '").append(search).append("'")
				.append(" numer of results: ").append(results.size())
				.append(", took (s): ").append( (System.currentTimeMillis() - counter)/(float)1000) );
		return results;
	}
}