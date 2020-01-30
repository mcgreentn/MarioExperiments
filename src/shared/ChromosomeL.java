package shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;


import engine.core.EventLogger;
import engine.core.MarioAgent;
import engine.core.MarioGame;
import engine.core.MarioResult;
import engine.helper.GameStatus;

public class ChromosomeL implements Comparable<ChromosomeL>{
	protected Random _rnd;
	protected int[] _genes;
	protected int[] _subGenes;
	protected ArrayList<String> _populationMechanics;
	protected int _appendingSize;
	protected double _constraints;
	protected double _fitness;
	protected double _matchMechs;
	protected double _missingMechs;
	protected double _extraMechs;
	protected int[] _dimensions;
	protected ScenesLibrary _library;
	private double _constraintProbability;
	private int _age;
	private int _numOfScenes;
	private int _numMechanicsInPlaythrough;
	private boolean _variableNumOfMechInScene;
	private String[] _listOfPossibleMechanics;
	private String[] _playthroughMechanics;
	private HashMap<String, String> _parameters;
	
	
	private boolean punishNeverMechs = false;
	private double percentagePenaltyExtraMechs = 0.2;


	//chromosome is a full level
	//a gene is a single scene
	public ChromosomeL(Random rnd, ScenesLibrary lib, int numOfScenes, int appendingSize, String[] playthroughMechanics, boolean variableNumOfMechInScene, HashMap<String, String> parameters) {
		this._rnd = rnd;
		this._library = lib;
		this._genes = new int[numOfScenes];
		this._subGenes = new int[numOfScenes];
		this._populationMechanics = new ArrayList<String>();
		this._appendingSize = appendingSize;
		this._constraints = 0;
		this._fitness = 0;
		this._dimensions = null;
		this._constraintProbability = 1.0;
		this._age = 0;
		this._numOfScenes = numOfScenes;
		this._variableNumOfMechInScene = variableNumOfMechInScene;
		this._playthroughMechanics = playthroughMechanics;
		this._numMechanicsInPlaythrough = 0;
		this._parameters = parameters;
		for(int i = 0; i < this._playthroughMechanics.length; i++) {
			this._numMechanicsInPlaythrough += this._playthroughMechanics[i].chars().filter(num -> num == '1').count();
		}

		this._listOfPossibleMechanics = new String[] {"Mario Jumps", "Low Jump", "High Jump", "Short Jump", "Long Jump", "Stomp Kill", "Shell Kill", "Fall Kill", "Mario Mode", "Coins Collected", "Bumping Brick Block", "Bumping Question Block"};
	}
	
	public void chromosomeLReset() {
		this._genes = new int[this._numOfScenes];
		this._subGenes = new int[this._numOfScenes];
		this._populationMechanics = new ArrayList<String>();
		this._constraints = 0;
		this._fitness = 0;
		this._dimensions = null;
		this._constraintProbability = 1.0;
		this._age = 0;
	}

	public void stringInitialize(String[] level) {
		String[] parts = level[0].split(",");
		this._age = Integer.parseInt(parts[0]);
		this._numOfScenes = Integer.parseInt(parts[1]);
		String[] geneString = level[1].split(",");
		this._genes = new int[this._numOfScenes];
		this._subGenes = new int[this._numOfScenes];
		String[] subParts = level[2].split(",");
		for (int i = 0; i < this._genes.length; i++) {
			this._genes[i] = Integer.parseInt(geneString[i]);
		}
		String[] subPartsGenes = level[2].split(",");
		for(int i = 0; i < this._subGenes.length; i++) {
			this._subGenes[i] = Integer.parseInt(subPartsGenes[i]);
		}
	}

	public ChromosomeL clone() {
		ChromosomeL chromosome = new ChromosomeL(this._rnd, this._library, this._numOfScenes, this._appendingSize, this._playthroughMechanics, this._variableNumOfMechInScene, this._parameters);
		for(int i=0; i < this._genes.length; i++) {
			chromosome._genes[i] = this._genes[i];
			chromosome._subGenes[i] = this._subGenes[i];
		}
		return chromosome;
	}

	public double getConstraintProbability() {
		return this._constraintProbability;
	}

	public int getNumberOfScenes() {
		return this._numOfScenes;
	}
	public int getAge() {
		return this._age;
	}
	public void setAge (int newAge) {
		this._age = newAge;
	}
	public int[] getDimensions() {
		return this._dimensions;
	}
	public double getConstraints() {
		return this._constraints;
	}
	public double getFitness() {
		return this._fitness;
	}
	public double getExtraMechs() {
		return this._extraMechs;
	}
	public double getMissingMechs() {
		return this._missingMechs;
	}
	public double getMatchMechs() {
		return this._matchMechs;
	}
	public String getGenes() {
		String result = "" + this._genes[0];
		for (int i = 1; i < this._genes.length; i++) {
			result += "," + this._genes[i];
		}
		result += "\n" + this._subGenes[0];
		for(int i = 1; i < this._subGenes.length; i++) {
			result += "," + this._subGenes[i];
		}
		return result;
	}
	public String[] getPlaythroughMechanics() {
		return this._playthroughMechanics;
	}
	
	public int[] getGenesArray() {
		return this._genes;
	}

	public void advanceAge() {
		this._age += 1; 
	}

	private int calcAvgMechanics() {
		int avg_mechanics = 3;
		if(this._variableNumOfMechInScene) {
			avg_mechanics = (int) Math.ceil((double)this._numMechanicsInPlaythrough/this._numOfScenes);
			if (avg_mechanics > 10) {
				avg_mechanics = 7;
			}
		}
		return avg_mechanics;
	}

	private void createGeneRandomly(int index, int avg_mechanics) {
		int sceneIndex;
		String mechanicsToUseString;
		int num_mechanics;
		do {
			sceneIndex = this._rnd.nextInt(this._library.getNumberOfScenes());
			mechanicsToUseString = this._library.getSceneMechanics(sceneIndex);

			this._genes[index] = sceneIndex;
			this._subGenes[index] = this._library.getSubSceneIndex(sceneIndex);

			num_mechanics = (int) mechanicsToUseString.chars().filter(num -> num == '1').count();
		} while(num_mechanics != avg_mechanics);
		this._populationMechanics.add(mechanicsToUseString);
	}
	public void randomInitialization() {
		int avg_mechanics = calcAvgMechanics();
		//create the genes
		int i;
		for(i = 0; i < this._genes.length-1; i++) {
			this.createGeneRandomly(i, avg_mechanics);
		}
		//the last gene will have the remaining scenes (in case of an uneven split)
		//if remaining scenes exceedes 10 -> force it to be 7
		avg_mechanics = this._numMechanicsInPlaythrough - avg_mechanics * (this._genes.length-1);
		if(avg_mechanics > 10) {
			avg_mechanics = 7;
		}
		if(avg_mechanics < 0) {
			avg_mechanics = 0;
		}
		if(!this._variableNumOfMechInScene) {
			avg_mechanics = 3;
		}

		this.createGeneRandomly(i, avg_mechanics);
	}
	
	public void completeRandomInitialization() {
		for(int i = 0; i < this._genes.length; i++) {
			int avg_mechanics = this._rnd.nextInt(11); //make number of mechanics in single scene is 10
			if(avg_mechanics < 0) {
				avg_mechanics = 0;
			}
			this.createGeneRandomly(i, avg_mechanics);
		}
	}
	
	public void randomGuassInitialization() {
		int avg_mechanics = calcAvgMechanics();
		//create the genes
		//every scene (aka gene) will have a avg+mechanics amount
		int i;
		for(i = 0; i < this._genes.length; i++) {
			double guass_noise = this._rnd.nextGaussian();
			int num_mechanics = (int) Math.floor(avg_mechanics+guass_noise);
//			System.out.println("Num of mechanics for Scene: " + i + " is " + num_mechanics);
			//fix the range
			if (num_mechanics > 10) {
				num_mechanics = 10;
			} else if (num_mechanics < 0) {
				num_mechanics = 0;
			}
			this.createGeneRandomly(i, num_mechanics);
		}
	}
	
	public void mutatedSmartInitialization(int min, int max) {
		int choice = this._rnd.nextInt(2);
		//deleting a scene
		if (choice == 0  && this._numOfScenes > min) {
			int indexToDelete = this._rnd.nextInt(this._genes.length);
			int[] new_genes = new int[this._genes.length - 1]; 
			int[] new_subGenes = new int[this._subGenes.length-1];
			System.arraycopy(this._genes, 0, new_genes, 0, indexToDelete); 
			System.arraycopy(this._subGenes, 0, new_subGenes, 0, indexToDelete); 
	        // Copy the elements from index + 1 till end 
	        // from original array to the other array 
	        System.arraycopy(this._genes, indexToDelete + 1, 
	        				 new_genes, indexToDelete, 
	                         this._genes.length - indexToDelete - 1); 
	        System.arraycopy(this._subGenes, indexToDelete + 1, 
	        			 	 new_subGenes, indexToDelete, 
	        			 	 this._subGenes.length - indexToDelete - 1);
	        this._genes = new_genes;
	        this._subGenes = new_subGenes;
	        this._numOfScenes -= 1;
		}
		//adding a scene
		else if(choice == 1 && this._numOfScenes < max) {
			int sceneIndex = this._rnd.nextInt(this._library.getNumberOfScenes());
			int indexToAdd = this._rnd.nextInt(this._genes.length);
			int[] new_genes = new int[this._genes.length + 1]; 
			int[] new_subGenes = new int[this._subGenes.length + 1];
			for (int i = 0; i < new_genes.length; i++) {
				if (i < indexToAdd) {
					new_genes[i] = this._genes[i];
					new_subGenes[i] = this._subGenes[i];
				}
				else if (i == indexToAdd){
					new_genes[i] = sceneIndex;
					new_subGenes[i] = this._library.getSubSceneIndex(sceneIndex);
				}
				else {
					new_genes[i] = this._genes[i-1];
					new_subGenes[i] = this._subGenes[i-1];
				}
			}
			this._genes = new_genes;
	        this._subGenes = new_subGenes;
			this._numOfScenes += 1;
		}
		//mutating a scene
		else {
			Set<Integer> mutatedSceneIndex = new HashSet<Integer> ();
			int num_scenes_mutate = (int)(0.3 * this._numOfScenes);
//			System.out.println("~~~~NEW ONE~~~~~");
			for(int _i = 0; _i < num_scenes_mutate; _i++) {
//				System.out.println("MUTATING SCENE: " + _i);
				boolean oneMutateHappen = false;
				//which scene to mutate - higher chance to pick a scene with more mechanics fired
				ArrayList<int[]> temp = new ArrayList<int[]>();
				for(int i = 0; i < this._genes.length; i++) {
					int singleGene = this._genes[i];
					String mechanicTemp = this._library.getSceneMechanics(singleGene);
					int num_mechanics_fired = ((int) mechanicTemp.chars().filter(num -> num == '1').count()) + 1;
					for (int j = 0; j < num_mechanics_fired; j++) {
						int[] t = new int[] {singleGene, i};
						temp.add(t);
					}
				}
				//shuffle the biased arrayList
				Collections.shuffle(temp);
				int weightSelected = this._rnd.nextInt(temp.size());
				int indexToMutate = temp.get(weightSelected)[1];
				int sceneToMutate = temp.get(weightSelected)[0];
				
//				System.out.println("indexToMutate: " + indexToMutate);
//				System.out.println("sceneToMutate: " + sceneToMutate);
//				System.out.println("sceneToMutateMechanics: " + this._library.getSceneMechanics(sceneToMutate));
//				System.out.println("genes before: " + Arrays.toString(this._genes));
//				System.out.println("subGenes before: " + Arrays.toString(this._subGenes));
				int mutationChoice = this._rnd.nextInt(4);
				//option 1: split
				if (mutationChoice == 0) {
					String selectedSceneMechanics = this._library.getSceneMechanics(sceneToMutate);
					ArrayList<Integer> bitsTurnedOnIndex = new ArrayList<Integer>();
					for(int i = 0; i < selectedSceneMechanics.length(); i++){
						if(selectedSceneMechanics.charAt(i) == '1'){
							bitsTurnedOnIndex.add(i);
						}
					}
					//too few mechanics, dont split, try another mutation field
					if (bitsTurnedOnIndex.size() <  4) {
						mutationChoice = this._rnd.nextInt(3) + 1;
					}
					else {
						//split 50% -> go rng left and right
						int split = bitsTurnedOnIndex.size()/2;
						StringBuilder leftSceneToAdd = new StringBuilder("000000000000");
						int addedLeft = 0;
						StringBuilder rightSceneToAdd = new StringBuilder("000000000000");
						int addedRight = 0;
						for (int i = 0; i < bitsTurnedOnIndex.size(); i++) {
							int leftOrRight = this._rnd.nextInt(2);
							if ( (leftOrRight == 0 && addedLeft <= split) || addedRight == split) {
								leftSceneToAdd.setCharAt(bitsTurnedOnIndex.get(i),'1');
								addedLeft++;
							} else {
								rightSceneToAdd.setCharAt(bitsTurnedOnIndex.get(i),'1');
								addedRight++;
							}
						}
						//chance the split gave us something tht is not in lib, remove mechanics until it is
						int[] leftSceneAndSubSceneIndex = this._library.getSceneIndex(leftSceneToAdd.toString());
						int leftRemovingMechanics = 0;
						while (leftSceneAndSubSceneIndex[0] == -1 || leftSceneAndSubSceneIndex[1] == -1) {
							leftSceneToAdd.setCharAt(leftRemovingMechanics, '0');
							leftRemovingMechanics++;
							leftSceneAndSubSceneIndex = this._library.getSceneIndex(leftSceneToAdd.toString());
						}
						int[] rightSceneAndSubSceneIndex = this._library.getSceneIndex(rightSceneToAdd.toString());
						int rightRemovingMechanics = 0;
						while (rightSceneAndSubSceneIndex[0] == -1 || rightSceneAndSubSceneIndex[1] == -1) {
							rightSceneToAdd.setCharAt(rightRemovingMechanics, '0');
							rightRemovingMechanics++;
							rightSceneAndSubSceneIndex = this._library.getSceneIndex(rightSceneToAdd.toString());
						}
						//chance both sides lost all mechanics
						//try different mutation scene
						if (rightSceneToAdd.toString().compareTo("000000000000") == 0 && leftSceneToAdd.toString().compareTo("000000000000") == 0) {
							mutationChoice = this._rnd.nextInt(4) + 1;
						}
						else {
							//adding the scene
							int[] new_genes = new int[this._genes.length + 1]; 
							int[] new_subGenes = new int[this._subGenes.length + 1];
							for (int i = 0; i < new_genes.length; i++) {
								if (i < indexToMutate) {
									new_genes[i] = this._genes[i];
									new_subGenes[i] = this._subGenes[i];
								}
								else if (i == indexToMutate){
									new_genes[i] = leftSceneAndSubSceneIndex[0];
									new_subGenes[i] = leftSceneAndSubSceneIndex[1];
								}
								else if (i == indexToMutate+1) {
									new_genes[i] = rightSceneAndSubSceneIndex[0];
									new_subGenes[i] = rightSceneAndSubSceneIndex[1];
								}
								else {
									new_genes[i] = this._genes[i-1];
									new_subGenes[i] = this._subGenes[i-1];
								}
							}
							this._genes = new_genes;
							this._subGenes = new_subGenes;
							this._numOfScenes += 1;
							oneMutateHappen = true;
						}
					}
				}
				//option 2: merge left
				if (mutationChoice == 1 && indexToMutate > 1 && this._numOfScenes > min && oneMutateHappen != true) {
					StringBuilder fusionLeft = new StringBuilder("000000000000");
					int indexLeft = indexToMutate - 1;
					int sceneLeft = this._genes[indexLeft];
					
					String selectedSceneMechanics = this._library.getSceneMechanics(sceneToMutate);
					String selectedLeftSceneMechanics = this._library.getSceneMechanics(sceneLeft);
					
					for(int i = 0; i < selectedSceneMechanics.length(); i++) {
						if(selectedSceneMechanics.charAt(i) == '1' || selectedLeftSceneMechanics.charAt(i) == '1') {
							fusionLeft.setCharAt(i, '1');
						}
					}
					
					//possible that the fusion scene is not in the scene library
					//if it isnt then scratch and perform the final option, mutate
					int[] fusedSceneAndSubSceneIndex = this._library.getSceneIndex(fusionLeft.toString());
					if (fusedSceneAndSubSceneIndex[0] == -1 || fusedSceneAndSubSceneIndex[1] == -1) {
						mutationChoice = 3;
					} else {
						int[] new_genes = new int[this._genes.length - 1]; 
						int[] new_subGenes = new int[this._subGenes.length-1];
						//copy elements from 0 to index
						//from original array to the other array
						System.arraycopy(this._genes, 0, new_genes, 0, indexToMutate); 
						System.arraycopy(this._subGenes, 0, new_subGenes, 0, indexToMutate); 
				        // Copy the elements from index + 1 till end 
				        // from original array to the other array 
				        System.arraycopy(this._genes, indexToMutate + 1, 
				        				 new_genes, indexToMutate, 
				                         this._genes.length - indexToMutate - 1); 
				        System.arraycopy(this._subGenes, indexToMutate + 1, 
				        			 	 new_subGenes, indexToMutate, 
				        			 	 this._subGenes.length - indexToMutate - 1);
				        new_genes[indexLeft] = fusedSceneAndSubSceneIndex[0];
				        new_subGenes[indexLeft] = fusedSceneAndSubSceneIndex[1];
				        this._genes = new_genes;
				        this._subGenes = new_subGenes;
				        this._numOfScenes -= 1;
				        oneMutateHappen = true;
					}
				} else {
					//want to merge left but couldn't, default to
					mutationChoice = 3;
				}
				//option 3: merge right
				if (mutationChoice == 2 && indexToMutate < this._genes.length-2 && this._numOfScenes < max && oneMutateHappen != true) {
					StringBuilder fusionRight = new StringBuilder("000000000000");
					int indexRight = indexToMutate + 1;
					int sceneRight = this._genes[indexRight];
					
					String selectedSceneMechanics = this._library.getSceneMechanics(sceneToMutate);
					String selectedLeftSceneMechanics = this._library.getSceneMechanics(sceneRight);
					
					for(int i = 0; i < selectedSceneMechanics.length(); i++) {
						if(selectedSceneMechanics.charAt(i) == '1' || selectedLeftSceneMechanics.charAt(i) == '1') {
							fusionRight.setCharAt(i, '1');
						}
					}
					
					//possible that the fusion scene is not in the scene library
					//if it isnt then scratch and perform the final option, mutate
					int[] fusedSceneAndSubSceneIndex = this._library.getSceneIndex(fusionRight.toString());
					if (fusedSceneAndSubSceneIndex[0] == -1 || fusedSceneAndSubSceneIndex[1] == -1) {
						mutationChoice = 3;
					} else {
						int[] new_genes = new int[this._genes.length - 1]; 
						int[] new_subGenes = new int[this._subGenes.length-1];
						//copy elements from 0 to index
						//from original array to the other array
						System.arraycopy(this._genes, 0, new_genes, 0, indexToMutate); 
						System.arraycopy(this._subGenes, 0, new_subGenes, 0, indexToMutate); 
				        // Copy the elements from index + 1 till end 
				        // from original array to the other array 
				        System.arraycopy(this._genes, indexToMutate + 1, 
				        				 new_genes, indexToMutate, 
				                         this._genes.length - indexToMutate - 1); 
				        System.arraycopy(this._subGenes, indexToMutate + 1, 
				        			 	 new_subGenes, indexToMutate, 
				        			 	 this._subGenes.length - indexToMutate - 1);
				        new_genes[indexToMutate] = fusedSceneAndSubSceneIndex[0];
				        new_subGenes[indexToMutate] = fusedSceneAndSubSceneIndex[1];
				        this._genes = new_genes;
				        this._subGenes = new_subGenes;
				        this._numOfScenes -= 1;
				        oneMutateHappen = true;
					}
				}
				//option 4: mutate - lower mechanics = higher chance, must have mechanics that fired for lower
				if (mutationChoice == 3 && oneMutateHappen != true) {
					int[] weightedScene = this._library.getWeightedScene(sceneToMutate);
					this._genes[indexToMutate] = weightedScene[0];
					this._subGenes[indexToMutate] = weightedScene[1];
//					System.out.println("weightedScene: " + Arrays.toString(weightedScene));
				}
				
//				System.out.println("genes after: " + Arrays.toString(this._genes));
//				System.out.println("subGenes after: " + Arrays.toString(this._subGenes));
				
				//old mutation - simple mutation
//				int sceneIndex = this._rnd.nextInt(this._library.getNumberOfScenes());
//				int indexToMutate = this._rnd.nextInt(this._genes.length);
//				while(mutatedSceneIndex.contains(indexToMutate)) {
//					indexToMutate = this._rnd.nextInt(this._genes.length);
//				}
//				mutatedSceneIndex.add(indexToMutate);
//				this._genes[indexToMutate] = sceneIndex;
//				this._subGenes[indexToMutate] = this._library.getSubSceneIndex(sceneIndex);
			}
		}
	}

	public void smartInitialization() {
		//create the gene in a smart way
		//guarantee the mechanics will occur in that order
		ArrayList<String> levelMechanics = new ArrayList<String>();
		for(int i = 0; i < this._numOfScenes; i++) {
			levelMechanics.add("000000000000");
		}

		int sceneIndex = 0;
		int playthroughMechanicIndex = 0;
		while(sceneIndex < this._numOfScenes) {
			float probability = this._rnd.nextFloat();
			int numScenesToSkip = probability < 0.85 ? 0 : (probability < 0.95 ? 1: 2);
			sceneIndex += numScenesToSkip;
			if(sceneIndex >= this._numOfScenes) {
				break;
			}
			String t = levelMechanics.get(sceneIndex);
			StringBuilder mechanicString = new StringBuilder(t);
			if (playthroughMechanicIndex >= this._playthroughMechanics.length) {
				break;
			}
			for(int z = 0; z < mechanicString.length(); z++){
				if(mechanicString.charAt(z) - '1' != 0){
					mechanicString.setCharAt(z, this._playthroughMechanics[playthroughMechanicIndex].charAt(z));
				}
			}
			//check if it is in scenelibrary
			if(this._library.getSceneIndex(mechanicString.toString())[0] != -1) {
				levelMechanics.set(sceneIndex, mechanicString.toString());
			} else {
				sceneIndex += 1;
				if(sceneIndex >= this._numOfScenes) {
					break;
				} else {
					levelMechanics.set(sceneIndex, this._playthroughMechanics[playthroughMechanicIndex]);
				}
			}
			playthroughMechanicIndex += 1;
		}
		//stuff left over playthroughMechanics into the end
		while(playthroughMechanicIndex < this._playthroughMechanics.length) {
			String t = levelMechanics.get(this._numOfScenes-1);
			StringBuilder mechanicString = new StringBuilder(t);
 			for(int z = 0; z < mechanicString.length(); z++){
				if(mechanicString.charAt(z) - '1' != 0){
					mechanicString.setCharAt(z, this._playthroughMechanics[playthroughMechanicIndex].charAt(z));
				}
			}
			//check if it is in scenelibrary
			if(this._library.getSceneIndex(mechanicString.toString())[0] != -1) {
				levelMechanics.set(this._numOfScenes-1, mechanicString.toString());
			} 
			playthroughMechanicIndex += 1;
		}
		for(int i = 0; i < levelMechanics.size(); i++) {
			int[] tempIndex = this._library.getSceneIndex(levelMechanics.get(i));
			this._genes[i] = tempIndex[0];
			this._subGenes[i] = tempIndex[1];
		}
		this._populationMechanics = levelMechanics;
	}

	public void childEvaluationInitialization(String values) {
		String[] parts = values.split(",");
		this._age = Integer.parseInt(parts[0]);
		this._constraints = Double.parseDouble(parts[1]);
		this._fitness = Double.parseDouble(parts[2]);
		this._matchMechs = Double.parseDouble(parts[3]);
		this._missingMechs = Double.parseDouble(parts[4]);
		this._extraMechs = Double.parseDouble(parts[5]);
		this._dimensions = new int[parts.length - 6];
		for(int i=6; i<parts.length; i++) {
			this._dimensions[i-6] = Integer.parseInt(parts[i]);
		}
	}

	protected MarioResult[] runAlgorithms(MarioGame[] games, MarioAgent[] agents, int maxTime) {
		MarioResult[] results = new MarioResult[agents.length];
//		long allRunsStart = System.nanoTime();
		for(int i=0; i<agents.length; i++) {
			//			System.out.println("\t Start playing game");
//			long startTime = System.nanoTime();
			results[i] = games[i].runGame(agents[i], this.toString(), maxTime);
			//			System.out.println("\t Finish playing game");
//			long endTime = System.nanoTime();
//			long elapsedTime = endTime - startTime;
//			System.out.println("Run " + i + " time: " + elapsedTime/1_000_000_000);
		}
//		long allRunsEnd = System.nanoTime();
//		long allRunsTime = allRunsEnd - allRunsStart;
//		System.out.println("Total Run Time: " + allRunsTime/1_000_000_000);
		return results;
	}

	private void calculateConstraints(MarioResult[] runs) {
		double avgConst = 0;
		double completeRuns = 0;
		for (MarioResult run : runs) {
			double tempConst = run.getCompletionPercentage();
			if (tempConst >= 1) {
				completeRuns += 1.0;
			}
			avgConst += tempConst;
		}
		completeRuns = Math.min(1, (completeRuns / runs.length) / 0.5);
		if (completeRuns >= 1) {
		  this._constraints = 1;
		}
		else{
		  avgConst /= runs.length;
		  this._constraints = avgConst;
		}
		
//		double avgConst = 0;
//		boolean flag = false;
//
//		for (MarioResult run : runs) {
//			double tempConst = run.getCompletionPercentage();
//			if (tempConst >= 1) {
//				flag = true;
//				this._constraints = 1.0;
//				break;
//			}
//			else
//				avgConst += tempConst;
//		}
//		if (!flag) {
//			avgConst /= runs.length;
//			this._constraints = avgConst;
//		}
			/*
			tempConst = runs[1].getCompletionPercentage() - tempConst;
			if(runs[1].getGameStatus() == GameStatus.WIN && runs[2].getGameStatus() == GameStatus.LOSE) {
				tempConst = 1;
			}*/
		//	for(int i = 1; i < runs.length; i++) {
		//		tempConst += runs[i].getCompletionPercentage();	
		//	}
		//	tempConst /= runs.length;
			
		
		//		if(this._age > 0) {
		//			this._constraints = Math.min(this._constraints, tempConst);
		//		}
		//		else {
		// this._constraints = avgConst;
		//		}
	}

	private void calculateDimensions(MarioResult run, int doNothingFallKills) {
		this._dimensions = new int[12];
		this._dimensions[0] = run.getNumJumps() >= 1? 1:0;
		this._dimensions[1] = (run.getMaxJumpAirTime() > 0 && run.getMaxJumpAirTime() <= 10.0)? 1:0;
		this._dimensions[2] = run.getMaxJumpAirTime() >= 12.0? 1:0;
		this._dimensions[3] = (run.getMaxXJump() > 0 && run.getMaxXJump() <= 40.0)? 1:0;
		this._dimensions[4] = run.getMaxXJump() >= 120.0? 1:0;
		this._dimensions[5] = run.getKillsByStomp() >= 1? 1:0;
		this._dimensions[6] = run.getKillsByShell() >= 1? 1:0;
		this._dimensions[7] = doNothingFallKills >= 1? 1:0;
		this._dimensions[8] = run.getMarioMode() >= 1? 1:0;
		this._dimensions[9] = run.getCurrentCoins() >= 1? 1:0;
		this._dimensions[10] = run.getNumBumpBrick() >= 1? 1:0;
		this._dimensions[11] = run.getNumBumpQuestionBlock() >= 1? 1:0;
	}

	private ArrayList<String> reduceMechanicsToActions(String[] mechanicsToReduceArrayExcess) {
		ArrayList<String> mechanicsAL = new ArrayList<String>(Arrays.asList(mechanicsToReduceArrayExcess));

		ArrayList<String> to_return = new ArrayList<String>();

		for(int i = 0; i < mechanicsAL.size(); i++) {
			StringBuilder mechanicString = new StringBuilder("000000000000");
			ArrayList<String> agentMechanics = new ArrayList<String>();
			String mechInfo = mechanicsAL.get(i);
			String starter = "Action\":\"";
			while(mechInfo.indexOf(starter) != -1) {
				int index = mechInfo.indexOf(starter);
				String actionExcess = mechInfo.substring(index+starter.length());
				int actionStopIndex = actionExcess.indexOf("\"");
				String action = actionExcess.substring(0, actionStopIndex);
				agentMechanics.add(action);	
				mechInfo = actionExcess.substring(actionStopIndex);
			}
			for(int j = 0; j < agentMechanics.size(); j++) {
				String triggeredMech = agentMechanics.get(j);
				switch(triggeredMech) {
				//				case "Mario Jumps":
				//					mechanicString.setCharAt(0, '1');
				//					break;
				case "Low Jump":
					mechanicString.setCharAt(0, '1');
					mechanicString.setCharAt(1, '1');
					break;
				case "High Jump":
					mechanicString.setCharAt(0, '1');
					mechanicString.setCharAt(2, '1');
					break;
				case "Short Jump":
					mechanicString.setCharAt(0, '1');
					mechanicString.setCharAt(3, '1');
					break;
				case "Long Jump":
					mechanicString.setCharAt(0, '1');
					mechanicString.setCharAt(4, '1');
					break;
				case "Stomp Kill":
					mechanicString.setCharAt(5, '1');
					break;
				case "Shell Kill":
					mechanicString.setCharAt(6, '1');
					break;
				case "Fall Kill":
					mechanicString.setCharAt(7, '1');
					break;
				case "Mario Mode":
					mechanicString.setCharAt(8, '1');
					break;
				case "Coins Collected":
					mechanicString.setCharAt(9, '1');
					break;
				case "Bumping Brick Block":
					mechanicString.setCharAt(10, '1');
					break;
				case "Bumping Question Block":
					mechanicString.setCharAt(11, '1');
					break;
				}
			}
			if (mechanicString.toString().compareTo("000000000000") == 0) {
				continue;
			}
			to_return.add(mechanicString.toString());
		}	
		return to_return;
	}
	private ArrayList<String> mapPlaythroughMechanics(){
		ArrayList<String> playMechanics = new ArrayList<String>();
		for(int i = 0; i < this._playthroughMechanics.length; i++) {
			String[] sceneMechanics = this._playthroughMechanics[i].split(",");
			for(int j = 0; j < sceneMechanics.length; j++) {
				if(sceneMechanics[j].equals("1")) {
					switch(j) {
					case 1:
						playMechanics.add("Mario Jumps");
						break;
					case 2:
						playMechanics.add("Low Jump");
						break;
					case 3:
						playMechanics.add("High Jump");
						break;
					case 4:
						playMechanics.add("Short Jump");
						break;
					case 5:
						playMechanics.add("Long Jump");
						break;
					case 6:
						playMechanics.add("Stomp Kill");
						break;
					case 7:
						playMechanics.add("Shell Kill");
						break;
					case 8:
						playMechanics.add("Fall Kill");
						break;
					case 9:
						playMechanics.add("Coins Collected");
						break;
					case 10:
						playMechanics.add("Bumping Brick Block");
						break;
					case 11:
						playMechanics.add("Bumping Question Block");
						break;
					}
				}
			}
		}
		return playMechanics;
	}

	public ArrayList<Double> calculateFitness(MarioResult run)
	{
		double fitnessScore = 100;

		//reduce the lists to just actions
		String[] agentMechanicsArrayExcess = EventLogger.getPlayedMechanics(run.getGameEvents());		
		ArrayList<String> agentActions = this.reduceMechanicsToActions(agentMechanicsArrayExcess);
		ArrayList<String> playthroughActions = new ArrayList<String>(Arrays.asList(this._playthroughMechanics));
		//go through agentActions and compareActions
		double missedMechs = 0;
		int agentMechanicPointer = 0; 
		int playthroughMechanicPointer = 0;
		double extraMechs = 0;
		for(; playthroughMechanicPointer < playthroughActions.size(); playthroughMechanicPointer++) {
			String mechanicToCheck = playthroughActions.get(playthroughMechanicPointer);
			ArrayList<String> subArray = new ArrayList<String>(agentActions.subList(agentMechanicPointer, agentActions.size()));
			int mechanicIndex = subArray.indexOf(mechanicToCheck);
			if(mechanicIndex == -1) {
				missedMechs += 1;
			}
			else {
				agentMechanicPointer += mechanicIndex + 1;
				extraMechs += mechanicIndex;
			}
			if(agentMechanicPointer >= agentActions.size()) {
				playthroughMechanicPointer++;
				break;
			}
		}
		fitnessScore = 100.0;


		//lose points for mechanics leftover from target playthrough
		double numberOfActionsLeft = 0;
		if(playthroughMechanicPointer  < playthroughActions.size()) {
			numberOfActionsLeft = playthroughActions.size() - playthroughMechanicPointer;
			
		}
		missedMechs += numberOfActionsLeft;
		
		//lose points for the mechanics the chromosome missed from the target sequence
		fitnessScore -= (missedMechs * 5 );
		
		// penalize for extra mechanics in the chromosome, as parameters allow
		if(Boolean.parseBoolean(this._parameters.get("punishExtraMechs"))) {
			double a = Double.parseDouble(this._parameters.get("a"));
			double b = Double.parseDouble(this._parameters.get("b"));
			double c = Double.parseDouble(this._parameters.get("c"));
			double percentageP = Double.parseDouble(this._parameters.get("percentagePenaltyExtraMechs"));
			
			double equation = a * Math.tanh(b * extraMechs) + c;
			double penalty = percentageP * equation * Math.abs(fitnessScore);
			System.out.println("Extra Count: " + extraMechs + " || Penalty: " + penalty + " || " + fitnessScore + " -> " + (fitnessScore-penalty));
			fitnessScore -= penalty;
		}

		if(Boolean.parseBoolean(this._parameters.get("punishNeverMechs"))) {
			
		}
		// add all mechanic counts to a returnable list
		ArrayList<Double> list = new ArrayList<Double>();
		list.add(fitnessScore);
		list.add(playthroughActions.size() - missedMechs);
		list.add(missedMechs);
		list.add(extraMechs);
		
		return list;
	}
	
	public int numberFiredMechanics(MarioResult run) {
		//reduce the lists to just actions
		String[] agentMechanicsArrayExcess = EventLogger.getPlayedMechanics(run.getGameEvents());		
		ArrayList<String> agentActions = this.reduceMechanicsToActions(agentMechanicsArrayExcess);
		int toReturn = 0;
		for (int i = 0; i < agentActions.size(); i++) {
			String temp = agentActions.get(i);
			int keyWeight = ((int) temp.chars().filter(num -> num == '1').count());
			toReturn += keyWeight;
		}
		return toReturn;
	}
	
	public void calculateFitnessEntropy(MarioResult[] runs) {
		//pick the run with the least fired mechanics
		int indexOfLeastFiredMechanicsRun = 0;
		int firedMechanics = numberFiredMechanics(runs[0]);
		for (int i = 1; i < runs.length; i++) {
			double tempConst = runs[i].getCompletionPercentage();
			if (tempConst >= 1.0) {
				int tempFiredMechanics = numberFiredMechanics(runs[i]);
				if (tempFiredMechanics < firedMechanics) {
					firedMechanics = tempFiredMechanics;
					indexOfLeastFiredMechanicsRun = i;
				}
			}
		}
		//found the run that is winnable with least number of mechanics fired
		ArrayList<Double> sourceList = this.calculateFitness(runs[indexOfLeastFiredMechanicsRun]);
		double score = sourceList.get(0);
		double matchMechs = sourceList.get(1);
		double missingMechs = sourceList.get(2);
		double extraMechs = sourceList.get(3);

		this._fitness = score;
		this._matchMechs = matchMechs;
		this._missingMechs = missingMechs;
		this._extraMechs = extraMechs;
		

//		ArrayList<Double> sourceList = this.calculateFitness(runs[0]);
//		double score = sourceList.get(0);
//		double matchMechs = sourceList.get(1);
//		double missingMechs = sourceList.get(2);
//		double extraMechs = sourceList.get(3);
//		for(int i = 1; i < runs.length; i++) {
//			ArrayList<Double> sl = this.calculateFitness(runs[i]);
//			score += sl.get(0);
//			matchMechs += sl.get(1);
//			missingMechs += sl.get(2);
//			extraMechs += sl.get(3);	
//		}
//		this._fitness = score / (double)runs.length;
//		this._matchMechs = matchMechs / (double)runs.length;
//		this._missingMechs = missingMechs / (double)runs.length;
//		this._extraMechs = extraMechs / (double)runs.length;
	}

	//since map elites uses 1 game with 1 agent, we only need the first result
	public void calculateFitnessEntropy(MarioResult run) {
		ArrayList<Double> sourceList = this.calculateFitness(run);
		this._fitness = sourceList.get(0);
		this._matchMechs = sourceList.get(1);
		this._missingMechs = sourceList.get(2);
		this._extraMechs = sourceList.get(3);
	}

	//map elites -> 1 game, 1 agent
	public void calculateResults(MarioGame game, MarioAgent agent, int maxTime) {
		this.calculateResults(new MarioGame[] {game}, new MarioAgent[] {agent}, maxTime);
	}

	public void calculateResults(MarioGame[] games, MarioAgent agent, int maxTime) {
		MarioAgent[] agents = new MarioAgent[games.length];
		for(int i=0; i<agents.length; i++) {
			agents[i] = agent;
		}
		this.calculateResults(games, agents, maxTime);
	}

	public void calculateResults(MarioGame game, MarioAgent[] agents, int maxTime) {
		MarioGame[] games = new MarioGame[agents.length];
		for(int i=0; i<games.length; i++) {
			games[i] = game;
		}
		this.calculateResults(games, agents, maxTime);
	}

	public void calculateResults(MarioGame[] games, MarioAgent[] agents, int maxTime) {
		MarioGame game = new MarioGame();
//		long fallKillsStart = System.nanoTime();
		int fallKills = game.runGame(new agents.doNothing.Agent(), this.toString(), maxTime).getKillsByFall();
//		long fallKillsEnd = System.nanoTime();
//		double fallEndTime = (double) (fallKillsEnd - fallKillsStart)/ 1_000_000_000;
//		System.out.println("fallKillsTotalTime: " + fallEndTime);
		MarioResult[] runs = this.runAlgorithms(games, agents, maxTime);
		this.calculateConstraints(runs);
		this._age += 1;
		this.calculateDimensions(runs[0], fallKills);
//		if (runs.length > 1) {
//			this.calculateFitnessEntropy(runs);
//		}
//		else {
//			if(this._constraints >= this._constraintProbability) {
//				this.calculateFitnessEntropy(runs[0]);
//			}
//			else {
//				this._fitness = 0;
//			}
//		}
		if(this._constraints >= this._constraintProbability) {
			if(runs.length > 1) {
				this.calculateFitnessEntropy(runs);
			} else {
				this.calculateFitnessEntropy(runs[0]);
			}
		}
		else {
			this._fitness = 0;
			this._matchMechs = 0;
			this._missingMechs = -1;
			this._extraMechs = -1;
		}
	}

	public ChromosomeL mutate(int min, int max) {
		ChromosomeL mutated = this.clone();
		int choice = mutated._rnd.nextInt(3);
//		System.out.println("choice: " + choice);
		//deleting a scene11
		if (choice == 0  && mutated._numOfScenes > min) {
//			System.out.println("Mutation - Deleting");
			int indexToDelete = mutated._rnd.nextInt(mutated._genes.length);
			int[] new_genes = new int[mutated._genes.length - 1]; 
			int[] new_subGenes = new int[mutated._subGenes.length-1];
			System.arraycopy(mutated._genes, 0, new_genes, 0, indexToDelete); 
			System.arraycopy(mutated._subGenes, 0, new_subGenes, 0, indexToDelete); 
	        // Copy the elements from index + 1 till end 
	        // from original array to the other array 
	        System.arraycopy(mutated._genes, indexToDelete + 1, 
	        				 new_genes, indexToDelete, 
	        				 mutated._genes.length - indexToDelete - 1); 
	        System.arraycopy(mutated._subGenes, indexToDelete + 1, 
	        			 	 new_subGenes, indexToDelete, 
	        			 	mutated._subGenes.length - indexToDelete - 1);
	        mutated._genes = new_genes;
	        mutated._subGenes = new_subGenes;
	        mutated._numOfScenes -= 1;
		}
		//adding a scene
		else if(choice == 1 && mutated._numOfScenes < max) {
//			System.out.println("Mutation - Adding");
			int sceneIndex = mutated._rnd.nextInt(mutated._library.getNumberOfScenes());
			int indexToAdd = mutated._rnd.nextInt(mutated._genes.length);
			int[] new_genes = new int[mutated._genes.length + 1]; 
			int[] new_subGenes = new int[mutated._subGenes.length + 1];
			for (int i = 0; i < new_genes.length; i++) {
				if (i < indexToAdd) {
					new_genes[i] = mutated._genes[i];
					new_subGenes[i] = mutated._subGenes[i];
				}
				else if (i == indexToAdd){
					new_genes[i] = sceneIndex;
					new_subGenes[i] = mutated._library.getSubSceneIndex(sceneIndex);
				}
				else {
					new_genes[i] = mutated._genes[i-1];
					new_subGenes[i] = mutated._subGenes[i-1];
				}
			}
			mutated._genes = new_genes;
			mutated._subGenes = new_subGenes;
			mutated._numOfScenes += 1;
		}
		//mutating a scene
		//Select which scene to mutate: higher chance to pick scene with more mechanics fired
		//From selected scene: replace, merge with either scene before or after split into 2
		else {
//			System.out.println("Mutation - Mutating");
			boolean oneMutateHappen = false;
			//which scene to mutate - higher chance to pick a scene with more mechanics fired
			ArrayList<int[]> temp = new ArrayList<int[]>();
			for(int i = 0; i < mutated._genes.length; i++) {
				int singleGene = mutated._genes[i];
				String mechanicTemp = mutated._library.getSceneMechanics(singleGene);
				int num_mechanics_fired = ((int) mechanicTemp.chars().filter(num -> num == '1').count()) + 1;
				for (int j = 0; j < num_mechanics_fired; j++) {
					int[] t = new int[] {singleGene, i};
					temp.add(t);
				}
			}
			//shuffle the biased arrayList
			Collections.shuffle(temp);
			int weightSelected = mutated._rnd.nextInt(temp.size());
			int indexToMutate = temp.get(weightSelected)[1];
			int sceneToMutate = temp.get(weightSelected)[0];
			//option 1: 
			int mutationChoice = this._rnd.nextInt(4);
			//option 1: split
			if (mutationChoice == 0) {
//				System.out.println("\tSplitting");
				String selectedSceneMechanics = mutated._library.getSceneMechanics(sceneToMutate);
				ArrayList<Integer> bitsTurnedOnIndex = new ArrayList<Integer>();
				for(int i = 0; i < selectedSceneMechanics.length(); i++){
					if(selectedSceneMechanics.charAt(i) == '1'){
						bitsTurnedOnIndex.add(i);
					}
				}
				//too few mechanics, dont split, try another mutation field
				if (bitsTurnedOnIndex.size() <  4) {
					mutationChoice = mutated._rnd.nextInt(3) + 1;
				}
				else {
					//split 50% -> go rng left and right
					int split = bitsTurnedOnIndex.size()/2;
					StringBuilder leftSceneToAdd = new StringBuilder("000000000000");
					int addedLeft = 0;
					StringBuilder rightSceneToAdd = new StringBuilder("000000000000");
					int addedRight = 0;
					for (int i = 0; i < bitsTurnedOnIndex.size(); i++) {
						int leftOrRight = mutated._rnd.nextInt(2);
						if ( (leftOrRight == 0 && addedLeft <= split) || addedRight == split) {
							leftSceneToAdd.setCharAt(bitsTurnedOnIndex.get(i),'1');
							addedLeft++;
						} else {
							rightSceneToAdd.setCharAt(bitsTurnedOnIndex.get(i),'1');
							addedRight++;
						}
					}
					//chance the split gave us something tht is not in lib, remove mechanics until it is
					int[] leftSceneAndSubSceneIndex = mutated._library.getSceneIndex(leftSceneToAdd.toString());
					int leftRemovingMechanics = 0;
					while (leftSceneAndSubSceneIndex[0] == -1 || leftSceneAndSubSceneIndex[1] == -1) {
						leftSceneToAdd.setCharAt(leftRemovingMechanics, '0');
						leftRemovingMechanics++;
						leftSceneAndSubSceneIndex = mutated._library.getSceneIndex(leftSceneToAdd.toString());
					}
					int[] rightSceneAndSubSceneIndex = mutated._library.getSceneIndex(rightSceneToAdd.toString());
					int rightRemovingMechanics = 0;
					while (rightSceneAndSubSceneIndex[0] == -1 || rightSceneAndSubSceneIndex[1] == -1) {
						rightSceneToAdd.setCharAt(rightRemovingMechanics, '0');
						rightRemovingMechanics++;
						rightSceneAndSubSceneIndex = mutated._library.getSceneIndex(rightSceneToAdd.toString());
					}
					//chance both sides lost all mechanics
					//try different mutation scene
					if (rightSceneToAdd.toString().compareTo("000000000000") == 0 && leftSceneToAdd.toString().compareTo("000000000000") == 0) {
						mutationChoice = this._rnd.nextInt(4) + 1;
					}
					else {
						//adding the scene
						int[] new_genes = new int[mutated._genes.length + 1]; 
						int[] new_subGenes = new int[mutated._subGenes.length + 1];
						for (int i = 0; i < new_genes.length; i++) {
							if (i < indexToMutate) {
								new_genes[i] = mutated._genes[i];
								new_subGenes[i] = mutated._subGenes[i];
							}
							else if (i == indexToMutate){
								new_genes[i] = leftSceneAndSubSceneIndex[0];
								new_subGenes[i] = leftSceneAndSubSceneIndex[1];
							}
							else if (i == indexToMutate+1) {
								new_genes[i] = rightSceneAndSubSceneIndex[0];
								new_subGenes[i] = rightSceneAndSubSceneIndex[1];
							}
							else {
								new_genes[i] = mutated._genes[i-1];
								new_subGenes[i] = mutated._subGenes[i-1];
							}
						}
						mutated._genes = new_genes;
						mutated._subGenes = new_subGenes;
						mutated._numOfScenes += 1;
						oneMutateHappen = true;
					}
				}
			}
			//option 2: merge left
			if (mutationChoice == 1 && indexToMutate > 1 && mutated._numOfScenes > min && oneMutateHappen != true) {
//				System.out.println("\tMerging Left");
				StringBuilder fusionLeft = new StringBuilder("000000000000");
				int indexLeft = indexToMutate - 1;
				int sceneLeft = mutated._genes[indexLeft];
				
				String selectedSceneMechanics = mutated._library.getSceneMechanics(sceneToMutate);
				String selectedLeftSceneMechanics = mutated._library.getSceneMechanics(sceneLeft);
				
				for(int i = 0; i < selectedSceneMechanics.length(); i++) {
					if(selectedSceneMechanics.charAt(i) == '1' || selectedLeftSceneMechanics.charAt(i) == '1') {
						fusionLeft.setCharAt(i, '1');
					}
				}
				
				//possible that the fusion scene is not in the scene library
				//if it isnt then scratch and perform the final option, mutate
				int[] fusedSceneAndSubSceneIndex = mutated._library.getSceneIndex(fusionLeft.toString());
				if (fusedSceneAndSubSceneIndex[0] == -1 || fusedSceneAndSubSceneIndex[1] == -1) {
//					System.out.println("\tCould not do the merge left");
					mutationChoice = 3;
				} else {
					int[] new_genes = new int[mutated._genes.length - 1]; 
					int[] new_subGenes = new int[mutated._subGenes.length-1];
					//copy elements from 0 to index
					//from original array to the other array
					System.arraycopy(mutated._genes, 0, new_genes, 0, indexToMutate); 
					System.arraycopy(mutated._subGenes, 0, new_subGenes, 0, indexToMutate); 
			        // Copy the elements from index + 1 till end 
			        // from original array to the other array 
			        System.arraycopy(mutated._genes, indexToMutate + 1, 
			        				 new_genes, indexToMutate, 
			        				 mutated._genes.length - indexToMutate - 1); 
			        System.arraycopy(mutated._subGenes, indexToMutate + 1, 
			        			 	 new_subGenes, indexToMutate, 
			        			 	mutated._subGenes.length - indexToMutate - 1);
			        new_genes[indexLeft] = fusedSceneAndSubSceneIndex[0];
			        new_subGenes[indexLeft] = fusedSceneAndSubSceneIndex[1];
			        mutated._genes = new_genes;
			        mutated._subGenes = new_subGenes;
			        mutated._numOfScenes -= 1;
			        oneMutateHappen = true;
				}
			} else {
				//want to merge left but couldn't, default to
				mutationChoice = 3;
			}
			//option 3: merge right
			if (mutationChoice == 2 && indexToMutate < mutated._genes.length-2 && mutated._numOfScenes < max && oneMutateHappen != true) {
//				System.out.println("\tMerging Right");
				StringBuilder fusionRight = new StringBuilder("000000000000");
				int indexRight = indexToMutate + 1;
				int sceneRight = mutated._genes[indexRight];
				
				String selectedSceneMechanics = mutated._library.getSceneMechanics(sceneToMutate);
				String selectedLeftSceneMechanics = mutated._library.getSceneMechanics(sceneRight);
				
				for(int i = 0; i < selectedSceneMechanics.length(); i++) {
					if(selectedSceneMechanics.charAt(i) == '1' || selectedLeftSceneMechanics.charAt(i) == '1') {
						fusionRight.setCharAt(i, '1');
					}
				}
				
				//possible that the fusion scene is not in the scene library
				//if it isnt then scratch and perform the final option, mutate
				int[] fusedSceneAndSubSceneIndex = mutated._library.getSceneIndex(fusionRight.toString());
				if (fusedSceneAndSubSceneIndex[0] == -1 || fusedSceneAndSubSceneIndex[1] == -1) {
//					System.out.println("\tCould not do the merge left");
					mutationChoice = 3;
				} else {
					int[] new_genes = new int[mutated._genes.length - 1]; 
					int[] new_subGenes = new int[mutated._subGenes.length-1];
					//copy elements from 0 to index
					//from original array to the other array
					System.arraycopy(mutated._genes, 0, new_genes, 0, indexToMutate); 
					System.arraycopy(mutated._subGenes, 0, new_subGenes, 0, indexToMutate); 
			        // Copy the elements from index + 1 till end 
			        // from original array to the other array 
			        System.arraycopy(mutated._genes, indexToMutate + 1, 
			        				 new_genes, indexToMutate, 
			        				 mutated._genes.length - indexToMutate - 1); 
			        System.arraycopy(mutated._subGenes, indexToMutate + 1, 
			        			 	 new_subGenes, indexToMutate, 
			        			 	mutated._subGenes.length - indexToMutate - 1);
			        new_genes[indexToMutate] = fusedSceneAndSubSceneIndex[0];
			        new_subGenes[indexToMutate] = fusedSceneAndSubSceneIndex[1];
			        mutated._genes = new_genes;
			        mutated._subGenes = new_subGenes;
			        mutated._numOfScenes -= 1;
			        oneMutateHappen = true;
				}
			} else {
				//want to merge right but couldn't, default to
				mutationChoice = 3;
			}
			//option 4: mutate - lower mechanics = higher chance, must have mechanics that fired for lower
			if (mutationChoice == 3 && oneMutateHappen != true) {
//				System.out.println("\tBiased Mutating");
				int[] weightedScene = mutated._library.getWeightedScene(sceneToMutate);
//				System.out.println("\t\tindexToMutate: " + indexToMutate);
//				System.out.println("\t\tmutated._genes: " + Arrays.toString(mutated._genes));
//				System.out.println("\t\tmutated._genes.length: " + mutated._genes.length);
				mutated._genes[indexToMutate] = weightedScene[0];
				mutated._subGenes[indexToMutate] = weightedScene[1];
			}
			
//			System.out.println("Mutation - Mutating");
//			int sceneIndex = mutated._rnd.nextInt(mutated._library.getNumberOfScenes());
//			int indexToMutate = mutated._rnd.nextInt(mutated._genes.length);
//			mutated._genes[indexToMutate] = sceneIndex;
//			mutated._subGenes[indexToMutate] = this._library.getSubSceneIndex(sceneIndex);
		}
//		System.out.println("Finished mutate function\n");
		return mutated;
	}

	public ChromosomeL crossover(ChromosomeL c) {
		ChromosomeL child;
		int choice = this._rnd.nextInt(2);
		
		//child is larger
		//get the index's from the smaller chromosome 
		//and copy into the larger
		if (choice == 0) {
			if (this._genes.length > c._genes.length) {
				child = this.clone();
				int index1 = c._rnd.nextInt(c._genes.length);
				int index2 = c._rnd.nextInt(c._genes.length);
				while(index2 == index1) {
					index2 = c._rnd.nextInt(c._genes.length);
				}
				if (index1 > index2) {
					int temp = index2;
					index2 = index1;
					index1 = temp;
				}
				//copy over from smaller to larger
				for (int i = index1; i < index2 + 1; i++) {
					child._genes[i] = c._genes[i];
					child._subGenes[i] = c._subGenes[i];
				}
				return child;
			}
			else {
				child = c.clone();
				//get the index's from the smaller chromosome
				int index1 = this._rnd.nextInt(this._genes.length);
				int index2 = this._rnd.nextInt(this._genes.length);
				while(index2 == index1) {
					index2 = this._rnd.nextInt(this._genes.length);
				}
				if (index1 > index2) {
					int temp = index2;
					index2 = index1;
					index1 = temp;
				}
				for (int i = index1; i < index2 + 1; i++) {
					child._genes[i] = this._genes[i];
					child._subGenes[i] = this._subGenes[i];
				}
				return child;
			}
		}
		//child is smaller
		else {
			if(this._genes.length < c._genes.length) {
				child = this.clone();
				//get the index's from the smaller chromosome
				int index1 = this._rnd.nextInt(this._genes.length);
				int index2 = this._rnd.nextInt(this._genes.length);
				while(index2 == index1) {
					index2 = this._rnd.nextInt(this._genes.length);
				}
				if (index1 > index2) {
					int temp = index2;
					index2 = index1;
					index1 = temp;
				}
				for (int i = index1; i < index2 + 1; i++) {
					child._genes[i] = c._genes[i];
					child._subGenes[i] = c._subGenes[i];
				}
				return child;
			}
			else {
				child = c.clone();
				//get the index's from the smaller chromosome
				int index1 = c._rnd.nextInt(c._genes.length);
				int index2 = c._rnd.nextInt(c._genes.length);
				while(index2 == index1) {
					index2 = c._rnd.nextInt(c._genes.length);
				}
				if (index1 > index2) {
					int temp = index2;
					index2 = index1;
					index1 = temp;
				}
				//copy over from smaller to larger
				for (int i = index1; i < index2 + 1; i++) {
					child._genes[i] = this._genes[i];
					child._subGenes[i] = this._subGenes[i];
				}
				return child;
			}
		}
		
	}

	public String toString() {
		String level = "";
		int height = this._library.getScene(this._genes[0], this._subGenes[0]).length;
		for (int i = 0; i < height; i++) {
			String appendingChar = "-";
			if (i == height - 2 || i == height - 3) {
				appendingChar = "X";
			}
			//padding
			//			for (int k = 0; k < this._appendingSize; k++) {
			//				level += appendingChar;
			//			}
			//level from the scenes
			for (int j = 0; j < this._genes.length; j++) {
				level += this._library.getScene(this._genes[j], this._subGenes[j])[i];
				//padding between the scenes
				if(i != height - 1) {
					for (int k = 0; k < this._appendingSize; k++) {
						level += appendingChar;
					}
				}
			}
			//padding
			//			for (int k = 0; k < this._appendingSize; k++) {
			//				level += appendingChar;
			//			}
			level += "\n";
		}

		return level;
	}

	@Override
	public int compareTo(ChromosomeL o) {
		// TODO Auto-generated method stub
		if (this._constraints == this._constraintProbability) {
			return (int) Math.signum(this._fitness - o._fitness);
		}
		return (int) Math.signum(this._constraints - o._constraints);
	}

}
