package eu.spitfire_project.ld4s.vocabulary;


import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.TransitiveProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class SptCtVocab {
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
	  public static final String NS = "http://spitfire-project.eu/ontology/ns/ct/";

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
	  public static final String PREFIX = "spt-ct";

	  // Vocabulary properties
	  // /////////////////////////

	  public static final TransitiveProperty TARGET = m_model
			    .createTransitiveProperty(SptCtVocab.NS+"target");
	  public static final TransitiveProperty PROPERTY_OF = m_model
			    .createTransitiveProperty(SptCtVocab.NS+"propertyOf");
	  public static final TransitiveProperty HAS_PROPERTY = m_model
			    .createTransitiveProperty(SptCtVocab.NS+"property");
	  public static final TransitiveProperty DECISION = m_model
			    .createTransitiveProperty(SptCtVocab.NS+"decision");
	  public static final TransitiveProperty CONFIDENCE_LEVEL = m_model
			    .createTransitiveProperty(SptCtVocab.NS+"confidenceLevel");
	  
	  public static final TransitiveProperty APPLICATION_DOMAIN_P = m_model
			    .createTransitiveProperty(SptCtVocab.NS+"applicationDomain");
	  public static final ObjectProperty PURPOSE_P = m_model
			    .createObjectProperty(SptCtVocab.NS+"purpose");
	  public static final ObjectProperty DETECTS = m_model
			    .createObjectProperty(SptCtVocab.NS+"detects");
	
	  
	  // Vocabulary classes
	  // /////////////////////////


	  public static final OntClass APPLICATION_DOMAIN = m_model
			    .createClass(SptCtVocab.NS+"ApplicationDomain");
	  public static final OntClass OF_INTEREST = m_model
			    .createClass(SptCtVocab.NS+"OfInterest");
	  public static final OntClass ACTUATOR_DECISION = m_model
			    .createClass(SptCtVocab.NS+"ActuatorDecision");
	  public static final OntClass ACTUATOR_DECISION_SUPPORT = m_model
			    .createClass(SptCtVocab.NS+"ActuatorDecisionSupport");
	  public static final OntClass ENTERTAINMENT = m_model
			    .createClass(SptCtVocab.NS+"Entertainment");
	  public static final OntClass MUSIC = m_model
			    .createClass(SptCtVocab.NS+"Music");
	  public static final OntClass PURPOSE = m_model
			    .createClass(SptCtVocab.NS+"Purpose");
	  public static final OntClass RELAX = m_model
			    .createClass(SptCtVocab.NS+"Relax");
	  public static final OntClass PRESENCE = m_model
			    .createClass(SptCtVocab.NS+"Presence");
	  public static final OntClass ABSENCE = m_model
			    .createClass(SptCtVocab.NS+"Absence");
	  
	  public static final OntClass LOW = m_model
	    .createClass(SptCtVocab.NS+"Low");
	  public static final OntClass MEDIUM = m_model
			    .createClass(SptCtVocab.NS+"Medium");
	  public static final OntClass HIGH = m_model
			    .createClass(SptCtVocab.NS+"High");
	  
	  // Vocabulary individuals
	  // /////////////////////////

	  public  SptCtVocab(){
		  ENTERTAINMENT.addProperty(RDFS.subClassOf, APPLICATION_DOMAIN);
		  MUSIC.addProperty(RDFS.subClassOf, ENTERTAINMENT);
		  RELAX.addProperty(RDFS.subClassOf, PURPOSE);
		  
		  ENTERTAINMENT.addProperty(TARGET, FOAF.Person);		  
		  
		  PROPERTY_OF.addInverseOf(HAS_PROPERTY);
		  
		  
	  }

}

