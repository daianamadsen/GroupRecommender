package edu.isistan.christian.recommenders.groups.tradGRec.rA.recsPerUserStrategy;

import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecGroup;

public interface RecsPerUserStrategy {

	public int getAmountOfRecsPerUser(int k, int amtItemsInRecommender, GRecGroup group);
	
	public String toString();
	
}
