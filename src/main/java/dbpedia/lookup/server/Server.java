package dbpedia.lookup.server;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.Scanner;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.sun.net.httpserver.HttpServer;

import dbpedia.lookup.indexing.ILookupSearcher;
import dbpedia.lookup.lucene.LuceneLookupSearcher;


public class Server 
{
    private static String exitCode = "exit";

	public static void main( String[] args ) throws IOException, ParseException
    {
    	ILookupSearcher searcher = new LuceneLookupSearcher("tmp");  // new SolrLookupSearcher(solrUrl, coreName);
        
    	ResourceConfig rc = new ResourceConfig()
			.packages( "dbpedia.lookup.server" )
			.registerInstances(new LookupSearcherResolver(searcher));
    	
    	System.out.println("Starting Lookup Service..");
    	HttpServer server = JdkHttpServerFactory.createHttpServer(URI.create( "http://localhost:8080/api" ), rc);
    	System.out.println("Lookup Service running.");
    	
    	Scanner scan = new Scanner(System.in);
    	
    	while(true) {
    		
    		
    		String input = scan.nextLine();
    		
    		
    		if(input.equals(exitCode)) {
    			System.out.println("Stopping Lookup Service..");
    			break;
    		} else {
    			System.out.println("Unknown command '" + input + "'.");
    		}
    	}
    	
    	scan.close();
		
		server.stop( 0 );
		System.out.println("Lookup Service stopped.");
	}
}
