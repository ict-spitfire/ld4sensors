package eu.spitfire_project.ld4s.resource.actuator_decision;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;

import eu.spitfire_project.ld4s.dataset.TDBManager;
import eu.spitfire_project.ld4s.network.sensor_network.SensorNetworkManager;
import eu.spitfire_project.ld4s.reasoner.DecisionSupport;
import eu.spitfire_project.ld4s.reasoner.NetworkDeviceFilter;
import eu.spitfire_project.ld4s.reasoner.ReasonerManager;
import eu.spitfire_project.ld4s.resource.LD4SApiInterface;
import eu.spitfire_project.ld4s.resource.LD4SDataResource;
import eu.spitfire_project.ld4s.server.ServerProperties;

public class ActuatorDecisionResource extends LD4SActuatorDecisionResource implements LD4SApiInterface{
	/** Service resource name. */
	protected String resourceName = "Actuator Decision";

	/** RDF Data Model of this Service resource semantic annotation. */
	protected Model rdfData = null;

	/** Resource provided by this Service resource. */
	protected ActuatorDecision ov = null;

	protected int port = 5683;


	@Override
	@Get
	public Representation get() {
		if (resourceId == null || resourceId.trim().compareTo("") == 0){
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Please request only a resource stored in this LD4S TDB");
			return null;
		}

		Representation ret = null;
		logger.fine(resourceName + " as Linked Data: Starting");

		try {
			//check cache
			//get all the resource information from the Triple DB
			logger.fine(resourceName + " LD4S: Requesting data");
			logRequest(resourceName, resourceId);

			rdfData = retrieve(this.uristr, this.namedModel);

			ret = serializeAccordingToReqMediaType(rdfData);
		}
		catch (Exception e) {
			setStatusError("Error creating " + resourceName + "  LD4S.", e);
			ret = null;
		}

		logger.info("REQUEST "+ this.uristr +" PROCESSING END - "+LD4SDataResource.getCurrentTime()); return ret;
	}

	@Override
	@Put
	public Representation put(Form obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Put
	public Representation put(JSONObject obj) {
		return null;
	}

	@Override
	@Post
	public Representation post(Form obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Post
	public Representation post(JSONObject obj) {
		Representation ret = null;

		logger.fine(resourceName + " LD4S: Now updating.");
		String currentDecisionUri = null;
		try {
			this.ov = new ActuatorDecision(obj);

			//1. scan the network (for now, use an hard-coded list of addresses --> NetworkManager)
			//for network devices' rdf descriptions
			String sensorsStaticDescriptions = this.ld4sServer.getServerProperties()
					.get(ServerProperties.SENSORS_RDF_DIR_KEY);
			System.out.println("STATIC DESCRIPTIONS PATH="+sensorsStaticDescriptions);
			SensorNetworkManager netman = new SensorNetworkManager(this.ld4sServer.getUri(),
					sensorsStaticDescriptions);
			Model descriptions = netman.sourceDiscovery();

			//2. store the descriptions locally applying the inference
			//Testing: inference of 
			//location (sensor02 nearby dericafe)
			//type (sensor02 type motiondev; sensor01 type noisedev)
			//property target (volume target people; noise target people)
			currentDecisionUri = netman.getNamedGraphUri();
			Reasoner customReasoner = ReasonerManager.createReasoner(getRuleFilePath());
			String datasetFolderPath = getDatasetFolderPath();
			if (!TDBManager.store(descriptions, customReasoner, 
					currentDecisionUri, datasetFolderPath)){
				logger.severe("Unable to store the RDF descriptions " +
						"collected from the network in the local TDB");
			}
//			LD4SDataResource.printDataset("select ?s {graph <"+currentDecisionUri+"> {?s ?p ?o}}", getDatasetFolderPath());
			
			Model actuatorInputModel = getActuatorInputAsRdf(this.ov, currentDecisionUri);
			if (!TDBManager.store(actuatorInputModel, customReasoner, 
					currentDecisionUri, datasetFolderPath)){
				logger.severe("Unable to store the RDF descriptions " +
						"collected from the network in the local TDB");
			}
			
			//3. filter out the network devices whose triples do not match certain criteria
			Model filteredSensors = NetworkDeviceFilter.applyFilter(this.ov, 
					currentDecisionUri, datasetFolderPath);

			//4. get the latest readings
			//for each address in the model
			HashMap<Resource, String> source2Reading = null;
			if ((source2Reading = netman.getSensorsLatestReading(filteredSensors, currentDecisionUri, getDatasetFolderPath(), port)) == null){
				setStatus(Status.SERVER_ERROR_INTERNAL, "Could not collect any sensor reading necessary to start the decision support procedure.");
				return null;
			}
			String latestReading = null;
			for (Resource res : source2Reading.keySet()){
				latestReading = source2Reading.get(res);
				//5. store the reading under this-decision-labelled named graph
				//Testing: inference of
				//presence/absence
				Model rdfObservedValue = getLatestReadingAsRdf(latestReading, res);
				TDBManager.store(rdfObservedValue, 
						customReasoner, 
						currentDecisionUri, datasetFolderPath);

			}
			
			
			//6. consult the rule set based on the coapResourcePath and all the collected 
			//readings, in order to select a choice
			rdfData = DecisionSupport.getChoice(currentDecisionUri, datasetFolderPath);
			//rdfData = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			//rdfData = makeOVLinkedData().getModel();

			//store the final decision only for archival purposes
			if (!TDBManager.store(rdfData, null, currentDecisionUri, datasetFolderPath)){
				logger.fine("Unable to store the final decision in the Triple DB");
			}
			setStatus(Status.SUCCESS_OK);
			ret = serializeAccordingToReqMediaType(rdfData);

		} catch (JSONException e) {
			e.printStackTrace();
			setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(Status.SERVER_ERROR_INTERNAL, "Unable to instantiate the requested resource\n"
					+e.getMessage());
			return null;
		}


		logger.info("REQUEST "+ this.uristr +" PROCESSING END - "+LD4SDataResource.getCurrentTime()); return ret;
	}

	@Override
	@Delete
	public void remove() {
		// TODO Auto-generated method stub

	}

}
