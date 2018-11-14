package dbpedia.inc_lookup;

import java.io.IOException;
import java.util.HashMap;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

public class Indexer {

	private HashMap<String, Integer> refCountMap;

	private HashMap<String, String> labelMap;
	
	private SolrClient solrClient;
	
	private String coreName;
	
	private SolrDocumentPool documentPool;
	
	private int updateCount;
	
	private int updateInterval;

	private String lastAction;

	public Indexer(String solrUrl, String coreName, int updateInterval) {
		
		documentPool = new SolrDocumentPool(updateInterval);
		refCountMap = new HashMap<String, Integer>(1);
		refCountMap.put("inc", 1);
		
		labelMap = new HashMap<String, String>(1);
		labelMap.put("add", null);
		
		solrClient = new HttpSolrClient.Builder(solrUrl)
        	.withConnectionTimeout(10000)
        	.withSocketTimeout(60000)
        	.build();
		
		this.coreName = coreName;
		this.updateInterval = updateInterval;
	
		
	}
	
	public void addLabel(String resource, String label) throws SolrServerException, IOException {
		
		SolrInputDocument doc = documentPool.get();
		doc.addField("resource", resource);
		
	
		labelMap.put("add", label);
		doc.addField("label", labelMap);
		
		lastAction = "added label  '" + label + "'";
		
		solrClient.add(coreName, doc);
		
		update();
		
		
	}
	
	public void increaseRefCount(String resource) throws SolrServerException, IOException {
		
		SolrInputDocument doc = documentPool.get();
		doc.addField("resource", resource);
		doc.addField("refCount", refCountMap);
		
		solrClient.add(coreName, doc);
		
		lastAction = "increased refCount of " + resource;
		update();
	}
	
	public void commit() throws SolrServerException, IOException {
		
		solrClient.commit(coreName);
		
		System.out.println("Commiting after " + updateInterval + " updates.");
		System.out.println("Last: " + lastAction);
		updateCount = 0;
		documentPool.reset();	
	}
	
	private void update() throws SolrServerException, IOException {
		
		updateCount++;
		
		if(updateCount >= updateInterval) {
			
			commit();
			
		}
	}

	public boolean clearIndex() {
		try {
			solrClient.deleteByQuery(coreName, "*:*");
			solrClient.commit(coreName);
			
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
