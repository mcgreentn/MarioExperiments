package shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

class SingleScene{
	private String sceneFitness;
	private String[] scene;
	private String sceneMechanics;
	private int sceneWeight;
	
	public SingleScene() {
		this.sceneFitness = new String();
		this.scene = new String[0];
		this.sceneMechanics = new String();
		this.sceneWeight = 0;
	}
	
	public void makeSingleScene(String sceneMechanics, String fitness, String[] scene) {
		this.sceneMechanics = sceneMechanics;
		this.sceneFitness = fitness;
		this.sceneWeight = ((int) sceneMechanics.chars().filter(num -> num == '1').count()) + 1;
		this.scene = scene;
	}
	
	public void makeSingleSceneHeight(String sceneMechanics, String fitness, String[] scene, int shift) {
		this.sceneMechanics = sceneMechanics;
		this.sceneFitness = fitness;
		this.sceneWeight = ((int) sceneMechanics.chars().filter(num -> num == '1').count()) + 1;
		
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
	public int getSceneWeight() {
		return this.sceneWeight;
	}
}

public class ScenesLibrary {
	private Random randObj; 
	private Map<Integer, ArrayList<Integer>> sceneWeights;
	
	private ArrayList<String> scenesMechanicsArrayList;
	protected String[] arrayedScenesMechanics;
	protected Map<String, ArrayList<SingleScene>> library;
		
	public ScenesLibrary(Random rnd) {
		this.randObj = rnd;
		this.sceneWeights = new HashMap<Integer, ArrayList<Integer>>(); 
		
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
		if(this.sceneWeights.containsKey(temp.getSceneWeight())) {
			this.sceneWeights.get(temp.getSceneWeight()).add(this.arrayedScenesMechanics.length);
		} else {
			ArrayList<Integer> t = new ArrayList<Integer>();
			t.add(this.arrayedScenesMechanics.length);
			this.sceneWeights.put(temp.getSceneWeight(), t);
		}
		this.scenesMechanicsArrayList.add(sceneMechanics);
		this.arrayedScenesMechanics = this.scenesMechanicsArrayList.toArray(new String[0]);
		
	}
	
	public int getNumberOfScenes() {
//		this.scenesMechanicsArrayList.size() is the same as this.arrayedScenesMechanics.length
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
	//get the subGene
	//we have a valid scene index from this.scenesMechanicsArraylist
	//get the corresponding subgenes
	public int getSubSceneIndex(int geneIndex) {
		String mechanicsToUseString = this.getSceneMechanics(geneIndex);
		ArrayList<SingleScene> sceneTempList = this.library.get(mechanicsToUseString);
		if(sceneTempList == null || sceneTempList.size() == 0) {
			return -1;
		} 
		return this.randObj.nextInt(sceneTempList.size());
	}
	public String getSceneMechanics(int index) {
		return this.scenesMechanicsArrayList.get(index);
	}
	
	//returns the index of a weighted scene
	public int[] getWeightedScene(int index) {
		
		String selectedSceneMechanics = this.getSceneMechanics(index);
		int num_mechanics_fired = ((int) selectedSceneMechanics.chars().filter(num -> num == '1').count());
		if (num_mechanics_fired == 0) {
			//mutating scene with no mechanics -> dont mutate
			int[] sceneAndSubSceneIndex = this.getSceneIndex(selectedSceneMechanics);
			return sceneAndSubSceneIndex;
		}
		ArrayList<Integer> bitsTurnedOnIndex = new ArrayList<Integer>();
		List<List<Integer>> allMutatedPermutations = new LinkedList<List<Integer>>();
		ArrayList<String> allMutatedPermutationsMechanics = new ArrayList<String>();
		for(int i = 0; i < selectedSceneMechanics.length(); i++){
			if(selectedSceneMechanics.charAt(i) == '1'){
				bitsTurnedOnIndex.add(i);
			}
		}
		for(int i = 1; i <= bitsTurnedOnIndex.size(); i++) {
			allMutatedPermutations.addAll(this.combination(bitsTurnedOnIndex, i));
		}
		//only look at the valid mechanics for all permutations
		HashMap<Integer, ArrayList<int[]>> weightsDictionary = new HashMap<Integer, ArrayList<int[]>>();
		HashSet<Integer> weightsAdded = new HashSet<Integer>();
		for(int i = 0; i < allMutatedPermutations.size(); i++) {
			StringBuilder temp = new StringBuilder("000000000000");
			for (int j = 0; j < allMutatedPermutations.get(i).size(); j++) {
				temp.setCharAt(allMutatedPermutations.get(i).get(j), '1');
			}
			int[] sceneAndSubSceneIndex = this.getSceneIndex(temp.toString());
			//valid permutation
			if (sceneAndSubSceneIndex[0] != -1 && sceneAndSubSceneIndex[1] != -1) {
				allMutatedPermutationsMechanics.add(temp.toString());
				int keyWeight = ((int) temp.chars().filter(num -> num == '1').count());
				weightsAdded.add(keyWeight);
				if (weightsDictionary.containsKey(keyWeight)) {
					weightsDictionary.get(keyWeight).add(sceneAndSubSceneIndex);
				} else {
					ArrayList<int[]> tempArrayList = new ArrayList<int[]>();
					tempArrayList.add(sceneAndSubSceneIndex);
					weightsDictionary.put(keyWeight, tempArrayList);
				}
			}
		}
		//flip weights 
		HashMap<Integer, ArrayList<int[]>> flippedWeightsDictionary = new HashMap<Integer, ArrayList<int[]>>();
		ArrayList<Integer> weightsAddedList = new ArrayList<>(weightsAdded);
		Collections.sort(weightsAddedList);
		int leftPointer = 0; 
		int rightPointer = weightsAddedList.size()-1;
		while(leftPointer <= rightPointer) {
			int leftKey = weightsAddedList.get(leftPointer);
			int rightKey = weightsAddedList.get(rightPointer);
			ArrayList<int[]> leftWeights = weightsDictionary.get(leftKey);
			flippedWeightsDictionary.put(leftKey, weightsDictionary.get(rightKey));
			flippedWeightsDictionary.put(rightKey, weightsDictionary.get(leftKey));
			leftPointer++;
			rightPointer--;
		}
		//create a biased arrayList with all the options and then pick rng
		ArrayList<int[]> bagOfPossibleScenes = new ArrayList<int[]>();
		//fill the biased arrayList
		for (int key : flippedWeightsDictionary.keySet()) {
			ArrayList<int[]> toAdd = flippedWeightsDictionary.get(key);
			for(int i = 0; i < toAdd.size(); i++) {
				for(int j = 0; j < key; j++) {
					bagOfPossibleScenes.add(toAdd.get(i));
				}
			}
		}
		//shuffle the biassed arrayList
		Collections.shuffle(bagOfPossibleScenes);
		//pick randomly from the biased arrayList
		int indexSelected = this.randObj.nextInt(bagOfPossibleScenes.size());
		int[] toReturn = bagOfPossibleScenes.get(indexSelected);
		
//		System.out.println("index: " + index);
//		System.out.println("selectedSceneMechanics: " + selectedSceneMechanics);
//		System.out.println("num_mechanics_fired: " + num_mechanics_fired);
//		System.out.println("bitsTurnedOnIndex: " + bitsTurnedOnIndex);
//		System.out.println("allMutatedPermutations: " + allMutatedPermutations);
//		System.out.println("allMutatedPermutationsMechanics: " + allMutatedPermutationsMechanics);
//		for (int name: weightsDictionary.keySet()){
//			int key = name;
//			ArrayList<int[]> value = weightsDictionary.get(name);  
//			for (int z = 0; z < value.size(); z++) {
//				System.out.println(key + " " + Arrays.toString(value.get(z)));
//				System.out.println("checking: " + this.getSceneMechanics(value.get(z)[0]));// Arrays.toString(this.getScene(value.get(z)[0], value.get(z)[1])));
//			}
//		}
//		System.out.println("weightsAddedList: " + Arrays.toString(weightsAddedList.toArray()));
//		for (int name: flippedWeightsDictionary.keySet()){
//			int key = name;
//			ArrayList<int[]> value = flippedWeightsDictionary.get(name);  
//			for (int z = 0; z < value.size(); z++) {
//				System.out.println(key + " " + Arrays.toString(value.get(z)));
//			}
//		}
//		System.out.println("bagOfPossibleScenes: " + Arrays.deepToString(bagOfPossibleScenes.toArray()));
//		System.out.println("indexSelected: " + indexSelected);
		return toReturn;
	}

	//used to calculate all permutations for a list
	public <T> List<List<T>> combination(List<T> values, int size) {
		if (0 == size) {
			return Collections.singletonList(Collections.<T> emptyList());
		}
		if (values.isEmpty()) {
			return Collections.emptyList();
		}
		List<List<T>> combinationToReturn = new LinkedList<List<T>>();
		T actual = values.iterator().next();
		List<T> subSet = new LinkedList<T>(values);
		subSet.remove(actual);
		List<List<T>> subSetCombination = combination(subSet, size - 1);
		for (List<T> set : subSetCombination) {
			List<T> newSet = new LinkedList<T>(set);
			newSet.add(0, actual);
			combinationToReturn.add(newSet);
		}
		combinationToReturn.addAll(combination(subSet, size));
		return combinationToReturn;
	}
}
