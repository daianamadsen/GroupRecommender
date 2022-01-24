package edu.isistan.christian.recommenders.groups.commons.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.isistan.christian.recommenders.sur.datatypes.SURItem;

public class GRecResult <T extends SURItem>{

	/** Every recommendation made to the group, and its statistics */
	protected HashMap<GRecRecommendation<T>, GRecRecommendationStats> recommendation;
	
	/** Group for whom the recommendations were made */
	protected GRecGroup group;
	
	/** Name of the recommender that made the recommendations */
	protected String recommenderName;
	/** Configs of the recommender used to make the recommendations */
	protected GRecConfigs configs;
	/** Time consumed by the recommender while making the recommendations */
	protected long recommendationTotalTime;
	
	public GRecResult (GRecGroup group, String recommenderName, GRecConfigs configs, long recommendationTotalTime){
		recommendation = new HashMap<>();
		this.configs = configs;
		this.group = group;
		this.recommenderName = recommenderName;
		this.recommendationTotalTime = recommendationTotalTime;
	}
	
	public void addRecommendation (GRecRecommendation<T> rec, GRecRecommendationStats recStats){
		recommendation.put(rec, recStats);
	}
	
	public List<GRecRecommendation<T>> getRecommendations(){
		List<GRecRecommendation<T>> recs = new ArrayList<>(recommendation.keySet());
		//because we need to return them in descending order and in the hashmap they don't have an order
		Collections.sort(recs); 
		Collections.reverse(recs);
		return recs;
	}
	
	
	public GRecRecommendation<T> getRecommendationBy (String itemID) throws Exception{
		List<GRecRecommendation<T>> recs = this.getRecommendations();
		
		for (GRecRecommendation<T> r : recs) {
			if (r.getRecommendedItem().getID().equals(itemID))
				return r;
		}
		throw new Exception("There isn't any recommendation with an item with the itemID '"+itemID+"'");
	}
	
	public GRecRecommendationStats getRecommendationStatsBy (String itemID) throws Exception {
		GRecRecommendation<T> rec = this.getRecommendationBy(itemID);
		return this.getRecommendationStats(rec);
	}
	
	public GRecRecommendationStats getRecommendationStats(GRecRecommendation<T> rec){
		return recommendation.get(rec);
	}
		
	public long getRecommendationTotalTime() {
		return recommendationTotalTime;
	}

	public GRecGroup getGroup() {
		return group;
	}

	public String getRecommenderName(){
		return recommenderName;
	}
	
	public GRecConfigs getConfigs(){
		return configs;
	}

	@Override
	public String toString() {
		return "GRecResult [recommendation=" + recommendation + ", configs="
				+ configs + "]";
	}
	
	
}
