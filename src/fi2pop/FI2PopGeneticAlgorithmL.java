package fi2pop;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import shared.Chromosome;
import shared.ChromosomeL;
import shared.ScenesLibrary;
import shared.SlicesLibrary;

public class FI2PopGeneticAlgorithmL {
	/*
	 * Example of _playthroughMechanics -> each entry is a gene of chromosome
	 * 		[ "0,0,1,0,0,0,0,0,0,0,0,0",
	 * 		  "0,0,0,0,0,0,0,0,0,0,0,0",
	 * 		  "0,0,0,0,0,1,0,0,0,0,1,1",
	 * 		  "0,1,1,0,0,0,0,0,0,1,0,0"]
	 */
	private ChromosomeL[] _population;
	private int _populationSize;
	private int _chromosomeLength;
	private int _appendingSize;
	private double _crossover;
	private double _mutation;
	private int _elitism;
	private Random _rnd;
	private ScenesLibrary _lib;
	private String[] _playthroughMechanics;
	private boolean _variableNumOfMechInScene;
	private HashMap<String, String> _parameters;

	public FI2PopGeneticAlgorithmL(ScenesLibrary lib, Random rnd, String[] playThroughMechanics, HashMap<String, String> parameters) {
		this._parameters = parameters;
		this._lib = lib;
		this._populationSize = Integer.parseInt(this._parameters.get("populationSize"));
		this._chromosomeLength = Integer.parseInt(this._parameters.get("chromosomeLength"));
		this._appendingSize = Integer.parseInt(this._parameters.get("appendingSize"));
		this._crossover = Double.parseDouble(this._parameters.get("crossover"));
		this._mutation = Double.parseDouble(this._parameters.get("mutation"));
		this._elitism = Integer.parseInt(this._parameters.get("elitism"));
		this._rnd = rnd;
		this._population = new ChromosomeL[0];
		this._playthroughMechanics = playThroughMechanics;
		this._variableNumOfMechInScene = Boolean.parseBoolean(this._parameters.get("variableNumberOfMechanics"));
	}

	public ChromosomeL[] getPopulation() {
		return this._population;
	}

	public void randomChromosomesInitialize() {
		this._population = new ChromosomeL[this._populationSize];
		for(int i=0; i<this._population.length; i++) {
			this._population[i] = new ChromosomeL(this._rnd, this._lib, this._chromosomeLength, this._appendingSize, this._playthroughMechanics, this._variableNumOfMechInScene, this._parameters);
			this._population[i].randomInitialization();
		}
	}

	public void smartChomosomesInitialize() {
		this._population = new ChromosomeL[this._populationSize];
		for(int i=0; i<this._population.length; i++) {
			this._population[i] = new ChromosomeL(this._rnd, this._lib, this._chromosomeLength, this._appendingSize, this._playthroughMechanics, this._variableNumOfMechInScene, this._parameters);
			this._population[i].smartInitialization();
		}
	}

	private ChromosomeL rankSelection(ChromosomeL[] pop) {
		double[] ranks = new double[pop.length];
		ranks[0] = 1;
		for(int i=1; i<pop.length; i++) {
			ranks[i] = ranks[i-1] + i + 1;
		}
		for(int i=0; i<pop.length; i++) {
			ranks[i] /= ranks[ranks.length - 1];
		}
		double randValue = this._rnd.nextDouble();
		for(int i=0; i<ranks.length; i++){
			if(randValue <= ranks[i]) {
				return pop[i];
			}
		}
		return pop[pop.length - 1];
	}

	private ChromosomeL[][] getFeasibleInfeasible(boolean descending){
		ArrayList<ChromosomeL> feasible = new ArrayList<ChromosomeL>();
		ArrayList<ChromosomeL> infeasible = new ArrayList<ChromosomeL>();
		for(int i=0; i<this._population.length; i++) {
			if(this._population[i].getConstraints() < this._population[i].getConstraintProbability()) {
				infeasible.add(this._population[i]);
			}
			else {
				feasible.add(this._population[i]);
			}
		}
		if(feasible.size() > 0) {
			Collections.sort(feasible);
			if (descending) {
				Collections.reverse(feasible);
			}
		}
		if(infeasible.size() > 0) {
			Collections.sort(infeasible);
			if (descending) {
				Collections.reverse(infeasible);
			}
		}
		return new ChromosomeL[][] {feasible.toArray(new ChromosomeL[0]), infeasible.toArray(new ChromosomeL[0]) };
	}

	public void getNextGeneration() {
		ChromosomeL[][] feasibleInfeasible = this.getFeasibleInfeasible(false);
		ChromosomeL[] newPopulation = new ChromosomeL[this._populationSize];

//		System.out.println("~~~~~~~~~~~~\nPOPULATION FEASIBLE");
//		for(int i=0; i<feasibleInfeasible[0].length; i++) {
//			System.out.println("\tChild: "+ feasibleInfeasible[0][i].getAge() +
//					"; Fitness: " + feasibleInfeasible[0][i].getFitness() + 
//					"; Constraint: " + feasibleInfeasible[0][i].getConstraints() +
//					"\n"+ feasibleInfeasible[0][i].getGenes());
//		}
//		System.out.println("~~~~~~~~~~~~\nPOPULATION INFEASIBLE");
//		for(int i=0; i<feasibleInfeasible[1].length; i++) {
//			System.out.println("\tChild: "+ feasibleInfeasible[1][i].getAge() +
//					"; Fitness: " + feasibleInfeasible[1][i].getFitness() + 
//					"; Constraint: " + feasibleInfeasible[1][i].getConstraints() +
//					"\n"+ feasibleInfeasible[1][i].getGenes());
//		}

		for (int i = 0; i < this._populationSize - this._elitism; i++) {
			ChromosomeL[] usedPopulation = feasibleInfeasible[1];
			if (this._rnd.nextDouble() < (double) (feasibleInfeasible[0].length) / this._populationSize) {
				usedPopulation = feasibleInfeasible[0];
			}
			ChromosomeL parent1 = this.rankSelection(usedPopulation);
			ChromosomeL child = (ChromosomeL)parent1.clone();
			if (this._rnd.nextDouble() < this._crossover) {
				ChromosomeL parent2 = this.rankSelection(usedPopulation);
				if((parent1.getGenes()).compareTo(parent2.getGenes()) == 0) {
					child = (ChromosomeL)child.mutate();
					
//					System.out.println("~~~~~~~~~~~~~~~~~~~");
//					System.out.println("CROSSOVER FAIL; AFTER MUTATION");
//					System.out.println("\tParent1: "+ parent1.getAge()+
//							"; Fitness: " + parent1.getFitness() + 
//							"; Constraint: " + parent1.getConstraints() + 
//							"\n"+ parent1.getGenes());
//					System.out.println("\tChild: "+ child.getAge() +
//							"; Fitness: " + child.getFitness() + 
//							"; Constraint: " + child.getConstraints() +
//							"\n"+ child.getGenes());
				}
				else {
					child = (ChromosomeL)parent1.crossover(parent2);
					if((child.getGenes()).compareTo(parent2.getGenes()) == 0 ||
							(child.getGenes()).compareTo(parent2.getGenes()) == 0
							) {
						child = (ChromosomeL)child.mutate();
					}
//					System.out.println("~~~~~~~~~~~~~~~~~~~");
//					System.out.println("AFTER CROSSOVER: ");
//					System.out.println("\tParent1: "+ parent1.getAge()+
//							"; Fitness: " + parent1.getFitness() + 
//							"; Constraint: " + parent1.getConstraints() + 
//							"\n"+ parent1.getGenes());
//					System.out.println("\tParent2: "+ parent2.getAge() +
//							"; Fitness: " + parent2.getFitness() + 
//							"; Constraint: " + parent2.getConstraints() + 
//							"\n"+ parent2.getGenes());
//					System.out.println("\tChild: "+ child.getAge() +
//							"; Fitness: " + child.getFitness() + 
//							"; Constraint: " + child.getConstraints() +
//							"\n"+ child.getGenes());
					if (this._rnd.nextDouble() < this._mutation) {
						
						child = (ChromosomeL)child.mutate();
//						System.out.println("AFTER MUTATION");
//						System.out.println("\tChild: "+ child.getAge() +
//								"; Fitness: " + child.getFitness() + 
//								"; Constraint: " + child.getConstraints() +
//								"\n"+ child.getGenes());
					}
				}
			} else {
				child = (ChromosomeL)child.mutate();
				
//				System.out.println("~~~~~~~~~~~~~~~~~~~");
//				System.out.println("AFTER MUTATION");
//				System.out.println("\tParent1: "+ parent1.getAge()+
//						"; Fitness: " + parent1.getFitness() + 
//						"; Constraint: " + parent1.getConstraints() + 
//						"\n"+ parent1.getGenes());
//				System.out.println("\tChild: "+ child.getAge() +
//						"; Fitness: " + child.getFitness() + 
//						"; Constraint: " + child.getConstraints() +
//						"\n"+ child.getGenes());
			}
			newPopulation[i] = child;
		}
		for (int i = 0; i < this._elitism; i++) {
			if (i < feasibleInfeasible[0].length) {
				newPopulation[newPopulation.length - 1 - i] = feasibleInfeasible[0][feasibleInfeasible[0].length - 1 - i];
			} else {
				newPopulation[newPopulation.length - 1 - i] = feasibleInfeasible[1][feasibleInfeasible[1].length - 1 - (i - feasibleInfeasible[0].length)];
			}
//			System.out.println("Elite " + i + ": " + newPopulation[newPopulation.length - 1 - i].getAge() + "\n" + newPopulation[newPopulation.length - 1 - i].getGenes());
		}
//		System.out.println("~~~~~~~~~~~~~~~~~~~");
		this._population = newPopulation;
	}

	public void writePopulation(String path) throws FileNotFoundException, UnsupportedEncodingException {
		ChromosomeL[][] pop = this.getFeasibleInfeasible(true);
		ChromosomeL[] feasible = pop[0];
		ChromosomeL[] infeasible = pop[1];
		int index = 0;
		PrintWriter resultWriter = new PrintWriter(path + "result.txt", "UTF-8");
		for (ChromosomeL ch : feasible) {
			PrintWriter writer = new PrintWriter(path + index + ".txt", "UTF-8");
			writer.println("Genes: " + ch.getGenes());
			writer.println("Fitness: " + ch.getFitness());
			writer.println("Constraints: " + ch.getConstraints());
			writer.println("Age: " + ch.getAge());
			writer.println("Level:\n" + ch.toString());
			writer.close();
			resultWriter.println("Chromosome " + index + ": " + ch.getConstraints() + ", " + ch.getFitness());
			index += 1;
		}
		for (ChromosomeL ch : infeasible) {
			PrintWriter writer = new PrintWriter(path + index + ".txt", "UTF-8");
			writer.println("Genes: " + ch.getGenes());
			writer.println("Fitness: " + ch.getFitness());
			writer.println("Constraints: " + ch.getConstraints());
			writer.println("Age: " + ch.getAge());
			writer.println("Level:\n" + ch.toString());
			writer.close();
			resultWriter.println("Chromosome " + index + ": " + ch.getConstraints() + ", " + ch.getFitness());
			index += 1;
		}
		resultWriter.close();
	}

	public double[] getStatistics() {
		int numFeasible = 0;
		int numInfeasible = 0;
		double maxFitness = 0;
		double avgFitness = 0;
		double minFitness = 0;
		double maxConstraints = 0;
		double avgConstraints = 0;
		double minConstraints = 0;
		double extraMechs = 0;
		double missingMechs = 0;
		double matchMechs = 0;

		ChromosomeL[][] pop = this.getFeasibleInfeasible(true);
		ChromosomeL[] feasible = pop[0];
		ChromosomeL[] infeasible = pop[1];

		numFeasible = feasible.length;
		for(ChromosomeL c:feasible) {
			avgFitness += c.getFitness();
		}
		if(numFeasible > 0) {
			maxFitness = feasible[0].getFitness();
			minFitness = feasible[numFeasible - 1].getFitness();
			avgFitness /= numFeasible;
			extraMechs = feasible[0].getExtraMechs();
			missingMechs = feasible[0].getMissingMechs();
			matchMechs = feasible[0].getMatchMechs();
		}
		double avgDifference = 0.0;
		for(int i = 1; i < numFeasible; i++) {
			avgDifference += (this.calculateDistance(feasible[0], feasible[i]) / 14);
		}
		avgDifference /= numFeasible - 1;

		numInfeasible = infeasible.length;
		//		for(ChromosomeL c:feasible) {
		//			avgConstraints += c.getConstraints();
		//		}
		if(numInfeasible > 0) {
			for(ChromosomeL c:infeasible) {
				avgConstraints += c.getConstraints();
			}
			maxConstraints = infeasible[0].getConstraints();
			minConstraints = infeasible[numInfeasible - 1].getConstraints();
			avgConstraints /= numInfeasible;
		}
		//		avgConstraints /= (numInfeasible+numFeasible);


		return new double[] {numFeasible, maxFitness, avgFitness, minFitness, numInfeasible, maxConstraints, avgConstraints, minConstraints, matchMechs, missingMechs, extraMechs};
	}
	
	public double calculateDistance(ChromosomeL one, ChromosomeL two) {
		int[] oneGenes = one.getGenesArray();
		int[] twoGenes = two.getGenesArray();
		
		double difference = 0;
		for(int i = 0; i < oneGenes.length; i++) {
			if(oneGenes[i] != twoGenes[i]) {
				difference += 1;
			}
		}
		return difference;
	}
}
