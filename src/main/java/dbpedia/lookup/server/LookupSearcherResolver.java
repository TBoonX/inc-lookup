package dbpedia.lookup.server;

import javax.ws.rs.ext.ContextResolver;

import dbpedia.lookup.indexing.ILookupSearcher;

public class LookupSearcherResolver implements ContextResolver<ILookupSearcher> {

	private ILookupSearcher searcher;
	
	public LookupSearcherResolver(ILookupSearcher searcher) {
		this.searcher = searcher;
		
	}
	public ILookupSearcher getContext(Class<?> arg0) {
		return searcher;
	}	
	
}
