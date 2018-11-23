package dbpedia.lookup.search;

import javax.ws.rs.ext.ContextResolver;

public class LookupSearcherResolver implements ContextResolver<LookupSearcher> {

	private LookupSearcher searcher;
	
	public LookupSearcherResolver(LookupSearcher searcher) {
		this.searcher = searcher;
		
	}
	public LookupSearcher getContext(Class<?> arg0) {
		return searcher;
	}	
	
}
