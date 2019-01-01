package dbpedia.lookup.indexing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import dbpedia.lookup.search.LuceneLookupSearcher;

public class LuceneLookupIndexer implements ILookupIndexer {

	private int updateCount;
	
	private int updateInterval;

	private IndexWriter writer;

	private IndexSearcher searcher;

	private HashMap<String, Document> documentCache;

	private FSDirectory index;

	private String filePath;

	private IndexWriterConfig config;

	private StandardAnalyzer analyzer;

	public LuceneLookupIndexer(String filePath, int updateInterval) {
		
		this.filePath = filePath;
		this.updateInterval = updateInterval;
		
		documentCache = new HashMap<String, Document>();
		analyzer = new StandardAnalyzer();
		
		try {

			index = FSDirectory.open(Paths.get(filePath));
			config = new IndexWriterConfig(analyzer);
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			
			writer = new IndexWriter(index, config);
			
			searcher = new IndexSearcher(DirectoryReader.open(index));
			
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void addLabel(String resource, String label) {
		
		try {
			
			Document doc = findDocument(resource);
			
			doc.add(new TextField(LuceneLookupSearcher.FIELD_LABEL, label, Field.Store.YES));
			
			writer.updateDocument(new Term(LuceneLookupSearcher.FIELD_RESOURCE, resource), doc);
						
			update();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void increaseRefCount(String resource) {
		
		long refCount = 0;
		
		try {
			
			Document doc = findDocument(resource);
			
			IndexableField refCountField = doc.getField(LuceneLookupSearcher.FIELD_REFCOUNT);
			
			if(refCountField != null) {
				refCount = (Long)refCountField.numericValue();
			}
		
			doc.removeFields(LuceneLookupSearcher.FIELD_REFCOUNT);
			refCount++;
			
			doc.add(new NumericDocValuesField(LuceneLookupSearcher.FIELD_REFCOUNT, refCount));
			doc.add(new StoredField(LuceneLookupSearcher.FIELD_REFCOUNT, refCount));
			
			writer.updateDocument(new Term("resource", resource), doc);
						
			update();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	private Document findDocument(String resource) throws IOException {
		
		// First try: search the cache
		if(documentCache.containsKey(resource)) {
			
			return documentCache.get(resource);
		}		

		// Second try: search the index
		Document document = getDocumentFromIndex(resource);
		
		if(document != null) {
			documentCache.put(resource, document);
			return document;
		}
	
		// Third try: create new document, add to cache
		Document doc = new Document();
		doc.add(new StringField(LuceneLookupSearcher.FIELD_RESOURCE, resource, Field.Store.YES));
			
		documentCache.put(resource, doc);
	
		return doc;
	}

	private Document getDocumentFromIndex(String resource) throws IOException {
		
		TopDocs docs = searcher.search(new TermQuery(new Term(LuceneLookupSearcher.FIELD_RESOURCE, resource)), 1);
		
		if(docs.totalHits > 0) {
			
			Document document = searcher.doc(docs.scoreDocs[0].doc);
			
			// Reset the queried "resource" field to STORED
			document.removeField("resource");
			document.add(new StringField("resource", resource, Field.Store.YES));
			
			return document;
		}
		
		return null;
	}
	
	public void commit() {
		
		System.out.println("=== COMMITING ===");
		System.out.println("Removing " + documentCache.size() + " documents from cache");
		documentCache.clear();
		
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
			e.printStackTrace();
		}
		
		return false;
	}
}
