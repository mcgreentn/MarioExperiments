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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import engine.core.MarioGame;
import fi2pop.FI2PopGeneticAlgorithmL;
import shared.ChromosomeL;
import shared.ScenesLibrary;
import shared.evaluator.ParentEvaluator;

public class FI2PopParentRunnerL {

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

	private static void deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		directoryToBeDeleted.delete();
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
	}

	public static void main(String[] args) {
		//get the parameters
		HashMap<String, String> parameters = null;
		try {
			parameters = readParameters("FI2PopParametersL.txt");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Random rnd = new Random(Integer.parseInt(parameters.get("seed")));
		//create Scene library
		ScenesLibrary lib = new ScenesLibrary(rnd);

		try {
			fillLibrary(lib, parameters.get("scenesFolder"));
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		//create FI2Pop 		
		int appendingSize = Integer.parseInt(parameters.get("appendingSize"));
		int chromosomeLength = Integer.parseInt(parameters.get("chromosomeLength"));
		int popSize = Integer.parseInt(parameters.get("populationSize"));
		double crossover = Double.parseDouble(parameters.get("crossover"));
		double mutation = Double.parseDouble(parameters.get("mutation"));
		int elitism = Integer.parseInt(parameters.get("elitism"));
		String playthroughMechanicsFolder = parameters.get("playthroughMechanicsFolder");
		String playthroughMechanicsLevelName = parameters.get("playthroughMechanicsLevelName"); 
		String[] playthroughMechanics = fillPlaythroughMechanics(playthroughMechanicsFolder+playthroughMechanicsLevelName);
		
		boolean variableNumberOfMechanics = Boolean.parseBoolean(parameters.get("variableNumberOfMechanics"));
		System.out.println("Initialize FI2Pop");
		FI2PopGeneticAlgorithmL gen = new FI2PopGeneticAlgorithmL(lib, rnd, popSize, chromosomeLength, appendingSize, crossover, mutation, elitism, playthroughMechanics, variableNumberOfMechanics);
		ParentEvaluator parent = new ParentEvaluator(parameters.get("inputFolder"), parameters.get("outputFolder"));
		String typeOfInitialization = parameters.get("initialization");
		System.out.println("First Batch of Chromosomes " + typeOfInitialization);
		if(typeOfInitialization.equals("smart")) {
			gen.smartChomosomesInitialize();
		}
		else {
			gen.randomChromosomesInitialize();
		}
		ChromosomeL[] chromosomes = gen.getPopulation();
		int iteration = 0;
		int maxIterations = Integer.parseInt(parameters.get("maxIterations"));

		while(true) {
			try {
				System.out.println("Generation " + iteration);
				String[] levels = new String[chromosomes.length];
				for(int i=0; i<chromosomes.length; i++) {
					levels[i] = chromosomes[i].getAge() + ",";
					levels[i] += chromosomes[i].getGenes() + "\n" + chromosomes[i].toString() + "\n";
				}
				System.out.println("length of levels " + levels.length);
				parent.writeChromosomes(levels);
				System.out.println("Waiting for children to finish");
				while(!parent.checkChromosomes(chromosomes.length)) {
					Thread.sleep(500);
				}
				Thread.sleep(1000);
				System.out.println("Reading and assigning children results");
				String[] values = parent.assignChromosomes(chromosomes.length);
				for(int i=0; i<chromosomes.length; i++) {
					chromosomes[i].childEvaluationInitialization(values[i]);
				}
				parent.clearOutputFiles(chromosomes.length);
				System.out.println("Writing results");
				File f = new File(parameters.get("resultFolder") + iteration + "/");
				f.mkdir();
				gen.writePopulation(parameters.get("resultFolder") + iteration + "/");
				appendInfo(parameters.get("resultFolder"), iteration, gen);
				//deleteDirectory(new File(parameters.get("resultFolder") + (iteration - 1) + "/"));
				if(maxIterations > 0 && iteration >= maxIterations) {
					System.out.println("Done! iteration: " + iteration + "; maxIterations: " + maxIterations);
					break;
				}
				System.out.println("Generate Next Population");
				gen.getNextGeneration();
				chromosomes = gen.getPopulation();
				iteration += 1;
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
		
		System.out.println("DONE");
	}


}
