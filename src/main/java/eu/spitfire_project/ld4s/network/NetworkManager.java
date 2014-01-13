package eu.spitfire_project.ld4s.network;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

import org.openjena.atlas.logging.Log;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.FileManager;

import eu.spitfire_project.ld4s.reasoner.NetworkDeviceFilter;
import eu.spitfire_project.ld4s.resource.actuator_decision.ActuatorDecision;
import eu.spitfire_project.ld4s.vocabulary.LD4SConstants;
import eu.spitfire_project.ld4s.vocabulary.SptVocab;

public abstract class NetworkManager {

	/** List of network device descriptions to store. */ 
	/** Temporarily hard-coded RDF file paths until a broadcasted request for descriptions is implemented. */
	protected String[] descriptionFilePaths = null;

	private String namedGraphUri = null; 

	private HashMap<String, String> obsprop2coapcode = null;

	/**
	 * Scan the network for collecting RDF descriptions from each surrounding neighbours
	 * 
	 * @return Model where the collected RDF descriptions are merged 
	 */
	public Model sourceDiscovery(){
		if (descriptionFilePaths == null){
			return null;
		}
		
		Model ret = ModelFactory.createDefaultModel();

		for (String filename : descriptionFilePaths){
				ret.add(FileManager.get().loadModel(filename));
		}
		return ret;
	}

	/**
	 * Map the observed property (or one of its seeAlso links) to the correct
	 * CoAP resource path (e.g., "/light").
	 * @param rdf
	 * @return CoAP resource path
	 */
	public String getResourceToQueryPath(String observedProperty){
		String ret = null;
		if (obsprop2coapcode.containsKey(observedProperty)){
			ret = obsprop2coapcode.get(observedProperty);
		}else{
			//dereference the observed property and check whether it's linked with any of 
			//the observed property uris supported by the mapping
			Model downloadedRdf = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			if (isUriAccessible(observedProperty)) {
				//					downloadedRdf.read(observedProperty, "RDF/XML");
				downloadedRdf.read(observedProperty);

				NodeIterator nit = downloadedRdf.listObjectsOfProperty(SptVocab.SEE_ALSO_LINK);
				RDFNode current = null;
				while (nit.hasNext() && ret == null){
					current = nit.next();
					observedProperty = current.asResource().getURI();
					if (obsprop2coapcode.containsKey(observedProperty)){
						ret = obsprop2coapcode.get(observedProperty);
					}
				}
				if (ret == null){
					nit = downloadedRdf.listObjectsOfProperty(SptVocab.SAME_AS_LINK);
					while (nit.hasNext() && ret == null){
						current = nit.next();
						observedProperty = current.asResource().getURI();
						if (obsprop2coapcode.containsKey(observedProperty)){
							ret = obsprop2coapcode.get(observedProperty);
						}
					}	
				}
			}
		}
		if (ret == null){
			Log.warn(this, "Warning: CoAP Path Code for the Observed Property "+observedProperty+" was not found.");
			System.err.println("Warning:  CoAP Path Code for the Observed Property "+observedProperty+" was not found.");
		}
		return ret;
	}

	private static boolean isUriAccessible(String uri) {
		HttpURLConnection connection = null;
		int code = -1;
		URL myurl;
		try {
			myurl = new URL(uri);

			connection = (HttpURLConnection) myurl.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(1000);
			code = connection.getResponseCode();
		} catch (MalformedURLException e) {
			System.err.println(uri + " is not accessible.");
		} catch (ProtocolException e) {
			System.err.println(uri + " is not accessible.");
		} catch (IOException e) {
			System.err.println(uri + " is not accessible.");
		}
		return (code == 200) ? true : false;
	}

	private boolean loadDescriptions(String dirpath){
		File dir  = new File (dirpath);
		boolean ret = dir.exists();

		if (ret){
			String[] names = dir.list();
			this.descriptionFilePaths = new String[names.length];
			int count = 0;
			for (int i=0 ;i<names.length ;i++){
				if (!names[i].endsWith("~")){ //skip the shadow-files
					this.descriptionFilePaths[count++] = dirpath+"/"+names[i];
				}
			}
		}

		return ret;		
	}

	public abstract void getSourceDescription(NetworkAddress addr);

	public abstract void sourceFiltering(ActuatorDecision obj, NetworkDeviceFilter settings);


	public NetworkManager(String baseUri, String descriptionsPath){
		//load the description files from the specified directory
		if (!loadDescriptions(descriptionsPath)){
			System.out.println("the directory "+descriptionsPath+" does not exist");
		}

		initObservedPropertyToCoapCode();

		if (baseUri.matches(".://.")){
			if (!baseUri.endsWith("/")){
				baseUri += "/";
			}
		}else{
			baseUri = "http://www.example.com/";
		}
		namedGraphUri = baseUri + UUID.randomUUID().toString(); ;
	}

	private void initObservedPropertyToCoapCode() {
		obsprop2coapcode = new HashMap<String, String>();
		obsprop2coapcode.put(LD4SConstants.URI_LIGHT, "/light");
		obsprop2coapcode.put(LD4SConstants.URI_TEMP, "/temperature");
		obsprop2coapcode.put(LD4SConstants.URI_ACCELEROMETER, "/accelerometer");
		obsprop2coapcode.put(LD4SConstants.URI_PIR1, "/pirSensor");
		obsprop2coapcode.put(LD4SConstants.URI_PIR, "/pirSensor");
	}

	public String getNamedGraphUri() {
		return namedGraphUri;
	}

	protected abstract String sensorLatestReadingCollector(NetworkAddress naddr, int port,
			String resourceToQueryPath) ;


}
