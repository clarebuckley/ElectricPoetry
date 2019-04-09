package PoetryGenerator.Generator;

import java.math.BigDecimal;
import java.util.List;

import org.bson.Document;

/**
 * Adds rhyme to a given poem
 * @author Clare Buckley
 * @version 09/04/19
 *
 */
public class RhymeGenerator {
	private final MongoInterface mongo = new MongoInterface("poetryDB");
	private final String collection = "languageModel";

	public RhymeGenerator() {
	}

	/**
	 * Get a word to rhyme with a given word
	 * @param prevWord1 - previous word in poem
	 * @param wordToReplace - current word in poem that should be replaced with a rhyming word
	 * @param wordToRhymeWith - word that wordToReplace should rhyme with
	 * @return word to replace wordToReplace
	 */
	public String getRhymingWord(String prevWord1, String wordToReplace, String wordToRhymeWith) {
		//Default
		String rhymingWord = "";
		wordToReplace = wordToReplace.split("[\\p{Punct}\\s]+")[0];
		wordToRhymeWith = wordToRhymeWith.split("[\\p{Punct}\\s]+")[0];

		Document wordFromDb = mongo.getSequenceMatches(collection, wordToReplace, "word").get(0);
		String wordPOS = wordFromDb.getString("POS");
		BigDecimal highestProbability = new BigDecimal(0);

		List<Document> matches = mongo.getSequenceMatches(collection, wordPOS, "POS");
		int i = 0;
		//For all words that have the same POS tag as the original word
		for(Document match : matches) {
			if(i == 30) {
				break;
			}
			String wordMatch = match.getString("word");
			if(doWordsRhyme(wordMatch, wordToRhymeWith)) {
				Document associations = (Document) match.get("associations");
				Document ngram = (Document) associations.get("2-gram");
				//For all bigram sequences for the word that rhymes
				for(String ngramSequence : ngram.keySet()) {
					String ngramPrev1 = ngramSequence.split(" ")[0].replaceAll("_", ".");
					Document ngramPrev1Db = mongo.getSequenceMatches(collection, ngramPrev1, "word").get(0);
					Document prevWord1Db = mongo.getSequenceMatches(collection, prevWord1, "word").get(0);
					String ngramPrev1POS = ngramPrev1Db.getString("POS");
					String prevWord1POS = prevWord1Db.getString("POS");
					if(wordMatch.equals(wordToReplace)){
						break;
					}
					
					//Make sure word isn't replaced by same word
					if(!wordMatch.equals(wordToReplace) && ngramPrev1POS.equals(prevWord1POS)) {
						Document thisDoc = (Document) ngram.get(ngramSequence);
						Double probability = new Double(thisDoc.get("probability").toString());
						BigDecimal thisProb = new BigDecimal(probability);
						if(thisProb.compareTo(highestProbability) > 0) {
							highestProbability = thisProb;
							rhymingWord = wordMatch;
						}
					} 
				}

			}
			i++;
		}

		return rhymingWord;
	}


	/**
	 * Check if two words rhyme
	 * Two words are considered to rhyme if they end in the same last two letters, e.g. 'cat' and 'hat'
	 * @param word1
	 * @param word2
	 * @return true if words rhyme
	 */
	public boolean doWordsRhyme(String word1, String word2) {
		if (word1.length() >= 2 && word2.length() >= 2) {
			String last1 = word1.substring(word1.length()-2).toLowerCase();
			String last2 = word2.substring(word2.length()-2).toLowerCase();
			if (last1.equals(last2)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
