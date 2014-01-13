package eu.spitfire_project.ld4s.vocabulary;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.TransitiveProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class SptSnVocab {
	/**
	   * <p>
	   * The ontology model that holds the vocabulary terms
	   * </p>
	   */
	  public static OntModel m_model = ModelFactory.createOntologyModel(
			  OntModelSpec.OWL_MEM, null);

	  /**
	   * <p>
	   * The namespace of the vocabulary as a string
	   * </p>
	   */
	  public static final String NS = "http://spitfire-project.eu/ontology/ns/sn/";

	  /**
	   * <p>
	   * The namespace of the vocabulary as a string
	   * </p>
	   *
	   * @see #NS
	   */
	  public static String getURI() {
	    return NS;
	  }

	  /**
	   * <p>
	   * The namespace of the vocabulary as a resource
	   * </p>
	   */
	  public static final Resource NAMESPACE = m_model.createResource(NS);

	  /** PREFIX. */
	  public static final String PREFIX = "spt-sn";

	  // Vocabulary properties
	  // /////////////////////////


	  
	  // Vocabulary classes
	  // /////////////////////////

	  public static final OntClass ACTUATOR = m_model
	    .createClass(SptSnVocab.NS+"Actuator");
	  public static final OntClass TRANSDUCER = m_model
	    .createClass(SptSnVocab.NS+"Testbed");
	  public static final OntClass TESTBED = m_model
	    .createClass(SptSnVocab.NS+"Testbed");
	  public static final OntClass MIDDLEWARE = m_model
    .createClass(SptSnVocab.NS+"Middleware");
	  public static final OntClass CLUSTERHEAD = m_model
	    .createClass(SptSnVocab.NS+"ClusterHead");
	  public static final OntClass ROUTER = m_model
	    .createClass(SptSnVocab.NS+"Router");
	  public static final OntClass RFID = m_model
	    .createClass(SptSnVocab.NS+"Middleware");
	  public static final OntClass GPS = m_model
	    .createClass(SptSnVocab.NS+"Gps");
	  public static final OntClass ACCELEROMETER = m_model
	    .createClass(SptSnVocab.NS+"Accelerometer");
	  public static final OntClass MOTION_SENSOR = m_model
	    .createClass(SptSnVocab.NS+"Motion");
	  public static final OntClass STATUS_LOGGER = m_model
	    .createClass(SptSnVocab.NS+"StatusLogger");
	  public static final OntClass TEMPERATURE_SENSOR = m_model
	    .createClass(SptSnVocab.NS+"Temperature");
	  public static final OntClass HUMIDITY_SENSOR = m_model
	    .createClass(SptSnVocab.NS+"Humidity");
	  public static final OntClass LIGHT_SENSOR = m_model
	    .createClass(SptSnVocab.NS+"Light");
	  public static final OntClass NOISE_SENSOR = m_model
	    .createClass(SptSnVocab.NS+"Noise");
	  public static final OntClass PRESSURE_SENSOR = m_model
	    .createClass(SptSnVocab.NS+"Pressure");
	  
	 
	  public static final OntClass SMARTPHONE = m_model
			    .createClass(SptSnVocab.NS+"Smartphone");
	  
	  public static final OntClass ACTUATOR_PROPERTY = m_model
			    .createClass(SptSnVocab.NS+"ActuatorProperty");
	  public static final OntClass VOLUME = m_model
			    .createClass(SptSnVocab.NS+"Volume");
	  
	
	  public static final TransitiveProperty ACTUATED_PROPERTY = m_model
			    .createTransitiveProperty(SptSnVocab.NS+"actuatedProperty");
	  
	  // Vocabulary individuals
	  // /////////////////////////

	  public  SptSnVocab(){
		 
		  SMARTPHONE.addProperty(RDFS.subClassOf, SsnVocab.SENSING_DEVICE);
		  
	  }

}
