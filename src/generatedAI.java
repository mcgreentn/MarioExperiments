import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import engine.core.MarioGame;
import shared.ChromosomeL;
import shared.ScenesLibrary;

public class generatedAI {
	private static String  fileLocation = "runAIVid/normal_test/lvl1/";
	private static String fileName = "0";
	public static ScenesLibrary fillLibrary(ScenesLibrary lib, String scenesFolder) throws Exception {
		File directory = new File(scenesFolder);
		File[] mechFolders = directory.listFiles();
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
	public static String[][] readChromosome() throws IOException {
		String[][] result = new String[1][2];
		int startIndex = 0;
		for(int i=0; i<1; i++) {
			result[i][0] = Files.readAllLines(Paths.get(fileLocation, fileName + ".txt")).get(0);
			result[i][1] = Files.readAllLines(Paths.get(fileLocation, fileName + ".txt")).get(1);
		}
		return result;
	}
	public static String readLevel() throws IOException {
		List resultArray = Files.readAllLines(Paths.get(fileLocation, fileName + ".txt"));
		String result = "";
		int i = 6;
		for(; i < resultArray.size()-1; i++) {
			result += resultArray.get(i);
			result += "\n";
		}
		result += resultArray.get(i);
		System.out.println(result);
		return result;
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

	public static void main(String[] args) {
		Random rnd = new Random(1023124);
		//create Scene library
		ScenesLibrary lib = new ScenesLibrary(rnd);
		String playthroughMechanicsFolder = "playthroughs/";
		String playthroughMechanicsLevelName = "ai-lvl-1.json"; 
		String[] playthroughMechanics = fillPlaythroughMechanics(playthroughMechanicsFolder+playthroughMechanicsLevelName);
		try {
			fillLibrary(lib, "scenelibrary/");
			
			String level = readLevel();
			MarioGame game2 = new MarioGame();
			game2.runGame(new agents.robinBaumgarten.Agent(), level, 20, 0, true);
//			game2.playGame(level, 20);

			
//			String[][] levels = readChromosome();
//			ChromosomeL[] chromosomes = new ChromosomeL[levels.length];
//			for(int i=0; i<chromosomes.length; i++) {
//				System.out.println(levels[i][0] + " ");
//				System.out.println(levels[i][1] + " ");
//				chromosomes[i] = new ChromosomeL(rnd, lib, 14, 2, playthroughMechanics, true);
//				levels[i][0] = "0," + levels[i][0];
//				chromosomes[i].stringInitialize(levels[i]);
//			}
//			int index = 0;	
//			for(ChromosomeL c:chromosomes) {
//				System.out.println("\tRunning Child number: " + ++index);
//				MarioGame game = new MarioGame();
//				System.out.println(c.toString());
//				game.runGame(new agents.robinBaumgarten.Agent(), c.toString(), 20, 0, true);
//			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
