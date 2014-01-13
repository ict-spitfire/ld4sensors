package eu.spitfire_project.ld4s.resource.actuator_decision;

import java.util.UUID;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import eu.spitfire_project.ld4s.resource.LD4SDataResource;
import eu.spitfire_project.ld4s.vocabulary.SptCtVocab;
import eu.spitfire_project.ld4s.vocabulary.SptVocab;
import eu.spitfire_project.ld4s.vocabulary.Wgs84Vocab;

public class LD4SActuatorDecisionResource extends LD4SDataResource {

	protected Model getLatestReadingAsRdf(String latestReading, Resource sensor){
		Model ret = ModelFactory.createDefaultModel();
		Resource ov = ret.createResource(getResourceUri(
				this.ld4sServer.getHostName(), "ov", UUID.randomUUID().toString()));
		ret.add(ov, RDF.type, SptVocab.OV);
		ret.add(ov, SptVocab.HAS_VALUE, latestReading, XSDDatatype.XSDdouble);
		ret.add(ov, SptVocab.TIME, String.valueOf(System.currentTimeMillis()),
				XSDDatatype.XSDunsignedLong);
		ret.add(ov, SptVocab.OUT_OF, sensor);
		ret.add(sensor, SptVocab.OUT, ov);
		return ret;
	}
	
	protected Model getActuatorInputAsRdf(ActuatorDecision ad, String subjectUri){
		Model ret = ModelFactory.createDefaultModel();
		Resource subj = ret.createResource(subjectUri);
		ret.add(subj, RDF.type, SptCtVocab.ACTUATOR_DECISION_SUPPORT);
		
		String actprop = ad.getActuatorProperty();
		if (actprop != null){
			ret.add(subj, SptVocab.ACTUATED_PROPERTY_P, ret.createResource(actprop));
		}
		
		if (ad.getLatitude() != null){
			ret.add(subj, Wgs84Vocab.LAT, ret.createLiteral(ad.getLatitude()));
		}
		if (ad.getLongitude() != null){
			ret.add(subj, Wgs84Vocab.LONG, ret.createLiteral(ad.getLongitude()));
		}
		
		if (ad.getApplicationDomain() != null){
			ret.add(subj, SptCtVocab.APPLICATION_DOMAIN_P, ret.createResource(ad.getApplicationDomain()));
		}
		return ret;
	}
}
