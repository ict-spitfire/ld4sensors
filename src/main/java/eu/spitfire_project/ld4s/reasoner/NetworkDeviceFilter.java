package eu.spitfire_project.ld4s.reasoner;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

import eu.spitfire_project.ld4s.dataset.TDBManager;
import eu.spitfire_project.ld4s.resource.LD4SDataResource;
import eu.spitfire_project.ld4s.resource.actuator_decision.ActuatorDecision;
import eu.spitfire_project.ld4s.vocabulary.SptCtVocab;
import eu.spitfire_project.ld4s.vocabulary.SptSnVocab;
import eu.spitfire_project.ld4s.vocabulary.SptVocab;

/**
 * Class that filters out the Network Devices whose readings are not of interest
 * based on both the client input (received via the API) and the ontology
 *    
 * @author iammyr
 *
 */
public class NetworkDeviceFilter extends ReasonerManager{

	/**
	 * Filter triples according to the ontolog, cutom rules defined in a specified text file, 
	 * and and hard-coded rules.
	 * 
	 * @param obj specific use case which the settings must be applied to
	 * @param base model whose triples have to be filtered according to the settings.
	 * @param ruleFilePath path of the text file containing the custom rules 
	 * @return model created by merging the descriptions of all the filtered network devices 
	 */
	public static Model applyFilter(ActuatorDecision obj, 
			String namedGraphUri, String datasetFolderPath){
		String actuatorProp = obj.getActuatorProperty();
		if (actuatorProp == null || datasetFolderPath == null){
			return null;
		}
		Model ret = ModelFactory.createDefaultModel();
		
		

		//1. filter-in sensors whose observedProp shares the same applicationDomain as the actuatorProp
		//1.A. identify the applicationDomain the actuatorProp is propertyOf
		//1.B. identify the subjects (sensors) whose observed property is propertyOf 
		//the same applicationDomain as the one for actuatorProp  	
		String selectBase = "select ?sensor ?obsProp where {" 
				+ "graph "
				;
		if (namedGraphUri != null){
			selectBase += " <"+namedGraphUri+"> ";
		}else{
			selectBase += " ?graph ";
		}
		String whereBase = 
				" <" +actuatorProp+"> <"+SptCtVocab.APPLICATION_DOMAIN_P.getURI()+"> ?appDomain ." +
				"?sensor <"+SptVocab.OBSERVED_PROPERTY.getURI()+"> ?obsProp ." 
				+ "?obsProp <"+SptCtVocab.APPLICATION_DOMAIN_P.getURI()+"> ?appDomain ." 
				;
		//2. filter-in sensors whose observedProperty shares the same 
		//target as the actuatorProp (e.g. People, as inherited by the application domain's target) 
		String whereBase1 = "<" +actuatorProp+"> <"+SptCtVocab.TARGET.getURI()+"> ?target ." +
				"?sensor <"+SptVocab.OBSERVED_PROPERTY.getURI()+"> ?obsProp ." +
				"?obsProp <"+SptCtVocab.TARGET.getURI()+"> ?target ." ;


		String queryString = selectBase + "{{"+whereBase +"}"
//		+ " union {"+whereBase1+"}" 
				+ "} }"
		;
		ResultSet results = TDBManager.search(queryString, datasetFolderPath);

		QuerySolution qs = null;
		String sensorUri = null;
		while ( results.hasNext() ) {
			qs = results.next();     
			sensorUri = qs.getResource( "sensor" ).asResource().getURI();
			System.out.println("\n\n\nsensoruri = "+sensorUri+" ; obsprop = "+qs.getResource( "obsProp" ).asResource().getURI());
			ret.add(TDBManager.open(sensorUri, namedGraphUri, datasetFolderPath));
		}



		//3. if any of the actuatorProp's targets is a Person then filter-in smartphone devices too
		whereBase = "?sensor <"+SptVocab.OBSERVED_PROPERTY.getURI()+"> ?obsProp ." +
				"?obsProp <"+SptCtVocab.TARGET.getURI()+"> <"+FOAF.Person.getURI()+"> .";
		queryString = selectBase +"{"+ whereBase+"}}limit 1";
		results = TDBManager.search(queryString, datasetFolderPath);
		if (results.hasNext()){
			whereBase = "?sensor a <"+SptSnVocab.SMARTPHONE.getURI()+"> .";
			queryString = selectBase +"{"+ whereBase + "}}";
			results = TDBManager.search(queryString, datasetFolderPath);
			while ( results.hasNext() ) {
				qs = results.next();     
				sensorUri = qs.getResource( "sensor" ).asResource().getURI();
				ret.add(TDBManager.open(sensorUri, namedGraphUri, datasetFolderPath));
			}
		}
		return ret;

	}



}
