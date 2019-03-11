package PoetryGenerator.Generator;

import org.bson.Document;

/**
 * Model for adding ngrams to the database
 * @author Clare Buckley
 * @version 11/03/19
 *
 */

public class WordModel {
	private String word;
	private String ngramType;
	private double probability;
	private double backoff;
	private String n1;
	private String n2;
	private String n3;
	Document wordModel = new Document();
	Document existingDoc = new Document();
	//Add syllables and rhyme in future 


	public WordModel(String word, String ngramType, double probability, double backoff, String n1, String n2) {
		this.word = word;
		this.ngramType = ngramType;
		this.probability = probability;
		this.backoff = backoff;
		this.n1 = n1;
		this.n2 = n2;
	}
	public WordModel(String word, String ngramType, double probability, double backoff, String n1) {
		this.word = word;
		this.ngramType = ngramType;
		this.probability = probability;
		this.backoff = backoff;
		this.n1 = n1;
	}
	public WordModel(String word, String ngramType, double probability, double backoff) {
		this.word = word;
		this.ngramType = ngramType;
		this.probability = probability;
		this.backoff = backoff;
	}
	public WordModel(String word, String ngramType, double probability, String n1) {
		this.word = word;
		this.ngramType = ngramType;
		this.probability = probability;
		this.n1 = n1;
	}
	public WordModel(String word, String ngramType, double probability, String n1, String n2) {
		this.word = word;
		this.ngramType = ngramType;
		this.probability = probability;
		this.n1 = n1;
		this.n2 = n2;
	}
	public WordModel(String word, String ngramType, double probability, String n1, String n2, String n3) {
		this.word = word;
		this.ngramType = ngramType;
		this.probability = probability;
		this.n1 = n1;
		this.n2 = n2;
		this.n3 = n3;
	}
	//Used when updating an existing document
	public WordModel(Document existingDoc) {
		this.existingDoc = existingDoc;
	}

	//Adds a bigram to an already existing document
	public Document addTwoGram(String n1, String n, double probability, double backoff) {
		Document associations = (Document) existingDoc.get("associations");
		String probAsString = Double.toString(probability);
		String probKey = probAsString.replace(".","_");
		Document ngramData = new Document("probability", probability)
				.append("backoff",backoff)
				.append("n-1", n1);
		Document twoGrams;
		if(associations.containsKey("2-gram")) {	
			twoGrams = (Document) associations.get("2-gram");
			twoGrams.append(probKey, ngramData);

		} else {
			twoGrams = new Document(probKey, ngramData);
			associations.append("2-gram", twoGrams);
		}
		((Document) existingDoc.get("associations")).put("2-gram", twoGrams);

		return existingDoc;
	}

	//Adds a trigram to an already existing document
	public Document addThreeGram(String n2, String n1, String n, double probability, double backoff) {
		Document associations = (Document) existingDoc.get("associations");
		String probAsString = Double.toString(probability);
		String probKey = probAsString.replace(".","_");
		Document ngramData = new Document("probability", probability)
				.append("backoff",backoff)
				.append("n-1", n1)
				.append("n-2", n2);
		
		Document threeGrams;
		if(associations.containsKey("3-gram")) {	
			threeGrams = (Document) associations.get("3-gram");
			threeGrams.append(probKey, ngramData);

		} else {
			threeGrams = new Document(probKey, ngramData);
			associations.append("3-gram", threeGrams);
		}
		((Document) existingDoc.get("associations")).put("3-gram", threeGrams);

		return existingDoc;
	}

	//Adds a fourgram to an already existing document
	public Document addFourGram(String n3, String n2, String n1, String n, double probability, double backoff) {
		Document associations = (Document) existingDoc.get("associations");
		String probAsString = Double.toString(probability);
		String probKey = probAsString.replace(".","_");
		Document ngramData = new Document("probability", probability)
				.append("backoff",backoff)
				.append("n-1", n1)
				.append("n-2", n2)
				.append("n-3", n3);
		
		Document fourGrams;
		if(associations.containsKey("4-gram")) {	
			fourGrams = (Document) associations.get("4-gram");
			fourGrams.append(probKey, ngramData);

		} else {
			fourGrams = new Document(probKey, ngramData);
			associations.append("4-gram", fourGrams);
		}
		((Document) existingDoc.get("associations")).put("4-gram", fourGrams);

		return existingDoc;
	}

	//Create document from this word model
	public Document buildDocument() {
		String probAsString = Double.toString(probability);
		String probKey = probAsString.replace(".","_");
		Document ngramData = new Document("probability", probability)
				.append("backoff", backoff);

		if(ngramType.equals("2-gram") ||  ngramType.equals("3-gram") || ngramType.equals("4-gram")) {
			ngramData.append("n-1", n1);
		}
		if(ngramType.equals("3-gram") || ngramType.equals("4-gram")) {
			ngramData.append("n-2", n2);
		}
		if(ngramType.equals("4-gram")) {
			ngramData.append("n-3", n3);
		}

		Document docKey = new Document(probKey, ngramData);
		Document associations = new Document(ngramType, docKey);
		Document document = new Document("word", word)
				.append("associations", associations);
		wordModel = document;
		return document;
	}


	public String getWord() {
		return word;
	}
}
