package dbpedia.lookup.indexing;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.json.JSONArray;
import org.json.JSONObject;
import dbpedia.lookup.lucene.LuceneLookupIndexer;



public class App {

	private static final String CFG_KEY_INDEX = "index";

	private static final String CFG_KEY_DATA = "data";

	private static final String CFG_KEY_COMMIT_INTERVAL = "commitInterval";

	private static final String CFG_KEY_CACHE_SIZE = "cacheSize";

	private static final String CFG_KEY_DATA_QUERY = "dataQuery";

	private static final String CFG_KEY_ENDPOINT_URL = "endPointUrl";

	private static final String CFG_KEY_PATH = "path";

	private static final String CFG_KEY_FIELDS = "fields";

	private static final String CFG_KEY_NAME = "name";

	private static final String CFG_KEY_URIS = "uris";

	private static ArrayList<IndexResource> indexResources;

	public static void main( String[] args ) throws IOException
	{

		String configString = new String(Files.readAllBytes(Paths.get("resources/index_config.json")));
		JSONObject config = new JSONObject(configString);
		JSONObject indexConfig = config.getJSONObject(CFG_KEY_INDEX);
		JSONObject dataConfig = config.getJSONObject(CFG_KEY_DATA);

		StringBuilder sb = new StringBuilder();
		JSONArray queryArray = dataConfig.getJSONArray(CFG_KEY_DATA_QUERY);

		for(int i = 0; i < queryArray.length(); i++) {
			sb.append(queryArray.get(i));
		}

		String dataSetQueryString = sb.toString();
		String endPointUrl = dataConfig.getString(CFG_KEY_ENDPOINT_URL);

		String indexPath = indexConfig.getString(CFG_KEY_PATH);
		int commitInterval = indexConfig.getInt(CFG_KEY_COMMIT_INTERVAL);
		int cacheSize = indexConfig.getInt(CFG_KEY_CACHE_SIZE);
		JSONArray indexFields = indexConfig.getJSONArray(CFG_KEY_FIELDS);

		System.out.println(dataSetQueryString);

		indexResources = new ArrayList<IndexResource>();

		for(int i = 0; i < indexFields.length(); i++) {

			JSONObject field = indexFields.getJSONObject(i);
			JSONArray uris = field.getJSONArray(CFG_KEY_URIS);

			IndexResource indexResource = new IndexResource();
			indexResource.key = field.getString(CFG_KEY_NAME);
			indexResource.uris = new String[uris.length()];

			for(int j = 0; j < uris.length(); j++) {
				indexResource.uris[j] = uris.getString(j);
			}

			indexResources.add(indexResource);
		}


		try {

			final ILookupIndexer indexer = new LuceneLookupIndexer(indexPath, commitInterval, cacheSize);

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

						IndexResource tripleIndexResource = null;

						if(arg0.getObject().isLiteral()) {
							for(int i = 0; i < indexResources.size(); i++) {

								IndexResource indexResource = indexResources.get(i);

								for(int j = 0; j < indexResource.uris.length; j++) {

									if(predicateURI.equals(indexResource.uris[j])) {
										tripleIndexResource = indexResource;
										break;
									}
								}
							}
						}

						if(tripleIndexResource != null) {

							indexer.indexField(tripleIndexResource.key, arg0.getSubject().getURI(),  arg0.getObject().getLiteralValue().toString());

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
