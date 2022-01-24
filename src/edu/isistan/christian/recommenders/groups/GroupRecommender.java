package edu.isistan.christian.recommenders.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.isistan.christian.recommenders.groups.commons.aggregationStrategy.AggregationStrategy;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecGroup;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecResult;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GroupUser;
import edu.isistan.christian.recommenders.groups.utils.MAEData;
import edu.isistan.christian.recommenders.sur.SingleUserRecommender;
import edu.isistan.christian.recommenders.sur.datatypes.SURItem;
import edu.isistan.christian.recommenders.sur.datatypes.SURPrediction;
import edu.isistan.christian.recommenders.sur.datatypes.SURRating;
import edu.isistan.christian.recommenders.sur.datatypes.SURUser;
import edu.isistan.christian.recommenders.sur.exceptions.SURException;
import edu.isistan.christian.recommenders.sur.exceptions.SURInexistentItemException;

public abstract class GroupRecommender <T extends SURItem>{
	
	private static final Logger logger = LogManager.getLogger(GroupRecommender.class);
	
	protected SingleUserRecommender<T> singleUserRecommender;
	protected AggregationStrategy groupRatingEstimationStrategy;
	
	public GroupRecommender(SingleUserRecommender<T> sur, AggregationStrategy groupRatingEstimationStrategy){
		this.singleUserRecommender = sur;
		this.groupRatingEstimationStrategy = groupRatingEstimationStrategy;
	}
	
	public SingleUserRecommender<T> getSingleUserRecommender(){
		return this.singleUserRecommender;
	}

	public abstract void initialize(boolean forceReInitialize) throws SURException;
	
	public abstract boolean isInitialized();
	
	public abstract boolean canRecommend();
	
	public abstract GRecResult<T> recommend(GRecGroup group, int howMany) throws SURException;
	
	public abstract void forgetPastRecommendations();
	
	//Used to build the 
	protected double estimateGroupRating (GRecGroup group, T recommendedItem) throws SURException{
		List<Double> userRatings = new ArrayList<>();
		for (SURUser member : group){
			double r;
			try {
				r = this.estimateUserRating(this.estimatePreference(member, recommendedItem));
			} catch (SURException e) {
				logger.warn("There was an issue will trying to estimate the preference of the member "+member+" over the item "+recommendedItem+". Cause: "+e.getMessage());
				r= 0;
			}
			userRatings.add(r);
		}

		return groupRatingEstimationStrategy.aggregate(userRatings);
	}
	
	//-------------------------------- ADD/REMOVE PREFERENCES

	public void addPreference(SURUser user, T item, double prefValue, double certainty) throws SURException{
		this.singleUserRecommender.addPreference(user, item, prefValue, certainty);
	}

	public void removePreference (SURUser user, T item) throws SURException{
		this.singleUserRecommender.removePreference(user, item);
	}

	public SURPrediction<T> estimatePreference (SURUser user , T item) throws SURException{
		return this.singleUserRecommender.estimatePreference(user, item);
	}
	
	public double estimateUserRating (SURPrediction<T> prediction) throws SURException{
		return this.singleUserRecommender.estimateUserRating(prediction);
	}
	
	/**
	 * 
	 * @param user
	 * @param item
	 * @return true if "user" has expressed a preference over "item" (meaning: "user" has rated the "item"
	 * @throws SURException
	 */
	public boolean hasPreferenceOver (SURUser user, T item) throws SURException{
		return this.singleUserRecommender.hasPreferenceOver(user, item);
	}
	
	/**
	 * 
	 * @param user1
	 * @param user2
	 * @return the similarity value among to users using the similarity metric 
	 * @throws SURException
	 */
	public double getSimilarityBetween (SURUser user1, SURUser user2) throws SURException{
		return this.singleUserRecommender.getSimilarityBetween(user1, user2);
	}
	
	//-------------------------------- ADD/REMOVE USERS

	public void addUser (SURUser newUser) throws SURException{
		this.singleUserRecommender.addUser(newUser);
	}
	
	public void removeUser (String userID) throws SURException{
		this.singleUserRecommender.removeUser(userID);
	}
	
	//-------------------------------- ADD/REMOVE ITEMS
	
	public void addItem (T item) throws SURException{
		this.singleUserRecommender.addItem(item);
	}

	public void removeItem(String itemID) throws SURException{
		this.singleUserRecommender.removeItem(itemID);
	}
	
	//-------------------------------- GETTER's

	public List<SURUser> getAllUsers(){
		return this.singleUserRecommender.getAllUsers();
	}

	public SURUser getUser(String userID) throws SURException{
		return this.singleUserRecommender.getUser(userID);
	}


	public List<T> getAllItems(){
		return this.singleUserRecommender.getAllItems();
	}

	public T getItem(String itemID) throws SURException{
		return this.singleUserRecommender.getItem(itemID);
	}

	public List<T> getItemsRatedBy(String userID) throws SURException{
		return this.singleUserRecommender.getItemsRatedBy(userID);
	}


	public List<SURRating> getRatingsGivenBy (String userID) throws SURException{
		return this.singleUserRecommender.getRatingsGivenBy(userID);
	}

	public List<SURRating> getRatingsGivenBy (SURUser user) throws SURException{
		return this.singleUserRecommender.getRatingsGivenBy(user);
	}


	public List<SURRating> getRatingsGivenTo (T item) throws SURException{
		return this.singleUserRecommender.getRatingsGivenTo(item);

	}

	public List<SURRating> getRatingsGivenTo (String itemID) throws SURException{
		return this.singleUserRecommender.getRatingsGivenTo(itemID);
	}

	//-------------------------------- GENERIC
	
	public abstract String toString();
	
	
	//-------------------------------- MAE computation
	
	private List<T> getItemsRatedByEveryone (GRecGroup group) throws SURException{
		List<T> ratedByEveryone = new ArrayList<>();
		
		//Get the full list of the items rated by the group members and count repetitions
		HashMap<T, Integer> allItemsRated = new HashMap<>();
		for (SURUser member : group){
			List<T> ratedByUser = singleUserRecommender.getItemsRatedBy(member.getID()); 
			for (T item : ratedByUser) {
				allItemsRated.compute(item, (key, value) -> value == null ? 1 : value+1);
			}
		}
		
		for (T item : allItemsRated.keySet()) {
			if (allItemsRated.get(item) == group.size()) //rated by every member of the group
				ratedByEveryone.add(item);
		}
		
		return ratedByEveryone;
	}
	
	private List<T> getTestingItems (List<T> items, double trainingPercentage) throws SURException{
		if (trainingPercentage >= 1.0)
			throw new SURException ("The training percentage was set to "+trainingPercentage+", and so it is impossible to create "
					+ "the testing set (Remember: trainingPercentage and testingPercentage must add up 1.0)");
		
		double testingPercentage = 1.0 - trainingPercentage;
		int testingItemsCount = (int) (items.size() * testingPercentage);
		
		if (testingItemsCount == 0)
			throw new SURException ("The list of items size is "+items.size()+" and the 'testingPercentage' is "+testingPercentage
					+" (trainingPercentage = "+trainingPercentage+"). Because of this the amount of items to select for testing is 0");
		
		List<T> testingItems = new ArrayList<>(items);
		Collections.shuffle(testingItems);
		
		return testingItems.subList(0, testingItemsCount); 
	}
	
	private SURRating computeGroupRating (T item, GRecGroup group, AggregationStrategy aggregationStrategy) {
		List<Double> mRatings = new ArrayList<>();
		List<Double> mCertainty = new ArrayList<>();
		
		for (SURUser member : group){
			try {
				SURPrediction<T> p = singleUserRecommender.estimatePreference(member, item);
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
		
		return new SURRating(group.getID(), item.getID(), aggregatedRating, aggregatedCertainty);
	}
	
	private HashMap<T, SURRating> computeGroupRatings (List<T> items, GRecGroup group, AggregationStrategy aggregationStrategy){
		HashMap<T, SURRating> groupRatings = new HashMap<>();
		
		for (T item : items){
			groupRatings.put(item, this.computeGroupRating(item, group, aggregationStrategy));		
		}
		return groupRatings;
	}
	
	public MAEData computeMAE (GRecGroup group, double trainingPercentage, AggregationStrategy aggregationStrategy) throws SURException {
		
		//Create the groupUser
		SURUser groupUser = new GroupUser(group); //be careful with the groupUser ID!! It should not be an ID of the user in the singleUserRecommender and must fulfill the singleUserRecommender rules for user IDs (datatypes used by them)
		
		List<T> ratedByEveryone = this.getItemsRatedByEveryone(group);
		
		//Generate the testing set
		List<T> testingItems = this.getTestingItems (ratedByEveryone, trainingPercentage);
		HashMap<T, SURRating> testingItemsRatings = this.computeGroupRatings(testingItems, group, aggregationStrategy);
		
		//Generate the training set
		ratedByEveryone.removeAll(testingItems); //we don't need to keep the items in the ratedByEveryone list (because of this we don't create a new list)
		
		//Compute the ratings for the items in the training set
		List<SURRating> groupRatings = new ArrayList<> (this.computeGroupRatings(ratedByEveryone, group, aggregationStrategy).values());
		
		//Add the ratings in the training set to the SUR
		for (SURRating r : groupRatings) {
			try {
				singleUserRecommender.addPreference(groupUser, this.getItem(r.getItemID()), r.getRating(), r.getCertainty());
			} catch (SURInexistentItemException e) {
				//skip item if does not exist
				logger.warn("Skipped group rating ["+r.toString()+"] because: "+e.getMessage());
			}
		}
		
		//Begin MAE computation (for each of the testing items predict its group rating and compute the MAE
		double mae = 0.0;
		int count = 0;
		for (T item : testingItems) {
			try {
				SURPrediction<T> p = singleUserRecommender.estimatePreference(groupUser, item);
				if (p.isValid() && !Double.isNaN(p.getPrediction())) {
					mae += Math.abs(testingItemsRatings.get(item).getRating() - p.getPrediction());
					count++;
				}
			} catch (Exception e){
				logger.error ("Skipping item ["+item.getID()+"] because the prediction is not valid or the estimation is NaN");
			}
		}
		
		mae /= count;
		double coverage = count/(double)testingItems.size();
		return new MAEData(mae, coverage);
	}
	
	public HashMap<GRecGroup, MAEData> computeMAE (List<GRecGroup> groups, double trainingPercentage, AggregationStrategy aggregationStrategy) throws SURException {
		HashMap<GRecGroup, MAEData> maes = new HashMap<>();
		for (GRecGroup group : groups) {
			try {
				maes.put(group, this.computeMAE(group, trainingPercentage, aggregationStrategy));
			} catch (SURException e) {
				logger.error ("Group ["+group+"] skipped due to an error when computing the MAE. Cause: "+ e.getMessage());
			}
		}
		
		return maes;
	}
	
	public MAEData computeAggregatedMAE (HashMap<GRecGroup, MAEData> groupMAEs) throws SURException{
		if (groupMAEs.isEmpty())
			throw new SURException ("Impossible to compute the aggregated MAE. The hashmap received is empty");
		
		double mae_score = 0.0;
		double coverage = 0.0;
		for (GRecGroup group : groupMAEs.keySet()) {
			mae_score += groupMAEs.get(group).getMaeScore();
			coverage += groupMAEs.get(group).getCoverage();
		}
		
		mae_score /= groupMAEs.size();
		coverage /= groupMAEs.size();
		
		return new MAEData(mae_score, coverage);
	}

}
