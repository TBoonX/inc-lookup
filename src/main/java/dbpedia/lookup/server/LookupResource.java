package dbpedia.lookup.server;

import java.io.IOException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;

import org.apache.solr.client.solrj.SolrServerException;

import dbpedia.lookup.search.ILookupSearcher;
import dbpedia.lookup.search.SolrLookupSearcher;

@Path( "search" )
public class LookupResource
{
	
	@GET
	@Produces( MediaType.APPLICATION_JSON )
	public String message(@QueryParam("query") String query, @Context Providers providers)
	{
		ContextResolver<ILookupSearcher> resolver = providers.getContextResolver(ILookupSearcher.class,  MediaType.WILDCARD_TYPE);
		ILookupSearcher searcher = resolver.getContext(ILookupSearcher.class);
		
		try {
			
			return searcher.search(query);
			
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "ERR";
	}
}