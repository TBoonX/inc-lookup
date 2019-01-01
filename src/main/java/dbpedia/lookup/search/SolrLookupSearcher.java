package dbpedia.lookup.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONObject;

public class SolrLookupSearcher implements ILookupSearcher {

	 
	private HttpSolrClient solrClient;
	private String coreName;

	public SolrLookupSearcher(String solrUrl, String coreName) {
		
		solrClient = new HttpSolrClient.Builder(solrUrl)
	        	.withConnectionTimeout(10000)
	        	.withSocketTimeout(60000)
	        	.build();
		

		this.coreName = coreName;
	}
	
	public String search(String query, int maxHits) {
		
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQuery(query);
		
		return "Those are not the droids you are looking for!";
	}
	
	public String labelEdismax(String query) throws SolrServerException, IOException {
		
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.set("q", query + "~");
		solrQuery.set("defType", "edismILookupSearcherax");
		solrQuery.set("qf", "label");
		
		QueryResponse respone = solrClient.query(coreName, solrQuery);
		SolrDocumentList list = respone.getResults();
		
		JSONObject returnResults = new JSONObject();
		HashMap<Integer, Object> solrDocMap = new HashMap<Integer, Object>();
		
		int counter = 1;
		
		for(@SuppressWarnings("rawtypes") Map singleDoc : list)
		{
		  solrDocMap.put(counter, new JSONObject(singleDoc));
		  counter++;
		}
		
		returnResults.put("docs", solrDocMap);
		return returnResults.toString();
	}
}
