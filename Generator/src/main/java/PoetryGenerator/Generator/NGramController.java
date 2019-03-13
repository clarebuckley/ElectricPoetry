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
	private JsonObject model;
	private String punctuation = ".,:;-'''`!---";

	public NGramController() {}

	public String addNGrams(int n, String poem) {
		String[] words = poem.split("\\b");

		for(int i = 0; i < words.length; i++) {
			String word = words[i].toLowerCase();
			if(!word.equals(" ") && !punctuation.contains(word)) {
				String prevWord;
				if(i == 0){
					prevWord = "<s>";
				} else {
					prevWord = words[i-1];
				}
				model = getWordModelFromDB(n, word);
				System.out.println("MODEL --------> " + model.toString());
				Double threshold = getAvgProbability(model);
			}
		}
		return poem;
	}

	private JsonObject getWordModelFromDB(int n, String word) {
		System.out.println(word);
		try {
			Document languageModel = mongo.getLanguageModel(collection, word.toLowerCase());
			Document associations = (Document)languageModel.get("associations");
			Document ngrams = (Document) associations.get(n + "-gram");
			JsonReader jsonReader = Json.createReader(new StringReader(ngrams.toJson()));
			JsonObject ngramJson = jsonReader.readObject();
			jsonReader.close();
			return ngramJson;
		}
		catch(Error err) {
			System.out.println(err);
			return null;
		}	
	}

	private Double getAvgProbability(JsonObject ngramJson) {
		Set<String> keys = ngramJson.keySet();
		System.out.println("Max: " + Collections.max(keys));
		Double total = 0.0;
		for(String key : keys) {
			Double keyVal = new Double(key.replace("_", "."));
			if(keyVal>1) {
				keys.remove(key);
			}
			total += keyVal;
		}
		Double average = total/keys.size();
		return average;
	}


	//	private JsonObject getCandidateWords(double threshold) {
	//		JsonObject candidateModel = model;
	//		System.out.println(candidateModel.size());
	//		Set<String> probabilities = model.keySet();
	//		
	//		for(String prob : probabilities) {
	//			Double probVal = new Double(prob.replace("_", "."));
	//			if(probVal > threshold) {
	//				System.out.println(candidateModel.getJsonObject(prob));
	//				System.out.println(candidateModel);
	//			//	candidateModel.remove(prob);
	//				candidateModel.remove(prob, model.get(prob));
	//			}
	//		}
	//		System.out.println(candidateModel.size());
	//		return candidateModel;
	//	}

}
