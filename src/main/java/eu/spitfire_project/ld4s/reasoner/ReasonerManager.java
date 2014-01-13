package eu.spitfire_project.ld4s.reasoner;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasonerFactory;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;

public class ReasonerManager {

	private static Model ontologyBaseModel = null;

	private static final String SPT_SOURCE = "http://spitfire-project.eu/ontology.owl";

	private static final String SPTSN_SOURCE = "http://spitfire-project.eu/sn.owl";

	private static final String SPTCT_SOURCE = "http://spitfire-project.eu/ct.owl";



	public static Reasoner createReasoner(String ruleFilePath){
		Reasoner reasoner = null;
		if (ruleFilePath != null){
			ontologyBaseModel = getOntologyBase();

			Model m = ModelFactory.createDefaultModel();
			Resource configuration = m.createResource();
			configuration.addProperty(ReasonerVocabulary.PROPruleMode, "hybrid");
			configuration.addProperty(ReasonerVocabulary.PROPruleSet, ruleFilePath);


			reasoner= GenericRuleReasonerFactory.theInstance().create(configuration).bindSchema(ontologyBaseModel);
		}
		return reasoner;
	}

	private static Model getOntologyBase(){
		if (ontologyBaseModel == null) {
			ontologyBaseModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			if (isUriAccessible(SPT_SOURCE)) {
//				ontologyBaseModel.read(SPT_SOURCE, "RDF/XML");
				ontologyBaseModel.read(SPT_SOURCE, "N3");
			}else{
				ontologyBaseModel.read("file:///home/iammyr/spt-new.owl", "N3");
			}
			if (isUriAccessible(SPTSN_SOURCE)) {
				ontologyBaseModel.read(SPTSN_SOURCE, "N3");
			}else{
				ontologyBaseModel.read("file:///home/iammyr/sn-new.owl", "N3");
			}
			if (isUriAccessible(SPTCT_SOURCE)) {
				ontologyBaseModel.read(SPTCT_SOURCE, "N3");
			}else{
				ontologyBaseModel.read("file:///home/iammyr/ct-new.owl", "N3");
			}
		}
		return ontologyBaseModel;
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

	public static Model applyInference(Model base, Reasoner reasoner){
		return ModelFactory.createInfModel(reasoner, base);
	}

}
