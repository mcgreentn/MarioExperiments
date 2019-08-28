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
	
	public void makeSingleSceneHeight(String sceneMechanics, String fitness, String[] scene, int shift) {
		this.sceneMechanics = sceneMechanics;
		this.sceneFitness = fitness;
		String[] temp = new String[scene.length]; 
		for(int i = 0; i < scene.length - shift-1; i++) {
			temp[i] = scene[i+shift];
		}
		for(int i = scene.length - shift-1; i < scene.length; i++) {
			temp[i] = new String();
			for(int j = 0; j < scene[i].length(); j++) {
				temp[i] += "X";
			}
		}
		
		this.scene = temp;
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
		
	public ScenesLibrary(Random rnd) {
		this.randObj = rnd;
		
		this.scenesMechanicsArrayList = new ArrayList<String>();
		this.arrayedScenesMechanics = new String[0];
		this.library = new HashMap<String, ArrayList<SingleScene>>();
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
		SingleScene tempHeight = new SingleScene();
		tempHeight.makeSingleSceneHeight(sceneMechanics, fitness, scene, this.randObj.nextInt(3)+1);
		if(this.library.containsKey(sceneMechanics)) {
			this.library.get(sceneMechanics).add(temp);
			this.library.get(sceneMechanics).add(tempHeight);
		}else {
			ArrayList<SingleScene> t = new ArrayList<SingleScene>();
			t.add(temp);
			t.add(tempHeight);
			this.library.put(sceneMechanics, t);
		}
		
		this.scenesMechanicsArrayList.add(sceneMechanics);
		this.arrayedScenesMechanics = this.scenesMechanicsArrayList.toArray(new String[0]);
	}
	
	public int getNumberOfScenes() {
		return this.arrayedScenesMechanics.length;
	}
	public String[] getArrayedScenesMechanics() {
		return this.arrayedScenesMechanics;
	}
	//get scene based off index
	public String[] getScene(int index, int subIndex) {
		String mechanicsTemp = this.getSceneMechanics(index);
		ArrayList<SingleScene> sceneTempList = this.library.get(mechanicsTemp);
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
