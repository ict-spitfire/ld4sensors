package eu.spitfire_project.ld4s.resource.sparql;

import java.net.URLDecoder;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

import eu.spitfire_project.ld4s.resource.LD4SDataResource;

/**
 * Resource representing a SPARQL query
 * 
 * @author Myriam Leggieri <iammyr@email.com>
 *
 */
public class SparqlResource extends LD4SDataResource{


	private String resourceName = "Sparql Request";
	

	
	/**
	 * Returns a serialized RDF Model 
	 * that contains the linked data associated with the
	 * specified path
	 *
	 * @return The resource representation.
	 */
	@Override
	public Representation get() {
		Representation ret = null;
		logger.fine(resourceName + " execution: Starting");
		try {
			//check cache
			//get all the resource information from the Triple DB
			logger.fine(resourceName + " : Requesting answer");

			//create the Repository Branch as LinkedServiceData.
			logRequest(resourceName);
			String querystr = null;
			int query = uristr.indexOf("?q=");
			if (query != -1){
				querystr = this.uristr.substring(query+3, uristr.length());
				querystr = URLDecoder.decode(querystr, "utf-8");
				Object answer = null;
				SparqlType type = null;
				if (querystr.startsWith(SparqlType.ASK.toString())){
					type = SparqlType.ASK;
				}else if (querystr.startsWith(SparqlType.SELECT.toString())){
					type = SparqlType.SELECT;
				}else if (querystr.startsWith(SparqlType.CONSTRUCT.toString())){
					type = SparqlType.CONSTRUCT;
				}else if (querystr.startsWith(SparqlType.DESCRIBE.toString())){
					type = SparqlType.DESCRIBE;
				}
				
				answer = sparqlQueryExec(querystr, type);
				if (querystr.startsWith(SparqlType.UPDATE.toString())){
					sparqlUpdateExec(querystr);
					answer = "SPARQL Update successfully executed.";
				}
				if (answer instanceof Model){
					ret = serializeAccordingToReqMediaType((Model)answer);	
				}else if (answer instanceof ResultSet){
					ret = serializeAccordingToReqMediaType(((ResultSet)answer).getResourceModel());
				}else{
					ret = new StringRepresentation (answer.toString());
					setStatus(Status.SUCCESS_OK);
				}
				
			}
			
		}
		catch (Exception e) {
			setStatusError("Error answering the " + resourceName + " - LD4S.", e);
			ret = null;
		}

		return ret;
	}
	
}
