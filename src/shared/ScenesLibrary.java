package shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

class SingleScene{
	private String scene;
	private String sceneFitness;
	private String[] arrayedScenesMechanics;
	
	public SingleScene() {
		this.scene = new String();
		this.sceneFitness = new String();
		this.arrayedScenesMechanics = new String[0];
	}
	
	public void makeSingleScene(String[] sceneMechanics, String fitness, String[] scene) {
		this.arrayedScenesMechanics = sceneMechanics;
		this.sceneFitness = fitness;
		for(int i = 0; i < scene.length; i++) {
			this.scene += scene[i];
		}
	}
	
	public String getScene() {
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
		return this.numberOfScenes;
	}
	//get scene based off index
	public String getScene(int index) {
		String [] mechanicsTemp = this.getSceneMechanics(index);
		for(int i = 0; i < mechanicsTemp.length; i++) {
			System.out.print(mechanicsTemp[i]);
		}
		ArrayList<SingleScene> sceneTempList = this.library.get(mechanicsTemp);
		SingleScene singleSceneTemp = sceneTempList.get(this.randObj.nextInt(sceneTempList.size()));
		return singleSceneTemp.getScene();
	}
	//get scene based off mechanics
	public String getScene(String[] mechnicsSceneInclude) {
		ArrayList<SingleScene> sceneTempList = this.library.get(mechnicsSceneInclude);
		if(sceneTempList.size() == 0) {
			return "";
		}
		SingleScene singleSceneTemp = sceneTempList.get(this.randObj.nextInt(sceneTempList.size()));
		return singleSceneTemp.getScene();
	}
	public String[] getSceneMechanics(int index) {
		return this.arrayedScenesMechanics[index];
	}
}
