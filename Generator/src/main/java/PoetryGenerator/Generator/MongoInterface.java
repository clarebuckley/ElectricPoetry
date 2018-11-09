package PoetryGenerator.Generator;

import java.util.Iterator; 
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient; 

/**
 * Interface for CRUD operations on MongoDB database
 * Used in poetry generator and for testing purposes
 * @author Clare Buckley
 * @version 09/11/2018
 *
 */
public class MongoInterface {

	private MongoClient mongo = new MongoClient( "localhost" , 27017 ); 
	
	
	private final DB database;
	private final String databaseName;
	
	MongoInterface(String databaseNameParam) {
		databaseName = databaseNameParam;
		database =  mongo.getDB(databaseName);
		
		System.out.println(getDocument("testData", 1).get("textLine"));
		updateDocument("testData", 1, "updatedTest", "updatedVal");
		System.out.println(getDocument("testData", 1));
		
	}
	
	public static void main(String args[]) {
		 new MongoInterface("test");
	}
	
	/**
	 * Get mongo database
	 * @return database
	 */
	public DB getDatabase() {
		return database;
	}
	
	/**
	 * Get collection from database
	 * @param collectionName
	 * @return collection with name 'collectionName'
	 */
	public DBCollection getCollection(String collectionName){
		return database.getCollection(collectionName);
	}
	
	/**
	 * Return document from the collection
	 * @param collectionName
	 * @param docId
	 * @return
	 */
	public DBObject getDocument(String collectionName, Object docId) {
		DBCollection collection = getCollection(collectionName);
		DBObject query = new BasicDBObject("id", docId);
		DBCursor cursor = collection.find(query);
		DBObject javaObject = cursor.one();
		return javaObject;
	}
		
	/**
	 * Insert a document into a collection
	 * @param collectionName - collection to add document to
	 * @param document - document to be added
	 */
	public void insertDocument(String collectionName, DBObject[] document) {
		DBCollection collection = getCollection(collectionName);
		collection.insert(document);
	}
	
	/**
	 * Update document in a collection
	 * @param collectionName - collection containing document
	 * @param docId - document id
	 * @param docKey - key for value to be updated
	 * @param docVal - new value to update with
	 */
	public void updateDocument(String collectionName, int docId, String docKey, String docVal) {
		DBCollection collection = getCollection(collectionName);
		BasicDBObject newDocument = new BasicDBObject();
		
		newDocument.append("$set", new BasicDBObject().append(docKey, docVal));
		BasicDBObject searchQuery = new BasicDBObject().append("id", docId);
		
		collection.update(searchQuery, newDocument);
	}
	
	/**
	 * Delete document in a collection
	 * @param collectionName - collection containing document
	 * @param docId - id of document to be deleted
	 */
	public void deleteDocument(String collectionName, int docId) {
		BasicDBObject query = new BasicDBObject();
		query.append("id", docId);

		DBCollection collection = getCollection(collectionName);
		collection.remove(query); 
	}
	
	/**
	 * Print out collection contents
	 * @param collectionName
	 */
	public void printCollectionContents(String collectionName) {
		DBCollection collection = getCollection(collectionName);
		
		// Getting the iterable object 
		DBCursor iterDoc = collection.find(); 
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
		for(String name : database.getCollectionNames()) {
			System.out.println(name);
		}
	}
	

}
