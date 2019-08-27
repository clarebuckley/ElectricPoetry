package PoetryGenerator.Generator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.bson.Document;


/**
 * TODO: this could do with being cleaned up --> don't need method for each n-gram, use regex in one method
 * Generate word sequences using n-grams stored in database
 * @author Clare Buckley
 * @version 09/04/19
 *
 */

public class NGramController {
	private final MongoInterface mongo = new MongoInterface("poetryDB");
	private final String collection = "languageModel";
	private String generationGram;
	private static final int SEARCH_LIMIT = 300;

	public NGramController(String generationGram) {
		this.generationGram = generationGram;
	}

	/**
	 * Returns word to be added to poem, given the previous n words in the poem
	 * @param word - current POS tag
	 * @param originalWord - current word from original poem
	 * @param prevWord1 - n-1
	 * @param prevWord2 - n-2
	 * @param prevWord1POS - POS tag for n-1
	 * @param prevWord2POS - POS tag for n-2
	 * @return word to be used in poem
	 */
	public String getWord(String word, String originalWord, String prevWord1, String prevWord2, String prevWord3, String prevWord1POS, String prevWord2POS, String prevWord3POS) {
		String wordToReturn = "";
		switch(generationGram) {
		case "2-gram":
			wordToReturn = findWordUsingBigram(prevWord1POS, prevWord1,  word);
		case "3-gram":
			wordToReturn = findWordUsingTrigram(prevWord2POS, prevWord1POS, prevWord2, prevWord1, word);
		case "4-gram":
			wordToReturn = findWordUsingFourGram(prevWord3POS, prevWord2POS, prevWord1POS, prevWord3, prevWord2, prevWord1, word);
		}

		if(word == 	"NNP" || word == "NNPS") {
			wordToReturn = wordToReturn.substring(0, 1).toUpperCase() + wordToReturn.substring(1);
		}
		return wordToReturn;
	}


	/**
	 * Get word using bigram
	 * If iteration limit is reached with no word replacement found, original poem word will be used
	 * @param prevWord1POS
	 * @param prevWord1
	 * @param wordPOS
	 * @return
	 */
	private String findWordUsingBigram(String prevWord1POS, String prevWord1, String wordPOS) {
		String result = null;
		if(prevWord1 == null || prevWord1.equals("")) {
			prevWord1 = "<s>";
		}
		if(prevWord1.equals("<s>")) {
			prevWord1POS = "";
		}
		List<Document> matches = mongo.getSequenceMatches(collection, wordPOS, "POS");
		int i = 0;
		BigDecimal highestProbability = new BigDecimal(0);
		for(Document match : matches) {
			Random random = new Random();
			if(i% (random.nextInt(5)+1)== 0) {
				continue;
			}
			Document associations = (Document) match.get("associations");
			Document bigramData = (Document) associations.get("2-gram");
			Set<String> bigramWords = bigramData.keySet();
			String bigramN1, bigramWord;

			int j = 0;
			for(String word : bigramWords ) {
				if(j == SEARCH_LIMIT) {
					break;
				}
				bigramN1 = word.split(" ")[0];
				bigramWord = word.split(" ")[1];
				if(prevWord1.equals(bigramN1)) {
					//check pos tag of previous words
					Document thisWord = (Document) bigramData.get(word);
					String posTags = thisWord.get("POS").toString();

					String prevWordPOS = posTags.split(" ")[0];
					if(prevWordPOS.equals(prevWord1POS)) {
						//check probability
						Double probability = new Double(thisWord.get("probability").toString());
						BigDecimal thisProb = new BigDecimal(probability);
						if(thisProb.compareTo(highestProbability) > 0) {
							highestProbability = thisProb;
							result = bigramWord;
						}
					}
				}
				j++;
			}
			i++;
			if(i == SEARCH_LIMIT) {
				break;
			}
		}
		//system.out.println("result --> " + result);
		return result;
	}


	/**
	 * Get word using 3-grams
	 * If iteration limit is reached with no word replacement found, original poem word will be used
	 * @param prevWord2POS - POS of word 2 places before word to find
	 * @param prevWord1POS - POS of word before the word to find
	 * @param prevWord2 - word 2 places before the word to find
	 * @param prevWord1 - word before the word to find
	 * @param wordPOS - pos of word to find
	 * @return word to be used in poem
	 */
	private String findWordUsingTrigram(String prevWord2POS, String prevWord1POS, String prevWord2, String prevWord1, String wordPOS) {
		String result = null;
		List<Document> matches = mongo.getSequenceMatches(collection, wordPOS, "POS");

		BigDecimal highestProbability = new BigDecimal(0);

		//	int matchSample = (int) (matches.size()*0.05);
	
		for(int i = 0; i < matches.size(); i++ /*Document match : matches*/) {
			Random random = new Random();
			if(i% (random.nextInt(5)+1) == 0) {
				continue;
			}
			Document match = matches.get(i);
			Document associations = (Document) match.get("associations");
			Document ngramData = (Document) associations.get("3-gram");

			if(ngramData == null) {
				return findWordUsingBigram(prevWord1POS, prevWord1, wordPOS);
			}

			Set<String> trigramWords = ngramData.keySet();
			String trigramN2 = "", trigramN1, trigramWord;

			int j = 0;
			for(String word : trigramWords ) {
				if(j == SEARCH_LIMIT) { 
					break;
				}
				trigramN2 = word.split(" ")[0];
				trigramN1 = word.split(" ")[1];
				trigramWord = word.split(" ")[2];

				if(prevWord1.equals(trigramN1) || prevWord2.equals(trigramN2)) {
					//check pos tag of previous words
					Document thisWord = (Document) ngramData.get(word);
					String posTags = thisWord.get("POS").toString();
					String[] posParts = posTags.split(" ");
					String trigramPrev2POS = posParts[2];
					String trigramPrev1POS = posParts[1];

					if(trigramPrev2POS.equals(prevWord2POS) || trigramPrev1POS.equals(prevWord1POS)) {
						//check probability
						Double probability = new Double(thisWord.get("probability").toString());
						BigDecimal thisProb = new BigDecimal(probability);
						if(thisProb.compareTo(highestProbability) > 0) {
							highestProbability = thisProb;
							result = trigramWord;
						}
					}
				}

				j++;
			}

		}
		//system.out.println("result --> " + result);
		return result;
	}

	/**
	 * 	
	/**
	 * Get word using 4-grams
	 * If iteration limit is reached with no word replacement found, original poem word will be used
	 * @param prevWord3POS - POS of word 3 places before word to find
	 * @param prevWord2POS - POS of word 2 places before word to find
	 * @param prevWord1POS - POS of word before the word to find
	 * @param prevWord3 - word 3 places before the word to find
	 * @param prevWord2 - word 2 places before the word to find
	 * @param prevWord1 - word before the word to find
	 * @param wordPOS - pos of word to find
	 * @return word to be used in poem
	 */

	private String findWordUsingFourGram(String prevWord3POS, String prevWord2POS, String prevWord1POS, String prevWord3, String prevWord2,  String prevWord1, String wordPOS) {
		String result = null;
		boolean useBigram = false;
		boolean useTrigram = false;
		List<Document> matches = mongo.getSequenceMatches(collection, wordPOS, "POS");
		int i = 0;
		BigDecimal highestProbability = new BigDecimal(0);

		for(Document match : matches) {
			Document associations = (Document) match.get("associations");
			Document ngramData = (Document) associations.get("4-gram");


			if(ngramData == null) {
				useTrigram = true;
				ngramData = (Document) associations.get("3-gram");
				if(ngramData == null) {
					useBigram = true;
					useTrigram = false;
					ngramData = (Document) associations.get("2-gram");
				}
			}

			if(useTrigram) return findWordUsingTrigram(prevWord2POS, prevWord1POS, prevWord2, prevWord1, wordPOS);
			if(useBigram) return findWordUsingBigram(prevWord1POS, prevWord1, wordPOS);

			Set<String> trigramWords = ngramData.keySet();
			String trigramN3 = "", trigramN2 = "", trigramN1, trigramWord;      	

			int j = 0;
			for(String word : trigramWords ) {
				Random random = new Random();
				if(i% (random.nextInt(5)+1) == 0) {
					continue;
				}
				if(j == SEARCH_LIMIT) {
					break;
				}

				trigramN3 = word.split(" ")[0];
				trigramN2 = word.split(" ")[1];
				trigramN1 = word.split(" ")[2];
				trigramWord = word.split(" ")[3];

				if((prevWord1.equals(trigramN1) && prevWord2.equals(trigramN2)) & prevWord3.equals(trigramN3)) {

					//check pos tag of previous words
					Document thisWord = (Document) ngramData.get(word);
					String posTags = thisWord.get("POS").toString();
					String[] posParts = posTags.split(" ");
					String fourgramPrev3POS = posParts[3];
					String fourgramPrev2POS = posParts[2];
					String fourgramPrev1POS = posParts[1];
					if(fourgramPrev3POS.equals(prevWord3POS) && fourgramPrev2POS.equals(prevWord2POS) && fourgramPrev1POS.equals(prevWord1POS)) {
						//check probability
						Double probability = new Double(thisWord.get("probability").toString());
						BigDecimal thisProb = new BigDecimal(probability);
						if(thisProb.compareTo(highestProbability) > 0) {
							highestProbability = thisProb;
							result = trigramWord;
						}
					}
				}

				j++;
			}
			i++;
			if(i == SEARCH_LIMIT) {
				break;
			}
		}
		//	//system.out.println("result --> " + result);
		return result;
	}


}
