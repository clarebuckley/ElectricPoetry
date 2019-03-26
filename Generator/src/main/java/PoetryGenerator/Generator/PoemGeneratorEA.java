package PoetryGenerator.Generator;


import java.math.BigDecimal;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
/**
 * Evolutionary algorithm to find the highest scoring poem
 * @author Clare Buckley
 * @version 26/03/19
 */

public class PoemGeneratorEA {

	private JLanguageTool langTool = new JLanguageTool(new BritishEnglish());
	private PoemGenerator poemGenerator = new PoemGenerator();
	private final MongoInterface mongo = new MongoInterface("poetryDB");
	private final String collection = "languageModel";
	//ArrayList of candidates to be used
	private ArrayList<String> population;
	//Number of candidate solutions
	private final int populationSize;
	//Probability of mutation being performed on candidates
	private final double mutationProbability;
	//Number of iterations to be completed by the algorithm
	private final int numberOfGenerations;
	//Sample size for tournament parent selection
	private final int tournamentSize;

	public static void main(String[] args) {
		new PoemGeneratorEA(2, 0.70, 1);
	}

	public PoemGeneratorEA(int populationSizeParam, double mutationProbabilityParam, int generationsParam){
		populationSize = populationSizeParam;
		mutationProbability = mutationProbabilityParam;
		numberOfGenerations = generationsParam;
		tournamentSize = (int) (populationSize * 0.5);

		findBestPoem();
	}

	private ArrayList<ArrayList<String>> findBestPoem(){
		System.out.println("Initialising population");
		initialisePopulation();

		System.out.println("\nGoing through " + numberOfGenerations + " generations...");
		evolve();

		System.out.println("\nFinding best cost from final population...");
		System.out.println("-------------------------------------------------------------");
		evaluateFinalPopulation();
		System.out.println("-------------------------------------------------------------");
		return null;
	}

	/**
	 * Initialise population for evolutionary algorithm
	 */
	private void initialisePopulation() {
		population = new ArrayList<String>();
		for(int i = 0; i < populationSize; i++) {
			population.add(poemGenerator.generatePoem(1));
			System.out.println(population.get(i));
		}
	}

	/**
	 * Go through number of generations and improve potential solutions
	 */
	private void evolve() {
		int generations = 0;
		while(generations < numberOfGenerations) {
			oneGeneration();
			generations++;
		}
	}

	/**
	 * One generation of the algorithm
	 */
	private void oneGeneration() {
		//Select parents
		String tournamentSelection = tournamentParentSelection();
//		String parent2 = tournamentParentSelection();
		//Recombine parents
//		String child = generateCrossover(parent1, parent2);
		//Mutate resulting offspring and add to possible solutions

		if(Math.random() < mutationProbability) {
			tournamentSelection =	mutatePoem(tournamentSelection);
		}

		ArrayList<String> newPopulation = population;
		newPopulation.add(tournamentSelection);
		//Replace weakest member of population
		population = replaceWeakestIndividual(newPopulation);

	}

	/**
	 * Select one parent from the poem with highest probability from
	 * a subgroup of the population
	 * @return
	 */
	private String tournamentParentSelection(){
		Random random = new Random();
		int sampleSize = tournamentSize;
		ArrayList<String> candidates = new ArrayList<String>();

		//Get candidate parents
		for(int i = 0; i < sampleSize; i++) {
			int randomIndex = random.nextInt(populationSize);
			candidates.add(population.get(randomIndex));
		}

		//Get best candidate from selection
		String bestCandidate = "";
		BigDecimal bestCost = new BigDecimal(0);
		for(String candidate : candidates) {
			BigDecimal thisCost = getCostOfPoem(candidate);
			if(thisCost.compareTo(bestCost) > 0) {
				bestCost = thisCost;
				bestCandidate = candidate;
			}
		}
		return bestCandidate;
	}

	/**
	 * Calculate cost of a candidate poem
	 * @return
	 */
	private BigDecimal getCostOfPoem(String poem) {
		String[] poemSentences = poem.split("\\?|\\.|\\!");
		BigDecimal probability = new BigDecimal(1);
		for(String sentence : poemSentences) {
			sentence = sentence.replace(",", " ,");
			sentence = sentence.replace("?", " ?");
			sentence = sentence.replace("!", " !");
			String[] words = sentence.split(" ");
			for(int i = 0; i < words.length; i++) {
				if(words[i].trim().length() != 0) {
					String sequence;
					if(i == 0) {
						sequence = "<s> " + words[i];
					}
					else if(i == words.length) {
						sequence = words[i] + "</s>";
					}
					else {
						sequence =  words[i-1] + " " + words[i];
						}
					BigDecimal sequenceProbability = getSequenceProbability(sequence);
					 probability =  probability.multiply(sequenceProbability);
					//P(w1⋯wn)=P(w1)P(w2|w1)P(w3|w1w2)P(w4|w2w3)⋯P(wn|wn−2wn−1).
				}
				
			}
		}
		return probability;
	}

	private BigDecimal getSequenceProbability(String sequence) {
		BigDecimal sequenceProb = new BigDecimal(0.000000000000000000000000000001);
	//	System.out.println(sequence);
		String word = sequence.split(" ")[1];
		List<Document> sequenceMatches = mongo.getSequenceMatches(collection, word, "word");
		for(Document match : sequenceMatches) {
			Document associations = (Document) match.get("associations");
			Document ngramData = (Document) associations.get("2-gram");
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
		return sequenceProb;
	}


//	private String generateCrossover(String parent1, String parent2){
//		Random random = new Random();
//		String child = "";
//		//TODO: FILL THIS IN
//		return child;
//	}

	private String mutatePoem(String poem){
		Random random = new Random();
		//TODO: FILL THIS IN
		return poem;
	}


	public BigDecimal evaluateFinalPopulation() {
		//Find best poem from end population
		BigDecimal bestCost = new BigDecimal(0);
		String bestPoem = "";
		for(int i = 0; i < population.size(); i++) {
			String thisPoem = population.get(i);
			BigDecimal thisCost = getCostOfPoem(thisPoem);
			if(thisCost.compareTo(bestCost) > 0) {
				bestCost = thisCost;
				bestPoem = thisPoem;
			}
		}
		System.out.println("Best cost: " + bestCost);
		System.out.println("Best poem: " + bestPoem); 
		return bestCost;
	}


	private ArrayList<String> replaceWeakestIndividual(ArrayList<String> candidates){
		BigDecimal highestCost = new BigDecimal(0);
		String weakestCandidate = "";
		for(String candidate : candidates) {
			BigDecimal thisCost = getCostOfPoem(candidate);
			if(thisCost.compareTo(highestCost) > 0) {
				highestCost = thisCost;
				weakestCandidate = candidate;
			}
		}
		int weakIndex = population.indexOf(weakestCandidate);
		candidates.remove(weakIndex);
		return candidates;
	}



}
