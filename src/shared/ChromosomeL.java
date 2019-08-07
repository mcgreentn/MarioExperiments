package shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

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
	protected int[] _dimensions;
	protected ScenesLibrary _library;
	private double _constraintProbability;
	private int _age;
	private int _numOfScenes;
	private int _numMechanicsInPlaythrough;
	private boolean _variableNumOfMechInScene;
	private String[] _listOfPossibleMechanics;
	private String[] _playthroughMechanics;

	//chromosome is a full level
	//a gene is a single scene
	public ChromosomeL(Random rnd, ScenesLibrary lib, int numOfScenes, int appendingSize, String[] playthroughMechanics, boolean variableNumOfMechInScene) {
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
		for(int i = 0; i < this._playthroughMechanics.length; i++) {
			this._numMechanicsInPlaythrough += this._playthroughMechanics[i].chars().filter(num -> num == '1').count();
		}
		
		this._listOfPossibleMechanics = new String[] {"Mario Jumps", "Low Jump", "High Jump", "Short Jump", "Long Jump", "Stomp Kill", "Shell Kill", "Fall Kill", "Mario Mode", "Coins Collected", "Bumping Brick Block", "Bumping Question Block"};
	}

	public void stringInitialize(String[] level) {
		String[] parts = level[0].split(",");
		this._age = Integer.parseInt(parts[0]);
		for (int i = 0; i < this._genes.length; i++) {
			this._genes[i] = Integer.parseInt(parts[i + 1]);
		}
		String[] subParts = level[1].split(",");
		for(int i = 0; i < this._subGenes.length; i++) {
			this._subGenes[i] = Integer.parseInt(subParts[i]);
		}
	}
	
	public ChromosomeL clone() {
		ChromosomeL chromosome = new ChromosomeL(this._rnd, this._library, this._numOfScenes, this._appendingSize, this._playthroughMechanics, this._variableNumOfMechInScene);
		for(int i=0; i < this._genes.length; i++) {
			chromosome._genes[i] = this._genes[i];
			chromosome._subGenes[i] = this._subGenes[i];
		}
		return chromosome;
	}

	public double getConstraintProbability() {
		return this._constraintProbability;
	}
	
	public int getAge() {
		return this._age;
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

	public void advanceAge() {
		this._age += 1; 
	}

	private int calcAvgMechanics() {
		int avg_mechanics = 3;
		if(this._variableNumOfMechInScene) {
			avg_mechanics = (int) Math.ceil((double)this._numMechanicsInPlaythrough/this._numOfScenes);
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
			
			int[] tempIndex = this._library.getSceneIndex(mechanicsToUseString);
			this._genes[index] = tempIndex[0];
			this._subGenes[index] = tempIndex[1];
			
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
		avg_mechanics = this._numMechanicsInPlaythrough - avg_mechanics * (this._genes.length-1);
		if(!this._variableNumOfMechInScene || avg_mechanics < 0) {
			avg_mechanics = 3;
		}
		this.createGeneRandomly(i, avg_mechanics);
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
		this._dimensions = new int[parts.length - 3];
		for(int i=3; i<parts.length; i++) {
			this._dimensions[i-3] = Integer.parseInt(parts[i]);
		}
	}

	protected MarioResult[] runAlgorithms(MarioGame[] games, MarioAgent[] agents, int maxTime) {
		MarioResult[] results = new MarioResult[agents.length];
		for(int i=0; i<agents.length; i++) {
//			System.out.println("\t Start playing game");
			results[i] = games[i].runGame(agents[i], this.toString(), maxTime);
//			System.out.println("\t Finish playing game");
		}
		return results;
	}

	private void calculateConstraints(MarioResult[] runs) {
		double tempConst = runs[0].getCompletionPercentage();
		if(runs.length > 1) {
			/*
			tempConst = runs[1].getCompletionPercentage() - tempConst;
			if(runs[1].getGameStatus() == GameStatus.WIN && runs[2].getGameStatus() == GameStatus.LOSE) {
				tempConst = 1;
			}*/
			for(int i = 1; i < runs.length; i++) {
				tempConst += runs[i].getCompletionPercentage();	
			}
			tempConst /= runs.length;
		}
//		if(this._age > 0) {
//			this._constraints = Math.min(this._constraints, tempConst);
//		}
//		else {
			this._constraints = tempConst;
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

	public double calculateFitness(MarioResult run)
	{
		double fitnessScore = 100;

		//reduce the lists to just actions
		String[] agentMechanicsArrayExcess = EventLogger.getPlayedMechanics(run.getGameEvents());		
		ArrayList<String> agentActions = this.reduceMechanicsToActions(agentMechanicsArrayExcess);
		ArrayList<String> playthroughActions = new ArrayList<String>(Arrays.asList(this._playthroughMechanics));
		//go through agentActions and compareActions
		int mechanicsMissed = 0;
		int agentMechanicPointer = 0; 
		int playthroughMechanicPointer = 0;
		for(; playthroughMechanicPointer < playthroughActions.size(); playthroughMechanicPointer++) {
			String mechanicToCheck = playthroughActions.get(playthroughMechanicPointer);
			ArrayList<String> subArray = new ArrayList<String>(agentActions.subList(agentMechanicPointer, agentActions.size()));
			int mechanicIndex = subArray.indexOf(mechanicToCheck);
			if(mechanicIndex == -1) {
				mechanicsMissed += 1;
			}
			else {
				agentMechanicPointer += mechanicIndex + 1;
			}
			if(agentMechanicPointer >= agentActions.size()) {
				playthroughMechanicPointer++;
				break;
			}
		}
		fitnessScore = 100.0;
		//lose points for the mechanics it missed
		fitnessScore -= (mechanicsMissed * 5);

		//lose points for mechanics left
		if(playthroughMechanicPointer  < playthroughActions.size()) {
			double numberOfActionsLeft = playthroughActions.size() - playthroughMechanicPointer;
			double penalty = numberOfActionsLeft * 1.25;
			fitnessScore -= penalty;
		}
		return fitnessScore;
	}
	public void calculateFitnessEntropy(MarioResult[] runs) {
		double score = this.calculateFitness(runs[0]);
		for(int i = 1; i < runs.length; i++) {
			double temp = this.calculateFitness(runs[i]);
			if (temp > score) {
				score = temp;
			}
		}
		this._fitness = score;
	}
	
	//since map elites uses 1 game with 1 agent, we only need the first result
	public void calculateFitnessEntropy(MarioResult run) {
		this._fitness = this.calculateFitness(run);
		
//		int playthroughPointer = 0;
//		int agentPointer = 0;
//		while(playthroughPointer < playthroughActions.size() && agentPointer < agentActions.size()) {
//			if(playthroughActions.get(playthroughPointer).equalsIgnoreCase(agentActions.get(agentPointer))) {
//				playthroughPointer++;
//			}
//			agentPointer++;
//		}
//
//		this._fitness = 100.0;
//		if(playthroughPointer  < playthroughActions.size()) {
//			double numberOfActionsLeft = playthroughActions.size() - playthroughPointer;
//			double penalty = numberOfActionsLeft * 2.5;
//			this._fitness -= penalty;
//		}
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
		int fallKills = game.runGame(new agents.doNothing.Agent(), this.toString(), maxTime).getKillsByFall();
		MarioResult[] runs = this.runAlgorithms(games, agents, maxTime);
		this.calculateConstraints(runs);
		this._age += 1;
		this.calculateDimensions(runs[0], fallKills);
		if(this._constraints >= this._constraintProbability) {
			if(runs.length > 1) {
//				MarioResult tosend = runs[0];
//				for(int i = 1; i < runs.length; i++) {
//					if (runs[i].getCompletionPercentage() > tosend.getCompletionPercentage()) {
//						tosend = runs[i];
//					}
//				}
//				this.calculateFitnessEntropy(tosend);
				this.calculateFitnessEntropy(runs);
			} else {
				this.calculateFitnessEntropy(runs[0]);
			}
		}
		else {
			this._fitness = 0;
		}
	}

	public ChromosomeL mutate() {
		ChromosomeL mutated = this.clone();
		int sceneIndex = mutated._rnd.nextInt(mutated._library.getNumberOfScenes());
		String mechanicsToUseString = mutated._library.getSceneMechanics(sceneIndex);
		
		int[] tempIndex = mutated._library.getSceneIndex(mechanicsToUseString);
		int indexToMutate = mutated._rnd.nextInt(mutated._genes.length);
		mutated._genes[indexToMutate] = tempIndex[0];
		mutated._subGenes[indexToMutate] = tempIndex[1];

		return mutated;
	}

	public ChromosomeL crossover(ChromosomeL c) {
		ChromosomeL child = this.clone();
		int index1 = child._rnd.nextInt(child._genes.length);
		int index2 = child._rnd.nextInt(child._genes.length);
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

	public String toString() {
		String level = "";
		int height = this._library.getScene(this._genes[0], this._subGenes[0]).length;
		for (int i = 0; i < height; i++) {
			String appendingChar = "-";
			if (i == height - 1 || i == height - 2) {
				appendingChar = "X";
			}
			//padding
			//			for (int k = 0; k < this._appendingSize; k++) {
			//				level += appendingChar;
			//			}
			//level
			for (int j = 0; j < this._genes.length; j++) {
				level += this._library.getScene(this._genes[j], this._subGenes[j])[i];
			}
			//			//padding
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
