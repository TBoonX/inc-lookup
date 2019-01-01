package dbpedia.lookup.indexing;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Hello world!
 *
 */
public class App {

	public static void main( String[] args ) 
	{
		
		Logger logger = LoggerFactory.getLogger(App.class);
		
		final String[] labelUris = new String[] { "http://www.w3.org/2000/01/rdf-schema#label", "http://xmlns.com/foaf/0.1/name" };
		/*
		final String dataSetQueryString =
				"select distinct ?dl where { "
						+	"?s a <http://dataid.dbpedia.org/ns/core#Dataset>. "
						+	"?s <http://www.w3.org/ns/dcat#distribution> ?dist. "
						+	"?dist <http://www.w3.org/ns/dcat#downloadURL> ?dl."
						+ 	"FILTER regex(?dist, 'enwiki'). }";
		
		final String endPointUrl = "https://databus.dbpedia.org/repo/sparql";
		*/
		
		try {
			
			
			
			final ILookupIndexer indexer = new LuceneLookupIndexer("tmp", 100000); //solrUrl, coreName, 20000);
			
			if(!indexer.clearIndex()) {
				return;
			}
			
			
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
						
						boolean isLabel = false;
						
						for(int i = 0; i < labelUris.length; i++) {
							if(predicateURI.equals(labelUris[i])) {
								isLabel = true;
								break;
							}
						}
					
						if(isLabel && arg0.getObject().isLiteral()) {
														
							indexer.addLabel(arg0.getSubject().getURI(),  arg0.getObject().getLiteralValue().toString());
												
						} else if(arg0.getObject().isURI()) {
							
							indexer.increaseRefCount(arg0.getObject().getURI());
						}
					
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					
				}
			};
			
			/*
			 * Test Dataset Construction:
			 * 
			 * DATASET 2
				CONSTRUCT { ?s ?p ?o } {
				 ?s ?p ?o. ?s <http://www.w3.org/2000/01/rdf-schema#label> ?o. ?s a dbo:Place.
				} LIMIT 50000
			 *
			 * DATASET 1 
				CONSTRUCT { ?s ?p ?o } {
				 ?s ?p ?o. ?o a dbo:Place.
				} LIMIT 100000

			 * 
			 */
				
			// DataSetQuery dataSetQuery = new DataSetQuery(endPointUrl, dataSetQueryString);
			
			// String[] downloadLinks = dataSetQuery.queryDownloadLinks();
			
			String[] testDataPaths = new String[] { "resources/data1.ttl", "resources/data2.ttl" };
			
			for(String link : testDataPaths) {

				logger.debug(">>>>> Reading from " + link);

				// Skip stupid TQL, only accept ttl
				//if(!link.endsWith(".ttl.bz2")) {
				//	continue;
				//}

				BufferedInputStream bzipIn = new BufferedInputStream(new FileInputStream(new File(link)));
				//BufferedInputStream in = new BufferedInputStream(new URL(link).openStream());

				//BZip2CompressorInputStream bzipIn = new BZip2CompressorInputStream(in);

				try 
				{
					RDFParser.create()
					.source(bzipIn)
					.lang(RDFLanguages.TTL)
					.parse(inputHandler);	
					


				} catch(RiotException e) {
					e.printStackTrace();
				}
			}
			

			indexer.commit();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Done.");

	}
}
