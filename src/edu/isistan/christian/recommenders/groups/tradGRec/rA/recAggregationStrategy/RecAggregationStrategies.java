package edu.isistan.christian.recommenders.groups.tradGRec.rA.recAggregationStrategy;

import edu.isistan.christian.recommenders.sur.datatypes.SURItem;

public enum RecAggregationStrategies {
	SIMPLE ("Simple (Recommendation Aggregation Strategy)")
	;

	//-------------- For every element in the enum
	private String name;
	RecAggregationStrategies(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public RecAggregationStrategy<SURItem> get(){
		RecAggregationStrategy<SURItem> st = null;
		switch (this){
		case SIMPLE:
			st = new RAStrategySimple<>();
			break;
		default:
			break;
		}

		return st;
	}
}