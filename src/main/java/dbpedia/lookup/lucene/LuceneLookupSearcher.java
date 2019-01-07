package dbpedia.lookup.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.lucene.document.Document;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.DoubleValuesSource;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONObject;

import dbpedia.lookup.indexing.ILookupSearcher;
import dbpedia.lookup.server.ResultFormat;

/**
 * Constructs queries and runs searches on the lucene index
 * @author Jan Forberg
 *
 */
public class LuceneLookupSearcher implements ILookupSearcher {
	
	public static final String FIELD_DOCUMENTS = "docs";
	
	public static final String FIELD_REFCOUNT = "refCount";
	
	public static final String FIELD_RESOURCE = "resource";
	
	public static final String FIELD_LABEL = "label";
	
	public static final String FIELD_DESCRIPTION = "description";
	
	private float exactMatchBoost = 5.0f;
	
	private int fuzzyPrefixLength = 2;
	
	private int fuzzyEditDistance = 1;
	
	private IndexSearcher searcher;

	private DoubleValuesSource refCountValueSource;

	/**
	 * Creates a new lookup searcher with default parameters
	 * @param filePath The file path of the index
	 * @throws IOException
	 * @throws java.text.ParseException
	 */
	public LuceneLookupSearcher(String filePath) throws IOException, java.text.ParseException {
		this(filePath, 0.1f, 5.0f, 2, 1);
	}
	
	/**
	 * Creates a new lookup searcher
	 * @param filePath The file path of the index
	 * @param refCountBoost The boost factor for the refCount field
	 * @param exactMatchBoost The boost factor for exact matches
	 * @param fuzzyPrefixLength The prefix length for fuzzy search
	 * @param fuzzyEditDistance The max edit distance for fuzzy search
	 * @throws IOException
	 * @throws java.text.ParseException
	 */
	public LuceneLookupSearcher(String filePath, float refCountBoost, float exactMatchBoost, int fuzzyPrefixLength, int fuzzyEditDistance) 
			throws IOException, java.text.ParseException {
		
		this.exactMatchBoost = exactMatchBoost;
		this.fuzzyEditDistance = fuzzyEditDistance;
		this.fuzzyPrefixLength = fuzzyPrefixLength;
		
		Directory index = FSDirectory.open(new File(filePath).toPath());
		IndexReader reader = DirectoryReader.open(index);
		
		this.searcher = new IndexSearcher(reader);
		
	
		Expression expression = JavascriptCompiler.compile(FIELD_REFCOUNT + " * " + refCountBoost + " + 1");
		SimpleBindings bindings = new SimpleBindings();
		bindings.add(FIELD_REFCOUNT, DoubleValuesSource.fromLongField(FIELD_REFCOUNT));
		
		this.refCountValueSource = expression.getDoubleValuesSource(bindings);
	}

	/**
	 * Searches the index based on a given query
	 * @param queryString The query string
	 * @param maxHits The maximum amount of search results
	 * @return The search results as a string
	 * @throws IOException
	 */
	public String search(String queryString, int maxHits) throws IOException {
		
		Term term = new Term(FIELD_LABEL, queryString.toLowerCase());
		Query fuzzyQuery = new FuzzyQuery(term, fuzzyEditDistance, fuzzyPrefixLength);
		Query termQuery = new BoostQuery(new TermQuery(term), exactMatchBoost);

			
		BooleanQuery booleanQuery = new BooleanQuery.Builder()
				.add(fuzzyQuery, Occur.SHOULD)
				.add(termQuery, Occur.SHOULD)
				.setMinimumNumberShouldMatch(1)
				.build();
		
		FunctionScoreQuery scoreQuery = FunctionScoreQuery.boostByValue(booleanQuery, refCountValueSource);
				
		return runQuery(scoreQuery, maxHits, ResultFormat.JSON);
	}
	
	/**
	 * Searches the index using a prefix query for auto suggestions
	 * @param queryString The query string
	 * @param maxHits THe maximum amount of search results
	 * @return The search results as a string
	 * @throws IOException
	 */
	public String suggest(String queryString, int maxHits) throws IOException {
		
		Term term = new Term(FIELD_LABEL, queryString.toLowerCase());
		Query fuzzyQuery = new PrefixQuery(term);
		Query termQuery = new BoostQuery(new TermQuery(term), exactMatchBoost);
			
		BooleanQuery booleanQuery = new BooleanQuery.Builder()
				.add(fuzzyQuery, Occur.SHOULD)
				.add(termQuery, Occur.SHOULD)
				.setMinimumNumberShouldMatch(1)
				.build();
		
		FunctionScoreQuery scoreQuery = FunctionScoreQuery.boostByValue(booleanQuery, refCountValueSource);
				
		return runQuery(scoreQuery, maxHits, ResultFormat.JSON);
	}
	
	/**
	 * Runs a query and returns the results as a formatted String
	 * @param query
	 * @param maxHits
	 * @return
	 * @throws IOException
	 */
	private String runQuery(Query query, int maxHits, ResultFormat format) throws IOException {
		
		TopDocs docs = searcher.search(query, maxHits);
		
		ScoreDoc[] hits = docs.scoreDocs;
		JSONObject returnResults = new JSONObject();
		
		ArrayList<Object> resultList = new ArrayList<Object>();
		   
		for(int i = 0; i < hits.length; i++) {

			int docId = hits[i].doc;
			Document document = searcher.doc(docId);
			
			resultList.add(parseResult(document));
		}
		
		returnResults.put(FIELD_DOCUMENTS, resultList);
		return returnResults.toString();
	}
	
	/**
	 * Creates a string-value map from a document
	 * @param document The document
	 * @return The string-value map
	 * @throws IOException
	 */
	private HashMap<String, Object> parseResult(Document document) throws IOException {
		
		HashMap<String, Object> documentMap = new HashMap<String, Object>();
		
		ArrayList<String> labels = new ArrayList<String>();
		
		for(IndexableField f : document.getFields(FIELD_LABEL)) {
			labels.add(f.stringValue());			    	
		}
		
		documentMap.put(FIELD_LABEL, labels);
		
		IndexableField refCountField = document.getField(FIELD_REFCOUNT);
		
		if(refCountField != null) {
			documentMap.put(FIELD_REFCOUNT, refCountField.numericValue());
		}
   
		IndexableField resourceField = document.getField(FIELD_RESOURCE);
		
		if(resourceField != null) {
			documentMap.put(FIELD_RESOURCE, resourceField.stringValue());
		}
		
		return documentMap;
	}
	
	
	
}
