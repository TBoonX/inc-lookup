package dbpedia.lookup.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONObject;

public class LuceneLookupSearcher implements ILookupSearcher {

	 
	

	private IndexSearcher searcher;
	
	private QueryParser queryParser;
	
	private double precision = 0.7;

	public LuceneLookupSearcher(String filePath) {
		
		StandardAnalyzer analyzer = new StandardAnalyzer();
		
		try {
			
			Directory index = FSDirectory.open(new File(filePath).toPath());
			
			IndexReader reader = DirectoryReader.open(index);
			
			searcher = new IndexSearcher(reader);
			queryParser = new QueryParser("label", analyzer);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}


	public String search(String query) throws SolrServerException, IOException {
		
		StringBuilder sb = new StringBuilder();
		
		
		try {
			TopDocs docs = searcher.search(queryParser.parse(query + "~" + precision), 1000);
		
			ScoreDoc[] hits = docs.scoreDocs;
			JSONObject returnResults = new JSONObject();
			HashMap<String, Object> solrDocMap = new HashMap<String, Object>();
			
			   
			for(int i = 0; i < hits.length; i++) {

			    HashMap<String, Object> innerDocMap = new HashMap<String, Object>();
				
			    int docId = hits[i].doc;
			    Document d = searcher.doc(docId);
			 
			    ArrayList<String> labels = new ArrayList<String>();
			    for(IndexableField f : d.getFields("label")) {
			    	labels.add(f.stringValue());			    	
			    }
			    
			    IndexableField refCountField = d.getField("refCount");
			    
			    if(refCountField != null) {
			    	innerDocMap.put("refCount", refCountField.numericValue());
			    }
			   
			    for(IndexableField f : d.getFields("label")) {
			    	labels.add(f.stringValue());			    	
			    }
			    
			    
			    innerDocMap.put("labels", labels);
			    
				solrDocMap.put(d.getField("resource").stringValue(), innerDocMap);
			    
			}
			
			returnResults.put("docs", solrDocMap);
			return returnResults.toString();
			
		} catch (ParseException e) {
			return null;
		}
	}
	
	
}
