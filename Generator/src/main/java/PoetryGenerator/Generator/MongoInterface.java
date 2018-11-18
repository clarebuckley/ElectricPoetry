package PoetryGenerator.Generator;

import java.util.Iterator;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase; 

/**
 * Interface for CRUD operations on MongoDB database
 * Used in poetry generator and for testing purposes
 * @author Clare Buckley
 * @version 09/11/2018
 *
 */
public class MongoInterface {

	private MongoClient mongo = new MongoClient( "localhost" , 27017 ); 
	
	
	private final MongoDatabase database;
	private final String databaseName;
	
	MongoInterface(String databaseNameParam) {
		databaseName = databaseNameParam;
		database =  mongo.getDatabase(databaseName);
		
		//Tests
		System.out.println(getDocument("testData", 1).get("textLine"));
		updateDocument("testData", 1, "updatedTest", "updatedVal");
		System.out.println(getDocument("testData", 1));
		
	}
	
	public static void main(String args[]) {
		//for testing
		 new MongoInterface("test");
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
	 * Return document from the collection
	 * @param collectionName
	 * @param docId
	 * @return
	 */
	public Document getDocument(String collectionName, Object docId) {
		MongoCollection<Document> collection = getCollection(collectionName);

		Document toFind = new Document().append("id", docId);
		FindIterable<Document> document = collection.find(toFind);
		Iterator<Document> iterator = document.iterator();
	    Document result = (Document) iterator.next();
		System.out.println(result.toJson());
		
		return result;
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
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.append("$set", new BasicDBObject().append(docKey, docVal));
		BasicDBObject searchQuery = new BasicDBObject().append("id", docId);	
		collection.updateOne(searchQuery, newDocument);
	}
	
	/**
	 * Delete document in a collection
	 * @param collectionName - collection containing document
	 * @param docId - id of document to be deleted
	 */
	public void deleteDocument(String collectionName, int docId) {
		MongoCollection<Document> collection = getCollection(collectionName);
		collection.deleteOne(new Document("id", docId));
	}
	
	/**
	 * Print out collection contents
	 * @param collectionName
	 */
	public void printCollectionContents(String collectionName) {
		MongoCollection<Document> collection = getCollection(collectionName);
		
		// Getting the iterable object 
		FindIterable<Document> iterDoc = collection.find(); 
		Iterator<Document> it = iterDoc.iterator(); 
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
