import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Evolutionary algorithm to find the highest scoring poem
 * @author Clare Buckley
 * @version 11/03/19
 */

public class PoemGeneratorEA {
	private PoemGenerator poemGenerator = new PoemGenerator();
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
		new PoemGeneratorEA(10, 0.70, 50);
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
		System.out.println("----------------------------");
		double bestCost = evaluateFinalPopulation();
		System.out.println("Best cost: " + bestCost);
		System.out.println("----------------------------");
		return null;
	}
	
	/**
	 * Initialise population for evolutionary algorithm
	 */
	private void initialisePopulation() {
		population = new ArrayList<String>();
		for(int i = 0; i < populationSize; i++) {
			population.add(poemGenerator.generatePoem(1));
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
		String parent1 = tournamentParentSelection();
		String parent2 = tournamentParentSelection();
		//Recombine parents
		String child = generateCrossover(parent1, parent2);
		//Mutate resulting offspring and add to possible solutions

		if(Math.random() < mutationProbability) {
			child =	mutatePoem(child);
		}

		ArrayList<String> newPopulation = population;
		newPopulation.add(child);
		//Replace weakest member of population
		population = replaceWeakestIndividual(newPopulation);

	}
	
	/**
	 * Select one parent from the poem with lowest cost from
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
		double bestCost = 10000;
		for(String candidate : candidates) {
			double thisCost = getCostOfPoem(candidate);
			if(thisCost < bestCost) {
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
	private double getCostOfPoem(String poem) {
		double totalCost = 0;
		//TODO: FILL THIS IN
		return totalCost;
	}
	
	private String generateCrossover(String parent1, String parent2){
		Random random = new Random();
		String child = "";
		//TODO: FILL THIS IN
		return child;
	}
	
	private String mutatePoem(String poem){
		Random random = new Random();
		//TODO: FILL THIS IN
		return poem;
	}

	
	public double evaluateFinalPopulation() {
		//Find best poem from end population
		double bestCost = 10000;
		String bestPoem = "";
		for(int i = 0; i < population.size(); i++) {
			String thisPoem = population.get(i);
			double thisCost = getCostOfPoem(thisPoem);
			if(thisCost < bestCost) {
				bestCost = thisCost;
				bestPoem = thisPoem;
			}
		}
		System.out.println("Best poem: " + bestPoem); 
		return bestCost;
	}


	private ArrayList<String> replaceWeakestIndividual(ArrayList<String> candidates){
		double highestCost = 0;
		String weakestCandidate = "";
		for(String candidate : candidates) {
			double thisCost = getCostOfPoem(candidate);
			if(getCostOfPoem(candidate) > highestCost) {
				highestCost = thisCost;
				weakestCandidate = candidate;
			}
		}
		int weakIndex = population.indexOf(weakestCandidate);
		candidates.remove(weakIndex);
		return candidates;
	}



}
