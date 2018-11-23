package dbpedia.lookup.indexing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
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
		
		Document doc = new Document();
		doc.add(new TextField("lbal", label, Field.Store.YES));
				
		try {
			writer.updateDocument(new Term("resource", resource), doc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
		update();		
	}
	
	public void increaseRefCount(String resource) {
		
		
		update();		
	}
	
	public void commit() {
		
		
		try {
			writer.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void update() {
		
		updateCount++;
		
		if(updateCount >= updateInterval) {
			
			commit();
			
		}
	}

	public boolean clearIndex() {
		
		try {
			writer.deleteAll();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
}
