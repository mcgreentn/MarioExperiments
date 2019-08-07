import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import engine.core.MarioGame;
import fi2pop.FI2PopGeneticAlgorithm;
import fi2pop.FI2PopGeneticAlgorithmL;
import shared.Chromosome;
import shared.ChromosomeL;
import shared.ScenesLibrary;

public class TestingLocally {

	public static ScenesLibrary fillLibrary(ScenesLibrary lib) throws Exception {
		File directory = new File("scenelibrary/");
		File[] mechFolders = directory.listFiles();
		System.out.println(mechFolders.length);
		for (File folder : mechFolders) {
			//loop through each folder for the mechanics
			String sceneMechanics = folder.getName().replace(",", "");
			File[] files = folder.listFiles();
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
		Random rnd = new Random(10);
		//create Scene library
		ScenesLibrary lib = new ScenesLibrary(rnd);
		try {
			fillLibrary(lib);
		} catch(Exception e) {
			e.printStackTrace();
		}
		//make the FI2Pop obj
		int populationSize = 10;
		int chromosomeLength = 14;
		int appendingSize = 2;
		double crossover = 0.50;
		double mutation = 0.10;
		int elitism = (int)(((double) populationSize)*0.1);
		String[] playthroughMechanics = new String[]{
				"1,1,0,0,0,1,0,0,0,0,0,0",
				"1,1,0,0,0,1,0,0,0,0,0,0",
				"1,1,0,0,0,1,0,1,0,0,0,0",
				"0,0,0,0,0,1,0,1,0,0,0,0",
				"0,0,0,0,0,0,0,0,0,0,0,0",
				"1,0,0,1,0,0,0,0,0,0,0,0",
				"0,0,0,0,0,0,0,0,0,0,0,0",
				"0,0,0,0,0,0,0,0,0,0,0,0",
				"1,0,1,0,0,0,0,0,0,0,0,0",
				"0,0,0,0,0,0,0,0,0,0,0,0",
				"0,0,0,0,0,0,0,0,0,0,0,0",
				"0,0,0,0,0,0,0,0,0,0,0,0",
				"0,0,0,0,0,0,0,0,0,0,0,0",
				"1,0,0,0,0,0,0,0,0,0,0,0"
		};
		boolean variableNumOfMechInScene = false;
		System.out.println("Initialize FI2Pop");
		FI2PopGeneticAlgorithmL gen = new FI2PopGeneticAlgorithmL(lib, rnd, populationSize, chromosomeLength, appendingSize, crossover, mutation, elitism, playthroughMechanics, variableNumOfMechInScene);
		System.out.println("Making Random Population of Chromosomes");
		gen.randomChromosomesInitialize();
//		gen.smartChomosomesInitialize();
		ChromosomeL[] chromosomes = gen.getPopulation();
		int iteration = 0; 
		int maxIterations = 3;

		while(true) {
			System.out.println("Generation " + iteration);
			int index = 0; 
			for(ChromosomeL c: chromosomes) {
				System.out.println("\tRunning Chromosome number: " + ++index);
				c.calculateResults(new MarioGame(), new agents.robinBaumgarten.Agent(), 20);
//				System.out.println("\t\tFitness: " + c.getFitness());
			}
			try {
				System.out.println("Writing results");
				File f = new File("result/" + iteration + "/");
				f.mkdir();
				gen.writePopulation("result/" + iteration + "/");
				appendInfo(("result/"), iteration, gen);				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			if(maxIterations > 0 && iteration >= maxIterations) {
				break;
			}
			System.out.println("\nGenerate Next Population");
			gen.getNextGeneration();
			chromosomes = gen.getPopulation();
			iteration += 1;
		}
		System.out.println("done");
	}

}
