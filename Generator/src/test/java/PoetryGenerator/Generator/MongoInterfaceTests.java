package PoetryGenerator.Generator;

import org.bson.Document;
import org.junit.Test;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import junit.framework.TestCase;

public class MongoInterfaceTests extends TestCase {
	MongoInterface mongo = new MongoInterface("test");
	MongoDatabase db = mongo.getDatabase();
	
	@Test
	public void testGetCollection() {
		MongoCollection<Document> collection = mongo.getCollection("testData");
		assertEquals(collection.toString(), "com.mongodb.client.internal.MongoCollectionImpl@68bbe345");
	}
	
	@Test
	public void testInsertDocument() {
		MongoCollection<Document> collection = mongo.getCollection("testData");
		PoemLine line = new PoemLine(1, "In youth from rock to rock I went", "(ROOT\r\n" + 
				"  (S\r\n" + 
				"    (PP (IN In)\r\n" + 
				"      (NP\r\n" + 
				"        (NP (NN youth))\r\n" + 
				"        (PP (IN from)\r\n" + 
				"          (NP\r\n" + 
				"            (NP (NN rock))\r\n" + 
				"            (PP (TO to)\r\n" + 
				"              (NP (NN rock)))))))\r\n" + 
				"    (NP (PRP I))\r\n" + 
				"    (VP (VBD went))))", "In/IN youth/NN from/IN rock/NN to/TO rock/NN I/PRP went/VBD");
		Document document = line.buildLineDocument();
		collection.insertOne(document);
	}
	
}
