package edu.isistan.christian.recommenders.groups.tradGRec.rA.recAggregationStrategy;

import java.util.HashMap;
import java.util.List;

import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecGroup;
import edu.isistan.christian.recommenders.sur.SingleUserRecommender;
import edu.isistan.christian.recommenders.sur.datatypes.SURItem;
import edu.isistan.christian.recommenders.sur.datatypes.SURRecommendation;
import edu.isistan.christian.recommenders.sur.datatypes.SURUser;

public interface RecAggregationStrategy<T extends SURItem> {

	public List<SURRecommendation<T>> aggregateRecommendations (GRecGroup group, 
			HashMap<SURUser, List<SURRecommendation<T>>> membersRecs, int amountOfRecsRequestedPerUser, 
			SingleUserRecommender<T> singleUserRecommender);
	
	public String toString();
}
