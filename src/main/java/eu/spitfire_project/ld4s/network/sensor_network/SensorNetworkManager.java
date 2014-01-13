package eu.spitfire_project.ld4s.network.sensor_network;

import java.util.HashMap;

import org.openjena.atlas.logging.Log;
import org.ws4d.coap.messages.CoapRequestCode;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import eu.spitfire_project.ld4s.dataset.TDBManager;
import eu.spitfire_project.ld4s.network.NetworkAddress;
import eu.spitfire_project.ld4s.network.NetworkManager;
import eu.spitfire_project.ld4s.reasoner.NetworkDeviceFilter;
import eu.spitfire_project.ld4s.resource.actuator_decision.ActuatorDecision;
import eu.spitfire_project.ld4s.vocabulary.SptVocab;

public class SensorNetworkManager extends NetworkManager {

	public SensorNetworkManager(String baseUri, String dirpath) {
		super(baseUri, dirpath);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void getSourceDescription(NetworkAddress addr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sourceFiltering(ActuatorDecision obj, NetworkDeviceFilter settings) {
		// TODO Auto-generated method stub

	}

	/**
	 * resourceToQueryPath is one of SensorChoice.
	 * port for CoAP-enabled devices usually is 5683
	 * milliseconds are the milliseconds that should pass before the latest reading is pulled again
	 * 
	 *  ToDo: still unclear when to stop the latest reading pulling and what to do with the readings
	 */
	@Override
	protected String sensorLatestReadingCollector(NetworkAddress naddr, int port, String resourceToQueryPath) {
		QueryForwarder queryfwd = new QueryForwarder(CoapRequestCode.GET, 
				resourceToQueryPath, naddr, port);
		String reading = null;
		
		System.out.println("Collecting the latest sensor reading: Please wait...");
		queryfwd.start();
		try {
			queryfwd.join();
		} catch (InterruptedException e) {
			System.err.println("Join interrupted.");
			e.printStackTrace();
		}
		
		
		if (queryfwd.status() == QueryForwarder.FAIL){
			System.err.println("Latest Reading Collection Failed. :(");
		}else{
			System.out.println("Latest Reading successfully collected! :))");
			reading = queryfwd.getLatestReading();
		}



		//		         while (<condition does not hold>)
		//		             queryfwd.wait();
		//		      // Perform action appropriate to condition
		//		     }

		return reading;
	}

	public HashMap<Resource, String> getSensorsLatestReading(Model filteredSensors, String namedGraph, String datasetFolderPath, int port){
		HashMap<Resource, String> ret = null;
		String queryString = "select distinct ?s ?gateaddr ?localaddr ?obsprop where {graph <"+namedGraph+"> { ?s <"+SptVocab.IP_DEVICE.getURI()+"> ?localaddr ;" +
				"<"+SptVocab.IP_GATE+"> ?gateaddr ;" +
				"<"+SptVocab.OBSERVED_PROPERTY+"> ?obsprop .}}";
		ResultSet results = TDBManager.search(queryString, datasetFolderPath);

		QuerySolution qs = null; 
		Resource rs = null;
		Literal rs1 = null;
		String localaddr = null, gateaddr = null, obsprop = null;
		NetworkAddress naddr = null;
		String latestReading = null;
		String coapResourcePath = null;
		Resource source = null;
		while ( results.hasNext() ) {
			qs = results.next();     
			rs1 = qs.getLiteral( "localaddr" );
			if (rs1 != null){
				localaddr = rs1.asLiteral().toString();
				System.out.println("?localaddr="+localaddr);
			}
			rs1 = qs.getLiteral( "gateaddr" );
			if (rs1 != null){
				gateaddr = rs1.asLiteral().toString();
				System.out.println("?gateAddr="+gateaddr);
			}
			rs = qs.getResource( "obsprop" );
			if (rs != null){
				obsprop = rs.asResource().getURI();
				System.out.println("?obsprop="+obsprop);
			}
			rs = qs.getResource( "s" );
			if (rs != null){
				source = rs;
				System.out.println("?source="+source);
			}
			System.out.println("");

			naddr = new NetworkAddress(gateaddr, localaddr);
			//4.1 fwd a coap request proper for the specific observed property
			coapResourcePath = getResourceToQueryPath(obsprop);
			if (coapResourcePath != null){
				latestReading = sensorLatestReadingCollector(naddr, port, coapResourcePath);
				if (latestReading == null){
					System.err.println("Could not get any latest reading from the sensor "+naddr.toString());
				}else{
					if (ret == null){
						ret = new HashMap<Resource, String>();
					}
					ret.put(source, latestReading);
				}
			}

		}

		return ret;
	}



}
