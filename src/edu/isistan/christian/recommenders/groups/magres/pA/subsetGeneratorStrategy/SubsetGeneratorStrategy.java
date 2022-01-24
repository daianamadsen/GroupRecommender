package edu.isistan.christian.recommenders.groups.magres.pA.subsetGeneratorStrategy;

import java.util.Set;

import edu.isistan.christian.recommenders.sur.datatypes.SURItem;

public interface SubsetGeneratorStrategy <T extends SURItem> {

	public Set<Set<T>> generateSubsets(Set<T> elements);
	
	public String toString();
}
