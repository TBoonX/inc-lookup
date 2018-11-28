package dbpedia.lookup.search;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;

public interface ILookupSearcher {

	String search(String query) throws SolrServerException, IOException; 
}
