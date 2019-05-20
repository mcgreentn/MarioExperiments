package shared;

import java.util.ArrayList;
import java.util.Random;

import engine.core.EventLogger;
import engine.core.MarioAgent;
import engine.core.MarioGame;
import engine.core.MarioResult;
import engine.helper.GameStatus;

public class ChromosomeL implements Comparable<ChromosomeL>{
	protected Random _rnd;
	protected int[] _genes;
	protected int _appendingSize;
	protected double _constraints;
	protected double _fitness;
	protected int[] _dimensions;
	protected ScenesLibrary _library;
	private int _age;
	private int _numOfScenes;
	private boolean _variableNumOfMechInScene;
	private String[] _listOfPossibleMechanics;
	private String[] _playthroughMechanics;
	
	//chromosome is a full level
	//a gene is a single scene
	public ChromosomeL(Random rnd, ScenesLibrary lib, int numOfScenes, int appendingSize, String[] playthroughMechanics, boolean variableNumOfMechInScene) {
		this._rnd = rnd;
		this._library = lib;
		this._genes = new int[numOfScenes];
		this._appendingSize = appendingSize;
		this._constraints = 0;
		this._fitness = 0;
		this._dimensions = null;
		this._age = 0;
		this._numOfScenes = numOfScenes;
		this._variableNumOfMechInScene = variableNumOfMechInScene;
		this._playthroughMechanics = playthroughMechanics;
		
		this._listOfPossibleMechanics = new String[] {"Low Jump", "High Jump", "Long Jump", "Short Jump", "Enemy Stomp", "Enemy Shell Kill", "Enemy Shell Kill", "Enemy Fall Kill", "Powerup Collected From Block", "Coin Collected From Floor", "Coin Collected From Block", "Block Breaking"};
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
	
	public void advanceAge() {
		this._age += 1; 
	}
	
	private int calcAvgMechanics() {
		int avg_mechanics = 3;
		if(this._variableNumOfMechInScene) {
			avg_mechanics = (int) this._playthroughMechanics.length/this._numOfScenes;
		}
		return avg_mechanics;
	}
	
	private void createGeneRandomly(int index, int avg_mechanics) {
		ArrayList<String> mechanicsToUse = new ArrayList<String>();
		for(int j = 0; j < avg_mechanics; j++) {
			mechanicsToUse.add( this._listOfPossibleMechanics[ this._rnd.nextInt( this._listOfPossibleMechanics.length ) ] );
		}
		String[] arrayedMechanicsToUse = mechanicsToUse.toArray(new String[0]);
		//ask the library for a scene with these mechanics
		this._genes[index] = this._library.getScene(arrayedMechanicsToUse);
	}
	public void randomInitialization() {
		int avg_mechanics = calcAvgMechanics();
		//create the genes
		int i;
		for(i = 0; i < this._genes.length-1; i++) {
			this.createGeneRandomly(i, avg_mechanics);
		}
		//the last gene will have the remaining scenes (in case of an uneven split 
		avg_mechanics = this._playthroughMechanics.length - avg_mechanics * (this._genes.length-1);
		this.createGeneRandomly(i, avg_mechanics);
	}
	
	private int createGeneSmartly(int index, int avg_mechanics, int mechanicCounter) {
		ArrayList<String> mechanicsToUse = new ArrayList<String>();
		while(mechanicCounter%avg_mechanics != 0) {
			mechanicsToUse.add(this._playthroughMechanics[mechanicCounter]);
			mechanicCounter += 1;
		}
		String[] arrayedMechanicsToUse = mechanicsToUse.toArray(new String[0]);
		//ask the library for a scene with these mechanics
		this._genes[index] = this._library.getScene(arrayedMechanicsToUse);
		return mechanicCounter;
	}
	public void smartInitialization() {
		int avg_mechanics = calcAvgMechanics();
		//create the gene in a smart way
		//guarantee the mechanics will occur in that order
		//iterate through this._playthroughMechanics, select avg_mechanics amount and ask a scene with those guys
		int mechanicCounter = 0;
		int i;
		for(i = 0; i < this._genes.length-1; i++) {
			mechanicCounter = this.createGeneSmartly(i, avg_mechanics, mechanicCounter);
		}
		avg_mechanics = this._playthroughMechanics.length - avg_mechanics * (this._genes.length-1);
		this.createGeneSmartly(i, avg_mechanics, mechanicCounter);
	}
	
	public void childEvaluationInitlization(String values) {
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
			while(mechInfo.indexOf("Action") != -1) {
				int index = mechInfo.indexOf("Action: ");
				String actionExcess = mechInfo.substring(index+8);
				int actionStopIndex = actionExcess.indexOf("\"");
				String action = actionExcess.substring(0, actionStopIndex);
				agentMechanics.add(action);	
				mechInfo = actionExcess.substring(actionStopIndex);
			}
		}
		
		return agentMechanics;
	}
	
	//since map elites uses 1 game with 1 agent, we only need the first result
	public void calculateFitnessEntropy(MarioResult run) {
		double fitnessScore = 0;
		
		//reduce the lists to just actions
		String[] agentMechanicsArrayExcess = EventLogger.getPlayedMechanics(run.getGameEvents());
		
		ArrayList<String> agentActions = this.reduceMechanicsToActions(agentMechanicsArrayExcess);
		ArrayList<String> playthroughActions = this.reduceMechanicsToActions(this._playthroughMechanics); 
		
		//go through agentActions and compareActions
		int playthroughPointer = 0;
		int agentPointer = 0;
		while(playthroughPointer < playthroughActions.size() && agentPointer >= agentActions.size()) {
			if(agentActions.get(playthroughPointer) == agentActions.get(agentPointer)) {
				playthroughPointer++;
			}
			agentPointer++;
		}
		
		this._fitness = 100.0;
		if(playthroughPointer  < playthroughActions.size()) {
			double numberOfActionsLeft = playthroughActions.size() - playthroughPointer;
			double penalty = numberOfActionsLeft * 1.8;
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
		MarioResult[] runs = this.runAlgorithms(games, agents, maxTime);
		this.calculateConstraints(runs);
		this._age += 1;
		MarioGame game = new MarioGame();
		int fallKills = game.runGame(new agents.doNothing.Agent(), this.toString(), maxTime).getKillsByFall();
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
		int height = this._library.getScene(this._genes[0]).length();
		for (int i = 0; i < height; i++) {
			String appendingChar = "-";
			if (i == height - 1 || i == height - 2) {
				appendingChar = "X";
			}
			for (int k = 0; k < this._appendingSize; k++) {
				level += appendingChar;
			}
			for (int j = 0; j < this._genes.length; j++) {
				level += this._library.getScene(this._genes[j]).charAt(i);
			}
			for (int k = 0; k < this._appendingSize; k++) {
				level += appendingChar;
			}
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
