package eu.spitfire_project.ld4s.resource.type;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;

import com.hp.hpl.jena.ontology.OntClass;

import eu.spitfire_project.ld4s.lod_cloud.Context;
import eu.spitfire_project.ld4s.resource.LD4SDataResource;
import eu.spitfire_project.ld4s.resource.LD4SObject;
import eu.spitfire_project.ld4s.vocabulary.LD4SConstants;
import eu.spitfire_project.ld4s.vocabulary.SptSnVocab;
import eu.spitfire_project.ld4s.vocabulary.SsnVocab;

/**
 * Sensing Device resource.
 * This resource is usually stored on the Sensor and transmitted rarely.
 * 
<10e2073a01080063> a spt-sn:WS ;
clf:bn <http://ex.org> ;
clf:bt "12-06-22T17:00Z" ;
spt:uom qudt:unit/Abampere ;
spt:obs <electricCurrentInstance112> ;
spt:out <obval11204id> .

 * @author Myriam Leggieri <iammyr@email.com>
 *
 */
public class Type extends LD4SObject  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8845385924519981423L;
	
	/** Temporarily (to enhance the link search): Feature of Interest. */
	private String foi = null;

	/** Unit of Measurement. */
	private String unit_of_measurement = null;

	/** Observed Property. */
	private String observed_property = null;

	/** Measurement Capability URIs. */
	private String[] meas_capabs = null;

	private String base_name;

	private String base_ov_name;

	private String[] tsproperties;


	public Type(String host, String uom,
			String foi, String observed_prop, 
			String[] capabilities) 
	throws Exception{
		super();
		this.setRemote_uri(host);
		this.setUnit_of_measurement(uom);
		this.setObserved_property(observed_prop);
		this.setFoi(foi);
		this.setMeasurementCapabilities(capabilities);
	}

	

	public Type(JSONObject json, String localhost) throws Exception {
		super(json);
		if (json.has("uom")){
			this.setUnit_of_measurement(LD4SDataResource.removeBrackets(
					json.getString("uom")));
		}
		if (json.has("observed"+LD4SConstants.JSON_SEPARATOR+"property")){
			this.setObserved_property(LD4SDataResource.removeBrackets(
					json.getString("observed"+LD4SConstants.JSON_SEPARATOR+"property")));
		}
		if (json.has("foi")){
			this.setFoi(LD4SDataResource.removeBrackets(
					json.getString("foi")));
		}
		if (json.has("measurement"+LD4SConstants.JSON_SEPARATOR+"capabilities")){
			this.setMeasurementCapabilities(json.getJSONArray("measurement"+LD4SConstants.JSON_SEPARATOR+"capabilities"));
		}
		if (json.has("context")){
			this.setLink_criteria(json.getString("context"), localhost);
		}
	}


	public Type (Form form, String localhost) throws Exception {
		super(form);
		this.setValues(form.getValuesArray("observation"+LD4SConstants.JSON_SEPARATOR+"values"));
		this.setTsproperties(form.getValuesArray("tsproperties"));
		this.setRemote_uri(form.getFirstValue("uri")); 
		this.setUnit_of_measurement(
				form.getFirstValue("uom"));
		this.setBase_name(
				form.getFirstValue("base"+LD4SConstants.JSON_SEPARATOR+"name"));
		this.setType(
				form.getFirstValue("type"));
		this.setBase_ov_name(
				form.getFirstValue("base"+LD4SConstants.JSON_SEPARATOR+"ov"+LD4SConstants.JSON_SEPARATOR+"name"));
		this.setObserved_property(
				form.getFirstValue("observed"+LD4SConstants.JSON_SEPARATOR+"property"));
		this.setLink_criteria(
				form.getFirstValue("context"), localhost);
	}

	private void setValues(String[] valuesArray) {
		// TODO Auto-generated method stub
		
	}



	private void setMeasurementCapabilities(JSONArray jsonArray) throws JSONException {
		String[] values = new String[jsonArray.length()];
		for (int i=0; i< jsonArray.length(); i++){
			values[i] = jsonArray.get(i).toString();
		}
		setMeasurementCapabilities(values);
	}
	
	private void setMeasurementCapabilities(String[] capabilities) {
		this.meas_capabs = capabilities;		
	}


	@Override
	public void setStoredRemotely(boolean storedRemotely) {
		this.stored_remotely = storedRemotely;		
	}

	@Override
	public boolean isStoredRemotely() {
		return this.stored_remotely;
	}

	@Override
	public boolean isStoredRemotely(String localUri) {
		if (getRemote_uri() == null
				||
				(localUri.contains(getRemote_uri())
						|| getRemote_uri().contains(localUri))){
			return false;
		}
		return true;
	}

	@Override
	public void setLink_criteria(Context link_criteria) {
		this.link_criteria = link_criteria;
	}

	@Override
	public void setLink_criteria(String link_criteria, String localhost) throws Exception {
		this.link_criteria = new Context(link_criteria, localhost);
	}

	@Override
	public Context getLink_criteria() {
		return this.link_criteria;
	}

	public void setUnit_of_measurement(String unit_of_measurement) {
		this.unit_of_measurement = unit_of_measurement;
	}

	public String getUnit_of_measurement() {
		return unit_of_measurement;
	}

	/**
	 * Search for an external observed property resource uri 
	 * @param observed_property
	 */
	public void setObserved_property(String observed_property) {
		this.observed_property = observed_property;
	}

	/**
	 * @return Observed Property URI
	 */
	public String getObserved_property() {
		return observed_property;
	}

	public void setBase_name(String base_name) {
		this.base_name = base_name;
	}

	public String getBase_name() {
		return base_name;
	}

	public void setBase_ov_name(String base_ov_name) {
		this.base_ov_name = base_ov_name;
	}

	public String getBase_ov_name() {
		return base_ov_name;
	}

	public void setTsproperties(String[] tsproperties) {
		this.tsproperties = tsproperties;
	}
	
	public void setTsproperties(JSONArray jvalues) throws JSONException {
		String[] values = new String[jvalues.length()];
		for (int i=0; i< jvalues.length(); i++){
			values[i] = jvalues.get(i).toString();
		}
		setTsproperties(values);
	}

	public String[] getTsproperties() {
		return tsproperties;
	}

	@Override
	protected void initAcceptedTypes() {
		this.setAcceptedTypes(new OntClass[]{
				SptSnVocab.ACTUATOR, SptSnVocab.TRANSDUCER,
				SptSnVocab.ACCELEROMETER,
				SptSnVocab.GPS,
				SptSnVocab.HUMIDITY_SENSOR,
				SptSnVocab.LIGHT_SENSOR,
				SptSnVocab.MOTION_SENSOR,
				SsnVocab.SENSING_DEVICE
		});
	}

	@Override
	protected void initDefaultType() {
		this.defaultType = SsnVocab.DEVICE;
	}

	public void setFoi(String foi) {
		this.foi = foi;
	}

	public String getFoi() {
		return foi;
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



	public String[] getValues() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
