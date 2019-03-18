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
		poem = "bottle and sadness? have passed forward";
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

		//WordModel for this word in the database
		JsonObject existingGramData = getWordModelFromDB(nGramVal, word);
		//All n-grams that exist for this word
		Set<String> existingGrams = existingGramData.keySet();
		//Average of all probabilities for this word
		Double threshold = getThreshold(existingGramData, nGramVal);
		String result = "";
		

		//For all n-grams that exist for this word
		for(String existingSequence : existingGrams) {
			String[] sequenceWords = existingSequence.split(" ");
			//n-1 value
			String n1 = sequenceWords[sequenceWords.length-2];
			if(nGramVal.equals("2-gram")) {
				//if n-1 for the current poem sequence, keep it as it is
				if(n1.equals(prevWord1)) {
					result = word;
				} else {
					//find a word that makes sense, given n-1
					result = replaceN1(prevWord1);
				}
			}
			if(nGramVal.equals("3-gram")) {
				//n-2 value for the current poem sequence
				String n2 = sequenceWords[sequenceWords.length -3];
				//if the word makes sense given the previous word, keep it
				if(n1.equals(prevWord1)) {
					result = word;
				} 
				//find a replacement that will make sense given n-1 and n-2
				else {
					result = getHighestProbSequence("3-gram", existingSequence, existingGramData);
				}
			}
			//word is words[words.length-1] --> 'n'

		}
		System.out.println("WORD: " + word + ", RESULT: " + result);
		return result;
	}
	
	
	private String replaceN1(String prevWord1) {
		String result = "";
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
		return result;
	}
	



	private double getThreshold(JsonObject model, String nGramVal) {
		double totalThreshold = 0;
		System.out.println("------------------> " + model.toString());
		Set<String> keys = model.keySet();
		for(String key : keys) {
			JsonObject data = model.getJsonObject(key);
			double probability = new Double(data.get("probability").toString());
			totalThreshold =+ probability;
		}
		double avgThreshold = totalThreshold / keys.size();
		return avgThreshold;
	}


	private String getHighestProbSequence(String nVal, String sequence, JsonObject sequenceModel) {
		System.out.println("-----------------" + nVal + ", " + sequence + ", " + sequenceModel);
		String[] nWords = sequence.split(" ");
		Set<String> modelKeys = sequenceModel.keySet();
		double highestProb = -1;
		for(String key : modelKeys) {
			JsonObject data = sequenceModel.getJsonObject(key);
			double probability = new Double(data.get("probability").toString());
			System.out.println(probability);
			if(probability > highestProb) {
				highestProb = probability;
			}
		}
		String probKey  = "" + highestProb;
		JsonObject mostLikelyModel = sequenceModel.getJsonObject(probKey.replace(".","_"));
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
					substitute = keyWords[1];			//should probably find highest probability here
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
