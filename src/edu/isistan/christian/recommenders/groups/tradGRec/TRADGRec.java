package edu.isistan.christian.recommenders.groups.tradGRec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.isistan.christian.recommenders.groups.GroupRecommender;
import edu.isistan.christian.recommenders.groups.commons.aggregationStrategy.AggregationStrategy;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecRecommendation;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecRecommendationStats;
import edu.isistan.christian.recommenders.sur.SingleUserRecommender;
import edu.isistan.christian.recommenders.sur.datatypes.SURItem;
import edu.isistan.christian.recommenders.sur.datatypes.SURPrediction;
import edu.isistan.christian.recommenders.sur.datatypes.SURUser;
import edu.isistan.christian.recommenders.sur.exceptions.SURException;

public abstract class TRADGRec <T extends SURItem> extends GroupRecommender<T>{
	private static final Logger logger = LogManager.getLogger(TRADGRec.class);

	public TRADGRec(SingleUserRecommender<T> sur, AggregationStrategy groupRatingEstimationStrategy) {
		super(sur, groupRatingEstimationStrategy);
	}

	public void initialize(boolean forceReInitialize) throws SURException{
		this.singleUserRecommender.initialize(forceReInitialize);
	}

	public boolean isInitialized(){
		return this.singleUserRecommender.isInitialized();
	}

	//-------------------------------- RECOMMENDATION 
	
	//	public List<SURRecommendation<T>> recommend(SURUser user, int howMany) throws SURException{
	//		return this.singleUserRecommender.recommend(user, howMany);
	//	}

	public boolean canRecommend(){
		return this.singleUserRecommender.canRecommend();
	}
	
	public int getAmountOfRecommendableItemsTo (SURUser user) throws SURException{
		return this.singleUserRecommender.getAmountOfRecommendableItemsTo(user);
	}
	
	//------- BUILD RESULTS DATA
	
	protected GRecRecommendationStats buildRecStats(
			GRecRecommendation<T> rec, long recommendationTime) {
		GRecRecommendationStats stats = new GRecRecommendationStats(recommendationTime);
		for (SURUser member : rec.getGroup()){
			SURPrediction<T> p;
			try {
				p = this.estimatePreference(member, rec.getRecommendedItem());
				stats.setUserRatingOfRecommendation(member.getID(), this.estimateUserRating(p));
			} catch (SURException e) {
				logger.warn ("Skipping: "+ member.toString() + "(Cause: "+e.getMessage()+")");
				e.printStackTrace();
			}
			
			boolean hasPreferenceOverItem = false;
			try {
				hasPreferenceOverItem = this.hasPreferenceOver(member, rec.getRecommendedItem());
			} catch (SURException e) {
				hasPreferenceOverItem = false;
				logger.warn ("Problem with group member: "+ member.toString() + "(Cause: "+e.getMessage()+")");
			}
			if (hasPreferenceOverItem) 
				stats.setUserAlreadyRatedItem(member.getID());
		}
		return stats;
	}
	
	
}
