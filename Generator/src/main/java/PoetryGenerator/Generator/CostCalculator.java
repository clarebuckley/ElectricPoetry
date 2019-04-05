package PoetryGenerator.Generator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Set;


import org.bson.Document;

public class CostCalculator {
	String evaluationGram;
	private final MongoInterface mongo = new MongoInterface("poetryDB");
	private final String collection = "languageModel";

	public CostCalculator(String evaluationGram) {
		this.evaluationGram = evaluationGram;
	}

	public BigDecimal getCost(String poem) {
		switch(evaluationGram) {
		case "2-gram":
			return getCostOfPoemBigram(poem);
		case "3-gram":
			return getCostOfPoemThreeGram(poem);
//		case "4-gram":
//			return getCostOfPoemThreeGram(poem);
			
			default:
				throw new Error("Evaluation n-gram must be 2, 3, or 4 gram");
		}
		
	}

	/**
	 * Calculate cost of a candidate poem using chain rule on bigrams
	 * P(A,B,C,D) = P(A) * P(B | A) * P(C | A, B) * P(D | A, B, C)
	 * try fourgram with bigram generation, etc
	 * discuss why results have given that result, interpret results and explore combinations
	 * allow config for different grams/cost generations
	 * @return
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
