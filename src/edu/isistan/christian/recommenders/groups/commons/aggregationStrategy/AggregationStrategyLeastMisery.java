package edu.isistan.christian.recommenders.groups.commons.aggregationStrategy;

import java.util.List;

public class AggregationStrategyLeastMisery implements
		AggregationStrategy {

	@Override
	public double aggregate(List<Double> ratings) {
		if (ratings.isEmpty())
			return 0;
		
		double min =  Double.POSITIVE_INFINITY; //assumes ratings > 0
		for (Double rating : ratings)
			if (rating <= min)
				min = rating;
		
		return min;
	}

	@Override
	public String toString() {
		return "AggregationStrategyLeastMisery []";
	}

	
}
