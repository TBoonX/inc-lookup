package dbpedia.lookup.server;

import java.net.URI;

import javax.swing.JOptionPane;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.sun.net.httpserver.HttpServer;


public class Server 
{
    public static void main( String[] args )
    {
        
    	ResourceConfig rc = new ResourceConfig().packages( "lookup.lookup_client" );
    	HttpServer server = JdkHttpServerFactory.createHttpServer( 
	    URI.create( "http://localhost:8080/api" ), rc );
    	JOptionPane.showMessageDialog( null, "Ende" );
		server.stop( 0 );
    }
}
