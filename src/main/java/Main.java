import java.io.File;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import eu.spitfire_project.ld4s.vocabulary.LD4SConstants;


public class Main {
	public static void main (String args[]) {
		String SOURCE = "http://spitfire-project.eu/ontology.rdf",
				SOURCE1 = "http://spitfire-project.eu/sn.rdf",
				NS = "http://spitfire-project.eu/ontology/ns/",
				NS1 = "http://spitfire-project.eu/ontology/ns/sn/";

		//create a model using reasoner
		OntModel model1_reasoner = 
				ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		OntModel model_instances = 
				ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		//create a model which doesn't use a reasoner
		OntModel model2_noreasoner = 
				ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM);

		/**===========1. STORE IN A NON-OwlFull MODEL==============**/		
		// read the RDF/XML file
		//        model1_reasoner.read( SOURCE, "RDF/XML" );
		//        model1_reasoner.read(SOURCE1, "RDF/XML");

		//		model2_noreasoner.read( SOURCE, "RDF/XML" );

		//		model2_noreasoner.read(SOURCE1, "RDF/XML");
		model1_reasoner.add(model1_reasoner.createResource(NS+"containedIn"), 
				RDF.type, OWL.TransitiveProperty);
		
		
		//add the instances
		//        model1_reasoner.add(model1_reasoner.createResource(NS+"fan123"), RDF.type, 
		//        		model1_reasoner.createResource(NS1+"Fan"));
//		model_instances.add(model_instances.createResource(NS+"fan123"), RDF.type, 
//				model_instances.createResource(NS1+"Fan"));
		
		model_instances.add(model_instances.getProperty(NS+"containedIn"),
				OWL.equivalentProperty,
				model_instances.createProperty("http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#hasLocation"));
		model_instances.add(model_instances.createResource(NS+"desk_a"),
				model_instances.getProperty(NS+"containedIn"),
				model_instances.createResource(NS+"floor3"));
		model_instances.add(model2_noreasoner.createResource(NS+"floor3"), 
				model_instances.getProperty(NS+"containedIn"), 
				model_instances.createResource(NS+"cti"));
		//		model1_reasoner.add(model2_noreasoner);


		model1_reasoner.add(model2_noreasoner);
		
		//prints out the RDF/XML structure
		printModel(model1_reasoner, null);
		printModel(model1_reasoner, model1_reasoner.getProperty(NS+"containedIn"));
//		printModel(model2_noreasoner);


		/**===========2. STORE IN THE TDB==============**/
		// Direct way: Make a TDB-backed dataset
		String directory = "/home/iammyr/.ld4s/tdb"
				+LD4SConstants.SYSTEM_SEPARATOR+"LD4SDataset1" ;
		File dirf = new File (directory);
		if (!dirf.exists()){
			dirf.mkdirs();
		}
		Dataset dataset = TDBFactory.createDataset(directory) ;
		TDB.sync(dataset ) ;

		Resource subj = model1_reasoner.listSubjects().next();
		dataset.begin(ReadWrite.WRITE) ;		
		try {
			dataset.addNamedModel(subj.getURI(), model1_reasoner);
			dataset.addNamedModel(NS+"desk_a", model_instances);
			dataset.commit() ;
			
			// Or call .abort()
		}catch(Exception e){
			e.printStackTrace();
		}  finally { 
			dataset.end() ;
			dataset.close();
		}

		/**===========3. QUERY==============**/
		// Create a new query
		String queryString =        
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
						"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "+
						"PREFIX dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#>  "+
						"PREFIX spt: <"+NS+">  "+
						"select ?uri "+
						"where { "+
						"?uri dul:hasLocation spt:cti  "+
						"} ";
		Query query = QueryFactory.create(queryString);

		System.out.println("----------------------");

		System.out.println("Query Result Sheet");

		System.out.println("----------------------");

		System.out.println("Direct&Indirect Descendants (model1)");

		System.out.println("-------------------");


		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model1_reasoner);
		com.hp.hpl.jena.query.ResultSet results =  qe.execSelect();

		// Output query results    
		ResultSetFormatter.out(System.out, results, query);

		qe.close();

		System.out.println("----------------------");
		System.out.println("Only Direct Descendants (model2)");
		System.out.println("----------------------");

		// Execute the query and obtain results
		qe = QueryExecutionFactory.create(query, model2_noreasoner);
		results =  qe.execSelect();

		// Output query results    
		ResultSetFormatter.out(System.out, results, query);  
		qe.close();
	}



	private static void printModel(OntModel model, Property prop) {
		StmtIterator stit = model.listStatements();
		Statement st = null;
		while (stit.hasNext()){
			st = stit.next();
			if (prop == null || prop.equals(st.getPredicate())){
				System.out.println("S: "+st.getSubject());
				System.out.println("P: "+st.getPredicate());
				System.out.println("O: "+st.getObject());
			}
		}
	}

}
