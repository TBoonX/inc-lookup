package dbpedia.lookup.server;

import java.net.URI;

import javax.swing.JOptionPane;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.sun.net.httpserver.HttpServer;

import dbpedia.lookup.search.LookupSearcher;
import dbpedia.lookup.search.LookupSearcherResolver;


public class Server 
{
    public static void main( String[] args )
    {
    	final String solrUrl = "http://localhost:8983/solr/";
		final String coreName = "fusion-labels";
		
    	LookupSearcher searcher = new LookupSearcher(solrUrl, coreName);
        
    	ResourceConfig rc = new ResourceConfig()
			.packages( "dbpedia.lookup.server" )
			.registerInstances(new LookupSearcherResolver(searcher));
    	
    	HttpServer server = JdkHttpServerFactory.createHttpServer( 
	    URI.create( "http://localhost:8080/api" ), rc );
    	JOptionPane.showMessageDialog( null, "Ende" );
		server.stop( 0 );
    }
}
