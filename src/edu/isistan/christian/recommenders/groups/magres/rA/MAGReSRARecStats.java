package edu.isistan.christian.recommenders.groups.magres.rA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecRecommendationStats;

public class MAGReSRARecStats extends GRecRecommendationStats{
	
	/** key: userID, value= amount of concessions made by the user */
	protected HashMap<String, Integer> concessionsMade;

	/** key: userID, Value= amount of information related to the proposals leaked by the agent that represents to the userID*/	
	protected HashMap<String, Double> infoLeakProposalsRevealed;
	/** key: userID, Value= amount of information related to the utilityFunction leaked by the agent that represents to the userID*/
	protected HashMap<String, Double> infoLeakUtilitiesRevealed;
	
	/** key: userID, Value= amount of proposals revealed/made by the agent that represents the user */
	private HashMap<String, Integer> proposalsMade;
	/** key: userID, Value= amount of proposals accepted by the agent that represents the user */
	private HashMap<String, HashMap<String, Integer>> proposalsAccepted;
	/** key: userID, Value= amount of proposals rejected by the agent that represents the user */
	private HashMap<String, HashMap<String, Integer>> proposalsRejected;

	public MAGReSRARecStats(long recommendationTime) {
		super(recommendationTime);
		this.concessionsMade = new HashMap<>();
		this.infoLeakProposalsRevealed = new HashMap<>();
		this.infoLeakUtilitiesRevealed = new HashMap<>();
		this.itemAlreadyRatedBy = new ArrayList<>();
		this.proposalsMade = new HashMap<>();
		this.proposalsAccepted = new HashMap<>();
		this.proposalsRejected = new HashMap<>();
	}
	
	/**
	 * 
	 * @param userID
	 * @param concessionsMadeAmt
	 * @param proposalsRevealedPercentage
	 * @param utilitiesRevealedPercentage
	 * @param itemAlreadyRated
	 * @param proposalsAcceptedAmt
	 * @param proposalsRejectedAmt
	 */
	public void addUserInfo (String userID, int concessionsMadeAmt, double proposalsRevealedPercentage,
			double utilitiesRevealedPercentage, boolean itemAlreadyRated, int proposalsMadeAmt, 
			HashMap<String, Integer> proposalsAcceptedPerUser, HashMap<String, Integer> proposalsRejectedPerUser){
		this.concessionsMade.put(userID, concessionsMadeAmt);
		this.infoLeakProposalsRevealed.put(userID, proposalsRevealedPercentage);
		this.infoLeakUtilitiesRevealed.put(userID, utilitiesRevealedPercentage);
		if (itemAlreadyRated)
			this.setUserAlreadyRatedItem(userID);
		this.proposalsMade.put(userID, proposalsMadeAmt);
		this.proposalsAccepted.put(userID, proposalsAcceptedPerUser);
		this.proposalsRejected.put(userID, proposalsRejectedPerUser);
	}

	public HashMap<String, Integer> getConcessionsMade() {
		return concessionsMade;
	}
	
	public int getConcessionsMade(String userID) {
		return (concessionsMade.containsKey(userID))? concessionsMade.get(userID): 0;
	}

	public HashMap<String, Double> getInfoLeak_Proposals() {
		return infoLeakProposalsRevealed;
	}

	public HashMap<String, Double> getInfoLeak_UtilityFunction() {
		return infoLeakUtilitiesRevealed;
	}
		
	public List<String> getItemAlreadyRatedBy() {
		return itemAlreadyRatedBy;
	}

	public double getProposalsRevealedPercentage(String userID){
		return (infoLeakProposalsRevealed.containsKey(userID))? infoLeakProposalsRevealed.get(userID): 0.0;
	}
	
	public double getUtilitiesRevealedPercentage(String userID){
		return (infoLeakUtilitiesRevealed.containsKey(userID))? infoLeakUtilitiesRevealed.get(userID): 0.0;
	}

	public boolean hadRatedItem (String userID){
		return itemAlreadyRatedBy.contains(userID);
	}
	
	public int getProposalsMade (String userID) {
		return (this.proposalsMade.containsKey(userID))? this.proposalsMade.get(userID) : 0;
	}
	
	public int getProposalsAccepted (String userID) {
		int accepted = 0;
		for (String otherUserID : this.proposalsAccepted.get(userID).keySet())
			accepted += this.proposalsAccepted.get(userID).get(otherUserID);
		
		return accepted;
	}
	
	public HashMap<String, Integer> getProposalsAcceptedMap (String userID){
		return this.proposalsAccepted.get(userID);
	}
	
	public int getProposalsRejected (String userID) {
		int rejected = 0;
		for (String otherUserID : this.proposalsRejected.get(userID).keySet())
			rejected += this.proposalsRejected.get(userID).get(otherUserID);
		
		return rejected;
	}
	
	public HashMap<String, Integer> getProposalsRejectedMap (String userID){
		return this.proposalsRejected.get(userID);
	}
	
	@Override
	public String toString() {
		return "PUMASGRecRecStats [recommendationTime=" + getRecommendationTime()
				+ ", itemRatingPerUser=" + getItemRatingPerUser()
				+ ", concessionsMade=" + concessionsMade
				+ ", itemAlreadyRatedBy=" + itemAlreadyRatedBy
				+ ", proposalsMade=" + proposalsMade
				+ ", proposalsAccepted=" + proposalsAccepted
				+ ", proposalsRejected=" + proposalsRejected 
				+ ", infoLeakProposalsRevealed=" + infoLeakProposalsRevealed 
				+ ", infoLeakUtilitiesRevealed=" + infoLeakUtilitiesRevealed 
				+ "]";
	}
	
	
	
}
