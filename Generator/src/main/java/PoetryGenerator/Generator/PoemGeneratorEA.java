package PoetryGenerator.Generator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;
/**
 * Evolutionary algorithm to find the highest scoring poem
 * Goal: maximise cost
 * @author Clare Buckley
 * @version 05/04/19
 */

public class PoemGeneratorEA {
	private PoemGenerator poemGenerator;
	private CostCalculator costCalculator;
	private BigDecimal costIncrease = new BigDecimal(0);
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
		new PoemGeneratorEA(5,1,1, "2-gram", "3-gram");
	}

	public PoemGeneratorEA(int populationSizeParam, double mutationProbabilityParam, int generationsParam, String generatorGram, String evaluatorGram){
		costCalculator = new CostCalculator(evaluatorGram);
		poemGenerator = new PoemGenerator(generatorGram);
		populationSize = populationSizeParam;
		mutationProbability = mutationProbabilityParam;
		numberOfGenerations = generationsParam;
		tournamentSize = (int) (populationSize * 0.5);

		findBestPoem();
	}

	private ArrayList<ArrayList<String>> findBestPoem(){
		System.out.println("Initialising population\n");
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
	//			String child = generateCrossover(parent1, parent2);   TODO: child = languageTool fixed version of parent
		
		//Mutate resulting offspring and add to possible solutions
		if(Math.random() < mutationProbability) {
			tournamentSelection =	mutatePoem(tournamentSelection);  //TODO: this will be child mutated
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
			BigDecimal thisCost = costCalculator.getCost(candidate);
			if(thisCost.compareTo(bestCost) > 0) {
				bestCost = thisCost;
				bestCandidate = candidate;
			}
		}
		return bestCandidate;
	}
	

	private String mutatePoem(String poem){
		//add rhyme --> costIncreased after mutation
		return poem;
	}


	public BigDecimal evaluateFinalPopulation() {
		//Find best poem from end population
		BigDecimal bestCost = new BigDecimal(0);
		String bestPoem = "";
		for(int i = 0; i < population.size(); i++) {
			String thisPoem = population.get(i);
			BigDecimal thisCost = costCalculator.getCost(thisPoem);
			if(thisCost.compareTo(bestCost) > 0) {
				bestCost = thisCost;
				bestPoem = thisPoem;
			}
		}
		System.out.println("Best cost: " + bestCost);
		System.out.println("Best poem: \n" + bestPoem); 
		return bestCost;
	}


	private ArrayList<String> replaceWeakestIndividual(ArrayList<String> candidates){
		BigDecimal lowestProb = new BigDecimal(100);
		String weakestCandidate = "";
		for(String candidate : candidates) {
			BigDecimal thisCost = costCalculator.getCost(candidate);
			if(thisCost.compareTo(lowestProb) < 0) {
				lowestProb = thisCost;
				weakestCandidate = candidate;
			}
		}

		int weakIndex = population.indexOf(weakestCandidate);
		candidates.remove(weakIndex);
		return candidates;
	}



}
