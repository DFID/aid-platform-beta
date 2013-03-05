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

	private Client client;
	private Node node;
	
//	public void connectoToExistingESInstance(List<String> hostsName, int port) {
//		Settings settings = ImmutableSettings.settingsBuilder()
//				.put("client.transport.sniff", true).build();
//
//		client = new TransportClient(settings);
//		for (String host : hostsName) {
//			client.addTransportAddress(new InetSocketTransportAddress(host,
//					port));
//		}
//
//	}

	public void connectToESNode(String dataLocation) {

		node = NodeBuilder.nodeBuilder() 
                .local(true).data(true) 
                .settings(ImmutableSettings.settingsBuilder().put("path.data",dataLocation)) 
                .node();
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

	public IndexResponse putIndex(Map<String, Object> indexMap, String indexName) {
		IndexRequest indexRequest = new IndexRequest(indexName);
		indexRequest.type("index");
		indexRequest.source(indexMap);
		return client.index(indexRequest).actionGet();
		
	}

	public Map<String, Object> search(String search) {
		Map<String, Object> result = new HashMap<String, Object>();
		SearchResponse response = client.prepareSearch().setQuery(QueryBuilders.queryString(search)).execute().actionGet();
		SearchHit[] results = response.getHits().getHits();
		for (SearchHit hit : results) {
			System.out.println("Keyword: '"+ search +"' Result: " + hit.getSourceAsString());
			result.putAll(hit.getSource());
		}
		return result;
	}
}
