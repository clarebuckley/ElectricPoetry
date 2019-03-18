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
		poem.replaceAll(".", ". * ");
		poem = "* " + poem;

		String[] words = poem.split(" ?(?<!\\G)((?<=[^\\p{Punct}])(?=\\p{Punct})|\\b) ?");
		String currentPoem = "";
		for(int i = 1; i < words.length; i++) {
			System.out.println(Arrays.toString(words));
			String word = words[i].toLowerCase();
			if(word.length() > 0 && word != null && !word.equals(" ") && !word.contains(System.getProperty("line.separator"))) {
				System.out.println(word);
				
				String prevWord1 = words[i-1];
				String prevWord2 = null, prevWord3 = null;
				prevWord2 = ((i>=2) ? words[i-2] : null);
				prevWord3 = ((i>=3) ? words[i-3] : null);
				
				if(prevWord1.equals("*")) prevWord1 = "<s>";
				if(prevWord2 != null && prevWord2.equals("*")) prevWord2 = "<s>";
				word = getPoemWord(word, prevWord1, prevWord2, prevWord3);

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
	private String getPoemWord(String word, String prevWord1, String prevWord2, String prevWord3) {
		String nGramVal = ((prevWord2 != null) ? "3-gram" : "2-gram");
		if(prevWord3 != null) nGramVal = "4-gram";
		
		JsonObject existingGramData = getWordModelFromDB(nGramVal, word);
		Set<String> existingGrams = existingGramData.keySet();
		String result = "";

		for(String existingSequence : existingGrams) {
			System.out.println(existingSequence);
			String[] sequenceWords = existingSequence.split(" ");
			System.out.println(Arrays.toString(sequenceWords));
			String n1 = sequenceWords[sequenceWords.length-2];
			//Process for bigrams, "2-gram"
			if(nGramVal.equals("2-gram")) {
				System.out.println("N-1: " + n1 + ", prev word" + prevWord1);
				if(n1.equals(prevWord1)) {
					result = word;
				} else {
					while(result.length() == 0) {
						JsonObject ngramJson = getRandomDocument(2);
						Set<String> newKeys = ngramJson.keySet();
						for(String key : newKeys) {
							String keyWords[] = key.split(" ");
							String possibleReplacement = keyWords[0];
							if(possibleReplacement.equals(prevWord1)) {
								result = keyWords[1];
							}
						}
					}
				}
			}
			//Trigram process, "3-gram"
			if(nGramVal.equals("3-gram")) {
				String n2 = sequenceWords[sequenceWords.length -3];
				if(n1 == prevWord1 || n2 == prevWord2) {
					result = word;
				} 
				else {
					result = getHighestProbSequence("3-gram", existingSequence, existingGramData);

				}
			}
			//word is words[words.length-1] --> 'n'

		}
		System.out.println("WORD: " + word + ", RESULT: " + result);
		return result;
	}


	private String getHighestProbSequence(String nVal, String sequence, JsonObject model) {
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
