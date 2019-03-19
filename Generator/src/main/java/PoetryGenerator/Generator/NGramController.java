package PoetryGenerator.Generator;

import java.io.StringReader;
import java.math.BigDecimal;
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
		//TODO: is this bit needed or does words[i]=word work?
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
		//Determine which n-gram to use from database
		String nGramVal = ((prevWord2 != null) ? "3-gram" : "2-gram");
		if(prevWord3 != null) nGramVal = "4-gram";

		//WordModel for this word in the database
		JsonObject existingGramData = getWordModelFromDB(nGramVal, word);
		//All n-grams that exist for this word
		Set<String> existingGrams = existingGramData.keySet();

		//Average of all probabilities for this word
		BigDecimal threshold = getThreshold(existingGramData, nGramVal);
		String result = "";

		//Check if poem sequence exists in database already
		String poemText = word + " " + prevWord1 + " " + prevWord2 + " " + prevWord3;
		if(poemTextExists(poemText.trim(), existingGrams)) {
			result = word;
		} else {
			int n = new Integer(nGramVal.split("-")[0]);
			switch(n) {
			case(2):
				result = replaceN1(prevWord1);
				break;
			case(3):
				break;
			
			}
//			if(nGramVal.equals("2-gram")) {
//				//find a word that makes sense, given n-1
//				
//			}
//			if(nGramVal.equals("3-gram")) {
//		//		result = getHighestProbSequence("3-gram", existingSequence, existingGramData);
//			}
		}

		System.out.println("WORD: " + word + ", RESULT: " + result);
		return result;
	}


	private boolean poemTextExists(String poemText, Set<String> existingGrams) {
		boolean exists = false;
		for(String existingSequence : existingGrams) {
			//if n-1 for the current poem sequence, keep it as it is
			if(existingSequence.equals(poemText)) {
				exists = true;
			} 
		}
		return exists;
	}

	/**
	 * Replaec word to fit 2-gram
	 * @param prevWord1
	 * @return
	 */
	private String replaceN1(String prevWord1) {
		String result = "";
		while(result.length() == 0) {
			JsonObject ngramJson = getRandomDocument(2);
			Set<String> newKeys = ngramJson.keySet();
			for(String key : newKeys) {
				String keyWords[] = key.split(" ");
				String possibleReplacement = keyWords[0];
				JsonObject sequenceModel = (JsonObject) ngramJson.get(key);
				BigDecimal threshold = getThreshold(ngramJson, "2-gram");
				BigDecimal probability = new BigDecimal(getProbability(sequenceModel));
				if(possibleReplacement.equals(prevWord1) && probability.compareTo(threshold) == 1) {
					result = keyWords[1];
				}
			}
		}
		return result;
	}
	
	/**
	 * Get probability from JsonObject and format it to avoid scientific notation
	 * @param sequenceModel
	 * @return
	 */
	private double getProbability(JsonObject sequenceModel) {
		Double probability =  new Double(sequenceModel.get("probability").toString());
		return probability;
	}




	private BigDecimal getThreshold(JsonObject model, String nGramVal) {
		double totalThreshold = 0;
//		System.out.println("------------------> " + model.toString());
		Set<String> keys = model.keySet();
		for(String key : keys) {
			JsonObject data = model.getJsonObject(key);
			double probability = getProbability(data);
			totalThreshold =+ probability;
		}
		double avgThreshold = totalThreshold / keys.size();
		String avgThresholdFormatted = String.format("%.12f",avgThreshold);
		return new BigDecimal(avgThresholdFormatted);
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
