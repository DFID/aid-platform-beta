package uk.gov.dfid.es;

import java.util.HashMap;
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

	public static Map<String, String> search(String search, String dataLocation) {
		if(client == null){
			System.out.println("Launching es client");
			connectToESNode(dataLocation);
		}
		Map<String, String> result = new HashMap<String, String>();
		SearchResponse response = client.prepareSearch()
				.setQuery(QueryBuilders.queryString(search)).execute()
				.actionGet();
		SearchHit[] results = response.getHits().getHits();
		for (SearchHit hit : results) {
			Map<String, String> hitMap = (Map<String, String>)(Object) hit.getSource();
			System.out.println("Keyword: '" + search + "' Result: " + hitMap);
			result.putAll(hitMap);
		}
		return result;
	}
}