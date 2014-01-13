package eu.spitfire_project.ld4s.server;

import java.util.Map;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

import eu.spitfire_project.ld4s.cache.FrontSideCache;
import eu.spitfire_project.ld4s.dataset.VocabVoIDResource;
import eu.spitfire_project.ld4s.resource.actuator_decision.ActuatorDecisionResource;
import eu.spitfire_project.ld4s.resource.device.DeviceResource;
import eu.spitfire_project.ld4s.resource.link.LinkResource;
import eu.spitfire_project.ld4s.resource.link_review.LinkReviewResource;
import eu.spitfire_project.ld4s.resource.measurement_capab.MCResource;
import eu.spitfire_project.ld4s.resource.measurement_prop.MPResource;
import eu.spitfire_project.ld4s.resource.other.OtherResource;
import eu.spitfire_project.ld4s.resource.ov.OVResource;
import eu.spitfire_project.ld4s.resource.ping.PingResource;
import eu.spitfire_project.ld4s.resource.platform.PlatformResource;
import eu.spitfire_project.ld4s.resource.temporal_property.platform.TempPlatfPropResource;
import eu.spitfire_project.ld4s.resource.temporal_property.sensor.TempSensPropResource;

public class Server extends Application{

	private Component component;

	/** Holds the host name associated with this Server. */
	private String hostName;
	
	/**Holds the frontside cache associated with this Server. */
	private FrontSideCache frontSideCache;

	//	  /** Holds the logger for this Service. */
	//	  private Logger logger;

	/** Holds the ServerProperties instance for this Service. */
	private ServerProperties properties;


	public Server() {
		// Complete the list of the application's known roles.
		// Used by ServerResource#isInRole(String), at the resource level
		getRoles().add(ServerProperties.ADMINISTRATOR);
		getRoles().add(ServerProperties.PUBLISHER);
		getRoles().add(ServerProperties.ANONYMOUS);
	}

	/**
	 * Creates a new instance of the HTTP server, listening on the supplied port.
	 *
	 * @return The Server instance created.
	 * @throws Exception If problems occur starting up this server.
	 */
	public static Server newInstance() throws Exception {
		return newInstance(new ServerProperties());
	}

	/**
	 * Returns the full URI associated with this server. Example:
	 * "http://0.0.0.0:9877/ld4s"
	 *
	 * @return The host uri.
	 */
	public String getUri() {
		return "http://"+this.hostName;
	}
	
	/**
	 * Creates a new instance of the HTTP server suitable for unit testing. DPD
	 * properties are initialized from the User's dailyprojectdata.properties file, then set to their
	 * "testing" versions.
	 *
	 * @return The Server instance created.
	 * @throws Exception If problems occur starting up this server.
	 */
	public static Server newTestInstance() throws Exception {
		ServerProperties properties = new ServerProperties();
		//	    properties.setTestProperties();
		return newInstance(properties);
	}

	/**
	 * Creates a new instance of a LiSeD HTTP server, listening on the supplied port.
	 *
	 * @param properties The ServerProperties instance used to initialize this server.
	 * @return The Server instance created.
	 * @throws Exception If problems occur starting up this server.
	 */
	public static Server newInstance(ServerProperties properties) throws Exception {
		Server server = new Server();
		server.properties = properties;
		server.hostName = ServerProperties.SERVER + ":" + ServerProperties.PORT + "/" 
		+ ServerProperties.CONTEXT_ROOT + "/";

		server.component = new Component();
		server.component.getServers().add(Protocol.HTTP, ServerProperties.PORT);
		server.component.getDefaultHost().attach("/" + ServerProperties.CONTEXT_ROOT, server);

		// Create and store the JAXBContext instances on the server context.
		// They are supposed to be thread safe.
		Map<String, Object> attributes = server.getContext().getAttributes();

		// Provide a pointer to this server in the Context so that Resources can get at this server.
		attributes.put("LD4Sensors", server);


		// Now let's open for business.
		//	    server.logger.warning("Host: " + server.hostName);
		//
		//	    server.logger.warning("LD4Sensors (Version " + getVersion() + ") now running.");
		server.component.start();
		
		return server;
	}
	



	/**
	 * Starts up the web service. Control-c to exit.
	 *
	 * @param args Ignored.
	 * @throws Exception if problems occur.
	 */
	public static void main(final String[] args) throws Exception {
		Server.newInstance();
	}

	/**
	 * Dispatch to the specific LinkedServiceData resource based upon the URI. We will authenticate
	 * all requests.
	 *
	 * @return The router Restlet.
	 */
	@Override
	public Restlet createRoot() {

			    // Now create our "top-level" router which will allow the Ping URI to proceed without
			    // authentication, but all other URI patterns will go to the guarded Router.
			    Router router = new Router(getContext());
			   

			 // OTHER
			    router.attach("/resource/{other}", OtherResource.class);
			    router.attach("/resource/{other}/", OtherResource.class);
			    router.attach("/resource/{other}/{id}", OtherResource.class);				
				
			    
			    router.attach("/ping", PingResource.class);
//			    router.attach("/ping?user={user}&password={password}", PingResource.class);
			    
//				router.attach("/sparql", SparqlResource.class);
//				router.attach("/ov/sparql", SparqlResource.class);
//				router.attach("/sensdev/sparql", SparqlResource.class);
//				router.attach("/tps/sparql", SparqlResource.class);
//				router.attach("/tpp/sparql", SparqlResource.class);
//				router.attach("/link/sparql", SparqlResource.class);
				
				// POST req: resource stored remotely IF resourceId == null
				router.attach("/ov/", OVResource.class);
				router.attach("/ov", OVResource.class);
				// GET req:resource stored locally
				// PUT req: resource stored locally + no Linked Data enrichment
				// POST req: resource stored locally + Linked Data enrichment
				// DELETE req: resource stored locally
				router.attach("/ov/{resource_id}", OVResource.class);
				// GET with query string for link filtering req: resource stored locally 
				router.attach("/ov/{resource_id}?d={domains}&nod={nodomains}&trange={time}&s={space}&th={thing}", OVResource.class);
					
					
				//=============================================================
				
				// POST req: resource stored remotely IF resourceId == null
				router.attach("/device/", DeviceResource.class);
				router.attach("/device", DeviceResource.class);
				// GET req:resource stored locally
				// PUT req: resource stored locally + no Linked Data enrichment
				// POST req: resource stored locally + Linked Data enrichment
				// DELETE req: resource stored locally
				router.attach("/device/{resource_id}", DeviceResource.class);
				// GET with query string for link filtering req: resource stored locally 
				router.attach("/device/{resource_id}?d={domains}&nod={nodomains}&trange={time}&s={space}&th={thing}", DeviceResource.class);

				//=============================================================
				
				// POST req: resource stored remotely IF resourceId == null
				router.attach("/platform/", PlatformResource.class);
				router.attach("/platform", PlatformResource.class);
				// GET req:resource stored locally
				// PUT req: resource stored locally + no Linked Data enrichment
				// POST req: resource stored locally + Linked Data enrichment
				// DELETE req: resource stored locally
				router.attach("/platform/{resource_id}", PlatformResource.class);
				// GET with query string for link filtering req: resource stored locally 
				router.attach("/platform/{resource_id}?d={domains}&nod={nodomains}&trange={time}&s={space}&th={thing}", PlatformResource.class);
				
				//=============================================================
				
				// POST req: resource stored remotely IF resourceId == null
				router.attach("/tps/", TempSensPropResource.class);
				router.attach("/tps", TempSensPropResource.class);
				// GET req:resource stored locally
				// PUT req: resource stored locally + no Linked Data enrichment
				// POST req: resource stored locally + Linked Data enrichment
				// DELETE req: resource stored locally
				router.attach("/tps/{resource_id}", TempSensPropResource.class);
				// GET with query string for link filtering req: resource stored locally 
				router.attach("/tps/{resource_id}?d={domains}&nod={nodomains}&trange={time}&s={space}&th={thing}", TempSensPropResource.class);
				
				//=============================================================
				
				// POST req: resource stored remotely IF resourceId == null
				router.attach("/tpp/", TempPlatfPropResource.class);
				router.attach("/tpp", TempPlatfPropResource.class);
				// GET req:resource stored locally
				// PUT req: resource stored locally + no Linked Data enrichment
				// POST req: resource stored locally + Linked Data enrichment
				// DELETE req: resource stored locally
				router.attach("/tpp/{resource_id}", TempPlatfPropResource.class);
				// GET with query string for link filtering req: resource stored locally 
				router.attach("/tpp/{resource_id}?d={domains}&nod={nodomains}&trange={time}&s={space}&th={thing}", TempPlatfPropResource.class);
				
				//=============================================================
				
				// POST req: resource stored remotely IF resourceId == null
				router.attach("/meas_capab/", MCResource.class);
				router.attach("/meas_capab", MCResource.class);
				// GET req:resource stored locally
				// PUT req: resource stored locally + no Linked Data enrichment
				// POST req: resource stored locally + Linked Data enrichment
				// DELETE req: resource stored locally
				router.attach("/meas_capab/{resource_id}", MCResource.class);
				// GET with query string for link filtering req: resource stored locally 
				router.attach("/meas_capab/{resource_id}?d={domains}&nod={nodomains}&trange={time}&s={space}&th={thing}", MCResource.class);
				
				//=============================================================
				
				// POST req: resource stored remotely IF resourceId == null
				router.attach("/meas_prop/", MPResource.class);
				router.attach("/meas_prop", MPResource.class);
				// GET req:resource stored locally
				// PUT req: resource stored locally + no Linked Data enrichment
				// POST req: resource stored locally + Linked Data enrichment
				// DELETE req: resource stored locally
				router.attach("/meas_prop/{resource_id}", MPResource.class);
				// GET with query string for link filtering req: resource stored locally 
				router.attach("/meas_prop/{resource_id}?d={domains}&nod={nodomains}&trange={time}&s={space}&th={thing}", MPResource.class);
				
				//=============================================================
				
				
				// GET req:resource stored locally
				// PUT req: resource stored locally + no Linked Data enrichment
				// DELETE req: resource stored locally
				// POST req: resource stored locally
				router.attach("/link/feedback/{resource_id}", LinkReviewResource.class);
				
 				// POST req: resource stored remotely 
				router.attach("/link/feedback", LinkReviewResource.class);
				router.attach("/link/feedback/", LinkReviewResource.class);
				
				//=============================================================
				
				
				// GET req:resource stored locally
				// PUT req: resource stored locally + no Linked Data enrichment
				// DELETE req: resource stored locally
				// POST req: resource stored locally
				router.attach("/link/{resource_id}", LinkResource.class);
				
				// POST req: resource stored remotely 
				router.attach("/link", LinkResource.class);
				router.attach("/link/", LinkResource.class);
				
				//=============================================================
				
				// DATASET DESCRIPTION
				router.attach("/void", VocabVoIDResource.class);
				
				//=============================================================
				
				
				// GRAPHs
				router.attach("/graph/{other}", OtherResource.class);
		
				// ACTUATOR DECISION SUPPORT
				router.attach("/actuator/decision", ActuatorDecisionResource.class);
				router.attach("/actuator/decision/", ActuatorDecisionResource.class);
		

		return router;

	}

	/**
	 * Returns the version associated with this Package, if available from the jar file manifest. If
	 * not being run from a jar file, then returns "Development".
	 *
	 * @return The version.
	 */
	public static String getVersion() {
		String version = Package.getPackage("eu.spitfire_project.ld4s.server")
		.getImplementationVersion();
		return (version == null) ? "Development" : version;
	}

	/**
	 * Returns the host name associated with this server. Example:
	 * "http://localhost:9877/ld4s"
	 *
	 * @return The host name.
	 */
	public String getHostName() {
		return this.hostName;
	}

	/**
	 * Returns the ServerProperties instance associated with this server.
	 *
	 * @return The server properties.
	 */
	public ServerProperties getServerProperties() {
		return this.properties;
	}

	 /** Returns the front side cache for this server.
	   *
	   * @return The FrontSideCache.
	   */
	  public FrontSideCache getFrontSideCache() {
	    return this.frontSideCache;
	  }


}
