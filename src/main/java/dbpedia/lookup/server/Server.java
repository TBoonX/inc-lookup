package dbpedia.lookup.server;

import java.io.IOException;
import java.net.URI;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.sun.net.httpserver.HttpServer;

import dbpedia.lookup.search.SolrLookupSearcher;
import dbpedia.lookup.search.ILookupSearcher;
import dbpedia.lookup.search.LookupSearcherResolver;
import dbpedia.lookup.search.LuceneLookupSearcher;


public class Server 
{
    private static String exitCode = "exit";

	public static void main( String[] args ) throws IOException
    {
    	final String solrUrl = "http://localhost:8983/solr/";
		final String coreName = "fusion-labels";
		
    	ILookupSearcher searcher = new LuceneLookupSearcher("tmp");  // new SolrLookupSearcher(solrUrl, coreName);
        
    	ResourceConfig rc = new ResourceConfig()
			.packages( "dbpedia.lookup.server" )
			.registerInstances(new LookupSearcherResolver(searcher));
    	
    	System.out.println("Starting Lookup Service..");
    	HttpServer server = JdkHttpServerFactory.createHttpServer( 
	    URI.create( "http://localhost:8080/api" ), rc );
    	System.out.println("Lookup Service running.");
    	
    	
    	while(true) {
    		
    		
    		Scanner scan = new Scanner(System.in);
    		String input = scan.nextLine();
    		
    		
    		if(input.equals(exitCode)) {
    			System.out.println("Stopping Lookup Service..");
    			break;
    		} else {
    			System.out.println("Unknown command '" + input + "'.");
    		}
    	}
    	
		server.stop( 0 );
		System.out.println("Lookup Service stopped.");
		
    }
}
