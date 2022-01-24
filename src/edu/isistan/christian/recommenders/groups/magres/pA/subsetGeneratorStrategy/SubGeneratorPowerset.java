package edu.isistan.christian.recommenders.groups.magres.pA.subsetGeneratorStrategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.isistan.christian.recommenders.sur.datatypes.SURItem;

public class SubGeneratorPowerset<T extends SURItem> implements SubsetGeneratorStrategy<T>{

	//implementation from: https://stackoverflow.com/questions/1670862/obtaining-a-powerset-of-a-set-in-java
	private Set<Set<T>> powerSet(Set<T> originalSet){
		Set<Set<T>> sets = new HashSet<Set<T>>();
		if (originalSet.isEmpty()) {
			sets.add(new HashSet<T>());
			return sets;
		}
		List<T> list = new ArrayList<T>(originalSet);
		T head = list.get(0);
		Set<T> rest = new HashSet<T>(list.subList(1, list.size())); 
		for (Set<T> set : powerSet(rest)) {
			Set<T> newSet = new HashSet<T>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}       
		return sets;
	}

	@Override
	public Set<Set<T>> generateSubsets(Set<T> originalSet) {
		return powerSet(new HashSet<T> (originalSet));
	}

	@Override
	public String toString() {
		return "SubGeneratorPowerset []";
	}
	
	/*
	public static Set<Set<Integer>> powerSet2(Set<Integer> originalSet){
		Set<Set<Integer>> sets = new HashSet<Set<Integer>>();
		if (originalSet.isEmpty()) {
			sets.add(new HashSet<Integer>());
			return sets;
		}
		List<Integer> list = new ArrayList<>(originalSet);
		Integer head = list.get(0);
		Set<Integer> rest = new HashSet<Integer>(list.subList(1, list.size())); 
		for (Set<Integer> set : powerSet2(rest)) {
			Set<Integer> newSet = new HashSet<Integer>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}       
		return sets;
	}

	public static void main(String[] args) {
		Set<Integer> mySet = new HashSet<Integer>();
		 mySet.add(1);
		 mySet.add(2);
		 mySet.add(3);
		 for (Set<Integer> s : powerSet2(mySet)) {
		     System.out.println(s);
		 }
	}
	
	*/
}
