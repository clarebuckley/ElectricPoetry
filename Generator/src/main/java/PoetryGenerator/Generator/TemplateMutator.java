package PoetryGenerator.Generator;

import java.util.Random;
import org.bson.Document;

public class TemplateMutator {
	private final MongoInterface mongo = new MongoInterface("poetryDB");
	private final String collection = "verses";

	public static void main(String[] args) {
		new TemplateMutator();
	}
	
	public TemplateMutator() {
		getTemplate(4);
	}
	
	
	public String getTemplate(int numLines) {
		Random random = new Random();
				
		long docCount = mongo.getDocumentCount(collection);
		System.out.println(random.nextInt((int)docCount));
		Document template = mongo.getDocument(collection, docCount);
		System.out.println(template.get("POS"));
		return "";
	}

}
