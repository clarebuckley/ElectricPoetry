package PoetryGenerator.Generator;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.bson.Document;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Sorts;


/**
 * Interface for CRUD operations on MongoDB database
 * Used in poetry generator and for testing purposes
 * @author Clare Buckley
 * @version 18/11/2018
 *
 */
public class MongoInterface {

	static public MongoClient mongo = null;
	
	private final MongoDatabase database;
	private final String databaseName;

	MongoInterface(String databaseNameParam) {
		try {
			mongo = getMongoClientInstance();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	public static synchronized MongoClient getMongoClientInstance() throws UnknownHostException {
	    if (mongo == null) {
	        mongo = new MongoClient( "localhost", 27017 );
	    }
	    return mongo;
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
	
	public Document getLanguageModel(String collectionName, Object word) {
		MongoCollection<Document> collection = getCollection(collectionName);
		Document toFind = new Document().append("word", word);
		List<Document> document = (List<Document>)collection.find(toFind).into(
				new ArrayList<Document>());
		Iterator<Document> iterator = document.iterator();
		Document result = (Document) iterator.next();
		return result;
	}
	
	//Sequence type can be either POS or word
	public List<Document> getSequenceMatches(String collectionName, String sequence, String sequenceType) {
		MongoCollection<Document> collection = getCollection(collectionName);
		Document toFind = new Document().append(sequenceType, sequence);
		List<Document> documents = (List<Document>)collection.find(toFind).into(new ArrayList<Document>());
		return documents;
	}

	public Document getSampleDocument(String collectionName) {
		MongoCollection<Document> collection = getCollection(collectionName);
		AggregateIterable<Document> iterator = collection.aggregate(Arrays.asList(Aggregates.sample(100)));
		Random random = new Random();
		int randomIndex = random.nextInt(100);
		ArrayList<Document> documents = new ArrayList<Document>();
		iterator.into(documents);
		Document result = documents.get(randomIndex);
		return result;
	}
	
	/**
	 * Get words associated with a POS tag
	 * @param collectionName - collection to be used
	 * @param docId - document id
	 * @return words associated with POS tag
	 */
	public Object getTagWords(String collectionName, Object docId) {
		System.out.println("--> " + docId);
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
	//	Double docId = (Double) document.get("id");
	//	Integer id = docId.intValue();
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
