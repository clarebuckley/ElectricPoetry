package PoetryGenerator.Generator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.bson.Document;


/**
 * Generate word sequences using n-grams stored in database
 * @author Clare Buckley
 * @version 25/03/19
 *
 */

public class NGramController {
	private final MongoInterface mongo = new MongoInterface("poetryDB");
	private final String collection = "languageModel";

	public NGramController() {}

	/**
	 * 
	 * @param word - current POS tag
	 * @param originalWord - current word from original poem
	 * @param prevWord1 - n-1
	 * @param prevWord2 - n-2
	 * @param prevWord1POS - POS tag for n-1
	 * @param prevWord2POS - POS tag for n-2
	 * @return
	 */
	public String getWord(String word, String originalWord, String prevWord1, String prevWord2, String prevWord1POS, String prevWord2POS) {
		String result;

	//	System.out.println("getting sequence matches for: " + word);
	//	if(prevWord2 == null | prevWord2.equals("")) {
			result	= findWordUsingBigram(prevWord1POS, prevWord1,  word);
	//	} else {
	//		result = findWordUsingTrigram(prevWord2POS, prevWord1POS, prevWord2, prevWord1, word);
	//	}

		return result;
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
			Document associations = (Document) match.get("associations");
			Document bigramData = (Document) associations.get("2-gram");
			Set<String> bigramWords = bigramData.keySet();
			String bigramN1, bigramWord;

			int j = 0;
			for(String word : bigramWords ) {
				if(j == 150) {
					break;
				}
				bigramN1 = word.split(" ")[0];
				bigramWord = word.split(" ")[1];
				if(prevWord1.trim().equals(bigramN1.trim())) {
					//check probability
					Document thisWord = (Document) bigramData.get(word);
					Double probability = new Double(thisWord.get("probability").toString());
					BigDecimal thisProb = new BigDecimal(probability);
					if(thisProb.compareTo(highestProbability) > 0) {
						highestProbability = thisProb;
						result = bigramWord;
						break;
					}
				}
				j++;
			}
			i++;
			if(i == 150) {
				break;
			}
		}
		System.out.println("result --> " + result);
		return result;
	}

	private String findWordUsingTrigram(String prevWord2POS, String prevWord1POS, String prevWord2, String prevWord1, String wordPOS) {
		String result = null;
		boolean useBigram = false;
		List<Document> matches = mongo.getSequenceMatches(collection, wordPOS, "POS");
		int i = 0;
		BigDecimal highestProbability = new BigDecimal(0);

		for(Document match : matches) {
			Document associations = (Document) match.get("associations");
			Document ngramData = (Document) associations.get("3-gram");

			if(ngramData == null) {
				useBigram = true;
				ngramData = (Document) associations.get("2-gram");
			}

			Set<String> trigramWords = ngramData.keySet();
			String trigramN2 = "", trigramN1, trigramWord;

			int j = 0;
			for(String word : trigramWords ) {
				if(j == 150) {
					break;
				}
				if(!useBigram) {
					trigramN2 = word.split(" ")[0];
					trigramN1 = word.split(" ")[1];
					trigramWord = word.split(" ")[2];
				} else {
					trigramN1 = "";
					trigramN1 = word.split(" ")[0];
					trigramWord = word.split(" ")[1];
				}
				if(useBigram) {
					if(prevWord1.equals(trigramN1)) {
						//check probability
						Document thisWord = (Document) ngramData.get(word);
						Double probability = new Double(thisWord.get("probability").toString());
						BigDecimal thisProb = new BigDecimal(probability);
						if(thisProb.compareTo(highestProbability) > 0) {
							highestProbability = thisProb;
							result = trigramWord;
							break;
						}
					}
				} else {
					//should favour results using trigram over bigram
					double probIncrease;
					if(prevWord1.equals(trigramN1) && prevWord2.equals(trigramN2)) {
						probIncrease = 0.002;
					} else {
						probIncrease = 0;
					}

					if(prevWord1.equals(trigramN1)) {
						//check probability
						Document thisWord = (Document) ngramData.get(word);
						Double probability = new Double(thisWord.get("probability").toString()) + probIncrease;  
						BigDecimal thisProb = new BigDecimal(probability);
						if(thisProb.compareTo(highestProbability) > 0) {
							highestProbability = thisProb;
							result = trigramWord;
							break;
						}
					}

				}
				j++;
			}
			i++;
			if(i == 150) {
				break;
			}
		}
		System.out.println("result --> " + result);
		return result;
	}


}
