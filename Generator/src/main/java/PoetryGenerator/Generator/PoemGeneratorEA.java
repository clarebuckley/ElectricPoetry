package PoetryGenerator.Generator;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.rules.RuleMatch;
import net.ricecode.similarity.LevenshteinDistanceStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;
/**
 * Evolutionary algorithm to find the highest scoring poem in terms of grammaticality, poeticness and meaningfulness
 * Goal: maximise cost of candidate solutions
 * @author Clare Buckley
 * @version 28/08/19
 */

public class PoemGeneratorEA {
	private PoemGenerator poemGenerator;
	private CostCalculator costCalculator;
	private RhymeGenerator rhymeGenerator;
	private FileWriter textWriter;
	private FileWriter csvWriter;
	//ArrayList of candidates to be used
	private HashMap<String,BigDecimal> population = new HashMap<String,BigDecimal>();    
	//Number of candidate solutions
	private  int populationSize;
	//Probability of mutation being performed on candidates
	private  double mutationProbability;
	//Number of iterations to be completed by the algorithm
	private  int numberOfGenerations;
	private int currentGeneration = 0;
	//Sample size for tournament parent selection
	private  int tournamentSize;
	//Number of verses in each poem
	private int numVerses;
	//Max limits for finding a result
	private static final double MAX_SIMILARITY = 0.4;
	private static final int GRAMMAR_SEARCH_LIMIT = 10;
	private static final int RHYME_SEARCH_LIMIT = 10;
	private static final BigDecimal MIN_PROBABILITY = new BigDecimal(0.01);

	/**
	 * Main method to run the system.
	 * Used to set parameters for the evolutionary algorithm.
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new PoemGeneratorEA(3,0.8,2,1, "2-gram", "4-gram");
	}


	public PoemGeneratorEA(int populationSizeParam, double mutationProbabilityParam, int generationsParam, int numVersesParam, String generatorGram, String evaluatorGram) throws IOException{
		costCalculator = new CostCalculator(evaluatorGram);
		poemGenerator = new PoemGenerator(generatorGram);
		rhymeGenerator = new RhymeGenerator();
		populationSize = populationSizeParam;
		mutationProbability = mutationProbabilityParam;
		numberOfGenerations = generationsParam;
		tournamentSize = (int) (populationSize * 0.5);
		numVerses = numVersesParam;
		textWriter = new FileWriter("./src/main/java/PoetryGenerator/Data/results/TESTpoemResults_generate=" + generatorGram + "_evaluate=" + evaluatorGram + "_pop=" + populationSize + "_generations=" + numberOfGenerations +  ".txt");
		csvWriter = new FileWriter("./src/main/java/PoetryGenerator/Data/results/TESTpoemResults_generate=" + generatorGram + "_evaluate=" + evaluatorGram + "_pop=" + populationSize + "_generations=" + numberOfGenerations +  ".csv");

		findBestPoem();
	}

	public PoemGeneratorEA() {}

	/**
	 * Main method for poetry generation
	 * @throws IOException
	 */
	private void findBestPoem() throws IOException{
		System.out.println("Initialising population\n");
		population.clear();
		initialisePopulation();

		System.out.println("\nGoing through " + numberOfGenerations + " generations...");
		evolve();

		System.out.println("\nFinding best cost from final population...");
		System.out.println("-------------------------------------------------------------");
		evaluateFinalPopulation();
		System.out.println("-------------------------------------------------------------");
	}

	/**
	 * Initialise population for evolutionary algorithm
	 */
	private void initialisePopulation() {
		while(population.size() != populationSize) {
			String poem = poemGenerator.generatePoem(numVerses);
			population.put(poem, costCalculator.getCost(poem));
			System.out.println(poem);
		}
	}

	/**
	 * Main method
	 * Go through number of generations and improve potential solutions
	 * @throws IOException 
	 */
	private void evolve() throws IOException {
		csvWriter.append("Generation");
		csvWriter.append(',');
		csvWriter.append("Best Cost Found");
		csvWriter.append(',');
		csvWriter.append('\n');

		while(currentGeneration < numberOfGenerations) {
			oneGeneration();
			currentGeneration++;
		}

		textWriter.flush();
		textWriter.close();
		csvWriter.flush();
		csvWriter.close();
	}

	/**
	 * One generation of the algorithm
	 * Add child to population, and mutate both child and another poem in the population 
	 * if the mutation probability is met.
	 * Child: crossover of two parents
	 * Mutation: selected poems have grammar fixed using LanguageTool and rhyme is added using RhymeGenerators
	 * @throws IOException 
	 */
	private void oneGeneration() throws IOException {
		//Select parents
		String parent1 = tournamentParentSelection();
		String parent2 = tournamentParentSelection();

		//A score of 0.0 means that the two strings are absolutely dissimilar, and 1.0 means that absolutely similar (or equal).
		SimilarityStrategy strategy = new LevenshteinDistanceStrategy();
		StringSimilarityService service = new StringSimilarityServiceImpl(strategy);
		double similarityScore = service.score(parent1, parent2);

		while(parent2.contentEquals(parent1) || similarityScore >= MAX_SIMILARITY) {
			parent2 = tournamentParentSelection();
		}
		String child = generateCrossover(parent1, parent2);

		//Mutate resulting offspring and add to possible solutions
		if(Math.random() < mutationProbability) {
			child =	fixGrammar(child); 
			child =	addRhyme(child); 
		}

		HashMap<String, BigDecimal> newPopulation = population;
		BigDecimal childProb = costCalculator.getCost(child);
		System.out.println("Child: \n" + child);
		System.out.println("probability: " + childProb);
		newPopulation.put(child, childProb);
		
		//Replace weakest members of population
		population = removeWeakestCandidaes(newPopulation);
		//Re-populate missing poems
		while(population.size() != populationSize) {
			String poem = poemGenerator.generatePoem(numVerses);
			population.put(poem, costCalculator.getCost(poem));
			System.out.println(poem);
		}

	}

	/**
	 * Create child from two parent candidates using crossover
	 * @param parent1 - parent selected from population using tournament selection
	 * @param parent2 - parent selected from population using tournament selection
	 * @return child poem consisting of both parent1 and parent2
	 */
	public String generateCrossover(String parent1, String parent2) {
		String child = "";
		String[] parent1Lines = parent1.split("\\r?\\n");
		String[] parent2Lines = parent2.split("\\r?\\n");
		int totalLines = parent1Lines.length + parent2Lines.length;
		int childLength = (int)totalLines/2;

		String[] part1, part2;
		int part1Length,part2Length;
		if(parent1Lines.length > parent2Lines.length) {
			part1 = parent1Lines;
			part2 = parent2Lines;
		} else {
			part1 = parent2Lines;
			part2 = parent1Lines;
		}

		part1Length = (int) childLength/2;
		part2Length = childLength - part1Length;
		for(int i = 0; i < part1Length; i++) {
			child = child + part1[i] + "\n";
		}
		int j = 0;
		int existingChildLines = child.split("\\r?\\n").length;
		while(existingChildLines < childLength && j < part2Length) {
			child = child + part2[j] + "\n";
			j++;
		}
		return child;
	}

	/**
	 * Select one parent from the poem with highest probability from
	 * a subgroup of the population
	 * @return strongest parent from tournament
	 */
	private String tournamentParentSelection(){
		Random random = new Random();
		ArrayList<String> candidates = new ArrayList<String>();

		//Get candidate parents
		for(int i = 0; i < tournamentSize; i++) {
			String randomPoem = (String) population.keySet().toArray()[random.nextInt(population.keySet().toArray().length)];
			candidates.add(randomPoem);
		}

		//Get best proportion of candidates from selection
		ArrayList<String> bestCandidates = new ArrayList<String>();
		BigDecimal bestCost = new BigDecimal(0);
		for(String candidate : candidates) {
			BigDecimal thisCost = population.get(candidate);
			if(thisCost.compareTo(bestCost) > 0 && bestCandidates.size() < tournamentSize*0.5) {
				bestCost = thisCost;
				bestCandidates.add(candidate);
			}
		}

		return bestCandidates.get(random.nextInt(bestCandidates.size()));
	}


	/**
	 * Add rhyme to poem using RhymeGenerator
	 * Poems with rhyming couples will be rewarded with a greater cost
	 * If no rhyme is found, original word will be returned
	 * @param poem: poem to add rhyme to
	 * @return poem with added rhyme
	 */
	private String addRhyme(String poem){
		System.out.println("Adding rhyme");
		String[] poemLines = poem.split("\\r?\\n");
		//Get  ending words
		ArrayList<String> wordsToRhymeWith = new ArrayList<String>();
		for(int i = 0; i < poemLines.length; i++) {
			String[] lineWords = poemLines[i].split(" ");
			wordsToRhymeWith.add(lineWords[lineWords.length-1]);
		}

		String updatedPoem = "";

		//Add rhyme to even lines
		for(int i = 0; i < poemLines.length; i++) {
			String wordToRhymeWith;
			if(i % 2 == 0) {
				int randomEvenNumber = new Random().nextInt((wordsToRhymeWith.size()/2)*2);
				wordToRhymeWith = wordsToRhymeWith.get(randomEvenNumber);  
			} else {
				int randomOddNumber = new Random().nextInt((wordsToRhymeWith.size()/2)*2-1);
				wordToRhymeWith = wordsToRhymeWith.get(randomOddNumber);
			}
			String[] lineWords = poemLines[i].split(" ");
			String wordToReplace = lineWords[lineWords.length-1];
			String prevWord1 = "<s>";
			if(lineWords.length > 1) {
				prevWord1 = lineWords[lineWords.length-2];
			}

			final String originalWord = wordToReplace;
			String rhymingWord = "";
			int j =0;
			while(rhymingWord.equals("") && j < RHYME_SEARCH_LIMIT) {
				rhymingWord = rhymeGenerator.getRhymingWord(prevWord1, wordToReplace, wordToRhymeWith);
				j++;
			}
			if(rhymingWord.equals("")) {
				//get random word of that POS tag to fill the gap
				rhymingWord = originalWord;
			}
			poemLines[i] = poemLines[i].substring(0, poemLines[i].length()-1-wordToReplace.length()) + " " + rhymingWord;

			updatedPoem += poemLines[i] + "\n";
		}

		return updatedPoem;
	}

	/**
	 * Fix grammar of poem using LanguageTool
	 * @param poem: poem to be corrected
	 * @return poem with corrected grammar
	 * @throws IOException
	 */
	public String fixGrammar(String poem) throws IOException {
		System.out.println("Fixing grammar");
		JLanguageTool langTool = new JLanguageTool(new BritishEnglish());
		List<RuleMatch> matches = langTool.check(poem);
		int i = 0;
		while(matches.size() > 0 && i < GRAMMAR_SEARCH_LIMIT) {
			RuleMatch match = matches.get(0);
			String ruleId = match.getRule().getId();

			int from = match.getFromPos();
			int to = match.getToPos();
			List<String> suggestions =  match.getSuggestedReplacements();
			if(suggestions.size() > 0) {
				poem = replaceWithSuggestion(poem, from, to, suggestions);
			} 
			else if(ruleId.equals("USELESS_THAT") || ruleId.equals("TIRED_INTENSIFIERS")) {
				String toReplace = poem.substring(match.getFromPos(), match.getToPos());
				poem = poem.replace(toReplace, "");
			}

			//To and from indexes will be incorrect for previous matches: 
			matches.clear();
			matches = langTool.check(poem);
			i++;
		}
		return poem;
	}

	/**
	 * Replace incorrect grammar with suggestions
	 * @param line - whole line containing error
	 * @param from - start index of error
	 * @param to - end index of error
	 * @param suggestions - possible ways to correct issue
	 * @return word to replace original with
	 */
	private String replaceWithSuggestion(String line, int from, int to, List<String> suggestions) {
		String replacement = suggestions.get(0);
		String contentBefore = line.substring(0, from);
		String contentAfter = line.substring(to, line.length());
		line = contentBefore + replacement + contentAfter;
		return line;
	}


	/**
	 * Evaluate final population after all generations have been completed
	 * Population with the greatest cost will be returned as the 'best poem'
	 * @return cost of the best poem
	 */
	public BigDecimal evaluateFinalPopulation() {
		System.out.println("end population size: " + population.size());
		//Find best poem from end population
		BigDecimal bestCost = new BigDecimal(0);
		String bestPoem = "";
		for(Map.Entry<String, BigDecimal> poem : population.entrySet())   {
			BigDecimal thisCost = poem.getValue();
			System.out.println("this cost:" + thisCost + " for poem:");
			System.out.println(poem.getKey());
			if(thisCost.compareTo(bestCost) > 0) {
				bestCost = thisCost;
				bestPoem = poem.getKey();
			}
		}
		System.out.println("Best cost: " + bestCost);
		System.out.println("Best poem: \n" + bestPoem); 
		    
		return bestCost;
	}


	/**
	 * Removes weakest poem from the population
	 * @param candidates: whole population
	 * @return population with weakest individual removed
	 */
	private HashMap<String, BigDecimal>  removeWeakestCandidaes(HashMap<String, BigDecimal> candidates){
		Set<String> weakestCandidates = new HashSet<String>();
		BigDecimal bestCost = new BigDecimal(0);
		String bestPoem = "";
		for(Map.Entry<String, BigDecimal> candidate:candidates.entrySet())   {
			BigDecimal thisCost = candidate.getValue();
			if(thisCost.compareTo(MIN_PROBABILITY) < 0) {
				weakestCandidates.add(candidate.getKey());
			}
			if(thisCost.compareTo(bestCost) > 0) {
				bestCost = thisCost;
				bestPoem = candidate.getKey();
			}
		}
		candidates.keySet().removeAll(weakestCandidates);

		try {
			System.out.println("writing to file for iteration: " + currentGeneration + " --> " + bestCost);
			csvWriter.append(Integer.toString(currentGeneration));
			csvWriter.append(',');
			csvWriter.append(bestCost.toString());
			csvWriter.append(',');
			csvWriter.append('\n');

			textWriter.append("Generation: ");
			textWriter.append(Integer.toString(currentGeneration));
			textWriter.append("\n");
			textWriter.append(bestPoem);
			textWriter.append("\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			    

		return candidates;
	}

}
