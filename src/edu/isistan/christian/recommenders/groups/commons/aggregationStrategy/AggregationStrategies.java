package edu.isistan.christian.recommenders.groups.commons.aggregationStrategy;

public enum AggregationStrategies {
	AVERAGE ("Average (Rating Aggregation Strategy)"),
	APPROVAL_VOTING ("Approval Voting (Rating Aggregation Strategy"),
	LEAST_MISERY ("Least Misery (Rating Aggregation Strategy)"),
	MOST_PLEASURE ("Most Pleasure (Rating Aggregation Strategy)"),
	UPWARD_LEVELING ("Upward Leveling (Rating Aggregation Strategy)");

	//-------------- For every element in the enum
	private String name;
	AggregationStrategies(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public AggregationStrategy get(){
		AggregationStrategy st = null;
		switch (this){
		case AVERAGE:
			st = new AggregationStrategyAverage(); break;
		case APPROVAL_VOTING:
			st = new AggregationStrategyApprovalVoting(); break;
		case LEAST_MISERY:
			st = new AggregationStrategyLeastMisery(); break;
		case MOST_PLEASURE:
			st = new AggregationStrategyMostPleasure(); break;
		case UPWARD_LEVELING:
			st = new AggregationStrategyUpwardLeveling(); break;	
		default: 
			break;
		}

		return st;
	}

	//For testing
	public static void main (String [] args){
		System.out.println("Testing: "+AggregationStrategies.valueOf("AVERAGE").getName());
	}

}
