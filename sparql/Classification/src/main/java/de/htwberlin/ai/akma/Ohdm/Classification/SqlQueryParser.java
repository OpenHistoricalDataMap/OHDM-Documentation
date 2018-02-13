package de.htwberlin.ai.akma.Ohdm.Classification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class SqlQueryParser {

	private String url;
	private String user;
	private String password;
	private Connection connection;
	private Statement statement;
	
	public SqlQueryParser(String ur, String u, String p){
		this.url = ur;
		this.user = u;
		this.password = p;
		connection = null;
		statement = null;
	}
	
	public void queryAndParse() throws SQLException, FileNotFoundException{
		try {
			prepareConnection();
			ResultSet resultSet;
			resultSet = statement.executeQuery("select * from ohdm.classification");
			String ohdm = "http://141.45.146.110:3030/ohdm-classification#";	//RDF Graph Link. anpassen
			String dbBase = "http://ohm.f4.htw-berlin.de/ohdm_public/";	//Geoobject Endpoint. anpassen
			
			Model model = ModelFactory.createDefaultModel();
			
			//Prefix setzen
			model.setNsPrefix("ohdm", "http://141.45.146.110:3030/ohdm-classification#");
			model.setNsPrefix("rdf", RDF.uri);
			model.setNsPrefix("rdfs", RDFS.uri);
			
			//Haupktklasse der Klassifikation
			model.createResource(ohdm+"Classification").addProperty(RDF.type, RDFS.Class)
			.addProperty(RDFS.isDefinedBy, ohdm)
			.addProperty(RDFS.label, "classification")
			.addProperty(RDFS.comment, "Classification of a geoobject");
			
			Resource classification = model.getResource(ohdm+"Classification");
			
			String ID ="", className ="", subclassName="", classNameToUp="", subclassNameToUp="";
			Set<String> classNames = new TreeSet<String>();
			Set<String> subclassNames = new TreeSet<String>();
			while (resultSet.next()){
				ID = resultSet.getString(1);
				if(ID.equals("-1")) //no_class
					continue;
				className = resultSet.getString(2);
				className.replaceAll("\\s", "");
				subclassName = resultSet.getString(3);
				subclassName.replaceAll("\\s", "");
				//RDF Klassennamen beginnen mit einer großen Buchstabe
				classNameToUp = className.substring(0, 1).toUpperCase() + className.substring(1).toLowerCase();
				subclassNameToUp = subclassName.substring(0, 1).toUpperCase() + subclassName.substring(1).toLowerCase();
				
				if(classNames.add(className))
				{
					model.createResource(ohdm+classNameToUp).addProperty(RDF.type, classification)
					.addProperty(RDFS.label, className);
				}
				if(subclassNames.add(subclassName))
				{
					model.createResource(ohdm+subclassNameToUp).addProperty(RDF.type, classification)
					.addProperty(RDFS.subClassOf, ResourceFactory.createResource(ohdm+classNameToUp))
					.addProperty(RDFS.label, subclassName);
				}
			}
			File file = new File("ohdm-classification");
    		FileOutputStream fos = new FileOutputStream(file);
			model.write(fos);
			model.write(System.out, "Turtle");
			
			//Geoobjekte laden. 
			Model model2 = ModelFactory.createDefaultModel();
			model2.setNsPrefix("ohdm", "http://141.45.146.110:3030/ohdm-classification#");
			model.setNsPrefix("rdf", RDF.uri);
			
			resultSet = statement.executeQuery("select id_geoobject_source, geoo.id, subclassname from "
					+ "(select id_geoobject_source, classification_id id from ohdm.geoobject_geometry where id_geoobject_source!=0 limit 1000) geoo "
					+ "natural join ohdm.classification");
			
			while(resultSet.next()){
				ID = resultSet.getString(1);
				ID = ID.replaceAll(" ", "");
				subclassName = resultSet.getString(3);
				subclassNameToUp = subclassName.substring(0, 1).toUpperCase() + subclassName.substring(1).toLowerCase();
				model2.createResource(dbBase+("geoobject/"+ID)).addProperty(RDF.type, ResourceFactory.createResource(ohdm+subclassNameToUp));
			}
			
			File file2 = new File("geoobject");
    		FileOutputStream fos2 = new FileOutputStream(file2);
			model2.write(fos2);
			//model2.write(System.out, "Turtle");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if(connection != null)
				connection.close();
			if(statement != null)
				statement.close();
		}
		
		
	}
	
	public void prepareConnection() throws SQLException
	{
		connection = DriverManager.getConnection(url, user, password);
		statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}
	
	public static void main( String[] args ) throws SQLException, FileNotFoundException{
		SqlQueryParser sqlQueryParser = new SqlQueryParser("jdbc:postgresql://ohm.f4.htw-berlin.de:5432/ohdm_public", "geoserver", "ohdm4ever!");
		sqlQueryParser.queryAndParse();
	}
}
