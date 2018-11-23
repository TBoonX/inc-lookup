package dbpedia.lookup.indexing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

public class LookupLuceneIndexer implements ILookupIndexer {

	private HashMap<String, Integer> refCountMap;

	private HashMap<String, String> labelMap;
	
	private String coreName;
	
	private int updateCount;
	
	private int updateInterval;

	private String lastAction;

	private IndexWriter writer;

	public LookupLuceneIndexer(String filePath, int updateInterval) {
		
		StandardAnalyzer analyzer = new StandardAnalyzer();
		
		try {

			Directory index = FSDirectory.open(new File(filePath).toPath());
			
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			writer = new IndexWriter(index, config);
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		refCountMap = new HashMap<String, Integer>(1);
		refCountMap.put("inc", 1);
		
		labelMap = new HashMap<String, String>(1);
		labelMap.put("add", null);
		
		
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
