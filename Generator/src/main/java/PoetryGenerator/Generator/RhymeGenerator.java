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

	public String getRhymingWord(String prevWord3, String prevWord2, String prevWord1, String wordToReplace, String wordToRhymeWith) {
		wordToReplace = wordToReplace.split("[\\p{Punct}\\s]+")[0];
		prevWord3 = prevWord3 + " ";
		prevWord2 = prevWord2 + " ";
		prevWord1 = prevWord1 + " ";
		switch(generationGram) {
		case "2-gram":
			prevWord3 = "";
			prevWord2 = "";
		case "3-gram":
			prevWord3 = "";
		}

		//Default: return original word
		String rhymingWord = wordToReplace;
		String prevSequence = prevWord3 + prevWord2 + prevWord1;

		Document wordFromDb = mongo.getSequenceMatches(collection, wordToReplace, "word").get(0);
		String wordPOS = wordFromDb.getString("POS");
		BigDecimal highestProbability = new BigDecimal(0);

		List<Document> matches = mongo.getSequenceMatches(collection, wordPOS, "POS");
		for(Document match : matches) {
			String wordMatch = match.getString("word");
			if(doWordsRhyme(wordMatch, wordToRhymeWith)) {
				System.out.println("Rhyme found!");
				Document associations = (Document) match.get("associations");
				Document gramData = (Document) associations.get(generationGram);
				if(gramData != null) {
					Set<String> gramWords = gramData.keySet();
					for(String word : gramWords ) {
						String prevParts = word.replace(wordMatch, "");
						Double probability;
						if(prevSequence.equals(prevParts) && !wordMatch.split("[\\p{Punct}\\s]+")[0].equals(wordToReplace)) {
							 probability = new Double(match.get("probability").toString());
						}
						else {
							 probability = 0.00000001;
						}
						BigDecimal thisProb = new BigDecimal(probability);
						if(thisProb.compareTo(highestProbability) > 0) {
							highestProbability = thisProb;
							rhymingWord = wordMatch;
						}
					} 
				}
			}
		}
		if(rhymingWord == wordToReplace) System.out.println("No rhymes found");
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
