package dbpedia.lookup.indexing;

import java.io.IOException;
import java.util.HashMap;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

public class LookupSolrIndexer implements ILookupIndexer {

	private HashMap<String, Integer> refCountMap;

	private HashMap<String, String> labelMap;
	
	private SolrClient solrClient;
	
	private String coreName;
	
	private SolrDocumentPool documentPool;
	
	private int updateCount;
	
	private int updateInterval;

	private String lastAction;

	public LookupSolrIndexer(String solrUrl, String coreName, int updateInterval) {
		
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
	
	public void addLabel(String resource, String label) {
		
		SolrInputDocument doc = documentPool.get();
		doc.addField("resource", resource);
		
	
		labelMap.put("add", label);
		doc.addField("label", labelMap);
		
		lastAction = "added label  '" + label + "'";
		
		try {
			solrClient.add(coreName, doc);
			update();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}
	
	public void increaseRefCount(String resource) {
		
		SolrInputDocument doc = documentPool.get();
		doc.addField("resource", resource);
		doc.addField("refCount", refCountMap);
		
		try {
			solrClient.add(coreName, doc);
		
		
			lastAction = "increased refCount of " + resource;
			update();
		
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void commit() {
		
		try {
			solrClient.commit(coreName);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Commiting after " + updateCount + " updates.");
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
