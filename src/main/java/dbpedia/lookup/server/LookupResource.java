package dbpedia.lookup.server;

import java.io.IOException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;

import dbpedia.lookup.search.ILookupSearcher;

@Path( "search" )
public class LookupResource
{
	
	@GET
	@Produces( MediaType.APPLICATION_JSON )
	public String message(@QueryParam("query") String query, @QueryParam("maxHits") int maxHits, @Context Providers providers)
	{
		ContextResolver<ILookupSearcher> resolver = providers.getContextResolver(ILookupSearcher.class,  MediaType.WILDCARD_TYPE);
		ILookupSearcher searcher = resolver.getContext(ILookupSearcher.class);
		
		try {
			
			if(maxHits == 0) {
				maxHits = 1000;
			}
			
			return searcher.search(query, maxHits);
			
		} catch (SolrServerException e) {
			return e.getMessage();
		} catch (IOException e) {
			return e.getMessage();
		} catch (ParseException e) {
			return e.getMessage();
		}
		
	}
}