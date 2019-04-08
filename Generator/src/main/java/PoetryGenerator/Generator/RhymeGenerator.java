package PoetryGenerator.Generator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.bson.Document;

public class RhymeGenerator {
	private final String generationGram;
	private final MongoInterface mongo = new MongoInterface("poetryDB");
	private final String collection = "languageModel";

	public RhymeGenerator(String generationGram) {
		this.generationGram = generationGram;
	}

	public String getRhymingWord(/*String prevWord3, String prevWord2,*/ String prevWord1, String wordToReplace, String wordToRhymeWith) {
		//Default
		String rhymingWord = "";
		wordToReplace = wordToReplace.split("[\\p{Punct}\\s]+")[0];
		wordToRhymeWith = wordToRhymeWith.split("[\\p{Punct}\\s]+")[0];
		//		prevWord3 = prevWord3 + " ";
		//		prevWord2 = prevWord2 + " ";
		//		prevWord1 = prevWord1 + " ";
	/*	switch(generationGram) {
		case "2-gram":
			prevWord3 = "";
			prevWord2 = "";
		case "3-gram":
			prevWord3 = "";
		}*/


	//	String prevSequence = prevWord3 + prevWord2 + prevWord1;

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
			System.out.println("Do " + wordMatch + " and " + wordToRhymeWith + " rhyme?");
			if(doWordsRhyme(wordMatch, wordToRhymeWith)) {
				System.out.println("Rhyme found!");
				Document associations = (Document) match.get("associations");
				Document ngram = (Document) associations.get("2-gram");
				//For all bigram sequences for the word that rhymes
				for(String ngramSequence : ngram.keySet()) {
					String ngramPrev1 = ngramSequence.split(" ")[0].replaceAll("_", ".");
					System.out.println("ngram prev1: " + ngramPrev1 + ", original prev1: " + prevWord1);
					Document ngramPrev1Db = mongo.getSequenceMatches(collection, ngramPrev1, "word").get(0);
					Document prevWord1Db = mongo.getSequenceMatches(collection, prevWord1, "word").get(0);
					String ngramPrev1POS = ngramPrev1Db.getString("POS");
					String prevWord1POS = prevWord1Db.getString("POS");
					System.out.println("does " + ngramPrev1POS + " = " + prevWord1POS +" and " + wordMatch + " != " + wordToReplace + "?");
					if(wordMatch.equals(wordToReplace)){
						break;
					}
					int j = 0;
					//Make sure word isn't replaced by same word
					if(!wordMatch.equals(wordToReplace) && ngramPrev1POS.equals(prevWord1POS)) {
						if(j == 60) {
							break;
						}
						System.out.println("-----> YES");
						System.out.println(ngram.get(ngramSequence));
						Document thisDoc = (Document) ngram.get(ngramSequence);
						Double probability = new Double(thisDoc.get("probability").toString());
						BigDecimal thisProb = new BigDecimal(probability);
						if(thisProb.compareTo(highestProbability) > 0) {
							highestProbability = thisProb;
							rhymingWord = wordMatch;
						}
					} 
					j++;
				}

			}
			i++;
		}

		return rhymingWord;
	}


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
