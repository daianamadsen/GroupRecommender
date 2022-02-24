package edu.isistan.christian.recommenders.groups.magres;

import java.util.HashMap;

import edu.isistan.christian.recommenders.groups.GroupRecommender;
import edu.isistan.christian.recommenders.groups.commons.aggregationStrategy.AggregationStrategy;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecGroup;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecResult;
import edu.isistan.christian.recommenders.groups.commons.pumas.PUMASAgentProfile;
import edu.isistan.christian.recommenders.sur.SingleUserRecommender;
import edu.isistan.christian.recommenders.sur.datatypes.SURItem;
import edu.isistan.christian.recommenders.sur.datatypes.SURUser;
import edu.isistan.christian.recommenders.sur.exceptions.SURException;

public abstract class MAGReS <T extends SURItem> extends GroupRecommender<T>{

	public MAGReS(SingleUserRecommender<T> sur, AggregationStrategy groupRatingEstimationStrategy) {
		super(sur, groupRatingEstimationStrategy);
	}
	
	public void initialize(boolean forceReInitialize) throws SURException{
		this.singleUserRecommender.initialize(forceReInitialize);
	}

	public boolean isInitialized(){
		return this.singleUserRecommender.isInitialized();
	}

	//-------------------------------- RECOMMENDATION 

	public boolean canRecommend(){
		return this.singleUserRecommender.canRecommend();
	}

	public int getAmountOfRecommendableItemsTo (SURUser user) throws SURException{
		return this.singleUserRecommender.getAmountOfRecommendableItemsTo(user);
	}
	
	public GRecResult<T> recommend(GRecGroup group, int howMany) throws SURException{
		return this.recommend(group, null, howMany, null, null, null);
	}
	
	@Override
	/**
	 * All the agents will use the default profile. This method exists just to comply with the interface
	 * of the GroupRecommender class
	 */
	public GRecResult<T> recommend(GRecGroup group, int howMany, HashMap<SURUser, Double> assertivenessFactors, HashMap<SURUser, Double> cooperativenessFactors, HashMap<SURUser, HashMap<SURUser, Double>> relationshipsFactors) throws SURException{
		return this.recommend(group, null, howMany, assertivenessFactors, cooperativenessFactors, relationshipsFactors);
	}
	
	/**
	 * 
	 * @param group
	 * @param userAgProfiles
	 * @param howMany
	 * @return
	 * @throws SURException
	 */
	public abstract GRecResult<T> recommend (GRecGroup group, HashMap<SURUser, PUMASAgentProfile<T>> userAgProfiles, int howMany, HashMap<SURUser, Double> assertivenessFactors, HashMap<SURUser, Double> cooperativenessFactors, HashMap<SURUser, HashMap<SURUser, Double>> relationshipsFactors) 
			throws SURException;

}
