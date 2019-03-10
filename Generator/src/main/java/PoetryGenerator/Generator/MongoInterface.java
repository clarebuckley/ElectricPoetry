package PoetryGenerator.Generator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.json.Json;

import org.bson.Document;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;


/**
 * Interface for CRUD operations on MongoDB database
 * Used in poetry generator and for testing purposes
 * @author Clare Buckley
 * @version 18/11/2018
 *
 */
public class MongoInterface {

	private MongoClient mongo = new MongoClient( "localhost", 27017 ); 
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
	 * Return document from the collection
	 * @param collectionName
	 * @param docId
	 * @return document with id docId in the collection
	 */
	public Document getDocument(String collectionName, Object docId) {
		MongoCollection<Document> collection = getCollection(collectionName);
		Document toFind = new Document().append("id", docId);
		List<Document> document = (List<Document>)collection.find(toFind).into(
				new ArrayList<Document>());
		Iterator<Document> iterator = document.iterator();
		Document result = (Document) iterator.next();
		return result;
	}
	
	/**
	 * Get words associated with a POS tag
	 * @param collectionName - collection to be used
	 * @param docId - document id
	 * @return words associated with POS tag
	 */
	public Object getTagWords(String collectionName, Object docId) {
		MongoCollection<Document> collection = getCollection(collectionName);
		Document toFind = new Document().append("tag", docId);
		FindIterable<Document> document = collection.find(toFind);
		Iterator<Document> iterator = document.iterator();
		Document result = (Document) iterator.next();
		return result.get("words");
	}
	

	/**
	 * Get id of last entered document in the collection
	 * @param collectionName collection to search
	 * @return highest docId in the collection
	 */
	public int getLastEnteredId(String collectionName) {
		MongoCollection<Document> collection = getCollection(collectionName);
		Document document = collection.find().sort(Sorts.descending("id")).first();
//		Double docId = (Double) document.get("id");
//		Integer id = docId.intValue();
		int id = (Integer) document.get("id");
		return id;
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
	 * @param collectionName - name of collection containing document
	 * @param searchId - search id
	 * @param searchVal - search value
	 * @param updateId - id to update with
	 * @param updateVal - value to be updated
	 */
	public void updateDocument(String collectionName, String searchId, Object searchVal, String updateId, int updateVal) {
		MongoCollection<Document> collection = getCollection(collectionName);
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.append("$set", new BasicDBObject().append(updateId, updateVal));
				
		BasicDBObject searchQuery = new BasicDBObject().append(searchId, searchVal);

		collection.updateOne(searchQuery, newDocument);
	}
	
	/**
	 * Update array element in a document
	 * @param collectionName - name of collection containing document
	 * @param searchId - search id
	 * @param searchVal - search value
	 * @param updateId - id to update with
	 * @param updateVal - value to be added
	 */
	public void updateDocumentArray(String collectionName, String searchId, String searchVal, String updateId, String updateVal) {
		MongoCollection<Document> collection = getCollection(collectionName);
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.append("$addToSet", new BasicDBObject().append(updateId, updateVal));
				
		BasicDBObject searchQuery = new BasicDBObject().append(searchId, searchVal);

		collection.updateOne(searchQuery, newDocument);
	}
	
	public void updateLanguageModel(String collectionName, String word, Document modelDocument) {
		//if word doesn't exist, add as a new document
		if(!wordExists(collectionName, word)) {
			insertDocument(collectionName, modelDocument);
		} else {
			//get document that exists
			MongoCollection<Document> collection = getCollection(collectionName);
			Document toFind = new Document().append("word", word);
			List<Document> document = (List<Document>)collection.find(toFind).into(
					new ArrayList<Document>());
			Iterator<Document> iterator = document.iterator();
			Document result = (Document) iterator.next();
			Document inDatabase = (Document) ((Document)result.get("associations"));
			
			//check which data needs to be added
			Document updateWith =  (Document) modelDocument.get("associations");
			System.out.println(updateWith.toJson());
			boolean updateOneGram = false, updateTwoGram = false, updateThreeGram = false, updateFourGram = false;
			if(updateWith.containsKey("1-gram")) updateOneGram = true;
			if(updateWith.containsKey("2-gram")) updateTwoGram = true;
			if(updateWith.containsKey("3-gram")) updateThreeGram = true;
			if(updateWith.containsKey("4-gram")) updateFourGram = true;

			if(updateOneGram) {
				System.out.println(updateWith.toJson());
				System.out.println(inDatabase.values());
				System.out.println("-------------------------------");
			}
			
			
			//update relevant parts of that document
			
			
			
			
		}
	}
	
	public boolean wordExists (String collectionName, String word) {
	    FindIterable<Document> iterable = database.getCollection(collectionName).find(new Document("word", word));
	    return iterable.first() != null;
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
		while (it.hasNext()) {  
			System.out.println(it.next());  
		}
	}
	
	/**
	 * Count number of documents in a given collection
	 * @param collectionName - name of collection
	 * @return number of documents
	 */
	public long getDocumentCount(String collectionName) {
		return getCollection(collectionName).countDocuments();
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
