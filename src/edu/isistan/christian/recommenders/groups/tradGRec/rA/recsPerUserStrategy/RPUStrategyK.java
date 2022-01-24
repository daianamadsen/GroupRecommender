package edu.isistan.christian.recommenders.groups.tradGRec.rA.recsPerUserStrategy;

import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecGroup;

public class RPUStrategyK implements RecsPerUserStrategy{

	@Override
	public int getAmountOfRecsPerUser(int k, int amtItemsInRecommender, GRecGroup group) {
		return k;
	}

	@Override
	public String toString() {
		return "RPUStrategyK []";
	}
}
