import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import engine.core.MarioAgent;
import engine.core.MarioGame;
import shared.ChromosomeL;
import shared.ScenesLibrary;
import shared.evaluator.ChildEvaluator;

public class FI2PopChildRunnerL {

	private static void runExperiment(ChromosomeL c, HashMap<String, String> parameters) {
		int runFixedRuns = 5;
		MarioAgent[] agents = new MarioAgent[runFixedRuns];
		for(int i = 0; i < agents.length; i++) {
			agents[i] = new agents.robinBaumgarten.Agent();
		}
		c.calculateResults(new MarioGame(), agents, 20);
//		c.setAge(10);
//		if(c.getAge() == 1 || c.getAge() == 0) {
//			int playthroughCount = Integer.parseInt(parameters.get("playthroughCount"));
//			MarioAgent[] marioAgents = new MarioAgent[playthroughCount];
//			for(int i = 0; i < playthroughCount; i++) {
//				marioAgents[i] = new agents.robinBaumgarten.Agent();
//			}
//			
//			c.calculateResults(new MarioGame(), marioAgents, 20);
//		}
//		else if (c.getAge() > 1) {
//			MarioAgent[] agents = new MarioAgent[c.getAge()];
//			for(int i = 0; i < agents.length; i++) {
//				agents[i] = new agents.robinBaumgarten.Agent();
//			}
//			c.calculateResults(new MarioGame(), agents, 20);
//		}
//		else {
//			System.out.println("ISSUE WITH THE AGE OF A CHROMOSOME: ITS AGE IS " + c.getAge());
//			c.calculateResults(new MarioGame(), new agents.robinBaumgarten.Agent(), 20);
//		}
		
		
	}

	public static ScenesLibrary fillLibrary(ScenesLibrary lib, String scenesFolder) throws Exception {
		File directory = new File(scenesFolder);
		File[] mechFolders = directory.listFiles();
		Arrays.sort(mechFolders);
		for (File folder : mechFolders) {
			//loop through each folder for the mechanics
			String sceneMechanics = folder.getName().replace(",", "");
			File[] files = folder.listFiles();
			Arrays.sort(files);
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
	
    private static final long MEGABYTE = 1024L * 1024L;

    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

	public static void main(String[] args) {
		 // Get the Java runtime
        Runtime runtime = Runtime.getRuntime();
       
		int id = Integer.parseInt(args[0]);
		int size = Integer.parseInt(args[1]);
		int startIndex = id * size;
		HashMap<String, String> parameters = null;
		try {
			parameters = readParameters("FI2PopParametersL.txt");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ChildEvaluator child = new ChildEvaluator(id, size, parameters.get("inputFolder"), parameters.get("outputFolder"));
		Random rnd = new Random(Integer.parseInt(parameters.get("seed")));
		//create Scene library
		ScenesLibrary lib = new ScenesLibrary(rnd);

		try {
			fillLibrary(lib, parameters.get("scenesFolder"));
		} catch(Exception e) {
			e.printStackTrace();
		}


		int appendingSize = Integer.parseInt(parameters.get("appendingSize"));
		int minChromosomeLength = Integer.parseInt(parameters.get("minChromosomeLength"));
		int maxChromosomeLength = Integer.parseInt(parameters.get("maxChromosomeLength"));
		boolean variableNumberOfMechanics = Boolean.parseBoolean(parameters.get("variableNumberOfMechanics"));
		String playthroughMechanicsFolder = parameters.get("playthroughMechanicsFolder");
		String playthroughMechanicsLevelName = parameters.get("playthroughMechanicsLevelName"); 
		String[] playthroughMechanics = fillPlaythroughMechanics(playthroughMechanicsFolder+playthroughMechanicsLevelName);
		ChromosomeL[] chromosomes = null;
		String[][] levels = null;
		try {
			while(!child.checkChromosomes()) {
				Thread.sleep(500);
			}
			levels = child.readChromosomesL();
			chromosomes = new ChromosomeL[levels.length];
			for(int i=0; i<chromosomes.length; i++) {
				int numOfScenes = 0; //since we are using stringInitialize, the numOfScenes will be set/overwritten from there
				chromosomes[i] = new ChromosomeL(rnd, lib, numOfScenes, appendingSize, playthroughMechanics, variableNumberOfMechanics, parameters);
				chromosomes[i].stringInitialize(levels[i]);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		int iteration = 0;
		int maxIterations = Integer.parseInt(parameters.get("maxIterations"));
		while(true) {
			try {
				System.out.println("Waiting for parent iteration " + iteration);
				while(!child.checkChromosomes()) {
					Thread.sleep(500);
				}
				Thread.sleep(1000);
				System.out.println("Reading children values");
				levels = child.readChromosomesL();
				for(int i=0; i<chromosomes.length; i++) {
					chromosomes[i].chromosomeLReset();// = new ChromosomeL(rnd, lib, chromosomeLength, appendingSize, playthroughMechanics, variableNumberOfMechanics);
					chromosomes[i].stringInitialize(levels[i]);
				}
				int index = 0;
				for(ChromosomeL c:chromosomes) {
					System.out.println("\tRunning Child number: " + (startIndex + index));
					index++;
					runExperiment(c, parameters);
				}
				System.out.println("Writing Chromosomes results.");
				String[] values = new String[chromosomes.length];
				for(int i=0; i<values.length; i++) {
					values[i] = chromosomes[i].getAge() + "," + chromosomes[i].getConstraints() + "," + chromosomes[i].getFitness() + "," + chromosomes[i].getMatchMechs() + "," + chromosomes[i].getMissingMechs()  + ","+ chromosomes[i].getExtraMechs() + "\n";
					values[i] += chromosomes[i].getGenes() + "\n";
					values[i] += chromosomes[i].toString() + "\n";
				}
				child.writeResults(values);
				System.out.println("Deleting input files for child");
				child.clearInputFiles();
				if(iteration >= maxIterations) {
					System.out.println("Done! iteration: " + iteration + "; maxIterations: " + maxIterations);
					break;
				}
				iteration += 1;
				
				 // Run the garbage collector
		        runtime.gc();
		        // Calculate the used memory
		        long memory = runtime.totalMemory() - runtime.freeMemory();
		        System.out.println("Used memory is bytes: " + memory);
		        System.out.println("Used memory is megabytes: "
		                + bytesToMegabytes(memory));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		 // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used memory is bytes: " + memory);
        System.out.println("Used memory is megabytes: "
                + bytesToMegabytes(memory));
		//		lib.printLib();

	}

}
