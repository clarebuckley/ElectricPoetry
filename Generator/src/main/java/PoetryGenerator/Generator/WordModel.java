package PoetryGenerator.Generator;

import javax.json.JsonObject;
import org.bson.Document;

public class WordModel {
	private String word;
	private JsonObject associations;
	//Add syllables and rhyme in future 


	public WordModel(String word, JsonObject associations) {
		this.word = word;
		this.associations = associations;
	}

	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public JsonObject getAssociations() {
		return associations;
	}
	public void setAssociations(JsonObject associations) {
		this.associations = associations;
	}
	
	public Document buildDocument() {
		Document document = new Document("word", word)
				.append("associations", associations);
		return document;
	}

}
