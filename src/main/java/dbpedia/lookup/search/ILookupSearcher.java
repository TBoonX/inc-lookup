package dbpedia.lookup.search;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;

public interface ILookupSearcher {

	String search(String query, int maxHits) throws SolrServerException, ParseException, IOException; 
}
