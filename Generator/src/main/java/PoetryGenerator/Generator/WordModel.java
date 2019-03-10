package PoetryGenerator.Generator;

import org.bson.Document;

public class WordModel {
	private String word;
	private String ngramType;
	private double probability;
	private double backoff;
	private String n1;
	private String n2;
	private String n3;
	//Add syllables and rhyme in future 


//	public WordModel(String word, String ngramType, double probability, double backoff, String n1, String n2, String n3) {
//		this.word = word;
//		this.ngramType = ngramType;
//		this.probability = probability;
//		this.backoff = backoff;
//		this.n1 = n1;
//		this.n2 = n2;
//		this.n3 = n3;
//	}
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
//	public WordModel(String word, String ngramType, double probability) {
//		this.word = word;
//		this.ngramType = ngramType;
//		this.probability = probability;
//	}
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


	public Document buildDocument() {
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
		Document associations = new Document(ngramType, ngramData);
		Document document = new Document("word", word)
				.append("associations", associations);
		System.out.println(document.toJson());
		return document;
	}

}
