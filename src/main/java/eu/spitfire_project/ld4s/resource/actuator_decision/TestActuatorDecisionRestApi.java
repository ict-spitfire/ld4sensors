package eu.spitfire_project.ld4s.resource.actuator_decision;

import static org.junit.Assert.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import eu.spitfire_project.ld4s.test.LD4STestHelper;
import eu.spitfire_project.ld4s.vocabulary.LD4SConstants;

public class TestActuatorDecisionRestApi extends LD4STestHelper{

	/** JSONObject containing the above data. */
	protected JSONObject json = null;

	/** Observed Property. */
	protected String observed_property = "powerconsumption" ; 
	//		"http://www.example3.org/prop/temperature12";
	//		"area";
	//		"temperature";
	//			"light";

	/** Actuator Property. */
	protected String actuator_property = "http://spitfire-project.eu/ontology/ns/ct/Volume" ; 

	/** Application Domain. */
	protected String application_domain = "http://spitfire-project.eu/ontology/ns/ct/Music" ;

	/** Latitude. */
	protected String latitude = "8.3432" ; 

	/** Longitude. */
	protected String longitude = "0.4243" ; 

	/** LD4S currently running server host. */
	protected String local_uri = "http://localhost:8182/ld4s/actuator/decision";
//			"http://myrdebby.myrdebby.com:8182/ld4s/device/";
	//			"http://spitfire-project.eu:8182/ld4s/device/";


	private void initJson(){
		this.json = new JSONObject();
		try {
			json.append("actuator"+LD4SConstants.JSON_SEPARATOR+"property", actuator_property);
			json.append("application"+LD4SConstants.JSON_SEPARATOR+"domain", application_domain);
			json.append("latitude", latitude);
			json.append("longitude", longitude);


			JSONArray vals = new JSONArray();
			vals.put("high");
			vals.put("middle");
			vals.put("low");
			json.append("decision"+LD4SConstants.JSON_SEPARATOR+"options", vals);

		} catch (JSONException e1) {
			e1.printStackTrace();
		}
	}


	private void setup(){
		initJson();
		
	}
	/**
	 * Test POST {host}/actuator/decision
	 * requirement: none
	 *
	 * @throws Exception If problems occur.
	 */
	@Test
	public void testJSONPostRemoteResource() throws Exception {
		System.out.println("Test POST - JSON payload");
		setup();
		System.out.println(json.toString());		 
		ClientResource cr = new ClientResource(local_uri);
		//ChallengeResponse authentication = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, user, 
		//user_password);
		//cr.setChallengeResponse(authentication);
		Representation resp = cr.post(json);
		Status status = cr.getStatus();
		System.out.println(status.getCode()+ " - "+cr.getStatus().getDescription());            
		assertTrue(status.isSuccess());

		String rdf = resp.getText();
		System.out.println("\n\n\n==============\nTesting ACTUATOR DECISION JSON POST \n"
				+ "sent : "+json
				+local_uri+"==============\n"+rdf);
	}

}
