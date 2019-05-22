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
	protected ArrayList<String> _populationMechanics;
	protected int _appendingSize;
	protected double _constraints;
	protected double _fitness;
	protected int[] _dimensions;
	protected ScenesLibrary _library;
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
		this._populationMechanics = new ArrayList<String>();
		this._appendingSize = appendingSize;
		this._constraints = 0;
		this._fitness = 0;
		this._dimensions = null;
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

	public ChromosomeL clone() {
		ChromosomeL chromosome = new ChromosomeL(this._rnd, this._library, this._numOfScenes, this._appendingSize, this._playthroughMechanics, this._variableNumOfMechInScene);
		for(int i=0; i < this._genes.length; i++) {
			chromosome._genes[i] = this._genes[i];
		}
		return chromosome;
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
			avg_mechanics = (int)(this._numMechanicsInPlaythrough/this._numOfScenes);
		}
		return avg_mechanics;
	}

	private void createGeneRandomly(int index, int avg_mechanics) {
		int sceneIndex = this._rnd.nextInt(this._library.getNumberOfScenes());
		this._genes[index] = sceneIndex;
		String[] arrayedMechanicsToUse = this._library.getSceneMechanics(sceneIndex);
		
		int num_mechanics = 0;
		for(int i = 0; i < arrayedMechanicsToUse.length; i++) {
			if(arrayedMechanicsToUse[i].equals("1")) {
				num_mechanics += 1;
			}
		}
		while(num_mechanics != avg_mechanics) {
			sceneIndex = this._rnd.nextInt(this._library.getNumberOfScenes());
			this._genes[index] = sceneIndex;
			arrayedMechanicsToUse = this._library.getSceneMechanics(sceneIndex);
			num_mechanics = 0;
			for(int i = 0; i < arrayedMechanicsToUse.length; i++) {
				if(arrayedMechanicsToUse[i].equals("1")) {
					num_mechanics += 1;
				}
			}
		}
		
		String temp2 = Arrays.toString(arrayedMechanicsToUse);
	    temp2 = temp2.substring(1, temp2.length()-1).replace(",", " ").replace(" ", "");
	    this._populationMechanics.add(temp2);
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
		if(!this._variableNumOfMechInScene) {
			avg_mechanics = 3;
		}
		this.createGeneRandomly(i, avg_mechanics);
	}

	public void smartInitialization() {
		//create the gene in a smart way
		//guarantee the mechanics will occur in that order
		int i;
		for(i = 0; i < this._genes.length; i++) {
			String[] tempStringArray = this._playthroughMechanics[i].split(",");
			int tempIndex = this._library.getSceneIndex(tempStringArray);
			this._genes[i] = tempIndex;
			
			String temp = Arrays.toString(tempStringArray);
		    temp = temp.substring(1, temp.length()-1).replace(",", " ").replace(" ", "");
		    this._populationMechanics.add(temp);
		}
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
			results[i] = games[i].runGame(agents[i], this.toString(), maxTime);
		}
		return results;
	}

	private void calculateConstraints(MarioResult[] runs) {
		double tempConst = runs[0].getCompletionPercentage();
		if(runs.length > 1) {
			tempConst = runs[1].getCompletionPercentage() - tempConst;
			if(runs[1].getGameStatus() == GameStatus.WIN && runs[2].getGameStatus() == GameStatus.LOSE) {
				tempConst = 1;
			}
		}
		if(this._age > 0) {
			this._constraints = Math.min(this._constraints, tempConst);
		}
		else {
			this._constraints = tempConst;
		}
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
		ArrayList<String> agentMechanics = new ArrayList<String>();
		for(int i = 0; i < mechanicsToReduceArrayExcess.length; i++) {
			String mechInfo = mechanicsToReduceArrayExcess[i];
			String starter = "Action\":\"";
			while(mechInfo.indexOf(starter) != -1) {
				int index = mechInfo.indexOf(starter);
				String actionExcess = mechInfo.substring(index+starter.length());
				int actionStopIndex = actionExcess.indexOf("\"");
				String action = actionExcess.substring(0, actionStopIndex);
				agentMechanics.add(action);	
				mechInfo = actionExcess.substring(actionStopIndex);
			}
		}	
		return agentMechanics;
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

	//since map elites uses 1 game with 1 agent, we only need the first result
	public void calculateFitnessEntropy(MarioResult run) {
		double fitnessScore = 0;

		//reduce the lists to just actions
		String[] agentMechanicsArrayExcess = EventLogger.getPlayedMechanics(run.getGameEvents());
		ArrayList<String> agentActions = this.reduceMechanicsToActions(agentMechanicsArrayExcess);
//		System.out.println("agent actions\n" + Arrays.toString(agentActions.toArray()));
		ArrayList<String> playthroughActions = this.mapPlaythroughMechanics();
//		System.out.println("playthough actions\n" + Arrays.toString(playthroughActions.toArray()));
		//go through agentActions and compareActions
		int playthroughPointer = 0;
		int agentPointer = 0;
		while(playthroughPointer < playthroughActions.size() && agentPointer < agentActions.size()) {
			if(playthroughActions.get(playthroughPointer).equalsIgnoreCase(agentActions.get(agentPointer))) {
				playthroughPointer++;
			}
			agentPointer++;
		}

		this._fitness = 100.0;
		if(playthroughPointer  < playthroughActions.size()) {
			double numberOfActionsLeft = playthroughActions.size() - playthroughPointer;
			double penalty = numberOfActionsLeft * 2.5;
			this._fitness -= penalty;
		}
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
		if(this._constraints >= 1) {
			this.calculateFitnessEntropy(runs[0]);
		}
		else {
			this._fitness = 0;
		}
	}

	public ChromosomeL mutate() {
		ChromosomeL mutated = this.clone();
		mutated._genes[mutated._rnd.nextInt(mutated._genes.length)] = mutated._rnd
				.nextInt(mutated._library.getNumberOfScenes());
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
		}
		return child;
	}

	public String toString() {
		String level = "";
		int height = this._library.getScene(this._genes[0]).length;
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
				level += this._library.getScene(this._genes[j])[i];
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
		if (this._constraints == 1) {
			return (int) Math.signum(this._fitness - o._fitness);
		}
		return (int) Math.signum(this._constraints - o._constraints);
	}

}
