package edu.isistan.christian.recommenders.groups.commons.aggregationStrategy;

import java.util.List;

public class AggregationStrategyMostPleasure implements
		AggregationStrategy {

	@Override
	public double aggregate(List<Double> ratings) {
		if (ratings.isEmpty())
			return 0;
		
		double max = Double.NEGATIVE_INFINITY; //assumes ratings > 0
		for (Double rating : ratings)
			if (rating >= max)
				max = rating;
		
		return max;
	}

	@Override
	public String toString() {
		return "AggregationStrategyMostPleasure []";
	}

}
