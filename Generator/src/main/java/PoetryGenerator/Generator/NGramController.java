package PoetryGenerator.Generator;

import java.io.StringReader;
import java.util.Collections;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.bson.Document;


public class NGramController {
	private final MongoInterface mongo = new MongoInterface("poetryDB");
	private final String collection = "languageModel";

	
	
	public NGramController() {
	}
	
	public String addNGrams(int n, String poem) {
		String[] words = poem.split(" ");
		System.out.println(words[0]);
		
		for(int i = 0; i < words.length; i++) {
			String prevWord;
			if(i == 0){
				prevWord = "<s>";
			} else {
				prevWord = words[i-1];
			}
		}
		
		String word = words[0].toLowerCase();
		getWordModelFromDB(n, word);
		
		return poem;
	}
	
	private void getWordModelFromDB(int n, String word) {
		Document languageModel = mongo.getLanguageModel(collection, word.toLowerCase());
		Document associations = (Document)languageModel.get("associations");
		Document ngrams = (Document) associations.get(n + "-gram");
	
	
		JsonReader jsonReader = Json.createReader(new StringReader(ngrams.toJson()));
	    JsonObject ngramJson = jsonReader.readObject();
	    jsonReader.close();
		
		Set<String> keys = ngramJson.keySet();

		System.out.println("Max: " + Collections.max(keys));
		for(String key : keys) {
			System.out.println(key + " --> " + ngrams.get(key));
		}
	}

}
