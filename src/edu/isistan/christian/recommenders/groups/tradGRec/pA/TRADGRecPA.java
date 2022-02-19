package edu.isistan.christian.recommenders.groups.tradGRec.pA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.isistan.christian.recommenders.groups.commons.aggregationStrategy.AggregationStrategy;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecGroup;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecRecommendation;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecResult;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GroupUser;
import edu.isistan.christian.recommenders.groups.tradGRec.TRADGRec;
import edu.isistan.christian.recommenders.sur.datatypes.SURItem;
import edu.isistan.christian.recommenders.sur.datatypes.SURPrediction;
import edu.isistan.christian.recommenders.sur.datatypes.SURRating;
import edu.isistan.christian.recommenders.sur.datatypes.SURRecommendation;
import edu.isistan.christian.recommenders.sur.datatypes.SURUser;
import edu.isistan.christian.recommenders.sur.exceptions.SURException;
import edu.isistan.christian.recommenders.sur.exceptions.SURInexistentItemException;
import edu.isistan.christian.recommenders.sur.exceptions.SURInexistentUserException;

public abstract class TRADGRecPA <T extends SURItem> extends TRADGRec<T> {

	private static final Logger logger = LogManager.getLogger(TRADGRecPA.class);
	protected TRADGRecPAConfigs<T> configs;
	
	protected AggregationStrategy aggregationStrategy;

	public TRADGRecPA(TRADGRecPAConfigs<T> configs){
		super(configs.getSUR(), configs.getGroupRatingEstimationStrategy());
		this.aggregationStrategy = configs.getAggregationStrategy();
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

	//------- RECOMMEND
	
	public GRecResult<T> recommend(GRecGroup group, int howMany) throws SURException{
		return this.recommend(group, howMany, null, null, null);
	}
	
	public GRecResult<T> recommend(GRecGroup group, int howMany, HashMap<SURUser, Double> assertivenessFactors, HashMap<SURUser, Double> cooperativenessFactors, HashMap<SURUser, HashMap<SURUser, Integer>> relationshipsFactors) throws SURException{

		long recommendationTimeTotal = 0;
		StopWatch timer = new StopWatch();
		timer.start();

		//Create the group profile
		// * We need to ensure that the group user id is unique, but as it depends of the group object passed through parameters that check will
		// only depend of the possibility of allowing the concurrent creation of group objects. AS the ID is built automatically we need to ensure that
		// it is unique in the constructor of said object.
		SURUser groupUser = new GroupUser(group); //be careful with the groupUser ID!! It should not be an ID of the user in the singleUserRecommender and must fulfill the singleUserRecommender rules for user IDs (datatypes used by them)

		//Add the user to the recommender
		singleUserRecommender.addUser(groupUser);

		/// We need to get a list of ratings for the group which should be computed using the ratings of the members aggregated with some technique
		List<SURRating> groupRatings = this.getGroupRatings(group, assertivenessFactors, cooperativenessFactors, relationshipsFactors);

		//Add all the ratings computed as feedbacks, this recommender will be able to use them to make recommendations
		//			singleUserRecommender.addRating(groupRatings);
		for (SURRating r : groupRatings)
			try {
				singleUserRecommender.addPreference(groupUser, this.getItem(r.getItemID()), r.getRating(), r.getCertainty());
			} catch (SURInexistentItemException e) {
				//skip item if does not exist
				logger.warn("Skipped group rating ["+r.toString()+"] because: "+e.getMessage());
			}

		//Temporarily remove the group members from the SUR
		HashMap<SURUser, List<SURRating>> backup = new HashMap<>();
		for (SURUser member : group) {
			backup.put(member, singleUserRecommender.getRatingsGivenBy(member.getID()));
			singleUserRecommender.removeUser(member.getID());
		}
		

		List<SURRecommendation<T>> recommendations = singleUserRecommender.recommend(groupUser, howMany);

		//UNDO OPERATIONS: remove the group data from the recommender
		singleUserRecommender.removeUser(groupUser.getID());	//it will remove the user and its preferences
		
		//Restore the backup
		for (SURUser member : backup.keySet()) {
			singleUserRecommender.addUser(member);
			List<SURRating> uRatings = backup.get(member); 
			for (SURRating uRating : uRatings)
				singleUserRecommender.addPreference(member, singleUserRecommender.getItem(uRating.getItemID()),
						uRating.getRating(), uRating.getCertainty());
		}
		
		//Stop the timer
		timer.stop();
		recommendationTimeTotal = timer.getTime();

		//Build the result object and return it
		return buildResult(group, recommendations, recommendationTimeTotal);
	}

	/**
	 * 
	 * @param gMembers
	 * @return
	 */
	protected List<SURRating> getGroupRatings (GRecGroup group, HashMap<SURUser, Double> assertivenessFactors, HashMap<SURUser, Double> cooperativenessFactors, HashMap<SURUser, HashMap<SURUser, Integer>> relationshipsFactors) throws SURInexistentUserException{
		List<SURRating> groupRatings = new ArrayList<>();

		//Get the full list of the items rated by the group members and estimate the group rating
		Set<T> allItemsRated = new HashSet<>();  //we use a set to avoid adding repeated id's if we don't check if they were already added
		for (SURUser member : group){
			allItemsRated.addAll(singleUserRecommender.getItemsRatedBy(member.getID()));
		}

		//Aggregate the ratings and create a RatableItemFeedback
		for (T m : allItemsRated){
			List<Double> mRatings = new ArrayList<>();
			List<Double> mCertainty = new ArrayList<>();
			for (SURUser member : group){
				try {
					SURPrediction<T> p = singleUserRecommender.estimatePreference(member, m);
					if (p.isValid())
						if (!Double.isNaN(p.getPrediction()) && !Double.isNaN(p.getCertainty())){
							mRatings.add(singleUserRecommender.estimateUserRating(p));
							mCertainty.add(p.getCertainty());
						}
						else
							logger.error ("Skipping member's preference for member ["+member.toString()+"] because the prediction of the estimation or the certainty are NaN ["+p.toString()+"]");
				} catch (SURException e) {
					logger.error("Skipping the member ["+member.toString()+"] because the "
							+ "singleUserRecommender.estimatePreference(member, m) thrown an exception ["+e.getMessage()+"]");
					//skip
				}

			}

			double aggregatedRating = aggregationStrategy.aggregate(mRatings);
			double aggregatedCertainty = aggregationStrategy.aggregate(mCertainty);
			groupRatings.add(new SURRating(group.getID(), m.getID(), aggregatedRating, aggregatedCertainty));		
		}

		return groupRatings;
	}

	//Maybe this method is not needed
	public void forgetPastRecommendations(){ 
		this.singleUserRecommender.forgetPastRecommendations();
	}
}
