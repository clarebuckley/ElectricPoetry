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
					word = findFirstWord(word);
					break;
				case 1:
					nNum = "3-gram";
					prevWord = words[i-1];
					word = getPoemWord(word);
					break;
				default:
					nNum = "4-gram";
					break;
				}

				//	Double threshold = getAvgProbability(model);
			}
			
			words[i] = word;
		}
		String newPoem = "";
		for(String word : words) {
			newPoem = newPoem + word + " ";
		}
		return newPoem;
	}

	/**
	 * Get word to be used in poem, given the previous n words
	 * @param word
	 * @return
	 */
	private String getPoemWord(String word) {
		String nGram = "3-gram";
		Set<String> existingGrams = getNGrams(word, nGram);
		for(String sequence : existingGrams) {
			System.out.println(sequence);

		}
		return word;
	}

	/**
	 * Retrieves ngram data for the given word and 'n'
	 * @param word
	 * @param nGramNum
	 * @return
	 */
	private Set<String> getNGrams(String word, String nGramNum) {
		JsonObject model = getWordModelFromDB(nGramNum, word);
		Set<String> modelKeys = model.keySet();
		return modelKeys;
	}

	/**
	 * Checks to see if <s> is possible n-1, attempts to replace word if not
	 * @param word
	 * @return
	 */
	private String findFirstWord(String word) {
		Set<String> modelKeys = getNGrams(word, "2-gram");
		for(String key : modelKeys) {
			String start = key.split(" ")[0];
			if(start.equals("<s>")) {
				return word;
			}
		}

		String substitute = "";
		while(substitute.length() == 0) {
			JsonObject ngramJson = getRandomDocument(2);
			Set<String> newKeys = ngramJson.keySet();
			for(String key : newKeys) {
				String keyWords[] = key.split(" ");
				String n1 = keyWords[0];
				if(n1.equals("<s>")) {
					substitute = keyWords[1];
				}
			}
		}
		return substitute;
	}

	/**
	 * Used for replacing words
	 * @param n
	 * @return
	 */
	private JsonObject getRandomDocument(int n) {
		Document randomDoc = mongo.getSampleDocument(collection);
		Document associations = (Document)randomDoc.get("associations");
		Document ngrams = (Document) associations.get(n + "-gram");
		JsonReader jsonReader = Json.createReader(new StringReader(ngrams.toJson()));
		JsonObject ngramJson = jsonReader.readObject();
		return ngramJson;
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

}
