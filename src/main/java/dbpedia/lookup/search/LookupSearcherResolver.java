package dbpedia.lookup.search;

import javax.ws.rs.ext.ContextResolver;

public class LookupSearcherResolver implements ContextResolver<ILookupSearcher> {

	private ILookupSearcher searcher;
	
	public LookupSearcherResolver(ILookupSearcher searcher) {
		this.searcher = searcher;
		
	}
	public ILookupSearcher getContext(Class<?> arg0) {
		return searcher;
	}	
	
}
