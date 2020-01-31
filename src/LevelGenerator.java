import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import engine.core.MarioAgent;
import engine.core.MarioGame;
import fi2pop.FI2PopGeneticAlgorithmL;
import shared.ChromosomeL;
import shared.ScenesLibrary;
import shared.evaluator.ParentEvaluator;

public class LevelGenerator {
	private static ChromosomeL[] _population;
	private static int _populationSize;
	private static int _minChromosomeLength;
	private static int _maxChromosomeLength;
	private static int _appendingSize;
	private double _crossover;
	private double _mutation;
	private int _elitism;
	private static Random _rnd;
	private static ScenesLibrary _lib;
	private static String[] _playthroughMechanics;
	private static boolean _variableNumOfMechInScene;
	private static HashMap<String, String> _parameters;
	
	private static int num_levels_generated = 0;
	
	public static ScenesLibrary fillLibrary(ScenesLibrary lib, String scenesFolder) throws Exception {
		File directory = new File(scenesFolder);
		File[] mechFolders = directory.listFiles();
		Arrays.sort(mechFolders);
		for (File folder : mechFolders) {
			//loop through each folder for the mechanics
			String sceneMechanics = folder.getName().replace(",", "");
			File[] files = folder.listFiles();
			Arrays.sort(files);
			int i = 0;
			for(File f : files) {
				String[] lines = Files.readAllLines(f.toPath()).toArray(new String[0]);
				String fitness = lines[0].split("fitness: ")[1];
				String[] scene = Arrays.copyOfRange(lines, 1, lines.length);

				lib.addScene(sceneMechanics, fitness, scene);
			}
		}
		return lib;
	}
	
	private static HashMap<String, String> readParameters(String filename) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get("", filename));
		HashMap<String, String> parameters = new HashMap<String, String>();
		for(int i=0; i<lines.size(); i++) {
			if(lines.get(i).trim().length() == 0) {
				continue;
			}
			String[] parts = lines.get(i).split("=");
			parameters.put(parts[0].trim(), parts[1].trim());
		}
		return parameters;
	}

	private static String[] fillPlaythroughMechanics(String playthroughMechanicsFile) {
		String[] toReturnStringArray = new String[0];
		ArrayList<String> to_return = new ArrayList<String>();
		JSONParser jsonParser = new JSONParser();
		try (FileReader reader = new FileReader(playthroughMechanicsFile))
		{
			//Read JSON file and convert to ordered array list
			JSONObject obj = (JSONObject) jsonParser.parse(reader);
			Set<String> s = obj.keySet();
			HashSet<Integer> hs = new HashSet<Integer>();
			Iterator<String> its = s.iterator();
			while(its.hasNext()) {
				Integer key = Integer.parseInt(its.next());
				hs.add(key);
			}
			SortedSet<Integer> keys = new TreeSet<>(hs);
			ArrayList<String> mechanicsArrayList = new ArrayList<String>();
			Iterator<Integer> it = keys.iterator();
			while (it.hasNext()) {
				String key = "" + it.next();
				String value = obj.get(key).toString();
				mechanicsArrayList.add(value);
			}

			//loop through array list and make the string mechanic of 0s and 1s
			for(int i = 0; i < mechanicsArrayList.size(); i++) {
				StringBuilder mechanicString = new StringBuilder("000000000000");
				ArrayList<String> agentMechanics = new ArrayList<String>();
				String mechInfo = mechanicsArrayList.get(i);
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
					//					case "Mario Jumps":
					//						mechanicString.setCharAt(0, '1');
					//						break;
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		toReturnStringArray = to_return.toArray(new String[0]);
		return toReturnStringArray;
	}

	private static void runExperiment(ChromosomeL c) {
		int runFixedRuns = 1;
		MarioAgent[] agents = new MarioAgent[runFixedRuns];
		for(int i = 0; i < agents.length; i++) {
			agents[i] = new agents.robinBaumgarten.Agent();
		}
		c.calculateResults(new MarioGame(), agents, 20);		
	}	
	
	public static void smartChomosomesInitialize() {
		_population = new ChromosomeL[_populationSize];
		for(int i=0; i<_population.length; i++) {
			int numOfScenes = _rnd.nextInt((_maxChromosomeLength - _minChromosomeLength) + 1) + _minChromosomeLength;
			_population[i] = new ChromosomeL(_rnd, _lib, numOfScenes, _appendingSize, _playthroughMechanics, _variableNumOfMechInScene,_parameters);
			System.out.println("Making Greedy playable level: " + i);
			boolean winnable = false;
			while(!winnable) {
				System.out.println("\tTrial: " + num_levels_generated);
				_population[i].smartInitialization();
				num_levels_generated += 1;
				runExperiment(_population[i]);
				if(_population[i].getConstraints() >=  1.0) {
					System.out.println("Found a winnable level at least once");
					winnable = true; 
				}
			}
			_population[i].setAge(0);
		}
		//Save the number of tries it took to generate a level
		System.out.println("Stats - num_levels_generated: " + num_levels_generated);
	}
	
	public static void randomGuassChromosomesInitialize() {
		_population = new ChromosomeL[_populationSize];
		//popsize is 20 for 20 levels
		
		for(int i=0; i<_population.length; i++) {
			int numOfScenes = _rnd.nextInt((_maxChromosomeLength - _minChromosomeLength) + 1) + _minChromosomeLength;
			_population[i] = new ChromosomeL(_rnd, _lib, numOfScenes, _appendingSize, _playthroughMechanics, _variableNumOfMechInScene,_parameters);
			
			System.out.println("Making Guass playable level: " + i);
			boolean winnable = false;
			while(!winnable) {
				System.out.println("\tTrial: " + num_levels_generated);
				_population[i].randomGuassInitialization();
				num_levels_generated += 1;
				runExperiment(_population[i]);
				if(_population[i].getConstraints() >=  1.0) {
					System.out.println("Found a winnable level at least once");
					winnable = true; 
				}
			}
			_population[i].setAge(0);
		}
		//Save the number of tries it took to generate a level
		System.out.println("Stats - num_levels_generated: " + num_levels_generated);
	}
	
	public static void completeRandomInitialization() {
		_population = new ChromosomeL[_populationSize];
		//popsize is 20 for 20 levels
		for(int i=0; i<_population.length; i++) {
			int numOfScenes = _rnd.nextInt((_maxChromosomeLength - _minChromosomeLength) + 1) + _minChromosomeLength;
			_population[i] = new ChromosomeL(_rnd, _lib, numOfScenes, _appendingSize, _playthroughMechanics, _variableNumOfMechInScene,_parameters);;
			
			System.out.println("Making Random playable level: " + i);
			boolean winnable = false;
			while(!winnable) {
				System.out.println("\tTrial: " + num_levels_generated);
				_population[i].completeRandomInitialization();
				num_levels_generated += 1;
				runExperiment(_population[i]);
				if(_population[i].getConstraints() >=  1.0) {
					System.out.println("Found a winnable level at least once");
					winnable = true; 
				}
			}
			_population[i].setAge(0);
		}
		//Save the number of tries it took to generate a level
		System.out.println("Stats - num_levels_generated: " + num_levels_generated);
	}
	
	private static void appendInfo(String path, int iteration, FI2PopGeneticAlgorithmL gen) throws FileNotFoundException {
		double[] stats = gen.getStatistics();
		PrintWriter pw = new PrintWriter(new FileOutputStream(new File(path + "result.txt"), true));
		String result = "";
		for(double v:stats) {
			result += v + ", ";
		}
		result = result.substring(0, result.length() - 1);
		pw.println("Batch number " + iteration + ": " + result);
		pw.close();
		//save the lengths for each generation in a new file
		HashMap<Integer, Integer> currIterSceneLengths = gen.getInterationsSceneLengths();
		String toPrint = "{";
		ArrayList<Integer> keys = new ArrayList<>(currIterSceneLengths.keySet());
		Collections.sort(keys);
		for (int i = 0; i < keys.size(); i++) {
			toPrint += keys.get(i) + ":" + currIterSceneLengths.get(keys.get(i));
			if (i != keys.size()-1) {
				toPrint += ",";
			} else {
				toPrint += "}";
			}
		}
		PrintWriter pw_scenes = new PrintWriter(new FileOutputStream(new File(path + "allSceneLengthsDistribution.txt"), true));
		pw_scenes.println("{ " + iteration + " : " + toPrint + " }");
		pw_scenes.close();
		
		PrintWriter pw_levels_made = new PrintWriter(new FileOutputStream(new File(path + "NumLevelsGenerated.txt"), true));
		pw_levels_made.println("{ Num Levels Generated : " + num_levels_generated + " }");
		pw_levels_made.close();
		
		System.out.println("appended Info");
		
	}
	
	private static void deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		directoryToBeDeleted.delete();
	}
	
	public static void main(String[] args) {
		System.out.println("STARTING");
		//get the parameters
		HashMap<String, String> parameters = null;
		try {
			parameters = readParameters("FI2PopParametersL.txt");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
//		Random rnd = new Random(Integer.parseInt(parameters.get("seed")));
		Random rnd = new Random();
		//create Scene library
		ScenesLibrary lib = new ScenesLibrary(rnd);

		try {
			fillLibrary(lib, parameters.get("scenesFolder"));
		} catch(Exception e) {
			e.printStackTrace();
		}
		String playthroughMechanicsFolder = parameters.get("playthroughMechanicsFolder");
		String playthroughMechanicsLevelName = parameters.get("playthroughMechanicsLevelName"); 
		String[] playthroughMechanics = fillPlaythroughMechanics(playthroughMechanicsFolder+playthroughMechanicsLevelName);
		
		_parameters = parameters;
		_lib = lib;
		_populationSize = Integer.parseInt(_parameters.get("populationSize"));
		_minChromosomeLength = Integer.parseInt(_parameters.get("minChromosomeLength"));
		_maxChromosomeLength = Integer.parseInt(_parameters.get("maxChromosomeLength"));
		_appendingSize = Integer.parseInt(_parameters.get("appendingSize"));
		_rnd = rnd;
		_population = new ChromosomeL[0];
		_playthroughMechanics = playthroughMechanics;
		_variableNumOfMechInScene = Boolean.parseBoolean(_parameters.get("variableNumberOfMechanics"));

		FI2PopGeneticAlgorithmL gen = new FI2PopGeneticAlgorithmL(lib, rnd, playthroughMechanics, parameters);
		ParentEvaluator parent = new ParentEvaluator(parameters.get("inputFolder"), parameters.get("outputFolder"));
		
		String typeOfInitialization = parameters.get("initialization");
		if(typeOfInitialization.equals("smart")) {
			smartChomosomesInitialize();
			gen.setPopulation(_population);
		}
		else if (typeOfInitialization.contentEquals("totalrandom")) {
			completeRandomInitialization();
			gen.setPopulation(_population);
		}
		ChromosomeL[] chromosomes = gen.getPopulation();
		int iteration = 0;
		int maxIterations = Integer.parseInt(parameters.get("maxIterations"));
		while(true) {
			try {
				String[] levels = new String[chromosomes.length];
				for(int i=0; i<chromosomes.length; i++) {
					levels[i] = chromosomes[i].getAge() + "," + chromosomes[i].getNumberOfScenes() + "\n";
					levels[i] += chromosomes[i].getGenes() + "\n" + chromosomes[i].toString() + "\n";
				}
				System.out.println("\tlength of levels " + levels.length);
				parent.writeChromosomes(levels);
				parent.clearOutputFiles(chromosomes.length);
				System.out.println("\tWaiting for children to finish");
				while(!parent.checkChromosomes(chromosomes.length)) {
					Thread.sleep(500);
				}
				Thread.sleep(1000);
				System.out.println("\tReading and assigning children results");
				String[] values = parent.assignChromosomes(chromosomes.length);
				for(int i=0; i<chromosomes.length; i++) {
					chromosomes[i].childEvaluationInitialization(values[i]);
				}
				System.out.println("\tWriting results");
				File f = new File(parameters.get("resultFolder") + iteration + "/");
				f.mkdir();
				gen.writePopulation(parameters.get("resultFolder") + iteration + "/");
				appendInfo(parameters.get("resultFolder"), iteration, gen);
				deleteDirectory(new File(parameters.get("resultFolder") + (iteration - 1) + "/"));
				if(iteration >= maxIterations) {
					System.out.println("Done! iteration: " + iteration + "; maxIterations: " + maxIterations);
					break;
				}
			} catch (Exception e) {
				System.err.println("Err Iter: " + iteration);
				System.err.println("Err chromosomes.length: " + chromosomes.length);
				e.printStackTrace();
			}	
		}
		
	}

}
