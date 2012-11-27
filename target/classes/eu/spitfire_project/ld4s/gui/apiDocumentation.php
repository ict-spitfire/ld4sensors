<?php 
require_once 'template.php';
$title = 'inContext Sensing | LD4Sensors 2.0 - REST API Specification';
printHeader($title);
?>
</head>
<?php 
$activeMenuItemNum = 2;
$body_onload = '';
$sidebar = '<div class="gadget">
<h2 class="star"><span>What</span></h2>
<div class="clr"></div>
Generate a RDF description for single sensor nodes.<br /> 
The returned description is split into sections according to the 
dependency of the included information, and "where" to store it is suggested. 
This service is available through either the manually fillable form here
or a <a href="apiDocumentation.php">RESTful API</a>.</div>
<div class="gadget">
<h2 class="star"><span>Why</span></h2>
<div class="clr"></div>
The unambiguity and schema-indepenedency features that characterize
RDF, are the key to enable 
<ul>
	<li>plug-and-play sensor nodes</li>
	<li>interoperability among different sensors</li>
</ul>
</div>';

printBodyTop($activeMenuItemNum, $sidebar, $body_onload);
?>
<h2>REST API Specification - LD4Sensors (LD4S) 2.0</h2>
<p>
<a href="changes.php" target="_blank">Changes from version 1.0:</a>
 <p>
      <p>Methods, resources and descriptions, together with suggestions on 
      which requests should be submitted
      and details on the accepted payload, follow below.<br />
      <strong><a href="http://spitfire-project.eu/incontextsensing/ld4s_samples.txt">Download</a> sample responses.</strong>
      As a payload, currently JSON and HTML Form are supported but only JSON has been thoroghfully
      tested, thus far.<br />
      PUT, POST and GET requests will always return the requested semantic annotation.<br />
      Any of the RDF existing serializations is supported (please specify it as a preferred
      media type in the HTTP header of your request).<br />
      Generally speaking, this API strictly adhere to the RESTful principles
      (see esp. the 
      <a href="https://jcalcote.wordpress.com/2008/10/16/put-or-post-the-rest-of-the-story/">
      PUT or POST argumentation</a>)so that the following rules re HTTP methods apply:
      <ul>
      <li>PUT: request for semantically annotating the submitted data
      and store the results. Such data are all the ones that constitute the 
      subject of the annotation. No mereological links can be added.
      The annotation is not already existing, otherwise it get fully deleted and substituted.</li>
      <li>POST: request for semantically annotating the submitted data
      and either store ot not te result, which will be returned as a response, in any case. 
      Such data might not be all the ones that constitute the subject of the annotation.
      Mereological links can be added on demand. The annotation might already exists;
      in this case only the submitted data will get to be replaced with the new annotation 
      rather than the whole resouce.</li>
      <li>DELETE: request for deleting a semantic annotation that had already been stored.</li>
      <li>GET: request for getting a semantic annotation that had already been stored.
      Mereological links can be added on demand.</li>
      </ul>
      <string>Each of the data submitted as a payload is OPTIONAL.</strong><br />
      In this specification such data has been associated with different requests to
      different resources only as a 
      convention and best practice recommendation. However, although data can be omitted 
      - apart from the cross-resources data (described below) which can
      always be added to the payload and receive semantic annotation - any other additional data
      eventually submitted, will not be annotated. 
      </p>
      
      <h4> Cross-resources</h4>
<p>Features that can be added in a JSON payload for a request addressed
	to any of the resources described in the following paragraphs.</p>
<ul>
      <li>Preferred resource type. In case a type different than the one provided by LD4S per default,
      is preferred, it can be specified. This will be considered only if a match is found in the 
      one of the SPITFIRE ontology modules (e.g., 
      <a href="http://spitfire-project.eu/ontology/ns/sn">sn</a> 
      and <a href="http://spitfire-project.eu/ontology/ns/ct">ct</a>).<br />
      <strong>Sample JSON payload:<br />
      
      </strong></li>
      <li>Remote URI. Whenever the user wishes a resource annotation not to be stored 
      on the LD4S TDB (thus receiving an {ld4s_host}/{resource_name}/{resource_id} type of URI ),
      the remote URI must be specified in the JSON payload.<br />
      <strong>Sample JSON payload:<br />
      {"uri":["http://www.example.org/device/remotea12b"]}
      </strong></li>
      <li>Location. latitude_longitude OR name<br />
      <strong>Sample JSON payload:<br />
      {"location-name":["Patras"],
      "location-coords":["38.24444_21.73444"]}
      </strong></li>
      <li>Time. Base datetime (as xsd:dateTime) 
      AND ((start datetime + end datetime) OR time point)(as either xsd:dateTime or 
      xsd:long millisecond shifts from the base time;
      see IETF CoRE Specification for a rationale about the ms shift). <br />
      <strong>Sample JSON payload:<br />
      {"base_datetime":["12-08-28T19:03Z"]}
      </strong></li>
      <li>Historical Archive URI<br />
      <strong>Sample JSON payload:<br />
      {"archive":["http://www.example.org/archive/testbed1"]}
      </strong></li>
      <li>Person or Entity in charge of the data generation<br />
      <strong>Sample JSON payload:<br />
      "author":[{"surname":["Leggieri"],"firstname":["Myriam"],
      "email":["myriam.leggieri@deri.org"],
      "homepage":["http://myriamleggieri.webs.com"],
      "nickname":["iammyr"],
      "weblog":["http://myriamleggieri.webs.com/apps/blog/"]}]
      </strong></li>
      <li>Generic description<br />
      <strong>Sample JSON payload:<br />
      {"description":["Generic description content."]}
      </strong></li>
      <li>Linking criteria for the mereological links creation. Any amount of nested
      logic expressions is supported, as long as using either AND or OR operators and 
      stated in prefix notation. For location data, the predicates IN, UNDER, OVER, NEAR 
      and OF (meant as "part of") are accepted. <br />
      <strong>Sample JSON payload:<br />
      {"context":[
      "d=crossdomain%20OR%20geography
	&s=NEAR(OR(shop1, shop2,shop3))UNDER(OR(home,lower dangan,
	AND(ireland, OR(palace, building), galway),galway county))
	OVER(AND(floor,garden,OR(metro,train),sky))
	&th=OR(red,AND(cotton,tshirt),tissue,dress)"
      ]}
      </strong></li> 
      </ul>
      
      <p>Resources:
    <ul>
    <li><a href="#ov">Observation Value</a></li>
    <li><a href="#dev">Device, Sensing Device, Sensor</a></li>
    <li><a href="#stp">Sensor Temporal Property</a></li>
    <li><a href="#p">Platform, Testbed</a></li>
    <li><a href="#ptp">Platform Temporal Property</a></li>
    <li><a href="#meas-capab">Measurement Capability</a></li>
    <li><a href="#meas-prop">Measurement Property</a></li>
    <li><a href="#dl">Data Link</a></li>
    <li><a href="#lrev">Link Review</a></li>
    <li><a href="#other">Others</a></li>
    </ul>
    
    </p>  
      <h4> <a name="ov">Observation Value </a></h4>
      Features to annotate:
      <ul>
      <li>Time: point or range (start, end)</li>
      <li>Value: single or multiple.</li>
      </ul>
     URI and Methods:<br />
      POST | {host}/ov | resource stored remotely<br />
GET | {host}/ov/{resource_id} | resource stored locally + no mereological links enrichment<br />
		PUT | {host}/ov/{resource_id} | resource stored locally + no mereological links enrichment<br />
		POST | {host}/ov/{resource_id} | resource stored locally + yes/no mereological links enrichment (either set or not set "context" field in the JSON payload)<br />
				DELETE | {host}/ov/{resource_id} | resource stored locally<br />
				GET | {host}/ov/{resource_id}?d={domains}&nod={nodomains}&trange={time}&s={space}&th={thing}
				 | resource stored locally + yes/no mereological links enrichment (either set or not set "context" field in the JSON payload) 
     
    
      <strong>Sample JSON payload:</strong><br />
      <ul><li>{"values":[["12.4","21.9","88.7","24.5"]],"start_range":["5800"],
      "end_range":["10321"]}
      </li>
      <li>
      {"values":[["12.4"]],
      "resource_time":["22846"]}
      </li>
      <li>
      {"values":[["12.4","21.9","88.7","24.5"]],"base_time":["12-08-23T19:03Z"]}
      </li>
      <li>
      </li>
      </ul>
     
      
      <h4> <a name="dev">Device, Sensing Device and Sensor </a></h4>
      Features to annotate:
      <ul>
      <li>Base time: dateTime</li>
      <li>Base name: eventual URI to be added as a prefix to the resource ID.</li>
      <li>Base name for the Observation Value instances that this device generates.</li>
      <li>Observation Value instances that this device generates.</li>
      <li>Observed Property.</li>
      <li>Unit of Measurement.</li>
      <li>Sensor Temporal Properties URIs (tsproperties)</li>
      
      </ul>
      
      URI and Methods:<br />
      POST | {host}/device | resource stored remotely<br />
GET | {host}/device/{resource_id} | resource stored locally + no mereological links enrichment<br />
		PUT | {host}/device/{resource_id} | resource stored locally + no mereological links enrichment<br />
		POST | {host}/device/{resource_id} | resource stored locally + yes/no mereological links enrichment (either set or not set "context" field in the JSON payload)<br />
				DELETE | {host}/device/{resource_id} | resource stored locally<br />
				GET | {host}/device/{resource_id}?d={domains}&nod={nodomains}&trange={time}&s={space}&th={thing}
				 | resource stored locally + yes/no mereological links enrichment (either set or not set "context" field in the JSON payload) 
      <strong>Sample JSON payload:</strong><br />
      <ul><li>
      {"tsproperties":[["id123","id456","id789","id101"]],<br />
      "observation_values":[["ov123","ov456","ov789","ov101"]],<br />
      "base_ov_name":["http://www.example1.org/ov/"],<br />
      "observed_property":["temperature"],<br />
      "uri":["http://www.example.org/device/remotea12b"],<br />
      "base_datetime":["12-08-28T19:03Z"],<br />
      "author":[{"surname":["Theodoridis"],"firstname":["Evangelos"]}],<br />
      "uom":["centigrade"]}      
      </li>
      </ul>
      

   <h4> <a name="stp">Sensor Temporal Property  </a></h4>
      Features to annotate:
      <ul>
      <li>Time range of validity: start datetime, end datetime</li>
      <li>Feature of Interest.</li>
      <li>URIs of opened network links.</li>
      <li>Network role.</li>
      <li>Location.</li>
      <li>Author (entity in charge of the data production)</li>
      </ul>
      
      URI and Methods:<br />
      POST | {host}/tps | resource stored remotely<br />
GET | {host}/tps/{resource_id} | resource stored locally + no mereological links enrichment<br />
		PUT | {host}/tps/{resource_id} | resource stored locally + no mereological links enrichment<br />
		POST | {host}/tps/{resource_id} | resource stored locally + yes/no mereological links enrichment (either set or not set "context" field in the JSON payload)<br />
				DELETE | {host}/tps/{resource_id} | resource stored locally<br />
				GET | {host}/tps/{resource_id}?d={domains}&nod={nodomains}&trange={time}&s={space}&th={thing}
				 | resource stored locally + yes/no mereological links enrichment (either set or not set "context" field in the JSON payload) 
     <strong>Sample JSON payload:</strong><br />
      <ul><li>
      {"net_links":[["124","219","887","245"]],
      "net_role":["http://www.example.org/role/clusterhead"],
      "foi":["water"],
      "author":[{"surname":["Theodoridis"],"firstname":["Evangelos"]}],
      "location-name":["Patras"],
      "location-coords":["38.24444_21.73444"]},
      </li>
      </ul>
      
      
      <h4> <a name="p">Platform, Testbed  </a></h4>
      Features to annotate:
      <ul>
      <li>Base time: dateTime</li>
      <li>Base name: eventual URI to be added as a prefix to the resource ID.</li>
      <li>Feed URIs.</li>
      <li>Status Page URI</li>
      <li>Archive URI: where historical data produced-by or related-with the devices 
      attached to this platform, are stored.</li>
      <li>Platform Temporal Properties URIs (tpproperties)</li>
      <li>Location (which might be different than the one of the attached devices)</li>
      </ul>
      URI and Methods:<br />
      POST | {host}/platform | resource stored remotely<br />
GET | {host}/platform/{resource_id} | resource stored locally + no mereological links enrichment<br />
		PUT | {host}/platform/{resource_id} | resource stored locally + no mereological links enrichment<br />
		POST | {host}/platform/{resource_id} | resource stored locally + yes/no mereological links enrichment (either set or not set "context" field in the JSON payload)<br />
				DELETE | {host}/platform/{resource_id} | resource stored locally<br />
				GET | {host}/platform/{resource_id}?d={domains}&nod={nodomains}&trange={time}&s={space}&th={thing}
				 | resource stored locally + yes/no mereological links enrichment (either set or not set "context" field in the JSON payload) 
      <strong>Sample JSON payload:</strong><br />
      <ul><li>
      {"tpproperties":[["id123","id456","id789","id101"]],<br />
      "base_datetime":["12-08-28T19:03Z"],<br />
      "author":[{"surname":["Theodoridis"],"firstname":["Evangelos"]}],<br />
      "location-name":["Patras"],<br />
      "location-coords":["38.24444_21.73444"]},<br />
      "base_name":["http://www.example2.org/device/"]}<br />      
      </li>
      </ul>
           
      <h4> <a name="ptp">Platform Temporal Property</a></h4>
      Features to annotate:
      <ul>
      <li>Time range of validity: start datetime, end datetime</li>
      <li>Attached devices URIs</li>
      <li>Algorithms in use</li>
      <li>Owners</li>
      <li>Carriers (entities that are transporting the platform)</li>
      </ul>
      URI and Methods:<br />
      POST | {host}/tpp | resource stored remotely + yes/no mereological links enrichment (either set or not set "context" field in the JSON payload)<br />
GET | {host}/tpp/{resource_id} | resource stored locally<br />
		PUT | {host}/tpp/{resource_id} | resource stored locally + no mereological links enrichment<br />
		POST | {host}/tpp/{resource_id} | resource stored locally + yes/no mereological links enrichment (either set or not set "context" field in the JSON payload)<br />
				DELETE | {host}/tpp/{resource_id} | resource stored locally<br />
				GET | {host}/tpp/{resource_id}?d={domains}&nod={nodomains}&trange={time}&s={space}&th={thing}
				 | resource stored locally + yes/no mereological links enrichment (either set or not set "context" field in the JSON payload) 
	<strong>Sample JSON payload:</strong><br />
      <ul><li>
      {
      "systems":[["http://www.example.org/device/2","http://www.example.org/device/1"]],<br />
      "algorithms":[["http://www.example.org/alg/45","http://www.example.org/alg/18"]],<br />
      "worn_by":[[<br />
      [{"surname":["Hausenblas"],"firstname":["Michael"]}],<br />
      [{"surname":["Amaxilatis"],"firstname":["Dimitrios"]}]<br />
      ]],<br />
      "owners":[[<br />
      [{"surname":["Leggieri"],"firstname":["Myriam"]}],<br />
      [{"surname":["Hauswirth"],"firstname":["Manfred"]}]<br />
      ]],<br />
      "author":[{"surname":["Theodoridis"],"firstname":["Evangelos"]}],<br />
      "location-name":["Patras"],<br />
      "location-coords":["38.24444_21.73444"]},<br />
      "start_range":["5800"],<br />
      "end_range":["10321"],<br />
      }      
      </li>
      </ul>  
        
           <h4> <a name="meas-capab">Measurement Capability</a></h4>
      Features to annotate:
      <ul>
      <li>Property this capability refers to (e.g. Humidity)</li>
      <li>Measurement Property URIs</li>
      </ul>
      URI and Methods:<br />
      POST | {host}/meas_capab | resource stored remotely + yes/no mereological links enrichment (either set or not set "context" field in the JSON payload)<br />
GET | {host}/meas_capab/{resource_id} | resource stored locally<br />
		PUT | {host}/meas_capab/{resource_id} | resource stored locally + no mereological links enrichment<br />
		POST | {host}/meas_capab/{resource_id} | resource stored locally + yes/no mereological links enrichment (either set or not set "context" field in the JSON payload)<br />
				DELETE | {host}/meas_capab/{resource_id} | resource stored locally<br />
				GET | {host}/meas_capab/{resource_id}?d={domains}&nod={nodomains}&trange={time}&s={space}&th={thing}
				 | resource stored locally + yes/no mereological links enrichment (either set or not set "context" field in the JSON payload) 
	<strong>Sample JSON payload:</strong><br />
      <ul><li>
      {
      "measurement_properties":[["http://www.example.org/meas_prop/accuracy","http://www.example.org/meas_prop/precision","http://www.example.org/meas_prop/recall"]],<br />
      "observed_property":["humidity"]}
      </li>
      </ul>  
      
       <h4> <a name="meas-prop">Measurement Property</a></h4>
      Features to annotate:
      <ul>
      <li>predicate (e.g. http://spitfire-project.eu/ontology/ns/hasValueRange)</li>
      <li>value (e.g. 0.5_3.2)</li>
      <li>unit of measurement (e.g. centigrade)
      <li>conditions that apply on this property, i.e. JSONArray of:
     <ul>
      <li>on condition property (e.g. pressure)</li>
      <li>on condition predicate (e.g. http://spitfire-project.eu/ontology/ns/hasMaxValue) </li>
      <li>on condition value (e.g. 20.0)</li>
      <li>on condition unit of measurement (e.g. pascal) </li>
      </ul>
      </li>
      
      
      </ul>
      URI and Methods:<br />
      POST | {host}/meas_prop | resource stored remotely + yes/no mereological links enrichment (either set or not set "context" field in the JSON payload)<br />
GET | {host}/meas_prop/{resource_id} | resource stored locally<br />
		PUT | {host}/meas_prop/{resource_id} | resource stored locally + no mereological links enrichment<br />
		POST | {host}/meas_prop/{resource_id} | resource stored locally + yes/no mereological links enrichment (either set or not set "context" field in the JSON payload)<br />
				DELETE | {host}/meas_prop/{resource_id} | resource stored locally<br />
				GET | {host}/meas_prop/{resource_id}?d={domains}&nod={nodomains}&trange={time}&s={space}&th={thing}
				 | resource stored locally + yes/no mereological links enrichment (either set or not set "context" field in the JSON payload) 
	<strong>Sample JSON payload:</strong><br />
      <ul><li>
      {<br />      
      "uom":["sec"],<br />
      "predicate":["http://spitfire-project.eu/ontology/ns/hasValueRange"],<br />
      "value":["0.5_1"],<br />
      "uri":["http://www.example.org/meas_prop/latency"]<br />
      
      "conditions":<br />
      [[<br />
      {"oncondition_uom":["g/m3"],<br />
      "oncondition_value":["0.8_2"],<br />
      "oncondition_predicate":["http://spitfire-project.eu/ontology/ns/hasValueRange"],<br />
      "oncondition_property":["humidity"]},<br /><br />
      
      {"oncondition_uom":["bar"],<br />
      "oncondition_value":["0.9"],<br />
      "oncondition_predicate":["http://spitfire-project.eu/ontology/ns/hasMaxValue"],<br />
      "oncondition_property":["pressure"]}<br />
      ]],<br /><br />
      
      }<br />
      </li>
      </ul>  
      
      <h4> <a name="dl">Data Link</a></h4>
     Features to annotate:
      <ul>
      <li>From (URI)</li>
      <li>To (URI)</li>
      <li>Title</li>
      <li>Feedback URIs (URIs of the LinkReview for this data link)</li>
      
       <li>URI and Methods (no mereological links enrichment is allowed):<br />
      POST | {host}/link | resource stored remotely<br />
GET | {host}/link/{resource_id} | resource stored locally<br />
		PUT | {host}/link/{resource_id} | resource stored locally<br />
		POST | {host}/link/{resource_id} | resource stored locally<br />
				DELETE | {host}/link/{resource_id} | resource stored locally	
      </li>
      <li><strong>Sample JSON payload:</strong><br />
      <ul><li>
         {
         "to":["http://www.example.com/to/test2.rdf"],<br />
         "title":["Sample Data Link"],<br />
         "updated":["12-08-23T19:03Z"],<br />
         "feedbacks":[["http://www.example.org/link/remotea12b/feedback1","http://www.example.org/link/remotea12b/feedback2","http://www.example.org/link/remotea12b/feedback3"]],<br />
         "from":["http://www.example.com/from/test1.rdf"],<br />
		}
         
      </li>
      </ul>
      
      
         <h4> <a name="lrev">Link Review</a></h4>
     Features to annotate:
      <ul>
      <li>DataLink URI</li>
      <li>Vote (double)</li>
      <li>Comment (string)</li>
      <li>Author (as described here in the Cross-resources section)</li>
      
       <li>URI and Methods (no mereological links enrichment is allowed):<br />
      POST | {host}/link/feedback | resource stored remotely<br />
GET | {host}/link/feedback/{resource_id} | resource stored locally<br />
		PUT | {host}/link/feedback/{resource_id} | resource stored locally<br />
		POST | {host}/link/feedback/{resource_id} | resource stored locally<br />
				DELETE | {host}/link/feedback/{resource_id} | resource stored locally	
      </li>
      <li><strong>Sample JSON payload:</strong><br />
      <ul><li>
         {
         "author":[{"surname":["Hauswirth"],"firstname":["Manfred"]}],<br />
         "vote":[2],<br />
         "link":["http://localhost:8182/ld4s/link/a12b"],<br />
         "comment":["Great Data Link!"]<br />
         }
         
      </li>
      </ul>
      
      <h4> Any Other URI created by LD4S during the annotation process</h4>
      URI and Methods:<br />
      GET | {host}/resource/{resource_name}/{resource_id} | resource stored locally + no mereological links enrichment<br />
		Available {resource_name} are: property (used for feature of interests and observed properties);
		uom (used for units of measurement); location (for locations); people (for agents).
      
          
             
<?php 
$copyright_uri = 'http://www.deri.ie';
$copyright_name = 'DERI';
printFooter($copyright_uri, $copyright_name);
?>
 
