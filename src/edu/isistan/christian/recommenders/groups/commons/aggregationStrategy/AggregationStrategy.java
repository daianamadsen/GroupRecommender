package edu.isistan.christian.recommenders.groups.commons.aggregationStrategy;

import java.util.List;

public interface AggregationStrategy {

	public double aggregate (List<Double> ratings);
	
	public String toString();
	
}
