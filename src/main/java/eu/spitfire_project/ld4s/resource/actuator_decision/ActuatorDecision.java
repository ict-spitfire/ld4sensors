package eu.spitfire_project.ld4s.resource.actuator_decision;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.spitfire_project.ld4s.lod_cloud.Context;
import eu.spitfire_project.ld4s.resource.LD4SDataResource;
import eu.spitfire_project.ld4s.resource.LD4SObject;
import eu.spitfire_project.ld4s.vocabulary.LD4SConstants;

public class ActuatorDecision extends LD4SObject  implements Serializable{

	/** Property that is the focus of an Actuator. **/
	private String actuatorProperty = null;

	/** Application Domain of an Actuator. **/
	private String applicationDomain = null;

	/** Optional actions that the actuator can apply on the property of focus. **/
	private String[] decisionOptions = null;
	
	/** Latitude for the actuator */
	private String latitude = null;
	
	/** Longitude for the actuator */
	private String longitude = null;


	/**
	 * 
	 */
	private static final long serialVersionUID = 5222281425211809256L;

	public ActuatorDecision(JSONObject json) throws JSONException {
		super(json);
		if (json.has("actuator"+LD4SConstants.JSON_SEPARATOR+"property")){
			this.setActuatorProperty(LD4SDataResource.removeBrackets(
					json.getString("actuator"+LD4SConstants.JSON_SEPARATOR+"property")));
		}
		if (json.has("application"+LD4SConstants.JSON_SEPARATOR+"domain")){
			this.setApplicationDomain(LD4SDataResource.removeBrackets(
					json.getString("application"+LD4SConstants.JSON_SEPARATOR+"domain")));
		}
		if (json.has("latitude")){
			this.setLatitude(LD4SDataResource.removeBrackets(
					json.getString("latitude")));
		}
		if (json.has("longitude")){
			this.setLongitude(LD4SDataResource.removeBrackets(
					json.getString("longitude")));
		}
		if (json.has("decision"+LD4SConstants.JSON_SEPARATOR+"options")){
			this.setDecisionOptions(
					json.getJSONArray("decision"+LD4SConstants.JSON_SEPARATOR+"options"));
		}
	}

	@Override
	protected void initDefaultType() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initAcceptedTypes() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getRemote_uri() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRemote_uri(String resourceHost) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStoredRemotely(boolean storedRemotely) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isStoredRemotely() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStoredRemotely(String localUri) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setLink_criteria(Context link_criteria) {
		// TODO Auto-generated method stub

	}

	@Override
	public Context getLink_criteria() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLink_criteria(String link_criteria, String localhost)
			throws Exception {
		// TODO Auto-generated method stub

	}

	public String getActuatorProperty() {
		return this.actuatorProperty;
	}

	public String getApplicationDomain() {
		return applicationDomain;
	}

	public void setApplicationDomain(String applicationDomain) {
		this.applicationDomain = applicationDomain;
	}
	
	public void setDecisionOptions (JSONArray jvalues) throws JSONException {
			String[] values = new String[jvalues.length()];
			for (int i=0; i< jvalues.length(); i++){
				values[i] = jvalues.get(i).toString();
			}
			setDecisionOptions(values);
	}

	public String[] getDecisionOptions() {
		return decisionOptions;
	}

	public void setDecisionOptions(String[] decisionOptions) {
		this.decisionOptions = decisionOptions;
	}

	public void setActuatorProperty(String actuatorProperty) {
		this.actuatorProperty = actuatorProperty;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}



}
