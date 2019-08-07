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
	private String sceneMechanics;
	
	public SingleScene() {
		this.sceneFitness = new String();
		this.scene = new String[0];
		this.sceneMechanics = new String();
	}
	
	public void makeSingleScene(String sceneMechanics, String fitness, String[] scene) {
		this.sceneMechanics = sceneMechanics;
		this.sceneFitness = fitness;
		this.scene = scene;
	}
	
	public String[] getScene() {
		return this.scene;
	}
	public String getSceneFitness() {
		return this.sceneFitness;
	}
	public String getSceneMechanics() {
		return this.sceneMechanics;
	}
}

public class ScenesLibrary {
	private Random randObj; 
	
	private ArrayList<String> scenesMechanicsArrayList;
	protected String[] arrayedScenesMechanics;
	protected Map<String, ArrayList<SingleScene>> library;
	protected int numberOfScenes;
	
	protected ArrayList<SingleScene> allScenes;
	
	public ScenesLibrary(Random rnd) {
		this.randObj = rnd;
		
		this.scenesMechanicsArrayList = new ArrayList<String>();
		this.arrayedScenesMechanics = new String[0];
		this.library = new HashMap<String, ArrayList<SingleScene>>();
		this.numberOfScenes = 0;
	
		this.allScenes = new ArrayList<SingleScene>();
	}
	public void printLib() {
		System.out.println("~~~LIBRARY~~~~");
		for(int i = 0; i < this.scenesMechanicsArrayList.size(); i++) {
			String mechanicsTemp = this.getSceneMechanics(i);
			ArrayList<SingleScene> sceneTempList = this.library.get(mechanicsTemp);
			System.out.println(i + ": " + mechanicsTemp + " : " + sceneTempList.size());
		}
	}
	
	//make new single scene, add it to the library based on the mechanics as the key
	public void addScene(String sceneMechanics, String fitness, String[] scene) {
		SingleScene temp = new SingleScene(); 
		temp.makeSingleScene(sceneMechanics, fitness, scene);
		if(this.library.containsKey(sceneMechanics)) {
			this.library.get(sceneMechanics).add(temp);
		}else {
			ArrayList<SingleScene> t = new ArrayList<SingleScene>();
			t.add(temp);
			this.library.put(sceneMechanics, t);
		}
		
		this.allScenes.add(temp);
		this.scenesMechanicsArrayList.add(sceneMechanics);
		this.arrayedScenesMechanics = this.scenesMechanicsArrayList.toArray(new String[0]);
		this.numberOfScenes += 1;
	}
	
	public int getNumberOfScenes() {
//		return this.library.size();
		return this.arrayedScenesMechanics.length;
	}
	public String[] getArrayedScenesMechanics() {
		return this.arrayedScenesMechanics;
	}
	//get scene based off index
	public String[] getScene(int index, int subIndex) {
//		System.out.println("index " + index + "; sub " + subIndex);
		String mechanicsTemp = this.getSceneMechanics(index);
		ArrayList<SingleScene> sceneTempList = this.library.get(mechanicsTemp);
//		System.out.println("mechanicsTemp " + mechanicsTemp);
//		System.out.println("sceneTempList.length " + sceneTempList.size());
		SingleScene singleSceneTemp = sceneTempList.get(subIndex);
		return singleSceneTemp.getScene();
	}
	//get index based off mechanics
	public int[] getSceneIndex(String mechanicsSceneInclude) {
		int[] toReturn = new int[2];
		toReturn[0] = this.scenesMechanicsArrayList.indexOf(mechanicsSceneInclude);
		ArrayList<SingleScene> sceneTempList = this.library.get(mechanicsSceneInclude);
		if(sceneTempList == null || sceneTempList.size() == 0) {
			toReturn[1] = -1;
		} else {
			toReturn[1] = this.randObj.nextInt(sceneTempList.size());
		}
		return toReturn;
	}
	public String getSceneMechanics(int index) {
		return this.scenesMechanicsArrayList.get(index);
	}
}
