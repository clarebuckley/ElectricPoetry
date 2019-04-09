package PoetryGenerator.Generator;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.rules.RuleMatch;
/**
 * Evolutionary algorithm to find the highest scoring poem
 * Goal: maximise cost
 * @author Clare Buckley
 * @version 08/04/19
 */

public class PoemGeneratorEA {
	private PoemGenerator poemGenerator;
	private CostCalculator costCalculator;
	private RhymeGenerator rhymeGenerator;
	//ArrayList of candidates to be used
	private HashMap<String,BigDecimal> population = new HashMap<String,BigDecimal>();    
	//Number of candidate solutions
	private  int populationSize;
	//Probability of mutation being performed on candidates
	private  double mutationProbability;
	//Number of iterations to be completed by the algorithm
	private  int numberOfGenerations;
	//Sample size for tournament parent selection
	private  int tournamentSize;

	public static void main(String[] args) throws IOException {
		new PoemGeneratorEA(2,1,1, "3-gram", "4-gram");
	}

	public PoemGeneratorEA(int populationSizeParam, double mutationProbabilityParam, int generationsParam, String generatorGram, String evaluatorGram) throws IOException{
		costCalculator = new CostCalculator(evaluatorGram, generatorGram);
		poemGenerator = new PoemGenerator(generatorGram);
		rhymeGenerator = new RhymeGenerator(generatorGram);
		populationSize = populationSizeParam;
		mutationProbability = mutationProbabilityParam;
		numberOfGenerations = generationsParam;
		tournamentSize = (int) (populationSize * 0.5);

		findBestPoem();
	}

	public PoemGeneratorEA() {

	}

	private ArrayList<ArrayList<String>> findBestPoem() throws IOException{
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
		return null;
	}

	/**
	 * Initialise population for evolutionary algorithm
	 */
	private void initialisePopulation() {
		for(int i = 0; i < populationSize; i++) {
			String poem = poemGenerator.generatePoem(1);
			population.put(poem, new BigDecimal(0));
			System.out.println(poem);
		}
	}

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
		int generations = 0;
		while(generations < numberOfGenerations) {
			oneGeneration();
			generations++;
		}
	}

	/**
	 * One generation of the algorithm
	 * @throws IOException 
	 */
	private void oneGeneration() throws IOException {
		//Select parents
		String parent = tournamentParentSelection();
		System.out.println("Adding child to population\n");
		String child = addRhyme(parent); 


		//Mutate resulting offspring and add to possible solutions
		if(Math.random() < mutationProbability) {
			child =	fixGrammar(child);  
		}

		HashMap<String, BigDecimal> newPopulation = population;
		BigDecimal childProb = costCalculator.getCost(child);
		System.out.println("Child: \n" + child);
		System.out.println("probability: " + childProb);
		newPopulation.put(child, childProb);
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
			String prevWord1 = lineWords[lineWords.length-2];

			final String originalWord = wordToReplace;
			String rhymingWord = "";
			//	System.out.println(rhymingWord + " - " + originalWord);
			int j =0;
			while(rhymingWord.equals("") && j <10) {
				//	String wordToRhymeWith = wordsToRhymeWith.get(j);
				//System.out.println("Word to rhyme with: " + wordToRhymeWith + " for line " + poemLines[i]);
				rhymingWord = rhymeGenerator.getRhymingWord(/*prevWord3, prevWord2, */prevWord1, wordToReplace, wordToRhymeWith);
				//System.out.println("RESULT ------------------------------------------>" + rhymingWord);
				j++;
			}
			if(rhymingWord.equals("")) {
				//get random word of that POS tag to fill the gap
				rhymingWord = originalWord;
			}

			//	System.out.println("line before: " + poemLines[i]);
			poemLines[i] = poemLines[i].substring(0, poemLines[i].length()-1-wordToReplace.length()) + " " + rhymingWord;
			//	System.out.println("line after: " + poemLines[i]);

			//	System.out.println("replaced " + wordToReplace + " with " + rhymingWord);

			updatedPoem += poemLines[i] + "\n";
		}

		return updatedPoem;
	}

	public String fixGrammar(String poem) throws IOException {
		JLanguageTool langTool = new JLanguageTool(new BritishEnglish());
		List<RuleMatch> matches = langTool.check(poem);
		while(matches.size() > 0) {
			RuleMatch match = matches.get(0);
			String ruleId = match.getRule().getId();

			int from = match.getFromPos();
			int to = match.getToPos();
			System.out.println(ruleId);
			List<String> suggestions =  match.getSuggestedReplacements();
			if(suggestions.size() > 0) {
				System.out.println("has suggestions");
				poem = replaceWithSuggestion(poem, from, to, suggestions);
			} 
			else if(ruleId.equals("USELESS_THAT") || ruleId.equals("TIRED_INTENSIFIERS")) {
				String toReplace = poem.substring(match.getFromPos(), match.getToPos());
				poem = poem.replace(toReplace, "");
			}

			//To and from indexes will be incorrect for previous matches: 
			matches.clear();
			matches = langTool.check(poem);
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


	private HashMap<String, BigDecimal>  replaceWeakestIndividual(HashMap<String, BigDecimal> candidates){
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
