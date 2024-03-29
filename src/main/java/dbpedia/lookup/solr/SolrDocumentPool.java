package dbpedia.lookup.solr;

import org.apache.solr.common.SolrInputDocument;

public class SolrDocumentPool {
	
	private int instanceIndex;
	
	private SolrInputDocument[] documents;

	public SolrDocumentPool(int size) {
		
		documents = new SolrInputDocument[size];
		
		for(int i = 0; i < documents.length; i++) {
			documents[i] = new SolrInputDocument();
		}
	}
	
	public SolrInputDocument get() {
		return documents[instanceIndex++];
	}
	
	public void reset() {
		instanceIndex = 0;
		for(int i = 0; i < documents.length; i++) {
			documents[i].clear();
		}
	}
}
