package edu.isistan.christian.recommenders.groups.tradGRec.rA;

import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecGroup;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecRecommendation;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecResult;
import edu.isistan.christian.recommenders.groups.tradGRec.TRADGRec;
import edu.isistan.christian.recommenders.groups.tradGRec.rA.recAggregationStrategy.RecAggregationStrategy;
import edu.isistan.christian.recommenders.groups.tradGRec.rA.recsPerUserStrategy.RecsPerUserStrategy;
import edu.isistan.christian.recommenders.sur.datatypes.SURItem;
import edu.isistan.christian.recommenders.sur.datatypes.SURRecommendation;
import edu.isistan.christian.recommenders.sur.datatypes.SURUser;
import edu.isistan.christian.recommenders.sur.exceptions.SURException;

public abstract class TRADGRecRA <T extends SURItem> extends TRADGRec<T> {

	private static final Logger logger = LogManager.getLogger(TRADGRecRA.class);
	protected TRADGRecRAConfigs<T> configs;
	
	protected RecAggregationStrategy<T> recAggregationStrategy;
	protected RecsPerUserStrategy recsPerUserStrategy;

	public TRADGRecRA(TRADGRecRAConfigs<T> configs){
		super(configs.getSUR(), configs.getGroupRatingEstimationStrategy());
		this.recAggregationStrategy = configs.getRecAggregationStrategy();
		this.recsPerUserStrategy = configs.getRecsPerUserStrategy();
		this.configs = configs;
	}	

	//-------------------------------- RECOMMENDATION 
	
	//------- BUILD RESULTS DATA

	protected GRecResult<T> buildResult (GRecGroup group, List<SURRecommendation<T>> recommendations, long recommendationTotalTime){
		GRecResult<T> recResult = new GRecResult<>(group, this.getClass().getSimpleName(), configs, recommendationTotalTime);
		for (SURRecommendation<T> rec : recommendations){
			//Build the group recommendation
			//	Here we do not use the predicted rating (rec.getPredictedUserRating()) for the recommendation made to the 
			//	group user created by this approach. We aggregate the ratings of the individual users.
			try {
				GRecRecommendation<T> groupRec = new GRecRecommendation<T>(group, rec.getRecommendedItem(), 
						estimateGroupRating(group, rec.getRecommendedItem()), rec.isValid());
				//Compute the recommendation time
				long recTime = recommendationTotalTime/(long) recommendations.size(); //for this item (average)
				recResult.addRecommendation(groupRec, buildRecStats(groupRec, recTime));
			} catch (SURException e) {
				logger.error ("Skipping result: "+rec+". Cause: "+e.getMessage());
			}			
		}

		return recResult;
	}

	public GRecResult<T> recommend(GRecGroup group, int howMany) throws SURException{

		long recommendationTimeTotal = 0;
		StopWatch timer = new StopWatch();
		timer.start();

		//Ask for recommendations for every group member
		HashMap<SURUser, List<SURRecommendation<T>>> membersRecs = new HashMap<>();
		
		int amountOfRecsPerUser = this.recsPerUserStrategy.getAmountOfRecsPerUser(howMany, singleUserRecommender.getAllItems().size(), group);
		for (SURUser member : group) {
			membersRecs.put(member, singleUserRecommender.recommend(member, amountOfRecsPerUser)); ///this will make the SUR put the recs on the cache. I need to undo this!
		}
		
		//Use an strategy to select the recommendations for the group
		List<SURRecommendation<T>> recommendations = recAggregationStrategy.aggregateRecommendations(group, membersRecs, amountOfRecsPerUser, this.singleUserRecommender);
		recommendations = recommendations.subList(0, howMany); //keep the first "howMany" items
		
		
		for (SURUser member : group) {
			singleUserRecommender.forgetPastRecommendations(member); //quiz�s es muy extremo porque deshace todo lo del usuario, no solo lo que le recomend� //TODO revise this
		}
		
		//Stop the timer
		timer.stop();
		recommendationTimeTotal = timer.getTime();

		//Build the result object and return it
		return buildResult(group, recommendations, recommendationTimeTotal);
	}

	public GRecResult<T> recommend(GRecGroup group, int howMany, boolean p, boolean r) throws SURException{
		return this.recommend(group, howMany);
	}

	//Maybe this method is not needed
	public void forgetPastRecommendations(){ 
		this.singleUserRecommender.forgetPastRecommendations();
	}
}
