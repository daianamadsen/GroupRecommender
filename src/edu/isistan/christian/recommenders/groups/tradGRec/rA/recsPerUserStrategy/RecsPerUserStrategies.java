package edu.isistan.christian.recommenders.groups.tradGRec.rA.recsPerUserStrategy;

public enum RecsPerUserStrategies {
	USE_K ("Use_K (RA - Amount of Recommendations Per Uer Strategy)"),
	USE_ALL_ITEMS_IN_DATASET ("Use_ALL (RA - Amount of Recommendations Per Uer Strategy)") 
	;

	//-------------- For every element in the enum
	private String name;
	RecsPerUserStrategies(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public RecsPerUserStrategy get(){
		RecsPerUserStrategy st = null;
		switch (this){
		case USE_ALL_ITEMS_IN_DATASET:
			st = new RPUStrategyAll();
			break;
		case USE_K:
			st = new RPUStrategyK();
			break;
		default:
			break;
		}

		return st;
	}
}
