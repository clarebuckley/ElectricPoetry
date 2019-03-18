package PoetryGenerator.Generator;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.bson.Document;


public class NGramController {
	private final MongoInterface mongo = new MongoInterface("poetryDB");
	private final String collection = "languageModel";

	public NGramController() {}

	public String addNGrams(String poem) {
		poem = "Glory and sadness? have passed forward";
		String[] words = poem.split(" ?(?<!\\G)((?<=[^\\p{Punct}])(?=\\p{Punct})|\\b) ?");
		System.out.println(Arrays.toString(words));
		for(int i = 0; i < words.length; i++) {
			String word = words[i].toLowerCase();
			if(word.length() > 0 && word != null && !word.equals(" ") && !word.contains(System.getProperty("line.separator"))) {
				System.out.println(word);
				//Which n to use to find n-grams
				String nNum;
				//Previous word to the current word
				String prevWord = "";
				switch(i) {
				case 0:
					nNum = "2-gram";
					prevWord = "<s>";
				case 1:
					nNum = "3-gram";
				default:
					nNum = "4-gram";
				}

				if(i==0) {
					word = findFirstWord(word);
				} else {
					prevWord = words[i-1];
					JsonObject model = getWordModelFromDB(nNum, word);
					System.out.println("model for " + word + ": " + model);
					if(model != null) {
						//find prevWord in word's associations (n-1)
						prevWord = getPrevWord("n-1", prevWord, model);
						System.out.println(prevWord);
						if(i>0) {
							words[i-1] = prevWord;
						}
					} else {
						//choose most probable word instead
					}
				}

				
				//	Double threshold = getAvgProbability(model);
			}
		}
		return poem;
	}

	/**
	 * Checks to see if <s> is possible n-1, attempts to replace word if not
	 * @param word
	 * @return
	 */
	private String findFirstWord(String word) {
		System.out.println(word);
		JsonObject model = getWordModelFromDB("2-gram", word);
		Set<String> modelKeys = model.keySet();
		if(modelKeys.contains("<s>")) {
			return word;
		} else {
			word = getSubstituteNextWord("<s>");
		}	
		return null;
	}

	/**
	 * Only works for bigrams atm
	 * @param prevWord
	 * @return
	 */
	private String getSubstituteNextWord(String prevWord) {
		String substitute = "";
		while(substitute.length() == 0) {
			JsonObject ngramJson = getRandomDocument(2);
			Set<String> modelKeys = ngramJson.keySet();
			for(String key : modelKeys) {
				String keyWords[] = key.split(" ");
				String n1 = keyWords[0];
				if(n1.equals(prevWord)) {
					substitute = keyWords[1];
				}
			}
		}
		return substitute;
	}

	private JsonObject getRandomDocument(int n) {
		Document randomDoc = mongo.getSampleDocument(collection);
		Document associations = (Document)randomDoc.get("associations");
		Document ngrams = (Document) associations.get(n + "-gram");
		JsonReader jsonReader = Json.createReader(new StringReader(ngrams.toJson()));
		JsonObject ngramJson = jsonReader.readObject();
		return ngramJson;
	}


	/**
	 * Gets most probable previous word
	 * @param nVal
	 * @param word
	 * @return
	 */
	private String getPrevWord(String nVal, String prevWord, JsonObject model) {
		Set<String> modelKeys = model.keySet();
		for(String key : modelKeys) {
			JsonObject ngramData = (JsonObject) model.get(key);
			String nGram = ngramData.getString(nVal);
			if(nGram.equals(prevWord)) {
				return prevWord;
			}	
		}

		return null;
	}



	private String findMostProbablePrevWord(String nVal, String word, JsonObject model) {
		Set<String> modelKeys = model.keySet();
		double highestProb = -1;
		for(String key : modelKeys) {
			JsonObject data = model.getJsonObject(key);
			double probability = new Double(data.get("probability").toString());
			if(probability > highestProb) {
				highestProb = probability;
			}
		}
		String probKey  = "" + highestProb;
		JsonObject mostLikelyModel = model.getJsonObject(probKey.replace(".","_"));
		return mostLikelyModel.get(nVal).toString();
	}

	/**
	 * Gets all associations related to the current word
	 */
	private JsonObject getWordModelFromDB(String n, String word) {
		try {
			Document languageModel = mongo.getLanguageModel(collection, word.toLowerCase());
			Document associations = (Document)languageModel.get("associations");
			Document ngrams = (Document) associations.get(n);
			JsonReader jsonReader = Json.createReader(new StringReader(ngrams.toJson()));
			JsonObject ngramJson = jsonReader.readObject();
			jsonReader.close();
			return ngramJson;
		}
		catch(Exception err) {
			System.out.println(err);
			return null;
		}	
	}

	/**
	 * Get average probability of ngrams to be used as threshold
	 * @param ngramJson
	 * @return
	 */
	private Double getThreshold(JsonObject ngramJson) {
		Set<String> keys = ngramJson.keySet();
		//System.out.println("Max: " + Collections.max(keys));  --> need to convert to double
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


}
