package dbpedia.lookup.indexing;

import java.io.IOException;

public interface ILookupIndexer {

	void indexField(String key, String resource, String text) throws IOException;
	
	void increaseRefCount(String resource);
	
	void commit();
	
	boolean clearIndex();
}
