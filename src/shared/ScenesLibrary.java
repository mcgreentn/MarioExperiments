package shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

class SingleScene{
	private String sceneFitness;
	private String[] scene;
	private String[] arrayedScenesMechanics;
	
	public SingleScene() {
		this.sceneFitness = new String();
		this.scene = new String[0];
		this.arrayedScenesMechanics = new String[0];
	}
	
	public void makeSingleScene(String[] sceneMechanics, String fitness, String[] scene) {
		this.arrayedScenesMechanics = sceneMechanics;
		this.sceneFitness = fitness;
		this.scene = scene;
	}
	
	public String[] getScene() {
		return this.scene;
	}
	public String getSceneFitness() {
		return this.sceneFitness;
	}
	public String[] getArrayedScenesMechanics() {
		return this.arrayedScenesMechanics;
	}
}

public class ScenesLibrary {
	private ArrayList<String[]> scenesMechanicsArrayList;
	private Random randObj; 

	protected String[][] arrayedScenesMechanics;
	protected Map<String[], ArrayList<SingleScene>> library;
	protected int numberOfScenes;
	
	public ScenesLibrary() {
		this.scenesMechanicsArrayList = new ArrayList<String[]>();
		this.randObj = new Random();

		this.arrayedScenesMechanics = new String[1][];
		this.library = new HashMap<String[], ArrayList<SingleScene>>();
		this.numberOfScenes = 0;
	}

	//make new single scene, add it to the library based on the mechanics as the key
	public void addScene(String[] sceneMechanics, String fitness, String[] scene) {
		
		SingleScene temp = new SingleScene(); 
		temp.makeSingleScene(sceneMechanics, fitness, scene);
		if(this.library.containsKey(sceneMechanics)) {
			this.library.get(sceneMechanics).add(temp);
		}else {
			ArrayList<SingleScene> t = new ArrayList<SingleScene>();
			t.add(temp);
			this.library.put(sceneMechanics, t);
		}
		
		this.scenesMechanicsArrayList.add(sceneMechanics);
		this.arrayedScenesMechanics = this.scenesMechanicsArrayList.toArray(new String[0][]);
		this.numberOfScenes += 1;
	}
	
	public int getNumberOfScenes() {
//		return this.library.size();
		return this.arrayedScenesMechanics.length;
	}
	public String[][] getArrayedScenesMechanics() {
		return this.arrayedScenesMechanics;
	}
	//get scene based off index
	public String[] getScene(int index) {
		String [] mechanicsTemp = this.getSceneMechanics(index);
		ArrayList<SingleScene> sceneTempList = this.library.get(mechanicsTemp);
		SingleScene singleSceneTemp = sceneTempList.get(this.randObj.nextInt(sceneTempList.size()));
		return singleSceneTemp.getScene();
	}
	//get scene based off mechanics
	public String[] getScene(String[] mechanicsSceneInclude) {
		ArrayList<SingleScene> sceneTempList = this.library.get(mechanicsSceneInclude);
		if(sceneTempList.size() == 0) {
			return new String[0];
		}
		SingleScene singleSceneTemp = sceneTempList.get(this.randObj.nextInt(sceneTempList.size()));
		return singleSceneTemp.getScene();
	}
	//get index based off mechanics
	public int getSceneIndex(String[] mechanicsSceneInclude) {
		for(String[] s : this.scenesMechanicsArrayList) {
			if (Arrays.deepEquals(s, mechanicsSceneInclude)) {
				return this.scenesMechanicsArrayList.indexOf(s);
			}
		}
		return -1;
	}
	public String[] getSceneMechanics(int index) {
		return this.arrayedScenesMechanics[index];
	}
}
