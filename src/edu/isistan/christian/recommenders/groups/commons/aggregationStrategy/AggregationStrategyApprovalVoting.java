package edu.isistan.christian.recommenders.groups.commons.aggregationStrategy;

import java.util.List;

public class AggregationStrategyApprovalVoting implements AggregationStrategy {
	
	double approvalThreshold;
	double maxRating;
	
	public AggregationStrategyApprovalVoting() {}
	
	public AggregationStrategyApprovalVoting(double approvalThreshold, double maxRating) {
		super();
		this.approvalThreshold = approvalThreshold;
		this.maxRating = maxRating;
	}

	public double getApprovalThreshold() {
		return approvalThreshold;
	}

	public void setApprovalThreshold(double approvalThreshold) {
		this.approvalThreshold = approvalThreshold;
	}

	public double getMaxRating() {
		return maxRating;
	}

	public void setMaxRating(double maxRating) {
		this.maxRating = maxRating;
	}

	@Override
	public double aggregate(List<Double> ratings) {
		if (ratings.isEmpty()) return 0.0;
		
		int approvalVotes = 0;
		for (double r : ratings)
			approvalVotes += (r >= this.approvalThreshold)? 1 : 0; //if r > threshold add 1 to the amount of "approvalVotes" (amount of users that approve the item)
		
		//convert the av to a "rating" => min-max normalisation
		int maxVotes = ratings.size(); //maxScore = the maximum amount of votes
		
		return  maxRating * (approvalVotes/(double)maxVotes); //min-max normalisation  z = value - min_of_range/ max_of_range - min_of_range => in this case: z= (votesCount - 0) / (maxVotes -0)
	}

	@Override
	public String toString() {
		return "AggregationStrategyApprovalVoting [approvalThreshold=" + approvalThreshold + ", maxRating=" + maxRating
				+ "]";
	}

}
