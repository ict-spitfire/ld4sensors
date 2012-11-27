<?php 
require_once 'template.php';
$title = 'inContext Sensing | LD4Sensors 2.0 - Changes from vers. 1.0';
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
<h2> LD4Sensors (LD4S) 2.0 - Changes from vers. 1.0</h2>

<ul>
<li>SameAs links <i>automatically</i> provided <br />
for specific features that need to be annotated, to external resources. 
The features for which this automatic search for external links is available, are:
<ul><li>Location: links with <a href="http://geonames.org/" target="_blank">Geonames</a>, 
from which it gathers data about: population, 
administrative districts, nearby points of interest, country, latitude and longitude;</li>
<li>Unit of Measurement: links with a repository of 
<a href="http://unitsofmeasure.org/" target="_blank">Unified Code for Units of Measure</a>, 
from which it gathers data about: measured property, preferred symbol; </li> 
<li>Observed Property (also known as capability) and Feature of Interest: links with 
<a href="http://dbpedia.org" target="_blank">DBpedia</a>, from which it gathers data about: 
application domains (e.g., <i>thermodinamics</i> in case of <i>temperature</i> 
as an observed property);</li>
<li>People and Organizations: links with 
<a href="http://citeseer.rkbexplorer.com" target="_blank">Citeseer</a>, from which it gathers
data about publications; links with 
<a href="http://www.foaf-project.org/" target="_blank">FOAF</a> profiles.</li></ul>
In case LD4Sensors was not able to find a linkable resource by using any of the above 
methodologies, it would attempt a simple, term-based, search on 
<a href="http://sindice.com/" target="_blank">Sindice</a>.</li>
<li>Mereological links provided <i>on-demand</i><br />
for any kind of resources. <i>Mereological</i> is a broad term used because it is actually
possible to make this general relationship as specific as preferred, by specifying criteria
that need to be matched in order for an external resource to be linked with the local one.
Such criteria can be: <ul><li>Application Domain: a link is searched for only among those 
datasets coming from this application domain. The supported ones are those identified in the
LOD cloud, i.e. Geography, Government, User-generated-content, Life Science,
Publication, Cross-domain.</li> <li>Context in terms of 
<ul><li>Time</li><li>Space</li><li>Subject Thing.</li></ul></li></ul> 
</li>
<li>Comments and Feedbacks on links enabled.<br />
The link with external resources is created indirectly. The act of linking is considered
a resource on its own, so that it can be commented, rated and improved.</li>
<li>Storage capability: Triple DB<br />
for resources that need to be created to complete the requested semantic annotation task
but are others than the subject of the requested annotation. In this way all the URIs
referenced in the provided annotation will be dereferenceable, as stated in the Linked 
Data principles. 
in the annotation</li>
<li>Availability to choice to store the requested semantic annotation or not.</li>
<li>Availability to choice to add mereological links with external resources according to 
use-case specific criteria, or not.</li>
<li>The above improvements resulted in a full adherence with both Linked Data and 
RESTful principles.</li>
<li> Last version of the SPITFIRE ontology (
<a href="http://spitfire-project.eu/ontology/ns">spt</a>) used<br />
i.e. alignment with social, provenance, IETF CoRE WG, Quantity and Measurement vocabularies; 
introduction of network components and energy related concepts. </li>
</ul>
</p>             
<?php 
$copyright_uri = 'http://www.deri.ie';
$copyright_name = 'DERI';
printFooter($copyright_uri, $copyright_name);
?>
 
