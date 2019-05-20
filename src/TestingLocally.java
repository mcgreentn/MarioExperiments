import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;

import shared.ScenesLibrary;

public class TestingLocally {

	public static ScenesLibrary fillLibrary(ScenesLibrary lib) throws Exception {
		File directory = new File("scenelibrary/");
		File[] mechFolders = directory.listFiles();
		System.out.println(mechFolders.length);
		for (File folder : mechFolders) {
			//loop through each folder for the mechanics
			String[] sceneMechanics = folder.getName().split(",");
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
	
	
	public static void main(String[] args) {
		//create Scene library
		ScenesLibrary lib = new ScenesLibrary();
		try {
			fillLibrary(lib);
			String test = lib.getScene(4);
			System.out.println(test);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
