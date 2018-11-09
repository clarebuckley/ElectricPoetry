package PoetryGenerator.Generator;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import junit.framework.TestCase;

public class MongoInterfaceTests extends TestCase {
	MongoInterface mongo = new MongoInterface("test");
	MongoDatabase db = mongo.getDatabase();
	MongoCollection<Document> collection = mongo.getCollection("testData");


	@Before
	public void reset() {
		collection.drop();
	}

	@Test
	public void testInsertDocument() {

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
		assertEquals(document.toString(), "Document{{id=1, textLine=In youth from rock to rock I went, POS=(ROOT\r\n" + 
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
				"    (VP (VBD went)))), taggedLine=In/IN youth/NN from/IN rock/NN to/TO rock/NN I/PRP went/VBD}}");

		//Add document to collection
		collection.insertOne(document);
		assertNotNull(collection.find(Filters.eq("id", "1")));

	}


}
