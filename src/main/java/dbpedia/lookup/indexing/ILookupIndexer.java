package dbpedia.lookup.indexing;

public interface ILookupIndexer {

	
	void addLabel(String resource, String label);
	
	void increaseRefCount(String resource);
	
	void commit();
	
	boolean clearIndex();
}
