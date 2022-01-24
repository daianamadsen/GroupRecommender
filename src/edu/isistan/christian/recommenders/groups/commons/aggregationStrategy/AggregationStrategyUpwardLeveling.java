package edu.isistan.christian.recommenders.groups.commons.aggregationStrategy;

import java.util.Arrays;
import java.util.List;

public class AggregationStrategyUpwardLeveling implements AggregationStrategy {
	
	private AggregationStrategyAverage avgStrategy;
	private AggregationStrategyApprovalVoting avStrategy;
	
	private double alpha, beta, gamma, maxRating;
	
	public AggregationStrategyUpwardLeveling (double alpha, double beta, double gamma, 
			double approvalThreshold, double maxRating) throws IllegalArgumentException{
		if (alpha+beta+gamma != 1)
			throw new IllegalArgumentException ("alpha, beta and gamma parameters must add up 1");
		this.alpha = alpha;
		this.beta = beta;
		this.gamma = gamma;
		this.maxRating = maxRating;
		
		this.avgStrategy = new AggregationStrategyAverage();
		this.avStrategy = new AggregationStrategyApprovalVoting(approvalThreshold, maxRating);
	}
			
	public AggregationStrategyUpwardLeveling() {
	}
	
	public void setAvgStrategy(AggregationStrategyAverage avgStrategy) {
		this.avgStrategy = avgStrategy;
	}

	public void setAvStrategy(AggregationStrategyApprovalVoting avStrategy) {
		this.avStrategy = avStrategy;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public double getBeta() {
		return beta;
	}

	public void setBeta(double beta) {
		this.beta = beta;
	}

	public double getGamma() {
		return gamma;
	}

	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	public void setMaxRating(double maxRating) {
		this.maxRating = maxRating;
	}

	public double computeDev (List<Double> ratings) {
		if (ratings.isEmpty()) return 0.0;
		
		double avg = this.avgStrategy.aggregate(ratings);
		double msd = 0.0;
		for (double r : ratings)
			msd += Math.pow(avg-r, 2);
		
		msd /= ratings.size();
		return maxRating - msd;
	}
	
	@Override
	public double aggregate(List<Double> ratings) {
//		System.out.println("AVG: "+ avgStrategy.aggregate(ratings));
//		System.out.println("AV: "+ avStrategy.aggregate(ratings));
//		System.out.println("DEV: "+ this.computeDev(ratings));
		return (alpha * this.avgStrategy.aggregate(ratings)
				+ beta * this.avStrategy.aggregate(ratings)
				+ gamma * this.computeDev(ratings));
	}

	
	public static void main(String[] args) {
//		List<Double> ratings = Arrays.asList(new Double[] {0.7,1.0,0.4});
//		List<Double> ratings = Arrays.asList(new Double[] {0.7,0.6,0.7});
//		List<Double> ratings = Arrays.asList(new Double[] {0.7,0.7,1.0});
		List<Double> ratings = Arrays.asList(new Double[] {0.8,0.2,0.4});
		double approvalThreshold = 0.8;
		double maxRating = 1.0;
		
//		List<Double> ratings = Arrays.asList(new Double[] {3.5,3.5,3.5});
//		double approvalThreshold = 4;
//		double maxRating = 5.0;
		
		AggregationStrategyUpwardLeveling ulStrategy = new AggregationStrategyUpwardLeveling(0.4,
				0.1, 0.5, approvalThreshold, maxRating);
		
		System.out.println("UL: "+ ulStrategy.aggregate(ratings));
	}

	@Override
	public String toString() {
		return "AggregationStrategyUpwardLeveling [alpha=" + alpha + ", beta=" + beta + ", gamma=" + gamma + ", maxRating=" + maxRating
				+ ", avgStrategy=" + avgStrategy + ", avStrategy=" + avStrategy
				+ "]";
	}
}
