package PoetryGenerator.Generator;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.rules.RuleMatch;
/**
 * Evolutionary algorithm to find the highest scoring poem
 * Goal: maximise cost
 * @author Clare Buckley
 * @version 07/04/19
 */

public class PoemGeneratorEA {
	private PoemGenerator poemGenerator;
	private CostCalculator costCalculator;
	//ArrayList of candidates to be used
	private HashMap<String,BigDecimal> population = new HashMap<String,BigDecimal>();    
	//Number of candidate solutions
	private final int populationSize;
	//Probability of mutation being performed on candidates
	private final double mutationProbability;
	//Number of iterations to be completed by the algorithm
	private final int numberOfGenerations;
	//Sample size for tournament parent selection
	private final int tournamentSize;

	public static void main(String[] args) throws IOException {
		new PoemGeneratorEA(2,1,1, "4-gram", "3-gram");
	}

	public PoemGeneratorEA(int populationSizeParam, double mutationProbabilityParam, int generationsParam, String generatorGram, String evaluatorGram) throws IOException{
		costCalculator = new CostCalculator(evaluatorGram);
		poemGenerator = new PoemGenerator(generatorGram);
		populationSize = populationSizeParam;
		mutationProbability = mutationProbabilityParam;
		numberOfGenerations = generationsParam;
		tournamentSize = (int) (populationSize * 0.5);

		findBestPoem();
	}

	private ArrayList<ArrayList<String>> findBestPoem() throws IOException{
		System.out.println("Initialising population\n");
		initialisePopulation();
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
		String child = fixGrammar(parent); 
		System.out.println("Child: " + child);

		//Mutate resulting offspring and add to possible solutions
		if(Math.random() < mutationProbability) {
			//		child =	addRhyme(child);  //TODO: this will be child mutated
			//		System.out.println("Fixed grammar: " + child);
		}

		HashMap<String, BigDecimal> newPopulation = population;
		newPopulation.put(child, new BigDecimal(0));
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
		//add rhyme --> costIncreased after mutation
		String[] poemLines = poem.split("\\r?\\n");
		ArrayList<String> rhymeCandidates = new ArrayList<String>();
		//Get alternate ending words
		for(int i = 0; i < poemLines.length; i++) {
			System.out.println(poemLines[i]);
			String[] lineWords = poemLines[i].split(" ");
			String lastWord = lineWords[lineWords.length-1];
			rhymeCandidates.add(lastWord);
		}
		return poem;
	}

	private String fixGrammar(String poem) throws IOException {
		String originalPoem = poem;
		boolean poemChanged = false;
		JLanguageTool langTool = new JLanguageTool(new BritishEnglish());
		List<RuleMatch> matches = langTool.check(poem);
		System.out.println(matches.size() + " matches");
		if(matches.size() > 0) {
			for (RuleMatch match : matches) {
				String ruleId = match.getRule().getId();

				int from = match.getFromPos();
				int to = match.getToPos();
				System.out.println(ruleId);
				List<String> suggestions =  match.getSuggestedReplacements();
				if(suggestions.size() > 0) {
					System.out.println("has suggestions");
					poem = replaceWithSuggestion(poem, from, to, suggestions);
					poemChanged = true;
				} 
				else if(ruleId.equals("USELESS_THAT") || ruleId.equals("TIRED_INTENSIFIERS")) {
					String toReplace = poem.substring(match.getFromPos(), match.getToPos());
					poem = poem.replace(toReplace, "");
					poemChanged = true;
				}
				else {
					System.out.println("other rule --> " + ruleId);
					System.out.println(match.getRule().getDescription());
					if(suggestions.size()>0) {
						System.out.println(suggestions.get(0));
					} else {
						System.out.println("no suggestions");
					}
				}
			}
		}

		if(poemChanged) {
			//More probable after fixing grammar
			BigDecimal newProb = population.get(originalPoem).add(new BigDecimal(0.0001));
			population.replace(poem, newProb);
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
		Random random = new Random();
		int randomIndex = random.nextInt(suggestions.size());
		String toReplace = line.substring(from, to);
		System.out.println("from " + from + " - to " + to);
		String replacement = suggestions.get(randomIndex);

		//line.replace doesn't work: can't be sure that toReplace pattern won't be repeated throughout line
		String contentBefore = line.substring(0, from-1);
		String contentAfter = line.substring(to, line.length());
		System.out.println("from " + from + " to " + to + " --> " + toReplace);

		line = contentBefore + replacement + contentAfter;


		System.out.println("Replaced '" + toReplace + "' with '" + replacement + "' --> " + line);

		return line;
	}


	public BigDecimal evaluateFinalPopulation() {
		//Find best poem from end population
		BigDecimal bestCost = new BigDecimal(0);
		String bestPoem = "";
		for(Map.Entry<String, BigDecimal> poem:population.entrySet())   {
			BigDecimal thisCost = poem.getValue();
			System.out.println("this cost:" + thisCost);
			if(thisCost.compareTo(bestCost) > 0) {
				bestCost = thisCost;
				bestPoem = poem.getKey();
				System.out.println("Best cost updated");
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
