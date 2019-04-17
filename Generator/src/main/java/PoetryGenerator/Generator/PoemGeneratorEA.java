package PoetryGenerator.Generator;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.io.FileWriter;

import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.rules.RuleMatch;
/**
 * Evolutionary algorithm to find the highest scoring poem
 * Goal: maximise cost
 * @author Clare Buckley
 * @version 10/04/19
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
	
	private int iteration;

	public static void main(String[] args) throws IOException {
		new PoemGeneratorEA(10,0.7,5,1, "4-gram", "4-gram");
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

		textWriter = new FileWriter("./src/main/java/PoetryGenerator/Data/results/poemResults_generate=" + generatorGram + "_evaluate=" + evaluatorGram + "_pop=" + populationSize + "_generations=" + numberOfGenerations +  ".txt");
		csvWriter = new FileWriter("./src/main/java/PoetryGenerator/Data/results/poemResults_generate=" + generatorGram + "_evaluate=" + evaluatorGram + "_pop=" + populationSize + "_generations=" + numberOfGenerations +  ".csv");
		csvWriter.append("Iteration");
		csvWriter.append(',');
		csvWriter.append("Best Cost Found");
		csvWriter.append(',');
		csvWriter.append('\n');
		for( iteration = 0; iteration < 10; iteration++) {
			findBestPoem();
		}

		textWriter.flush();
		textWriter.close();
		csvWriter.flush();
		csvWriter.close();

	}

	//Used for testing
	public PoemGeneratorEA() {}

	/**
	 * Main method for poetry generation
	 * @throws IOException
	 */
	private void findBestPoem() throws IOException{


		System.out.println("Initialising population\n");
		initialisePopulation();
		System.out.println("Initialising population probabilities\n");
		initialisePopulationProbabilities();

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
		population.clear();
		for(int i = 0; i < populationSize; i++) {
			String poem = poemGenerator.generatePoem(numVerses);
			population.put(poem, new BigDecimal(0));
			System.out.println(poem);
		}
	}

	/**
	 * Get probabilities for each poem in the population
	 */
	private void initialisePopulationProbabilities() {
		for(Map.Entry<String, BigDecimal> poem : population.entrySet()) {
			BigDecimal poemCost = costCalculator.getCost(poem.getKey());
			System.out.println(poemCost);
			population.replace(poem.getKey(), poemCost);
		}
	}

	/**
	 * Go through number of generations and improve potential solutions
	 * @throws IOException 
	 */
	private void evolve() throws IOException {
		while(currentGeneration < numberOfGenerations) {
			oneGeneration();
			currentGeneration++;
		}
	}

	/**
	 * One generation of the algorithm
	 * Add child to population, and mutate both child and another poem in the population 
	 * if the mutation probability is met.
	 * Child: parent selected using tournament selection, rhyme is added to this parent
	 * Mutation: selected poems have grammar fixed using LanguageTool
	 * @throws IOException 
	 */
	private void oneGeneration() throws IOException {
		//Select parents
		String parent1 = tournamentParentSelection();

		System.out.println("Creating child");
		String child = addRhyme(parent1); 
		String mutatedParent = "";
		String toMutate = "";

		if(child.equals(parent1)) {
			System.out.println("No rhyme added");
			child = poemGenerator.generatePoem(numVerses);
		}

		//Mutate resulting offspring and add to possible solutions
		if(Math.random() < mutationProbability) {
			child =	fixGrammar(child); 
			toMutate = tournamentParentSelection();
			mutatedParent = fixGrammar(toMutate);

		}

		HashMap<String, BigDecimal> newPopulation = population;
		BigDecimal childProb = costCalculator.getCost(child);
		if(mutatedParent.length() > 0) {
			BigDecimal mutatedParentProb = costCalculator.getCost(mutatedParent);
			newPopulation.remove(toMutate);
			newPopulation.put(mutatedParent, mutatedParentProb);
			System.out.println("Mutated: \n" + mutatedParent);
			System.out.println("probability: " + mutatedParentProb);
		}

		System.out.println("Child: \n" + child);
		System.out.println("probability: " + childProb);
		newPopulation.put(child, childProb);
		//Replace weakest member of population
		population = removeWeakestIndividual(newPopulation);

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
			String randomPoem = (String) population.keySet().toArray()[random.nextInt(population.keySet().toArray().length)];
			candidates.add(randomPoem);
		}

		//Get best candidate from selection
		String bestCandidate = "";
		BigDecimal bestCost = new BigDecimal(0);
		for(String candidate : candidates) {
			BigDecimal thisCost = population.get(candidate);
			if(thisCost.compareTo(bestCost) > 0) {
				bestCost = thisCost;
				bestCandidate = candidate;
			}
		}
		return bestCandidate;
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
			while(rhymingWord.equals("") && j <5) {
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
		while(matches.size() > 0 && i < 10) {
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

		try {
			System.out.println("writing to file for iteration: " + iteration + " --> " + bestCost);
			csvWriter.append(Integer.toString(iteration));
			csvWriter.append(',');
			csvWriter.append(bestCost.toString());
			csvWriter.append(',');
			csvWriter.append('\n');

			textWriter.append("Generation: ");
			textWriter.append(Integer.toString(iteration));
			textWriter.append("\n");
			textWriter.append(bestPoem);
			textWriter.append("\n");


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			    
		return bestCost;


	}


	/**
	 * Removes weakest poem from the population
	 * @param candidates: whole population
	 * @return population with weakest individual removed
	 */
	private HashMap<String, BigDecimal>  removeWeakestIndividual(HashMap<String, BigDecimal> candidates){
		BigDecimal lowestProb = new BigDecimal(100);
		String weakestCandidate = "";
		for(Map.Entry<String, BigDecimal> candidate:candidates.entrySet())   {
			BigDecimal thisCost = candidate.getValue();
			if(thisCost.compareTo(lowestProb) < 0) {
				lowestProb = thisCost;
				weakestCandidate = candidate.getKey();
			}
		}
		candidates.remove(weakestCandidate);
		return candidates;
	}

}
