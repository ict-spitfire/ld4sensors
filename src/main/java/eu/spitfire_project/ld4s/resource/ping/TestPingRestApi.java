package eu.spitfire_project.ld4s.resource.ping;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;

import eu.spitfire_project.ld4s.client.LD4SClient;
import eu.spitfire_project.ld4s.test.LD4STestHelper;
import eu.spitfire_project.ld4s.vocabulary.LD4SConstants;

public class TestPingRestApi extends LD4STestHelper{
	/**
	   * Test that authenticated GET {host}/ping returns the service name.
	   *
	   * @throws Exception If problems occur.
	   */
	  @Test
	  public void testAuthenticatedPing() throws Exception {
	    LD4SClient client = new LD4SClient(user, user_password);
	    assertTrue(client.makeRequest("ping", Method.GET, MediaType.ALL, null).getEntity().getText()
	    		.compareTo(LD4SConstants.AUTHENTICATED_PINGTEXT+user_role.getName()+" ") == 0);
	  }
	  
	  /**
	   * Test that unauthenticated GET {host}/ping returns the service name.
	   *
	   * @throws Exception If problems occur.
	   */
	  @Test
	  public void testUnauthenticatedPing() throws Exception {
	    LD4SClient client = new LD4SClient(null, null);
	    assertTrue(client.makeRequest("ping", Method.GET, MediaType.ALL, null).getEntity().getText()
	    		.startsWith(LD4SConstants.AUTHENTICATED_PINGTEXT));
	  }
}
