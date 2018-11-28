package dbpedia.lookup.indexing;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class LookupLuceneIndexer implements ILookupIndexer {

	private HashMap<String, Integer> refCountMap;

	private HashMap<String, String> labelMap;
	
	private String coreName;
	
	private int updateCount;
	
	private int updateInterval;

	private IndexWriter writer;

	private IndexSearcher searcher;

	private QueryParser queryParser;
	
	private HashMap<String, Document> documentCache;

	private FSDirectory index;

	private String filePath;

	private IndexWriterConfig config;

	private StandardAnalyzer analyzer;

	public LookupLuceneIndexer(String filePath, int updateInterval) {
		
		this.filePath = filePath;
		
		documentCache = new HashMap<String, Document>();
		analyzer = new StandardAnalyzer();
		
		try {

			index = FSDirectory.open(new File(filePath).toPath());
			config = new IndexWriterConfig(analyzer);
			
			writer = new IndexWriter(index, config);
			
			IndexReader reader = DirectoryReader.open(index);
			
			
			searcher = new IndexSearcher(reader);
			queryParser = new QueryParser("resource", analyzer);
			
		
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		
		refCountMap = new HashMap<String, Integer>(1);
		refCountMap.put("inc", 1);
		
		labelMap = new HashMap<String, String>(1);
		labelMap.put("add", null);
		
		
		this.updateInterval = updateInterval;
	
		
	}
	
	public void addLabel(String resource, String label) {
		
		try {
			
			// TODO: try to pool this
			Term term = new Term("resource", resource);
			
			Document doc = findDocument(resource, term);
			
			if(resource.equals("http://dbpedia.org/resource/Brad_Pitt")) {
				System.out.println("BRAD labels: " + doc.getFields("label").length);
			}
			
			doc.add(new TextField("label", label, Field.Store.YES));
			
			writer.updateDocument(new Term("resource", resource), doc);
						
			update();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Document findDocument(String resource, Term term) throws IOException {
		
		// First try: search the cache
		if(documentCache.containsKey(resource)) {
			
			Document doc = documentCache.get(resource);
			if(resource.equals("http://dbpedia.org/resource/Brad_Pitt")) {
				System.out.println("BRAD from cache");
				System.out.println("REF: " + doc.getField("refCount").numericValue());
				System.out.println("LABELS: " + doc.getFields("label").length);
			}
			
			
			return documentCache.get(resource);
		}		

		// Second try: search the index
		TopDocs docs = searcher.search(new TermQuery(term), 1);
		
		if(docs.totalHits > 0) {
			
			Document doc = searcher.doc(docs.scoreDocs[0].doc);
			
			
			if(resource.equals("http://dbpedia.org/resource/Brad_Pitt")) {
				System.out.println("BRAD from LUCENE");
				System.out.println("LABELS: " + doc.getFields("label").length);
			}
			
			
			documentCache.put(resource, doc);
			return doc;
		}
	
		if(resource.equals("http://dbpedia.org/resource/Brad_Pitt")) {
			System.out.println("BRAND NEW BRAD");
		}
		
		// Third try: create new document, add to cache
		Document doc = new Document();
		doc.add(new StringField("resource", resource, Field.Store.YES));
			
		documentCache.put(resource, doc);
	
		return doc;
	}
	
	public void increaseRefCount(String resource) {
		
		int refCount = 0;
		
		try {
			
			
			
			Term term = new Term("resource", resource);
			
			Document doc = findDocument(resource, term);
			
			IndexableField refCountField = doc.getField("refCount");
			
			if(refCountField != null) {
				refCount = (Integer) refCountField.numericValue();
				
				
			}
		
			doc.removeFields("refCount");
			
			doc.add(new IntPoint("refCount", refCount + 1));
			doc.add(new StoredField("refCount", refCount + 1));
			
			writer.updateDocument(new Term("resource", resource), doc);
						
			update();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void commit() {
		
		System.out.println("Removing " + documentCache.size() + " documents from cache");
		// documentCache.clear();
		try {
			
			writer.commit();
			writer.close();
			
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
					
			index = FSDirectory.open(new File(filePath).toPath());
			
			writer = new IndexWriter(index, config);
						
			IndexReader reader = DirectoryReader.open(index);
			searcher = new IndexSearcher(reader);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void update() {
		
		updateCount++;
		
		if(updateCount >= updateInterval) {
			
			updateCount = 0;
			
			commit();
			
		}
	}

	public boolean clearIndex() {
		
		try {
			writer.deleteAll();
			commit();
			return true;
		} catch(IndexNotFoundException e) {
	
		return true;
	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	}
		
		
		return false;
	}
}
