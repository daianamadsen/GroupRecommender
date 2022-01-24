package edu.isistan.christian.recommenders.groups.commons.aggregationStrategy;

import java.util.List;

public class AggregationStrategyAverage implements AggregationStrategy {

	@Override
	public double aggregate(List<Double> ratings) {
	
		if (ratings.isEmpty())
			return 0;
		
		double average = 0;
		for (Double r : ratings){
			average += r;
		}
		
		return average/ratings.size();
	}

	@Override
	public String toString() {
		return "AggreagationStrategyAverage []";
	}

}
