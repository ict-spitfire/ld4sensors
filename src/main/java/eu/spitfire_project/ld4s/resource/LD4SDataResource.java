package eu.spitfire_project.ld4s.resource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.restlet.service.MetadataService;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import eu.spitfire_project.ld4s.lod_cloud.Context;
import eu.spitfire_project.ld4s.lod_cloud.Context.Domain;
import eu.spitfire_project.ld4s.lod_cloud.EncyclopedicApi;
import eu.spitfire_project.ld4s.lod_cloud.GenericApi;
import eu.spitfire_project.ld4s.lod_cloud.LocationApi;
import eu.spitfire_project.ld4s.lod_cloud.PeopleApi;
import eu.spitfire_project.ld4s.lod_cloud.Person;
import eu.spitfire_project.ld4s.lod_cloud.SearchRouter;
import eu.spitfire_project.ld4s.lod_cloud.UomApi;
import eu.spitfire_project.ld4s.lod_cloud.WeatherApi;
import eu.spitfire_project.ld4s.resource.link.Link;
import eu.spitfire_project.ld4s.server.Server;
import eu.spitfire_project.ld4s.server.ServerProperties;
import eu.spitfire_project.ld4s.vocabulary.CorelfVocab;
import eu.spitfire_project.ld4s.vocabulary.FoafVocab;
import eu.spitfire_project.ld4s.vocabulary.LD4SConstants;
import eu.spitfire_project.ld4s.vocabulary.ProvVocab;
import eu.spitfire_project.ld4s.vocabulary.RevVocab;
import eu.spitfire_project.ld4s.vocabulary.SiocVocab;
import eu.spitfire_project.ld4s.vocabulary.SptVocab;
import eu.spitfire_project.ld4s.vocabulary.SsnVocab;
import eu.spitfire_project.ld4s.vocabulary.VoIDVocab;

public abstract class LD4SDataResource extends ServerResource{
	protected static enum SparqlType {SELECT, CONSTRUCT, UPDATE, DESCRIBE, ASK};

	/** Current user. */
	protected User user;

	/** Its role(s). */
	protected List<Role> roles;

	/** This server (ld4s). */
	protected Server ld4sServer;

	/** Resource identification */
	protected String resourceId;

	/** To be retrieved from the URL as the 'timestamp' template parameter, or null. */
	protected String timestamp = null;

	/** Records the time at which each HTTP request was initiated. */
	protected long requestStartTime = new Date().getTime();

	/**  Preferred media type. */
	protected MediaType requestedMedia;

	//	/** Default URI for annotating unknown resources. */
	//	protected String defaultUri = null;

	/** Query string from the URI. */
	protected String query = null;

	/** Requested string. */
	protected String uristr = null;

	/** Vocabulary of Interlinked Data used to describe the Hackystat dataset. */
	public static final Model voIDModel = LD4SDataResource.initVoIDModel();

	/** Logger for messages. */
	protected Logger logger = null;

	/**Dataset to handle the data stored in the triple db. */
	protected Dataset dataset = null;

	/**Submitted Entity. */
	protected Representation entity = null;

	/** Contextual criteria for link creation. */
	protected eu.spitfire_project.ld4s.lod_cloud.Context context = null;

	/** Modality in which the user prefers to get a resource, i.e., linked with external data or not. */
	protected boolean linked = true;

	/** Name of the Named Graph where all the instances of each Service resource are stored. */
	protected String namedModel = null;

	private String generalNamedModel = null;

	protected static HashMap<String, String> resource2namedGraph = null;


	protected void initResource2NamedGraph(String baseHost){
		String base = baseHost+"graph/";
		resource2namedGraph = new HashMap<String, String>();
		resource2namedGraph.put("ov", base+"ov");
		/**datlink resources are the only resource among the 
		ld4s enriched main ones, that gets generated while
		annotated other resources. Then for logistics
		reasons, they need to go in the generic named graph
		rather than having their own named graph.**/
//		resource2namedGraph.put("link", base+"link");
		resource2namedGraph.put("device", base+"device");
		resource2namedGraph.put("tpp", base+"tpp");
		resource2namedGraph.put("tps", base+"tps");
		resource2namedGraph.put("platform", base+"platform");
		resource2namedGraph.put("meas_capab", base+"meas_capab");
		resource2namedGraph.put("meas_prop", base+"meas_prop");
	}

	@Override
	protected void doInit() throws ResourceException {
		this.user = getClientInfo().getUser();
		if (this.user == null){
			this.user = new User();
		}
		this.roles = getClientInfo().getRoles();
		this.requestedMedia = selectMedia(getClientInfo().getAcceptedMediaTypes());
		this.entity = getRequestEntity();
		MetadataService ms = getMetadataService(); 
        ms.addCommonExtensions(); 
        ms.addExtension("ttl", MediaType.APPLICATION_RDF_TURTLE);
        ms.addExtension("rdf", MediaType.APPLICATION_RDF_XML);
        ms.addExtension("n3", MediaType.TEXT_RDF_N3);
        
		//		if (this.requestedMedia == null){
		//			setStatusError("The requested Media Type " + requestedMedia + " is not supported .");
		//		}
		this.ld4sServer = (Server) getContext().getAttributes().get("LD4Sensors");
		if (resource2namedGraph == null){
			initResource2NamedGraph(this.ld4sServer.getHostName());
			//			initResource2NamedGraph("http://192.168.56.1:8182/ld4s/");
		}
		this.resourceId = ((String) getRequest().getAttributes().get("resource_id"));
		this.timestamp = (String) getRequest().getAttributes().get("timestamp");
		this.uristr = this.getRequest().getResourceRef().toString();
		this.generalNamedModel = this.ld4sServer.getHostName()+"graph/general";
		this.namedModel = getNamedModel(this.uristr);
		if (this.namedModel == null){
			this.namedModel = generalNamedModel ;
		}
		this.query = ((String) getRequest().getAttributes().get("query"));

		//handle the linking criteria (context) when appended at the URI in a GET request.
		try {
			this.context = new Context(
					getRequest().getResourceRef().getQueryAsForm(),
					this.ld4sServer.getHostName());
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(Status.SERVER_ERROR_INTERNAL, "Unable to extract the linking criteria from the submitted query string.");
			return;
		}
		logger = this.ld4sServer.getLogger();
	}

	public static String getNamedModel(String uri) {
		Iterator<String> it = resource2namedGraph.keySet().iterator();
		String key = null;
		String nm = null;
		while (it.hasNext() && nm == null){
			key = it.next();
			if (uri.contains("/"+key+"/")){
				nm = resource2namedGraph.get(key);
			}	
		}
		return nm;
	}

	private MediaType selectMedia(List<Preference<MediaType>> acceptedMediaTypes) {
		MediaType ret = null;
		MediaType media = null;
		if (acceptedMediaTypes.size() == 0){
			ret = MediaType.APPLICATION_ALL;
		}else{
			for (int i=0; i<acceptedMediaTypes.size() && ret==null ;i++){
				media = acceptedMediaTypes.get(i).getMetadata();
				if (media.equals(MediaType.ALL)
						|| media.equals(MediaType.APPLICATION_ALL) || media.equals(MediaType.TEXT_ALL)
						|| media.equals(MediaType.TEXT_RDF_N3) || media.equals(MediaType.APPLICATION_RDF_XML)
						|| media.equals(MediaType.TEXT_RDF_NTRIPLES)
						|| media.equals(MediaType.APPLICATION_RDF_TURTLE))
				{
					ret = media;
				}
			}
		}
		return ret;
	}

	/**
	 * Initialize the specified model with the Spitfire ontology. 
	 * In this way, future inferences
	 * based on this ontology will be allowed.
	 *
	 * @param model model.
	 */
	protected Model initModel(Model model, String rdfFileName) {
		if (model == null) {
			String schemapath = SptVocab.class.getResource(rdfFileName).getPath();
			model = ModelFactory.createDefaultModel();
			// use the FileManager to find the input file
			java.io.InputStream in = FileManager.get().open(schemapath);
			if (in == null) {
				throw new IllegalArgumentException("File: " + schemapath + " not found");
			}
			// read the RDF file
			model.read(in, null);
		}
		return model;
	}

	protected void saveVocabEditsToFile(Model model, String rdfFileName) 
	throws IOException{
		if (model == null) {
			model = ModelFactory.createDefaultModel();
		}
		String schemapath = SptVocab.class.getResource(rdfFileName).getPath();
		File file = new File(schemapath); 
		if(!file.exists()){
			file.createNewFile();
		}
		model.write(new PrintWriter(file));
	}

	/**
	 * Creates and returns a new Restlet StringRepresentation built from rdfData. The rdfData will be
	 * prefixed with a processing instruction indicating UTF-8 and version 1.0.
	 *
	 * @param rdfData The rdf data as a string.
	 * @return A StringRepresentation of that rdfdata.
	 */
	public static StringRepresentation getStringRepresentationFromRdf(String rdfData, MediaType media) {
		return new StringRepresentation(rdfData, media, Language.ALL, CharacterSet.UTF_8);
	}

	/**
	 * Generates a log message indicating the type of request, the elapsed time required, the user who
	 * requested the data, and the day.
	 *
	 * @param requestType The type of LD4S request, such as "OV", etc.
	 * @param optionalParams Any additional parameters to the request.
	 */
	protected void logRequest(String requestType, String... optionalParams) {
		long elapsed = new Date().getTime() - requestStartTime;
		String sp = LD4SConstants.SEPARATOR1_ID;
		StringBuffer msg = new StringBuffer(20);
		msg.append(elapsed).append(" ms: ").append(requestType).append(sp);
		if (user != null){
			msg.append(user.getIdentifier()).append(sp);
		}
		if (resourceId != null){
			msg.append(resourceId).append(sp);
		}
		msg.append(timestamp);
		for (String param : optionalParams) {
			msg.append(sp).append(param);
		}
		ld4sServer.getLogger().info(msg.toString());
	}


	/**
	 * Create the RDF model that describes this Spitfire published 
	 * sensor dataset, using the voID vocabulary.
	 */
	public static Model initVoIDModel() {
		Model model = ModelFactory.createDefaultModel();
		com.hp.hpl.jena.rdf.model.Resource dataset = model.createResource(SptVocab.NS);
		dataset.addProperty(RDF.type, VoIDVocab.DATASET);
		dataset.addProperty(DC.title, "LD4Sensors");
		dataset.addProperty(DC.description,
				"Data about sensors, sensing devices in general and " +
				"sensor measurements stored in the LD4Sensors Triple DB"
				+ "published as Linked Data.");
		dataset.addProperty(VoIDVocab.URI_REGEX_PATTERN, ".*resource/ov/.*");
		dataset.addProperty(VoIDVocab.URI_REGEX_PATTERN, ".*resource/ov/sparql?query=.*");
		dataset.addProperty(DC.creator,
		"http://myr.altervista.org/foaf.rdf#iammyr");
		dataset
		.addProperty(
				DC.publisher,
				model
				.createResource()
				.addProperty(RDF.type, FoafVocab.ORGANIZATION)
				.addProperty(
						RDFS.label,
				"LD4Sensors - Digital Enterprise Research Institute (DERI) - National University of Ireland, Galway at Galway")
				.addProperty(FoafVocab.HOMEPAGE, "http://spitfire-project.eu/ld4s"));
		/** The following subject URIs come from the UMBEL dataset (based upon OpenCyc). */
		dataset.addProperty(DC.subject, "http://umbel.org/umbel/sc/SoftwareProject");
		dataset.addProperty(DCTerms.accessRights, "http://www.gnu.org/copyleft/fdl.html");
		dataset.addProperty(VoIDVocab.SPARQL_ENDPOINT,
		"http://spitfire-project.eu/ld4s/ov/sparql?query=");
		dataset.addProperty(VoIDVocab.VOCABULARY, FoafVocab.NS);
		dataset.addProperty(VoIDVocab.VOCABULARY, SptVocab.NS);
		dataset.addProperty(VoIDVocab.VOCABULARY, SiocVocab.NS);
		dataset.addProperty(VoIDVocab.VOCABULARY, VoIDVocab.NS);
		dataset.addProperty(VoIDVocab.VOCABULARY, DC.NS);
		dataset.addProperty(VoIDVocab.VOCABULARY, OWL.NS);
		dataset.addProperty(VoIDVocab.VOCABULARY, DCTerms.NS);
		dataset.addProperty(VoIDVocab.VOCABULARY, "http://umbel.org/umbel/sc/");
		return model;
	}

	/**
	 * Called when an error resulting from an exception is caught during processing.
	 *
	 * @param msg A description of the error.
	 * @param e A chained exception.
	 */
	protected void setStatusError(String msg, Exception e) {
		String responseMsg = String.format("%s:%n  Request: %s %s%n  Caused by: %s", msg, this
				.getRequest().getMethod().getName(), this.getRequest().getResourceRef().toString(), e
				.getMessage());
		this.getLogger().info(responseMsg);
		getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, removeNewLines(responseMsg));
	}

	/**
	 * Called when an error occurs during processing.
	 *
	 * @param msg A description of the error.
	 */
	protected void setStatusError(String msg) {
		String responseMsg = String.format("%s:%n  Request: %s %s", msg, this.getRequest().getMethod()
				.getName(), this.getRequest().getResourceRef().toString());
		this.getLogger().info(responseMsg);
		getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, removeNewLines(responseMsg));
	}


	/**
	 * Helper function that removes any newline characters from the supplied string and replaces them
	 * with a blank line.
	 *
	 * @param msg The msg whose newlines are to be removed.
	 * @return The string without newlines.
	 */
	private String removeNewLines(String msg) {
		return msg.replace(System.getProperty("line.separator"), LD4SConstants.SEPARATOR1_ID);
	}


	/**
	 * Creates and returns a string representation of a given RDF model, using the specified RDF
	 * serialization (N3, RDF/XML, etc.)
	 *
	 * @param model
	 * @param relativeUriBase
	 * @param lang
	 * @return
	 */
	public static String serializeRDFModel(Model model, String relativeUriBase, String lang) {
		String ret = null;
		java.io.OutputStream os = null;
		// Serialize over an outputStream
		os = new ByteArrayOutputStream();
		model.write(os, lang, relativeUriBase);
		ret = os.toString();
		try {
			os.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return ret;
	}

	/**
	 * Build the uri for a resource of the current instance of the LD4S service.
	 * If the resource hosting service is the LD4S one, then it checks whether it actually
	 * exists in the TDB and if not, it creates a new one (with basic data, which could be
	 * eventually enriched by the user, later on, by the LD4S API for update requests).
	 * @param name resource identificator
	 * @return uri
	 */
	public static String getResourceUri(String host,
			String type, String name) {
		String uri = host + type +"/"+ name.toLowerCase();
		
		return uri;
	}

	/**
	 * Detects the presence inside a given model, of resources that
	 * should be stored under the LD4S generic named model.  
	 * @param model
	 */
	private Model handleGenericResources(Model model,
			Model genericmodel) {
		if (model == null || genericmodel == null){
			return null;
		}
		StmtIterator rit = model.listStatements();
		Resource res = null;
		Statement st = null;
		while (rit.hasNext()){
			st = rit.next();
			res = st.getSubject();
			//if the resource's uri is local to this LD4S instance
			if (res.getURI() == null){//in case of blank nodes
				genericmodel.add(st);
			}else{
				if (res.getURI().startsWith(this.ld4sServer.getHostName())){
					//if the resource is not part of the linked data
					//enriched set provided by LD4S
					Iterator<String> it= 
						resource2namedGraph.keySet().iterator();
					while (it.hasNext() && 
							!res.getURI().contains("/"+it.next()+"/"))
						;
					if (!it.hasNext()){
						genericmodel.add(st);	
					}								
				}
			}
		}
		return genericmodel;
	}
	
	/**
	 * Initialize a connection with the triple db
	 */
	private void initTDB(){
		// Direct way: Make a TDB-backed dataset
		String directory = ld4sServer.getServerProperties().getFoldername()+
		LD4SConstants.SYSTEM_SEPARATOR+"tdb"
		+LD4SConstants.SYSTEM_SEPARATOR+"LD4SDataset1" ;
		File dirf = new File (directory);
		if (!dirf.exists()){
			dirf.mkdirs();
		}
		dataset = TDBFactory.createDataset(directory) ;
		TDB.sync(dataset ) ;
	}

	protected void sparqlUpdateExec(String query){
		try{
			initTDB();
			GraphStore graphStore = GraphStoreFactory.create(dataset) ;
			UpdateAction.parseExecute(query, graphStore) ;
		}finally{closeTDB();}
	}
	/**
	 * Close connection with the triple db
	 */
	private void closeTDB(){
		dataset.close() ;
	}

	/**
	 * Store the given model in the triple db
	 * @param rdfData model to be stored
	 * @return success
	 */
	protected boolean store(Model rdfData, String namedModel){
		boolean ret = true;
		//remove invalid entries from the model to be stored
		rdfData = rdfData.removeAll(null, null, rdfData.createLiteral(""));
		rdfData = rdfData.removeAll(null, null, rdfData.createLiteral("null"));
		//special handler for resources (out of the scope of the 
		//requested ones
		initTDB();
		this.dataset.begin(ReadWrite.WRITE) ;		
		try {
			Model genericmodel = ModelFactory.createDefaultModel();
			genericmodel = handleGenericResources(rdfData, genericmodel);
			rdfData.remove(genericmodel);
			if (!dataset.containsNamedModel(generalNamedModel)){
				dataset.addNamedModel(generalNamedModel, genericmodel);
			}else{
				Model model = dataset.getNamedModel(generalNamedModel) ;
				model.add(genericmodel);
			}
			if (!dataset.containsNamedModel(namedModel)){
				dataset.addNamedModel(namedModel, rdfData);
			}else{
				Model model = dataset.getNamedModel(namedModel) ;
				model.add(rdfData);
			} 
			dataset.commit() ;
			// Or call .abort()
		}catch(Exception e){
			e.printStackTrace();
			ret = false;
		}  finally { 
			dataset.end() ;
			closeTDB();
		}
//		testSparqlPrint(namedModel);
		return ret; 

	}



	/**
	 * 
	 * @param resource
	 * @param searchType
	 * @param context not necessarily referred to the one of the web service requested
	 * resource, since Linked Data creation might be needed for other resources to be created
	 * as a side-effect of the main resource semantic annotation process. 
	 * @return
	 * @throws Exception
	 */
	public Resource addLinkedData(Resource resource,
			Domain searchType, Context context) throws Exception {
		SearchRouter searchobj = null;
		switch(searchType){
		case ALL:
			searchobj = new GenericApi(this.ld4sServer.getHostName(), 
					context, this.user, resource);
			break;
		case PEOPLE:
			searchobj = new PeopleApi(this.ld4sServer.getHostName(), 
					context, this.user, resource);
			break;
		case WEATHER:
			searchobj = new WeatherApi(this.ld4sServer.getHostName(), 
					context, this.user, resource);
			break;
		case LOCATION:
			searchobj = new LocationApi(this.ld4sServer.getHostName(), 
					context, this.user, resource);
			break;
		case FEATURE: //searched in DBpedia ONLY
			searchobj = new EncyclopedicApi(this.ld4sServer.getHostName(), 
					context, this.user, resource);
			break;
		case UNIT:
			searchobj = new UomApi(this.ld4sServer.getHostName(), 
					context, this.user, resource, getUomFilePath());
			break;
		default: //searched in cross-domain datasets
			searchobj = new EncyclopedicApi(this.ld4sServer.getHostName(), 
					context, this.user, resource);
		}
		Model model = searchobj.start();
		if (model != null){
			store(model, namedModel);
		}
		return resource;
	}
	
	protected String getRuleFilePath(){
		ServerProperties sp = this.ld4sServer.getServerProperties();
		return sp.get(ServerProperties.RULES_FILE_KEY);
	}
	
	protected String getDatasetFolderPath(){
		return ld4sServer.getServerProperties().getFoldername()+
				LD4SConstants.SYSTEM_SEPARATOR+"tdb"
				+LD4SConstants.SYSTEM_SEPARATOR+"LD4SDataset1";
	}
	
	public static String getCurrentTime(){
		String now = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		//get current date time with Date()
		Date date = new Date();
		now = dateFormat.format(date);
		return now;
	}
	

	protected String getUomFilePath(){
		ServerProperties sp = this.ld4sServer.getServerProperties();
		return sp.get(ServerProperties.UOM_FILE_KEY);
	}


	public static String removeBrackets(String string) {
		if (string == null || string.trim().compareTo("") == 0){
			return null;
		}
		if (string.startsWith("[")){
			string = string.substring(1);
		}
		if (string.endsWith("]")){
			string = string.substring(0, string.length()-1);
		}
		if (string.startsWith("\"")){
			string = string.substring(1);
		}
		if (string.endsWith("\"")){
			string = string.substring(0, string.length()-1);
		}
		if (string.trim().compareTo("null")==0){
			string = null;
		}
		return string;
	}


	protected Resource crossResourcesAnnotation(LD4SObject ov, Resource resource) throws Exception{
		String 
		//check whether the specified subtype is a valid one,
		item = ov.getType();
		OntClass[] at = null;
		OntClass preftype = null;;
		if (item != null && item.trim().compareTo("")!=0){
			at = ov.getAcceptedTypes();
			if (at != null){
				for (int ind=0; ind<at.length&&preftype==null ;ind++){
					if (at[ind].getURI().toLowerCase().contains(item.toLowerCase())){
						preftype = at[ind];
					}
				}
				if (preftype != null){
					resource.addProperty(RDF.type, preftype);
					//eventually append a new subtype in the ld4s.rdf file
				}
			}			
		}
		if (item == null || item.trim().compareTo("")==0
				|| at == null || preftype == null){
			// if not, just assign the more general type
			resource.addProperty(RDF.type, ov.getDefaultType());
		}

		item = ov.getLocation_name();
		String[] item1 = ov.getCoords();
		if (item != null && item.startsWith("http://")){
				resource.addProperty(
						resource.getModel().createProperty(
						"http://www.ontologydesignpatterns.org/ont/dul/DUL.owl/hasLocation"), 
						resource.getModel().createResource(item));	
		}else {
			resource = addLocation(resource, item, item1);
			
		}
		item = ov.getResource_time();
		if (item != null && item.trim().compareTo("")!=0){
			resource.addProperty(CorelfVocab.RESOURCE_TIME, 
					resource.getModel().createTypedLiteral(item, XSDDatatype.XSDlong));
		}
		item = ov.getTime();
		if (item != null && item.trim().compareTo("")!=0){
			resource.addProperty(CorelfVocab.TIME, 
					resource.getModel().createTypedLiteral(item, XSDDatatype.XSDlong));
		}
		item = ov.getStart_range();
		if (item != null && item.trim().compareTo("")!=0){
			resource.addProperty(SptVocab.START_TIME, 
					resource.getModel().createTypedLiteral(item, XSDDatatype.XSDlong));
		}
		item = ov.getEnd_range();
		if (item != null && item.trim().compareTo("")!=0){
			resource.addProperty(SptVocab.END_TIME, 
					resource.getModel().createTypedLiteral(item, XSDDatatype.XSDlong));
		}		
		item = ov.getBase_datetime();
		if (item != null && item.trim().compareTo("")!=0){
			resource.addProperty(CorelfVocab.BASE_TIME, 
					resource.getModel().createTypedLiteral(item, XSDDatatype.XSDdateTime));
		}
		item = ov.getArchive();
		if (item != null && item.trim().compareTo("")!=0){
			resource.addProperty(SptVocab.TS_MAP, resource.getModel().createResource(item));
		}


		Person person = null;
		Resource publisher_resource = null;
		if (this.user.getIdentifier() != null){
			person = new Person(user.getFirstName(), user.getLastName(), user.getName(), user.getEmail(), null, null, null);
			publisher_resource = addPerson(resource, person, DC.publisher);
		}
		person = ov.getAuthor();
		if (person != null){
			if (publisher_resource != null){
				addPerson(publisher_resource, person, ProvVocab.ACTED_ON_BEHALF_OF);
			}
			addPerson(resource, person, ProvVocab.WAS_GENERATED_BY);
		}
		return resource;
	}



	protected Resource addWeatherForecast(Resource resource) throws Exception{
		String id = null;		
		Date[] dates = this.context.getTime_range(); 
		String[] location_coords = this.context.getLocation_coords();
		String location = this.context.getLocation();
		if (dates != null && dates.length == 2){
			if (location != null){
				id = URLEncoder.encode(location+"_"+dates[0]
				                                          +dates[1], "utf-8");
			}else if (location_coords != null && location_coords.length == 2){
				id = URLEncoder.encode(location_coords[0]+"_"+location_coords[1]
				                                                              +"_"+dates[0]+"_"+dates[1], "utf-8");
			}else{
				throw new Exception("Unable to link with an external weather forecast resource" +
				"because of a wrongly specified location coordinates");
			}
		}else{
			throw new Exception("Unable to link with an external weather forecast resource" +
			"because of a wrongly specified time range");
		}
		String item_uri = getResourceUri(this.ld4sServer.getHostName(), "resource/weather_forecast", id);
		Resource item_resource = resource.getModel().createResource(item_uri);
		Context con = new Context(this.ld4sServer.getHostName());
		con.setTime_range(dates);
		con.setLocation(location);
		con.setLocation_coords(location_coords);
		item_resource = addLinkedData(item_resource, Domain.WEATHER, con);
		resource.addProperty(SptVocab.WEATHER_FORECAST, item_resource);
		return resource;
	}

	protected Resource addLocation(Resource resource, String location_name, String[] location_coords){
		String item_uri = null;
		if (location_name != null){
			item_uri = getResourceUri(this.ld4sServer.getHostName(), "resource/location", location_name);
		}else if (location_coords != null && location_coords.length > 0){
			String lc = "";
			for (int i =0;i<location_coords.length;i++){
				lc += location_coords[i];
				if (i+1<location_coords.length){
					lc += "_";
				}
			}
			item_uri = getResourceUri(this.ld4sServer.getHostName(), "resource/location", lc);
		}else{
			return resource;
		}
		Resource item_resource = resource.getModel().createResource(item_uri);
		Context con = new Context(this.ld4sServer.getHostName());
		con.setLocation(location_name);
		con.setLocation_coords(location_coords);
		try {
			item_resource = addLinkedData(item_resource, Domain.LOCATION, con);
			if (item_resource != null){
				resource.addProperty(
						resource.getModel().createProperty(
						"http://www.ontologydesignpatterns.org/ont/dul/DUL.owl/hasLocation"), 
						item_resource);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Unable to create Linked Data about the specified location.");
		}
		return resource;

		//		String[][] space_item = ov.getSpace();
		////	if (space_item != null && space_item.length > 0){
		////		String item_uri = null;
		////		//getResourceUri(this.ld4sServer.getHostName(), "space", item);
		//		Resource item_resource = null;
		////		//resource.getModel().createResource(item_uri);
		//		Context con = null;		
		//		if (space_item != null){
		//			for (int row=0; row<space_item.length ;row++){
		//				for (int col=0; col<space_item[row].length ;col++){
		//					//get which space rel is referred by checking the row
		//					if (row<Context.spaceRelations.length && space_item[row][col] != null){
		//						con = new Context(this.ld4sServer.getHostName());
		//						con.setThing(space_item[row][col]);
		//						item_resource = addLinkedData(item_resource, Domain.LOCATION, con);
		//						if (item_resource != null){
		//							resource.addProperty(
		//									resource.getModel().createProperty(Context.relation_sem[row]), 
		//									item_resource);
		//						}
		//					}else if (space_item[row][col] != null){
		//						con = new Context(this.ld4sServer.getHostName());
		//						con.setThing(space_item[row][col]);
		//						item_resource = addLinkedData(item_resource, Domain.LOCATION, con);
		//
		//						resource.addProperty(
		//								resource.getModel().createProperty(
		//								"http://www.ontologydesignpatterns.org/ont/dul/DUL.owl/hasLocation"), 
		//								item_resource);
		//					}
		//				}
		//			}
		//
		//		}
		////	}
	}

	protected Resource addFoi(Resource resource, String foi) throws Exception{
		if (foi.contains("weather")){
			resource = addWeatherForecast(resource);
		}else{
			String item_uri = getResourceUri(this.ld4sServer.getHostName(), "resource/property", foi);
			Resource item_resource = resource.getModel().createResource(item_uri);
			Context con = new Context(this.ld4sServer.getHostName());
			con.setThing(foi);
			item_resource = addLinkedData(item_resource, Domain.FEATURE, con);
			if (item_resource != null){
				resource.addProperty(SsnVocab.FEATURE_OF_INTEREST, item_resource);
			}
		}

		return resource;
	}

	protected Resource addObservedProperty (Resource resource, String observed_property,
			Property prop)
	throws Exception{
		String item_uri = getResourceUri(this.ld4sServer.getHostName(), "resource/property", observed_property);
		Resource item_resource = resource.getModel().createResource(item_uri);
		Context con = new Context(this.ld4sServer.getHostName());
		con.setThing(observed_property);
//		con.setAdditionalTerms(new String[][]{
//				{"", foi}
//		});
		item_resource = addLinkedData(item_resource, Domain.FEATURE, con);
		if (item_resource != null){
			resource.addProperty(prop, item_resource);
		}
		return resource;
	}

	protected Resource addUom(Resource resource, String uom) throws Exception{
		String item_uri = getResourceUri(this.ld4sServer.getHostName(), "resource/uom", uom);
		Resource item_resource = resource.getModel().createResource(item_uri);
		Context con = new Context(this.ld4sServer.getHostName());
		con.setThing(uom);
		item_resource = addLinkedData(item_resource, Domain.UNIT, con);
		if (item_resource != null){
			resource.addProperty(SptVocab.UOM, item_resource);
		}
		return resource;
	}
	
	public static Resource[] createDataLinkResource(Resource from_resource,
			String baseHost, Link link, Property linking_predicate, String to_uri){
		Resource to_resource = null;
		Resource[] ret = new Resource[]{to_resource, from_resource};
		if (link == null || link.getTo() == null || 
				linking_predicate == null || from_resource == null || baseHost == null){
			return ret;
		}
		try {			 		
			//check whether this link already exists
			Model model = null;
			if (to_uri == null){
				to_uri = LD4SDataResource.getResourceUri(
						baseHost, "link", 
						URLEncoder.encode(from_resource.getURI(), "utf-8")
						+"_"
						+
						URLEncoder.encode((new SimpleDateFormat
								("yyyy/MM/dd HH:mm:ss")).format(new Date())
								, "utf-8"));
			}
			//if not already existing, create it
			if (model == null){
				model = from_resource.getModel();
				to_resource = model.createResource(to_uri);
			}
			//2. add properties to the dataLink resource
			to_resource.addProperty(RDF.type, SptVocab.DATALINKING);
			to_resource.addProperty(SptVocab.LINK_TO, 
					model.createResource(link.getTo()));
			to_resource.addProperty(SptVocab.LINK_FROM, from_resource);
			
			if (link.getBytes() != -1){
				to_resource.addProperty(SptVocab.BYTES, 
						model.createTypedLiteral(String.valueOf(link.getBytes()),
						XSDDatatype.XSDlong));
			}
			if (link.getTitle() != null){
				to_resource.addProperty(SptVocab.TITLE, 
						model.createResource(link.getTitle()));
			}
			if (link.getDatetime() != null){
				to_resource.addProperty(DCTerms.temporal, 
						model.createTypedLiteral(link.getDatetime(),
								XSDDatatype.XSDdateTime));
			}
			if (link.getFeedbacks() != null){
				for (String uri: link.getFeedbacks()){
					to_resource.addProperty(RevVocab.HAS_FEEDBACK, 
							model.createResource(uri));
				}
			}
			//3. add the author to the dataLink resource
			if (link.getAuthor() != null && link.getAuthor().getUri() != null){
				to_resource.addProperty(DCTerms.creator, 
						model.createResource(link.getAuthor().getUri()));
			}
			//4. link the datalink resource back to the local resource by a predicate that
			//represents the reason why this link was created
			from_resource.addProperty(linking_predicate, to_resource);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.err.println("Unable to encode the from-resource during a data link instance creation");
		}

		return new Resource[]{to_resource, from_resource};
	}
	
	/**
	 * Links a subject resource to a person object by mean of a specifiable predicate.
	 * Afterwards, it adds linked data for this person, by searching for FOAF external resources.
	 * @param resource subject resource
	 * @param item person
	 * @param prop predicate
	 * @return resource representing the person
	 * @throws Exception
	 */
	protected Resource addPerson(Resource resource, Person item, Property prop) throws Exception{
		String id = null;		
		if (item.getEmail() != null && item.getEmail().trim().compareTo("") != 0){
			id = URLEncoder.encode(item.getEmail(), "utf-8");
		}else if (item.getWeblog() != null && item.getWeblog().trim().compareTo("") != 0){
			id = URLEncoder.encode(item.getWeblog(), "utf-8");
		}else if (item.getHomepage() != null && item.getHomepage().trim().compareTo("") != 0){
			id = URLEncoder.encode(item.getHomepage(), "utf-8");
		}else if (item.getNickname() != null && item.getNickname().trim().compareTo("") != 0){
			id = URLEncoder.encode(item.getNickname(), "utf-8");
		}else if (item.getFirstname() != null && item.getFirstname().trim().compareTo("") != 0 
				&& item.getSurname() != null && item.getSurname().trim().compareTo("") != 0){
			id = URLEncoder.encode(item.getFirstname()+item.getSurname(), "utf-8");
		}else{
			return null;
		}
		String item_uri = getResourceUri(this.ld4sServer.getHostName(), "resource/people", id);
		Resource item_resource = resource.getModel().createResource(item_uri);
		Context con = new Context(this.ld4sServer.getHostName());
		con.setPerson(item);
		item_resource = addLinkedData(item_resource, Domain.PEOPLE, con);
		if (item_resource != null){
			resource.addProperty(prop, item_resource);
		}
		return item_resource;
	}

	

	protected Object sparqlQueryExec(String queryString, 
			SparqlType type){
		Object ret = null;
		QueryExecution qexec = null;
		try {
			Query query = QueryFactory.create(queryString);
			initTDB();
			this.dataset.begin(ReadWrite.READ) ;
			//the following MUST be changed to query against eventually specified named graphs
			Model model = null;
			if (this.namedModel.endsWith("general")){
				model = dataset.getDefaultModel() ;
			}else{
				model = dataset.getNamedModel(this.namedModel);
			}
			qexec = QueryExecutionFactory.create(query, model);
			switch(type){
			case SELECT: 		
				ret = qexec.execSelect();
				break;
			case CONSTRUCT:
				ret = qexec.execConstruct();
				break;
			case DESCRIBE:
				ret = qexec.execDescribe();
				break;
			case ASK:
				ret = qexec.execAsk();
			default:
				ret = null;
			}
		} finally { 
			if (qexec != null)qexec.close();
			if (dataset != null)dataset.end() ;
			closeTDB();
		}

		return ret;	
	}

	/**
	 * Prints the dataset content with a limit of 100results, for test purposes.
	 */
	@SuppressWarnings("unused")
	private void testSparqlPrint(String namedModel){
		// A SPARQL query will see the new statement added.
		initTDB();
		this.dataset.begin(ReadWrite.READ) ;
		QueryExecution qExec = null;
		String query = null;
		ResultSet rs = null;
		try{
			query = //			           "SELECT (count(*) AS ?count) { ?s ?p ?o} LIMIT 10", 
				//			           dataset) ;
				//				"SELECT DISTINCT ?graph { GRAPH ?graph {?s ?p ?o}}";
				//				"SELECT * from named <http://192.168.56.1:8182/ld4s/graph/general> WHERE { ?s ?p ?o }";
				//				"SELECT * from <http://192.168.56.1:8182/ld4s/graph/ov> WHERE { ?s ?p ?o }";
				//				"SELECT * WHERE { graph <http://192.168.56.1:8182/ld4s/graph/ov> {?s ?p ?o }}limit 2";
				"select ?s ?p ?o from <http://lsm.deri.ie/metadata#> where {?s ?p ?o.} limit 20";
			System.out.println("Querying:\n"+query);
			qExec = QueryExecutionFactory.create(
					query, dataset) ;
			rs = qExec.execSelect() ;
			ResultSetFormatter.out(rs) ;

			query = //			           "SELECT (count(*) AS ?count) { ?s ?p ?o} LIMIT 10", 
				//			           dataset) ;
				//				"SELECT DISTINCT ?graph { GRAPH ?graph {?s ?p ?o}}";
				//				"SELECT * from named <http://192.168.56.1:8182/ld4s/graph/general> WHERE { ?s ?p ?o }";
				//				"SELECT * from <http://192.168.56.1:8182/ld4s/graph/ov> WHERE { ?s ?p ?o }";
				"SELECT * from <http://192.168.56.1:8182/ld4s/graph/ov> WHERE { ?s ?p ?o }limit 2";
			System.out.println("Querying:\n"+query);
			qExec = QueryExecutionFactory.create(
					query, dataset) ;
			rs = qExec.execSelect() ;
			ResultSetFormatter.out(rs) ;

			query = "SELECT distinct ?g WHERE { graph ?g {?s ?p ?o }}";
			System.out.println("Querying:\n"+query);
			qExec = QueryExecutionFactory.create(
					query, dataset) ;
			rs = qExec.execSelect() ;
			ResultSetFormatter.out(rs) ;

			namedModel = "http://localhost:8182/ld4s/graph/ov";
			query = //			           "SELECT (count(*) AS ?count) { ?s ?p ?o} LIMIT 10", 
				//			           dataset) ;
				"SELECT * { GRAPH <"+namedModel+"> {?s ?p ?o}} LIMIT 100";
			System.out.println("Querying:\n"+query);
			qExec = QueryExecutionFactory.create(
					query, dataset) ;
			rs = qExec.execSelect() ;
			ResultSetFormatter.out(rs) ;


			//			query = "SELECT *  FROM <localhost:8182/ld4s/ov> {?s ?p ?o} LIMIT 100";
			//			System.out.println("Querying:\n"+query);
			//			qExec = QueryExecutionFactory.create(
			//					query, dataset) ;
			//			rs = qExec.execSelect() ;
			//			ResultSetFormatter.out(rs) ;


		}catch(Exception e){
			e.printStackTrace();
		} finally { 
			if (qExec != null)qExec.close();
			if (dataset != null)dataset.end() ;
			closeTDB();
		}



		//
		//			     // ... perform a SPARQL Update
		//			     GraphStore graphStore = GraphStoreFactory.create(dataset) ;
		//			     String sparqlUpdateString = StrUtils.strjoinNL(
		//			          "PREFIX . <http://example/>",
		//			          "INSERT { :s :p ?now } WHERE { BIND(now() AS ?now) }"
		//			          ) ;
		//
		//			     UpdateRequest request = UpdateFactory.create(sparqlUpdateString) ;
		//			     UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore) ;
		//			     proc.execute() ;
	}

	protected Model retrieve (String uri, String namedModel){
		if (uri == null){
			return null;
		}
		if (namedModel == null){
			namedModel = generalNamedModel;
		}
		Model ret = ModelFactory.createDefaultModel();
		ret.createResource(uri);
		initTDB();
		this.dataset.begin(ReadWrite.READ) ;
		try {
			if (!dataset.containsNamedModel(namedModel)){
				return null;
			}
			Model model = dataset.getNamedModel(namedModel) ;
			StmtIterator retit = model.listStatements(model.createResource(uri), null, (RDFNode)null);
			while (retit.hasNext()){
				ret.add(retit.next());
			} 
			dataset.commit() ;
			// Or call .abort()
		}catch(Exception e){
			e.printStackTrace();
		}  finally { 
			dataset.end() ;
			closeTDB();
		}
//		testSparqlPrint(namedModel);
		return ret;
	}

	protected Representation serializeAccordingToReqMediaType(Model rdfData){
		String str_rdfData = null;
		if (requestedMedia == null){
			requestedMedia = MediaType.APPLICATION_RDF_XML;
		}
		if (requestedMedia.getName().equalsIgnoreCase(LD4SConstants.MEDIA_TYPE_RDF_JSON)) {
			str_rdfData = serializeRDFModel(rdfData, LD4SConstants.RESOURCE_URI_BASE,
					LD4SConstants.LANG_RDFJSON);
		}
		else if (requestedMedia.equals(MediaType.APPLICATION_RDF_XML)) {
			str_rdfData = serializeRDFModel(rdfData, LD4SConstants.RESOURCE_URI_BASE,
					LD4SConstants.LANG_RDFXML);
		}
		else if (requestedMedia.equals(MediaType.TEXT_RDF_NTRIPLES)) {
			str_rdfData = serializeRDFModel(rdfData, LD4SConstants.RESOURCE_URI_BASE,
					LD4SConstants.LANG_NTRIPLE);
		}else if (requestedMedia.equals(MediaType.TEXT_ALL) 
				|| requestedMedia.equals(MediaType.TEXT_RDF_N3)
				|| requestedMedia.equals(MediaType.APPLICATION_RDF_TURTLE)
				|| requestedMedia.equals(MediaType.APPLICATION_ALL)
				|| requestedMedia.equals(MediaType.ALL)
				)			
		{
			str_rdfData = serializeRDFModel(rdfData, null, LD4SConstants.LANG_TURTLE);
		}else{
			setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
			return null;
		}
		Representation ret = getStringRepresentationFromRdf(str_rdfData, requestedMedia);
		try {
			this.getLogger().info("***RESPONSE***" +ret.getText());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * Creates a new type in the LD4S vocabulary IFF in case neither in there nor in the 
	 * SPITFIRE-sn vocabulary there is a resource having the specified
	 * type contained in its rdf:label 
	 * 
	 * @param type
	 * @return either the newly created type (class) or an already existing one matching with 
	 * the requests
	 * @throws IOException
	 */
	protected Resource createNewType(String type){
		//search whether the type requested is already existing in the local vocabulary
		Statement st = null;
		Resource subj = null;
		Literal lit = null;
		Model vocab = ModelFactory.createDefaultModel();

		//in the ld4s vocabulary
		initModel(vocab, "ld4s.rdf");
		StmtIterator it = vocab.listStatements(null, RDFS.label, (RDFNode)null);
		while (it.hasNext() && subj == null){
			st = it.nextStatement();
			if (st.getObject().isLiteral()){
				lit = (Literal)st.getObject();
				if (lit.getLexicalForm().toLowerCase().contains(type.toLowerCase())){
					subj = st.getSubject();
				}
			}
		}
		//or in the SPITFIRE-sn one
		if (subj == null){
			initModel(vocab, "spt_sn.rdf");
			it = vocab.listStatements(null, RDFS.label, (RDFNode)null);
			while (it.hasNext() && subj == null){
				st = it.nextStatement();
				if (st.getObject().isLiteral()){
					lit = (Literal)st.getObject();
					if (lit.getLexicalForm().contains(type)){
						subj = st.getSubject();
					}
				}
			}
		}
		if (subj != null){
			return subj;
		}
		//not found then just create a new one in the local ld4s vocabulary
		Resource newtype = vocab.createResource(this.ld4sServer.getHostName()
				+"ns/"+type.replaceAll(" ", "_"));
		newtype.addProperty(RDF.type, RDFS.Class);
		try {
			saveVocabEditsToFile(vocab, "ld4s.rdf");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return newtype;
	}

	/**
	 * Remove from the triple db any triple that matches subject and predicate
	 * of those triples in the given model
	 * @param rdfData model containing the subj-pred couples to be matched and removed
	 * @return success
	 */
	protected boolean delete(Model rdfData, String namedModel){
		boolean ret = true;
		initTDB();
		this.dataset.begin(ReadWrite.WRITE) ;

		try {
			if (!dataset.containsNamedModel(namedModel)){
				return true;
			}
			Model model = dataset.getNamedModel(namedModel) ;
			StmtIterator iter = rdfData.listStatements();
			Statement stmt = null;
			Resource  subject = null;
			Property  predicate = null;
			while (iter.hasNext()) {
				stmt = iter.nextStatement();  // get next statement
				subject   = stmt.getSubject();     // get the subject
				predicate = stmt.getPredicate();   // get the predicate
				model.removeAll(subject, predicate, null);
			}
			dataset.commit() ;
			// Or call .abort()
		}catch(Exception e){
			e.printStackTrace();
			ret = false;
		}  finally { 
			dataset.end() ;
			closeTDB();
		}
//		testSparqlPrint(namedModel);
		return ret;
	}

	/**
	 * Remove from the triple db those triples that match subj-pred of those in the given model
	 * and adds new ones with the given values
	 * @param rdfData model containing the subj-pred couples to be updated in their values
	 * @return success
	 */
	protected boolean update(Model rdfData, String namedModel){
		delete(rdfData, namedModel);
		return store(rdfData, namedModel);
	}

	protected boolean delete(String uri, String namedModel){
		boolean ret = true;
		initTDB();
		this.dataset.begin(ReadWrite.WRITE) ;

		try {
			if (!dataset.containsNamedModel(namedModel)){
				return true;
			}
			Model model = dataset.getNamedModel(namedModel);
			model.removeAll(model.createResource(uri), null, null);
			dataset.commit() ;
			// Or call .abort()
		}catch(Exception e){
			e.printStackTrace();
			ret = false;
		}  finally { 
			dataset.end() ;
			closeTDB();
		}
//		testSparqlPrint(namedModel);
		return ret;
	}
}
