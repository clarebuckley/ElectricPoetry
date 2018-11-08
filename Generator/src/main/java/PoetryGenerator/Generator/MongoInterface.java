package PoetryGenerator.Generator;
import com.mongodb.client.FindIterable; 
import com.mongodb.client.MongoCollection; 
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import java.util.Iterator; 
import org.bson.Document; 
import com.mongodb.MongoClient; 

/**
 * Interface for CRUD operations on MongoDB database
 * Used in poetry generator and for testing purposes
 * @author Clare Buckley
 * @version 08/11/2018
 *
 */
public class MongoInterface {

	private MongoClient mongo = new MongoClient( "localhost" , 27017 ); 
	private final MongoDatabase database;  
	private final String databaseName;
	
	MongoInterface(String databaseNameParam) {
		databaseName = databaseNameParam;
		database = mongo.getDatabase(databaseName);
	}
	
	/**
	 * Get mongo database
	 * @return database
	 */
	public MongoDatabase getDatabase() {
		return database;
	}
	
	/**
	 * Get collection from database
	 * @param collectionName
	 * @return collection with name 'collectionName'
	 */
	public MongoCollection<Document> getCollection(String collectionName){
		return database.getCollection(collectionName);
	}
		
	/**
	 * Insert a document into a collection
	 * @param collectionName - collection to add document to
	 * @param document - document to be added
	 */
	public void insertDocument(String collectionName, Document document) {
		MongoCollection<Document> collection = getCollection(collectionName);
		collection.insertOne(document);
	}
	
	/**
	 * Update document in a collection
	 * @param collectionName - collection containing document
	 * @param docId - document id
	 * @param docKey - key for value to be updated
	 * @param docVal - new value to update with
	 */
	public void updateDocument(String collectionName, int docId, String docKey, String docVal) {
		MongoCollection<Document> collection = getCollection(collectionName);
		collection.updateOne(Filters.eq("id", docId), Updates.set(docKey, docVal));
	}
	
	/**
	 * Delete document in a collection
	 * @param collectionName - collection containing document
	 * @param docId - id of document to be deleted
	 */
	public void deleteDocument(String collectionName, int docId) {
		MongoCollection<Document> collection = getCollection(collectionName);
		collection.deleteOne(Filters.eq("id", docId)); 
	}
	
	/**
	 * Print out collection contents
	 * @param collectionName
	 */
	public void printCollectionContents(String collectionName) {
		MongoCollection<Document> collection = getCollection(collectionName);
		
		// Getting the iterable object 
		FindIterable<Document> iterDoc = collection.find(); 
		Iterator it = iterDoc.iterator(); 
		int count = 1;
		while (it.hasNext()) {  
			System.out.println(it.next());  
			count++; 
		}
	}
	
	/**
	 * List all collections in the database
	 */
	public void listCollections() {
		for(String name : database.listCollectionNames()) {
			System.out.println(name);
		}
	}
	

}
