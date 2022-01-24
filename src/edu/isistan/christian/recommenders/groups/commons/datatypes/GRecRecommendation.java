package edu.isistan.christian.recommenders.groups.commons.datatypes;

import edu.isistan.christian.recommenders.sur.datatypes.SURItem;

public class GRecRecommendation<T extends SURItem> implements Comparable<GRecRecommendation<T>>{
	
	protected GRecGroup group;
	protected T recommendedItem;
	protected double predictedGroupRating;
	protected boolean valid;

	public GRecRecommendation(GRecGroup group, T recommendedItem,
			double predictedGroupRating, boolean valid) {
		super();
		this.group = group;
		this.recommendedItem = recommendedItem;
		this.predictedGroupRating = predictedGroupRating;
		this.valid = valid;
	}

	
	public GRecGroup getGroup() {
		return group;
	}

	public T getRecommendedItem() {
		return recommendedItem;
	}

	public double getPredictedGroupRating() {
		return predictedGroupRating;
	}

	public boolean isValid() {
		return valid;
	}

	@Override
	public String toString() {
		return "GRecRecommendation [group=" + group + ", recommendedItem="
				+ recommendedItem + ", predictedGroupRating="
				+ predictedGroupRating + ", valid=" + valid
				+ "]";
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		long temp;
		temp = Double.doubleToLongBits(predictedGroupRating);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((recommendedItem == null) ? 0 : recommendedItem.hashCode());
		result = prime * result + (valid ? 1231 : 1237);
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
		@SuppressWarnings("unchecked")
		GRecRecommendation<T> other = (GRecRecommendation<T>) obj;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (Double.doubleToLongBits(predictedGroupRating) != Double
				.doubleToLongBits(other.predictedGroupRating))
			return false;
		if (recommendedItem == null) {
			if (other.recommendedItem != null)
				return false;
		} else if (!recommendedItem.equals(other.recommendedItem))
			return false;
		if (valid != other.valid)
			return false;
		return true;
	}


	@Override
	public int compareTo(GRecRecommendation<T> otherRec) {
		return Double.compare(this.getPredictedGroupRating(), otherRec.getPredictedGroupRating());
	}
	
	
	
}
