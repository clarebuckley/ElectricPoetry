package PoetryGenerator.Generator;

import java.util.ArrayList;

import org.bson.Document;

public class WordbankEntry {
	//POS tag that categorises the word to be added
	private String tag;
	//Word to be added 
	private String word;
	//nGram of word [to be added in future iterations]
	private String[] nGram;
	private ArrayList<String> tags = new ArrayList<String>();
	
	public WordbankEntry(String tag, String word, String[] nGram) {
		this.word = word;
		this.nGram = nGram;
		this.tag = tag;
	}
	
	public Document buildDocument() {
		Document document = new Document("tag", tag)
				.append("word", word);
		return document;
	}

}
