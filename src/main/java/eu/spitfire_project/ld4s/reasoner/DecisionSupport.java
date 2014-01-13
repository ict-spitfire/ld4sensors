package eu.spitfire_project.ld4s.reasoner;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import eu.spitfire_project.ld4s.dataset.TDBManager;
import eu.spitfire_project.ld4s.vocabulary.LD4SConstants;
import eu.spitfire_project.ld4s.vocabulary.SptCtVocab;
import eu.spitfire_project.ld4s.vocabulary.SptVocab;

/**
 * Class that selects a decision according to the custom rules and the readings collected from
 * the (filtered list of) network devices.   
 * @author iammyr
 *
 */
public class DecisionSupport extends ReasonerManager {

	public enum PirValues{
		PIR_MOTION(1), PIR_NO_MOTION(0);
		private int value = 0;
		
		private PirValues(int value){
			this.value = value;
		}
		
		public int getValue(){
			return this.value;
		}
	}
	
	/**
	 * Given the client input and custom rules, select the best decision with some confidence
	 * 
	 * @param obj 
	 * @param namedGraphUri
	 * @param datasetFolderPath
	 * @return
	 */
	public static Model getChoice(String namedGraphUri,
			String datasetFolderPath) {
		if (datasetFolderPath == null || namedGraphUri == null){
			return null;
		}
		Model ret = ModelFactory.createDefaultModel();
		String decisionUri = namedGraphUri+"_decision";
		Resource subject =  ret.createResource(decisionUri);
		ret.add(subject, RDF.type, SptCtVocab.ACTUATOR_DECISION);

		//1. for each collected reading
		//1.a get the sensor producing it
		//1.b get the observed property of such sensor  	
		String selectBase = "select ?sensor ?obsprop ?value ?time  where {graph ";
		selectBase += " <"+namedGraphUri+"> {";
		String whereBase = " ?ov <"+RDF.type.getURI()+"> <"+SptVocab.OV.getURI()+"> ;" +
				"<"+SptVocab.OUT_OF.getURI()+"> ?sensor ;"+
				"<"+SptVocab.HAS_VALUE.getURI()+"> ?value ;" +
				"<"+SptVocab.TIME+"> ?time ."+
				"?sensor <"+SptVocab.OBSERVED_PROPERTY.getURI()+"> ?obsprop ." 
//				+ "?choice <"+RDF.type+"> <"+SptCtVocab.ACTUATOR_CHOICE.getURI()+"> ."
				;


		String queryString = selectBase + whereBase + "}}";
		ResultSet results = TDBManager.search(queryString, datasetFolderPath);

		//1.c put it in an hashmap<observed-property,<sensor,[reading,time]>>
		QuerySolution qs = null;
		String sensor = null, obsprop = null, value = null, time = null;
//		LinkedList<String> choices = new LinkedList<String>();
		HashMap<String, HashMap<String, String[]>> obsprop2sensor2value_time = 
				new HashMap<String, HashMap<String,String[]>>();
		HashMap<String, String[]> sensor2value_time = null;
		while ( results.hasNext() ) {
			qs = results.next();     
			sensor = qs.getResource( "sensor" ).asResource().getURI();
			obsprop = qs.getResource("obsprop").asResource().getURI();
			value = qs.getLiteral("value").getString();
			time = qs.getLiteral("time").getString();
//			choices.add(qs.getResource("choice").asResource().getURI());
			if (obsprop2sensor2value_time.containsKey(obsprop)){
				sensor2value_time = obsprop2sensor2value_time.get(obsprop);
				sensor2value_time.put(sensor, new String[]{value, time});
			}else{
				sensor2value_time = new HashMap<String, String[]>();
				sensor2value_time.put(sensor, new String[]{value, time});
				obsprop2sensor2value_time.put(obsprop, sensor2value_time);
			}
		}
		//2. for each hashmap key
		String sensoruri = null;
		for (String prop : obsprop2sensor2value_time.keySet()){
			//2.a count the total amount of readings available
			sensor2value_time = obsprop2sensor2value_time.get(prop);
			Iterator<String> it = sensor2value_time.keySet().iterator();
			if (it.hasNext()){
				sensoruri = it.next();
			}			
			//2.b do something
			switch(prop){
			case LD4SConstants.URI_PIR:
			case LD4SConstants.URI_PIR1:

				//-->2.b.1 get the amount of areas
				selectBase = "select ?totareas ?purpose where {graph ";
				selectBase += " <"+namedGraphUri+"> {";
				//@todo: check the location when filtering in/out sensors
				//@todo: add triple for choice options
				whereBase = "<"+sensoruri+"> <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#hasLocation> ?loc ." +
						"?loc <"+SptVocab.TOT_AREAS.getURI()+"> ?totareas ;" +
						"<"+SptCtVocab.PURPOSE_P.getURI()+"> ?purpose .";

				queryString = selectBase + whereBase + "}} limit 1";
				results = TDBManager.search(queryString, datasetFolderPath);

				qs = null;
				int totareas = 0;
				String purpose = null;
				if ( results.hasNext() ) {
					qs = results.next();     
					totareas = Integer.valueOf(qs.getLiteral( "totareas" ).asLiteral().getString());
					purpose = qs.getResource( "purpose" ).asResource().getURI();
				}
				//2.b.2 get whether the majority of readings detected presence or absence
				selectBase = "select (count(?ov1) as ?tot_presence) (count(?ov2) as ?tot_absence) where {graph ";
				selectBase += " <"+namedGraphUri+"> {";
				whereBase = "{?ov1 <"+SptCtVocab.DETECTS.getURI()+"> <"+SptCtVocab.PRESENCE.getURI()+"> .}" 
						+ "union {?ov2 <"+SptCtVocab.DETECTS.getURI()+"> <"+SptCtVocab.ABSENCE.getURI()+"> .}"
						;

				queryString = selectBase + whereBase + "}}";
				results = TDBManager.search(queryString, datasetFolderPath);

				qs = null;
				int presenceReadings = 0, absenceReadings = 0;
				if ( results.hasNext() ) {
					qs = results.next();     
					presenceReadings = Integer.valueOf(qs.getLiteral( "tot_presence" ).asLiteral().getString());
					absenceReadings = Integer.valueOf(qs.getLiteral( "tot_absence" ).asLiteral().getString());
//					String ov1 = qs.getResource( "ov1" ).asResource().getURI();
				}
				
				//if the amount of readings is greater than half the 
				//areas in the room
				if (sensor2value_time.values().size() > (totareas/2)){
					//and
					//the room purpose is relax
					if(purpose.compareTo(SptCtVocab.RELAX.getURI()) == 0){
						//and time is in the morning or evening or absence detected then
						if (isItMorningNow() || isItEveningNow()
								|| absenceReadings > presenceReadings){
							//choice is low
							ret.add(subject,
									SptCtVocab.DECISION,
									SptCtVocab.LOW);
						}else if (presenceReadings > absenceReadings){
							//if the majority of readings detect presence then
							//choice is middle
							ret.add(subject,
									SptCtVocab.DECISION,
									SptCtVocab.MEDIUM);
						}
					}else if (presenceReadings > absenceReadings){ 
						//if the room purpose is not realx and the majority 
						//of readings detect presence  
						//then choice is high 
						ret.add(subject,
								SptCtVocab.DECISION,
								SptCtVocab.HIGH);
					}else{ //or else choice is low 
						ret.add(subject,
								SptCtVocab.DECISION,
								SptCtVocab.LOW);
					}
				}else{
					//not enough information --> choice is low
					ret.add(subject,
							SptCtVocab.DECISION,
							SptCtVocab.LOW);
				}
				break;
			case LD4SConstants.URI_LIGHT:
				break;
			default:

			}
		}

		//4. return the ActuatorDecisionSupport resources which will now include 
		//the confidence level for each decision option		
		return ret;
	}

	private static boolean isItMorningNow(){
		Calendar calendar = new GregorianCalendar();
		int hour = calendar.get(Calendar.HOUR);
		return (hour >= 6 && calendar.get(Calendar.AM_PM) == 0 && hour < 12);	
	}

	private static boolean isItAfternoonNow(){
		Calendar calendar = new GregorianCalendar();
		int hour = calendar.get(Calendar.HOUR);
		return (hour >= 12 && calendar.get(Calendar.AM_PM) == 1 && hour < 7);	
	}

	private static boolean isItEveningNow(){
		Calendar calendar = new GregorianCalendar();
		int hour = calendar.get(Calendar.HOUR);
		return (hour >= 7 && calendar.get(Calendar.AM_PM) == 1 && hour <= 11);	
	}

	private static boolean isItNightNow(){
		Calendar calendar = new GregorianCalendar();
		int hour = calendar.get(Calendar.HOUR);
		return (hour >= 12 && calendar.get(Calendar.AM_PM) == 0 && hour < 6);	
	}



}
