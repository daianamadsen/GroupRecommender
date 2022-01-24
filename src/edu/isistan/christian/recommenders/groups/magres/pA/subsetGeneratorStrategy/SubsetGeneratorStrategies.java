package edu.isistan.christian.recommenders.groups.magres.pA.subsetGeneratorStrategy;

import edu.isistan.christian.recommenders.groups.commons.aggregationStrategy.AggregationStrategies;
import edu.isistan.christian.recommenders.sur.datatypes.SURItem;

public enum SubsetGeneratorStrategies {
	POWERSET ("Powerset [SubsetGeneratorStrategy]");
	
	private String name;
	SubsetGeneratorStrategies(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public SubsetGeneratorStrategy<SURItem> get(){
		SubsetGeneratorStrategy<SURItem> st = null;
		switch (this){
		case POWERSET:
			st = new SubGeneratorPowerset<>(); break;
		default: 
			break;
		}

		return st;
	}

	//For testing
	public static void main (String [] args){
		System.out.println("Testing: "+AggregationStrategies.valueOf("POWERSET").getName());
	}

}
