package dbpedia.inc_lookup;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.apache.solr.client.solrj.SolrServerException;
import org.json.*;


/**
 * Hello world!
 *
 */
public class App {

	public static void main( String[] args ) 
	{
		final String sameAsUri = "http://www.w3.org/2000/01/rdf-schema#label";
		final String dataSetQueryString =
				"select distinct ?dl where { "
						+	"?s a <http://dataid.dbpedia.org/ns/core#Dataset>. "
						+	"?s <http://www.w3.org/ns/dcat#distribution> ?dist. "
						+	"?dist <http://www.w3.org/ns/dcat#downloadURL> ?dl."
						+ 	"  FILTER regex(?dist, 'label'). FILTER regex(?dist, 'enwiki'). }";
		
		final String endPointUrl = "https://databus.dbpedia.org/repo/sparql";
		final String solrUrl = "http://localhost:8983/solr/";
		final String coreName = "fusion-labels";
		
		
		
		try {
			
			
			
			final Indexer indexer = new Indexer(solrUrl, coreName, 1000);
			
			
			StreamRDF inputHandler = new StreamRDF() {
				
				public void start() {
					// TODO Auto-generated method stub

				}

				public void quad(Quad arg0) {
					// TODO Auto-generated method stub

				}

				public void prefix(String arg0, String arg1) {
					// TODO Auto-generated method stub

				}

				public void finish() {
					// TODO Auto-generated method stub

				}

				public void base(String arg0) {
					// TODO Auto-generated method stub

				}

				// Process a triple
				public void triple(Triple arg0) {
					
					// Retrieve the predicate URI
					String predicateURI = arg0.getPredicate().getURI();
					
					try {
					
						if(predicateURI.equals(sameAsUri) && arg0.getObject().isLiteral()) {
														
							indexer.addLabel(arg0.getSubject().getURI(),  arg0.getObject().getLiteralValue().toString());
												
						} else {
							
							indexer.increaseRefCount(arg0.getObject().getURI());
						}
					
					} catch (SolrServerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
					
					
				}
			};
				
			DataSetQuery dataSetQuery = new DataSetQuery(endPointUrl, dataSetQueryString);
			
			String[] downloadLinks = dataSetQuery.queryDownloadLinks();
		
			
			for(String link : downloadLinks) {

				System.out.println(">>>>> Reading from " + link);

				// Skip stupid TQL, only accept ttl
				if(!link.endsWith(".ttl.bz2")) {
					continue;
				}

				BufferedInputStream in = new BufferedInputStream(new URL(link).openStream());

				BZip2CompressorInputStream bzipIn = new BZip2CompressorInputStream(in);

				try 
				{
					RDFParser.create()
					.source(bzipIn)
					.lang(RDFLanguages.TTL)
					.parse(inputHandler);	
					

					indexer.commit();

				} catch(RiotException e) {
					e.printStackTrace();
				} catch (SolrServerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Done.");

	}
}
