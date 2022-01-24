package edu.isistan.christian.recommenders.groups.tradGRec.rA.recAggregationStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.isistan.christian.recommenders.groups.commons.aggregationStrategy.AggregationStrategy;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecGroup;
import edu.isistan.christian.recommenders.sur.SingleUserRecommender;
import edu.isistan.christian.recommenders.sur.datatypes.SURItem;
import edu.isistan.christian.recommenders.sur.datatypes.SURPrediction;
import edu.isistan.christian.recommenders.sur.datatypes.SURRecommendation;
import edu.isistan.christian.recommenders.sur.datatypes.SURUser;
import edu.isistan.christian.recommenders.sur.exceptions.SURException;

public class RAStrategySimple<T extends SURItem> implements RecAggregationStrategy<T> {

	private static final Logger logger = LogManager.getLogger(RAStrategySimple.class);

	private AggregationStrategy ratingAggregationStrategy;

	public AggregationStrategy getRatingAggregationStrategy() {
		return ratingAggregationStrategy;
	}

	public void setRatingAggregationStrategy(AggregationStrategy ratingAggregationStrategy) {
		this.ratingAggregationStrategy = ratingAggregationStrategy;
	}

	@Override
	public List<SURRecommendation<T>> aggregateRecommendations(GRecGroup group, 
			HashMap<SURUser, List<SURRecommendation<T>>> membersRecs, int amountOfRecsRequestedPerUser, 
			SingleUserRecommender<T> singleUserRecommender) {

		HashMap<SURRecommendation<T>, Double> groupRatings = new HashMap<>();
		HashMap<String, T> itemsRecommended = new HashMap<>(); //to remember which items were already considered (and avoid repetitions in case the same item was recommended to multiple group members)

		for (SURUser member : group) {
			for (SURRecommendation<T> memberRec : membersRecs.get(member)) {
				if (!itemsRecommended.containsKey(memberRec.getRecommendedItem().getID())) {
					itemsRecommended.put(memberRec.getRecommendedItem().getID(), memberRec.getRecommendedItem());
					
					List<Double> mRatings = new ArrayList<>();
					mRatings.add(memberRec.getPredictedUserRating()); //the predicted rating of the active user
					for (SURUser otherMember : group) {
						if (!otherMember.equals(member)) { //to avoid making an unnecessary prediction
							try {
								SURPrediction<T> p = singleUserRecommender.estimatePreference(otherMember, memberRec.getRecommendedItem());
								if (p.isValid())
									if (!Double.isNaN(p.getPrediction()))
										mRatings.add(singleUserRecommender.estimateUserRating(p));
									else {
										mRatings.add(0.0);
										logger.warn ("The rating prediction for the pair "
												+ "<userID="+otherMember.getID()+",itemID="+memberRec.getRecommendedItem().getID()+"> was NaN. "
												+ "We will replace the NaN with 0.0 for aggregation purposes.");
									}
								else {
									mRatings.add(0.0);
									logger.warn ("The rating prediction for the pair "
											+ "<userID="+otherMember.getID()+",itemID="+memberRec.getRecommendedItem().getID()+"> was invalid. "
											+ "For aggregation purposes we set a 0.0 rating for that <user,item> pair.");
								}
							} catch (SURException e) {
								logger.warn ("Skipping the member ["+member.toString()+"] because the "
										+ "singleUserRecommender.estimatePreference(member, m) thrown an exception ["+e.getMessage()+"]");
								mRatings.add(0.0);
								//skip
							}
						}
					}
					double rAggregated = this.ratingAggregationStrategy.aggregate(mRatings);
					groupRatings.put(memberRec, rAggregated);
				}
			}
		}

		List<Entry<SURRecommendation<T>, Double>> entriesList = new ArrayList<>(groupRatings.entrySet());
		entriesList.sort(Entry.comparingByValue());
		Collections.reverse(entriesList);

		List<SURRecommendation<T>> groupRec = new ArrayList<>();
		for (Entry<SURRecommendation<T>, Double> entry : entriesList)
			groupRec.add(entry.getKey());

		return groupRec;
	}

	@Override
	public String toString() {
		return "RAStrategySimple [ratingAggregationStrategy=" + ratingAggregationStrategy + "]";
	}

}


