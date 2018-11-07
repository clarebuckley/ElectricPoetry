package PoetryGenerator.Generator;
import com.mongodb.client.FindIterable; 
import com.mongodb.client.MongoCollection; 
import com.mongodb.client.MongoDatabase;  

import java.util.Iterator; 
import org.bson.Document; 
import com.mongodb.MongoClient; 

//TODO: use https://www.tutorialspoint.com/mongodb/mongodb_java.htm to finish class

public class MongoInterface {

	private MongoClient mongo = new MongoClient( "localhost" , 27017 ); 
	private final MongoDatabase database;  
	
	MongoInterface() {
		database = mongo.getDatabase("poetrydb");
	}
	
	public MongoCollection<Document> getCollection(String collectionName){
		return database.getCollection(collectionName);
	}
	
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

}
