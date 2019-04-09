package PoetryGenerator.Generator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import org.bson.Document;

/**
 * Calculates cost of poem
 * @author Clare
 * @version 09/04/2019
 */
public class CostCalculator {
	private String evaluationGram;
	private final MongoInterface mongo = new MongoInterface("poetryDB");
	private final String collection = "languageModel";
	private RhymeGenerator rhyme = new RhymeGenerator();
	private BigDecimal thisCost = new BigDecimal(0);

	public CostCalculator(String evaluationGram) {
		this.evaluationGram = evaluationGram;
	}

	/**
	 * Main method
	 * @param poem
	 * @return cost of poem
	 */
	public BigDecimal getCost(String poem) {
		switch(evaluationGram) {
		case "2-gram":
			thisCost = getCostOfPoemBigram(poem);
		case "3-gram":
			thisCost =  getCostOfPoemThreeGram(poem);
		case "4-gram":
			thisCost =  getCostOfPoemFourGram(poem);
		}
		thisCost = thisCost.add(checkRhymeCost(poem));
		thisCost = thisCost.add(checkLengthCost(poem));
		return thisCost;
		
	}
	
	/**
	 * Reward poems that have short line lengths
	 * @param poem
	 * @return cost to be added to thisCost
	 */
	private BigDecimal checkLengthCost(String poem) {
		BigDecimal costIncrease = new BigDecimal(0);
		String[] poemLines = poem.split("\\r?\\n");
		for(String poemLine : poemLines) {
			int lineLength = poemLine.split(" ").length;
			if(lineLength <= 6) {
				costIncrease = thisCost.multiply(new BigDecimal(0.35));
			}
		}
		return costIncrease;
	}
	
	/**
	 * Reward poems that have rhyme
	 * @param poem
	 * @return cost to be added to thisCost
	 */
	private BigDecimal checkRhymeCost(String poem) {
		BigDecimal costIncrease = new BigDecimal(0);
		String[] poemLines = poem.split("\\r?\\n");
		ArrayList<String> rhymeCandidates = new ArrayList<String>();
		//Get ending words
		for(int i = 0; i < poemLines.length; i++) {
			String[] lineWords = poemLines[i].split(" ");
			String lastWord = lineWords[lineWords.length-1];
			rhymeCandidates.add(lastWord);
		}
		
		for(int i = 0; i < rhymeCandidates.size(); i++) {
		    String candidate1 = rhymeCandidates.get(i);
		    for(int j = 0; j < rhymeCandidates.size(); j++) {
		        if(j == i) continue; // will  increase j
		        String candidate2 = rhymeCandidates.get(j);
		        if(rhyme.doWordsRhyme(candidate1, candidate2)) {
		        	if(!candidate1.equals(candidate2)) {
		        		costIncrease = thisCost.multiply(new BigDecimal(0.3));
		        	} else {
		        		costIncrease = thisCost.multiply(new BigDecimal(0.2));
		        	}
				}
		    }
		} 
		return costIncrease;
	}
	
	

	/**
	 * Calculate cost of a candidate poem using chain rule on bigrams
	 * P(A,B,C,D) = P(A) * P(B | A) * P(C | A, B) * P(D | A, B, C)
	 * @return cost of candidate poem
	 */
	private BigDecimal getCostOfPoemBigram(String poem) {
		String[] poemSentences = poem.split("\\?|\\.|\\!");

		//probability += joint probability of words
		BigDecimal probability = new BigDecimal(0);
		for(String sentence : poemSentences) {
			//Split punctuation to help with process
			sentence = sentence.replace(",", " ,");
			sentence = sentence.replace("?", " ?");
			sentence = sentence.replace("!", " !");
			String[] words = sentence.split(" ");
			//Find probability of words[i]
			for(int i = 0; i < words.length; i++) {
				if(words[i].trim().length() != 0) {
					//sequence: n-1, n
					String sequence;
					if(i == 0) {
						sequence = "<s> " + words[i];
					}
					else if(i == words.length) {
						sequence = words[i] + " </s>";
					}
					else {
						sequence =  words[i-1] + " " + words[i];
					}
					//prob of this sequence = P(words[i]) * P(words[i-1] words[i])
					String[] sequenceParts = sequence.split(" ");
					BigDecimal thisProbability = getSequenceProbability(sequenceParts[0]).multiply(getSequenceProbability(sequenceParts[1]));
					probability =  probability.add(thisProbability);
				}

			}
		}
		probability = probability.divide(new BigDecimal(poemSentences.length), MathContext.DECIMAL64);
		System.out.println("probability: " + probability);
		return probability;
	}


	/**
	 * Calculate cost of a candidate poem using chain rule on trigrams
	 * P(A,B,C,D) = P(A) * P(B | A) * P(C | A, B) * P(D | A, B, C)
	 * @return cost of candidate poem
	 */
	private BigDecimal getCostOfPoemThreeGram(String poem) {
		String[] poemSentences = poem.split("\\?|\\.|\\!");

		//probability += joint probability of words
		BigDecimal probability = new BigDecimal(0);
		for(String sentence : poemSentences) {
			sentence = sentence.trim();
			if(sentence.length() > 0) {
				//Split punctuation to help with process
				sentence = sentence.replace(",", " ,");
				sentence = sentence.replace("?", " ?");
				sentence = sentence.replace("!", " !");
				String[] words = sentence.split(" ");
				//Find probability of words[i]
				for(int i = 1; i < words.length; i++) {
					if(words[i].trim().length() != 0) {
						//sequence: n-1, n
						String sequence;
						if(i == 1) {
							sequence = "<s> " + words[i-1] + " " + words[i];
						}
						else if(i == words.length) {
							sequence = words[i-1] + " " + words[i] + "</s>";
						}
						else {
							sequence =  words[i-2] + " " + words[i-1] + " " + words[i];
						}
						//BigDecimal probOfXGivenY = 
						//prob of this sequence = P(words[i]) * P(words[i-1] words[i])
						String[] sequenceParts = sequence.split(" ");
						BigDecimal thisProbability = getSequenceProbability(sequenceParts[0]).multiply(getSequenceProbability(sequenceParts[1] + " " + sequenceParts[2]));
						probability =  probability.add(thisProbability);
					}

				}
			}
		}
		
		probability = probability.divide(new BigDecimal(poemSentences.length), MathContext.DECIMAL64);
		System.out.println("probability: " + probability);
		return probability;
	}
	
	/**
	 * Calculate cost of a candidate poem using chain rule on fourgrams
	 * P(A,B,C,D) = P(A) * P(B | A) * P(C | A, B) * P(D | A, B, C)
	 * @return cost of candidate poem
	 */
	private BigDecimal getCostOfPoemFourGram(String poem) {
		String[] poemSentences = poem.split("\\?|\\.|\\!");

		//probability += joint probability of words
		BigDecimal probability = new BigDecimal(0);
		for(String sentence : poemSentences) {
			sentence = sentence.trim();
			if(sentence.length() > 0) {
				//Split punctuation to help with process
				sentence = sentence.replace(",", " ,");
				sentence = sentence.replace("?", " ?");
				sentence = sentence.replace("!", " !");
				String[] words = sentence.split(" ");
				//Find probability of words[i]
				for(int i = 2; i < words.length; i++) {
					if(words[i].trim().length() != 0) {
						//sequence: n-1, n
						String sequence;
						if(i == 2) {
							sequence = "<s> " + words[i-2] + " " +  words[i-1] + " " + words[i];
						}
						else if(i == words.length) {
							sequence = words[i-1] + " " + words[i] + "</s>";
						}
						else {
							sequence = words[i-3] + " " +  words[i-2] + " " + words[i-1] + " " + words[i];
						}
						//BigDecimal probOfXGivenY = 
						//prob of this sequence = P(words[i]) * P(words[i-1] words[i])
						String[] sequenceParts = sequence.split(" ");
						BigDecimal thisProbability = getSequenceProbability(sequenceParts[0]).multiply(getSequenceProbability(sequenceParts[1] + " " + sequenceParts[2] + " " + sequenceParts[3]));
						probability =  probability.add(thisProbability);
					}
				}
			}
		}
		
		probability = probability.divide(new BigDecimal(poemSentences.length), MathContext.DECIMAL64);
		System.out.println("probability: " + probability);
		return probability;
	}
	
	
	/**
	 * Get probability of a given sequence from the database
	 * @param sequence - sequence to be checked
	 * @return probability of sequence
	 */
	private BigDecimal getSequenceProbability(String sequence) {
		BigDecimal defaultProb = new BigDecimal(0.000000000000000000000000000001);
		String word = sequence;
		String gramVal = "1-gram";
		if(sequence.split(" ").length == 2) {
			word = sequence.split(" ")[1];
			gramVal = "2-gram";
		}
		if(sequence.split(" ").length == 3) {
			word = sequence.split(" ")[2];
			gramVal = "3-gram";
		}

		List<Document> sequenceMatches = mongo.getSequenceMatches(collection, word, "word");
		for(Document match : sequenceMatches) {
			Document associations = (Document) match.get("associations");
			Document ngramData = (Document) associations.get(gramVal);
			Set<String> words = ngramData.keySet();
			for(String keyWord : words) {
				if(keyWord.equalsIgnoreCase(sequence)) {
					Document thisWord = (Document) ngramData.get(keyWord);
					Double probability = new Double(thisWord.get("probability").toString());
					BigDecimal thisProb = new BigDecimal(probability);
					return thisProb;
				}
			}
		}
		return defaultProb;
	}
}
