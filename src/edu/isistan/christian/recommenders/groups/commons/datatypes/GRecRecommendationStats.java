package edu.isistan.christian.recommenders.groups.commons.datatypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GRecRecommendationStats {

	protected long recommendationTime;
	/** Key: User (member of {@link #group}), Value: estimated rating (/utility) of the {@Link #recommendedItem} for given user*/
	protected HashMap<String, Double> itemRatingPerUser; //key: agentID, Value= the utility value of the result negotiation (from the agent point of view)

	/** Stores the ID's of the users that had already rated the item being recommended 
	 * (in the movies domain: the recommendation is a movie they've already watched*/
	protected List<String> itemAlreadyRatedBy;
	
	public GRecRecommendationStats(long recommendationTime) {
		super();
		this.recommendationTime = recommendationTime;
		this.itemRatingPerUser = new HashMap<>();
		this.itemAlreadyRatedBy = new ArrayList<>();
	}

	public void setUserRatingOfRecommendation (String userID, double rating){
		itemRatingPerUser.put(userID, rating);
	}
	
	/**
	 * Registers the user as one of the group members that had rated the item in the past
	 * @param userID
	 */
	public void setUserAlreadyRatedItem (String userID) {
		itemAlreadyRatedBy.add(userID);
	}

	public List<String> getItemAlreadyRatedBy() {
		return itemAlreadyRatedBy;
	}
	
	public boolean hadRatedItem (String userID){
		return itemAlreadyRatedBy.contains(userID);
	}
	
	public long getRecommendationTime() {
		return recommendationTime;
	}

	public HashMap<String, Double> getItemRatingPerUser() {
		return itemRatingPerUser;
	}

	public double getItemRatingForUser (String userID){
		return (itemRatingPerUser.containsKey(userID))? itemRatingPerUser.get(userID): 0.0;
	}

	@Override
	public String toString() {
		return "GRecRecommendationStats [recommendationTime="
				+ recommendationTime + ", itemRatingPerUser="
				+ itemRatingPerUser + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((itemRatingPerUser == null) ? 0 : itemRatingPerUser
						.hashCode());
		result = prime * result
				+ (int) (recommendationTime ^ (recommendationTime >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GRecRecommendationStats other = (GRecRecommendationStats) obj;
		if (itemRatingPerUser == null) {
			if (other.itemRatingPerUser != null)
				return false;
		} else if (!itemRatingPerUser.equals(other.itemRatingPerUser))
			return false;
		if (recommendationTime != other.recommendationTime)
			return false;
		return true;
	}

}
