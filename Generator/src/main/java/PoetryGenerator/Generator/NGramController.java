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
	private final BigDecimal replaceThreshold = new BigDecimal(0.00001);

	public NGramController() {}

	public String addNGrams(String poem) {
		poem.replaceAll(".", ". * ");
		poem = "* " + poem;
		String[] words = poem.split(" ?(?<!\\G)((?<=[^\\p{Punct}])(?=\\p{Punct})|\\b) ?");
		for(int i = 1; i < words.length; i++) {
			System.out.println(Arrays.toString(words));
			String word = words[i].toLowerCase();
			
			BigDecimal wordProb = getWordProbability(word);
			System.out.println(wordProb);
			if(wordProb.compareTo(replaceThreshold) < 0) {
				word = findMoreProbableWord(word);
			}
			
			if(word.length() > 0 && word != null && !word.equals(" ") && !word.equals(System.getProperty("line.separator"))) {
				System.out.println(word);

				String prevWord1 = words[i-1].toLowerCase();
				String prevWord2 = "", prevWord3 = "";
				prevWord2 = ((i>=2) ? words[i-2].toLowerCase() : "");
				prevWord3 = ((i>=3) ? words[i-3].toLowerCase(): "");

				if(prevWord1.equals("*")) prevWord1 = "<s>";
				if(prevWord2 != "" && prevWord2.equals("*")) prevWord2 = "<s>";
				word = getPoemWord(word, prevWord1, prevWord2, prevWord3);
			} 
			
			String[] replacements = word.split(" ");
			int nGramLength = replacements.length;
			System.out.println(nGramLength);
			int index = 0;
			while(nGramLength > 0) {
				words[i-nGramLength] = replacements[index];
				nGramLength--;
				index++;
			}

			System.out.println("DONE WORD!!!!+===========================================================================");
		}
		String newPoem = "";
		for(String word : words) {
			newPoem = newPoem + word.toLowerCase() + " ";
		}
		return newPoem;
	}
	
	/**
	 * Replace 'word' with word of higher probability in database
	 * @param word
	 * @param threshold
	 * @return
	 */
	private String findMoreProbableWord(String word) {
		String replacement = "";
		int iterations = 0;
		while(replacement.equals("")) {
			JsonObject ngramJson = getRandomDocument(1);
			String potentialReplacement = (String) ngramJson.keySet().toArray()[0];
			System.out.println(ngramJson.keySet().toString());
			System.out.println("here: " + potentialReplacement);
			System.out.println(ngramJson.toString());
			BigDecimal prob = getWordProbability(potentialReplacement);
			if(prob.compareTo(replaceThreshold) > 0 || iterations > 200) {
				replacement = potentialReplacement;
				System.out.println("replaced word:  " + word + " --> " + replacement);
			}
			iterations++;
		}
		return replacement;
	}
	
	private BigDecimal getWordProbability(String word) {
		//WordModel for this word in the database
		JsonObject data = getWordModelFromDB("1-gram", word);
		System.out.println("get word probability: " + word);
		try {
			JsonObject wordData = data.getJsonObject(word);
			Double probability = new Double(wordData.get("probability").toString());
			return new BigDecimal(probability);
		} catch(Exception e) {
			return new BigDecimal(0);
		}
	}

	/**
	 * Get word to be used in poem, given the previous n words
	 * @param word
	 * @return
	 */
	private String getPoemWord(String word, String prevWord1, String prevWord2, String prevWord3) {
		//Determine which n-gram to use from database
		String nGramVal = ((!prevWord2.equals("")) ? "3-gram" : "2-gram");
		if(!prevWord3.equals("")) nGramVal = "4-gram";

		//WordModel for this word in the database
		JsonObject existingGramData = getWordModelFromDB(nGramVal, word);
		//All n-grams that exist for this word
		Set<String> existingGrams = existingGramData.keySet();
		String result = "";

		//Check if poem sequence exists in database already
		String poemText = /*prevWord3 + " " + prevWord2 + " " +*/ prevWord1 + " " +  word;
		if(poemTextExists(poemText.trim(), existingGrams, existingGramData)) {
			result = word;
		} else {
			int n = new Integer(nGramVal.split("-")[0]);
			switch(n) {
			case(2):
				//bigrams
				result = bigramReplace(prevWord1, word);
			break;
			case(3):
				//trigrams
				result = bigramReplace(prevWord1, word);
			//	result = trigramReplace(prevWord1, prevWord2);
			case(4):
				//fourgrams
				result = bigramReplace(prevWord1, word);
			break;
			

			}

			//			if(nGramVal.equals("3-gram")) {
			//		//		result = getHighestProbSequence("3-gram", existingSequence, existingGramData);
			//			}
		}
		if(result.split(" ").length > 1 && result.split(" " )[0].equals("<s>")) {
			result = result.replaceAll("<s>", "*");
		}
		System.out.println("WORD: " + word + ", RESULT: " + result);
		return result;
	}


	private boolean poemTextExists(String poemText, Set<String> existingGrams, JsonObject ngramJson) {
		System.out.println("Checking exists");
		boolean exists = false;
		//int n = poemText.split(" ").length;
		int n = 2;  //change this once you start making 3/4 grams
		for(String existingSequence : existingGrams) {
//			JsonObject sequenceModel = (JsonObject) ngramJson.get(existingSequence);
//			BigDecimal threshold = getThreshold(ngramJson, n +"-gram");
//			BigDecimal probability = new BigDecimal(getProbability(sequenceModel));
			//if n-1 for the current poem sequence, keep it as it is
			if(existingSequence.equals(poemText) /*&& probability.compareTo(threshold) > 0*/) {
				exists = true;
			} 
		}
		return exists;
	}

	/**
	 * Replace word to fit 2-gram
	 * @param prevWord1
	 * @return
	 */
	private String bigramReplace(String prevWord1, String word) {
		System.out.println("replacing bigram");
		boolean replacementFound = false;
		String resultN1 = "";
		String resultWord = "";
		int iterations = 0;
		while(!replacementFound) {
			JsonObject ngramJson = getRandomDocument(2);
			Set<String> newKeys = ngramJson.keySet();
			for(String key : newKeys) {
				if(iterations == 200) {
					resultWord = findMoreProbableWord(word);
					resultN1 = prevWord1;
					replacementFound = true;
				}
				
				String keyWords[] = key.split(" ");
				String possibleReplacementN1 = keyWords[0].toLowerCase();
				String possibleReplacementWord = keyWords[1].toLowerCase();
//				JsonObject sequenceModel = (JsonObject) ngramJson.get(key);
//				BigDecimal threshold = getThreshold(ngramJson, "2-gram");
//				BigDecimal probability = new BigDecimal(getProbability(sequenceModel));
		//		System.out.println(possibleReplacementWord + " = " + word + " OR " + possibleReplacementN1 + " = " + prevWord1); 
				if(possibleReplacementN1.equals(prevWord1) || possibleReplacementWord.equals(word)/*&& probability.compareTo(threshold) > 0*/) {
					resultN1 = possibleReplacementN1;
					resultWord = possibleReplacementWord;
					replacementFound = true;
				}
			}
			iterations++;
			System.out.println(iterations);
		}
		return resultN1 + " " + resultWord;
	}


	private String trigramReplace(String prevWord1, String prevWord2) {
		String result = "";
		while(result.length() == 0) {

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
		Set<String> keys = model.keySet();
		double highest = -1;
		double lowest = 10000;
		for(String key : keys) {
			JsonObject data = model.getJsonObject(key);
			double probability = getProbability(data);
			if(probability > highest) {
				highest = probability;
			}
			if(probability < lowest) {
				lowest = probability;
			}
		}
		double threshold = (highest + lowest) / 2;
		return new BigDecimal(threshold);
	}


//	private String getHighestProbSequence(String nVal, String sequence, JsonObject sequenceModel) {
//		System.out.println("-----------------" + nVal + ", " + sequence + ", " + sequenceModel);
//		String[] nWords = sequence.split(" ");
//		Set<String> modelKeys = sequenceModel.keySet();
//		double highestProb = -1;
//		for(String key : modelKeys) {
//			JsonObject data = sequenceModel.getJsonObject(key);
//			double probability = new Double(data.get("probability").toString());
//			System.out.println(probability);
//			if(probability > highestProb) {
//				highestProb = probability;
//			}
//		}
//		String probKey  = "" + highestProb;
//		JsonObject mostLikelyModel = sequenceModel.getJsonObject(probKey.replace(".","_"));
//		return mostLikelyModel.get(nVal).toString();
//	}




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
