package eu.spitfire_project.ld4s.resource.sparql;

import static org.junit.Assert.assertTrue;

import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import eu.spitfire_project.ld4s.test.LD4STestHelper;

public class TestSparqlEndpoint extends LD4STestHelper{
	
	/**
	 * Test that  PUT {host}/resource/ov/{resource_id}
	 *
	 * @throws Exception If problems occur.
	 */
	@Test
	public void testSelect() throws Exception {
		String filters = "SELECT * " +
//				"FROM NAMED <http://localhost:8182/ld4s/graph/ov> " +
				"{" +
//					"<http://localhost:8182/ld4s/link/http%3a%2f%2flocalhost%3a8182%2fld4s%2fov%2fx12_red+dress>" +
						"?x ?y ?z" 
					+ "}" 
				+"LIMIT 10";
		filters = URLEncoder.encode(filters, "utf-8");
		ClientResource cr = new ClientResource(
				"http://localhost:8182/ld4s/link/sparql?q="+filters);
		//ChallengeResponse authentication = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, user, 
				//user_password);
		//cr.setChallengeResponse(authentication);
		List<Preference<MediaType>> accepted = new LinkedList<Preference<MediaType>>();
		accepted.add(new Preference<MediaType>(MediaType.APPLICATION_RDF_XML));
		cr.getClientInfo().setAcceptedMediaTypes(accepted);
		Representation resp = cr.get();
		System.out.println("RESPONSE to the SPARQL QUERY***\n"+resp.getText());
		Status status = cr.getStatus();
		System.out.println(status.getCode()+ " - "+cr.getStatus().getDescription());            
		assertTrue(status.isSuccess());
	}

	


}
