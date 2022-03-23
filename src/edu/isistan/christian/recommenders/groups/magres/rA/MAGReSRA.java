package edu.isistan.christian.recommenders.groups.magres.rA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecGroup;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecRecommendation;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecResult;
import edu.isistan.christian.recommenders.groups.commons.pumas.PUMASAgentProfile;
import edu.isistan.christian.recommenders.groups.magres.MAGReS;
import edu.isistan.christian.recommenders.sur.datatypes.SURItem;
import edu.isistan.christian.recommenders.sur.datatypes.SURPrediction;
import edu.isistan.christian.recommenders.sur.datatypes.SURUser;
import edu.isistan.christian.recommenders.sur.exceptions.SURException;
import edu.isistan.pumas.framework.dataTypes.NegotiableItemSimple;
import edu.isistan.pumas.framework.dataTypes.NegotiationResult;
import edu.isistan.pumas.framework.protocols.PUMASCoordinatorAg;
import edu.isistan.pumas.framework.protocols.commons.exceptions.ZeroAgentsInCoordinatorException;
import edu.isistan.pumas.framework.protocols.commons.userAgents.UserAg;

public abstract class MAGReSRA <T extends SURItem> extends MAGReS<T>{

	private static final Logger logger = LogManager.getLogger(MAGReSRA.class);

	protected MAGReSRAConfigs<T> configs;

	public MAGReSRA(MAGReSRAConfigs<T> configs){
		super(configs.getPUMASConfigs().getSUR(), configs.getGroupRatingEstimationStrategy());
		this.configs = configs;
	}

	//-------------------------------- RECOMMENDATION 
	
	//------- BUILD RESULTS DATA
	
	protected MAGReSRARecStats buildRecStats (GRecRecommendation<T> rec, long recommendationTime, NegotiationResult<T> result){
		MAGReSRARecStats stats = new MAGReSRARecStats(recommendationTime);
		for (SURUser member : rec.getGroup()){
			try {
				SURPrediction<T> p = this.estimatePreference(member, rec.getRecommendedItem());
				stats.setUserRatingOfRecommendation(member.getID(), this.estimateUserRating(p));
			} catch (SURException e) {
				stats.setUserRatingOfRecommendation(member.getID(), 0.0); //default: 0
				logger.warn ("Problem with group member: "+ member.toString() + "(Cause: "+e.getMessage()+")");
			}
				
				//Add exclusive PUMAS information
				String agentRepresenterID = result.getAgentIDLinkedTo(member.getID());
				
				List<SURUser> otherMembers = new ArrayList<>(rec.getGroup());
				otherMembers.remove(member);
				
				//Create the user accept y reject map (in the "result" the maps use agents IDs, we need maps with user ids instead
				HashMap<String, Integer> userAcceptMap = new HashMap<>();
				HashMap<String, Integer> userRejectMap = new HashMap<>();
				
				for (SURUser otherMember : otherMembers) {
					String otherAgID = result.getAgentIDLinkedTo(otherMember.getID());
					Integer accepts = result.getProposalsAcceptedMap(agentRepresenterID).get(otherAgID); // amount of proposals created by agent representing "otherMember" that were ACCEPTED by the agent representing "member"
					Integer rejects = result.getProposalsRejectedMap(agentRepresenterID).get(otherAgID); // amount of proposals created by agent representing "otherMember" that were ACCEPTED by the agent representing "member"
					userAcceptMap.put(otherMember.getID(), (accepts == null)? 0 : accepts);
					userRejectMap.put(otherMember.getID(), (rejects == null)? 0 : rejects);
				}
				
				boolean hasPreferenceOverItem = false;
				try {
					hasPreferenceOverItem = this.hasPreferenceOver(member, rec.getRecommendedItem());
				} catch (SURException e) {
					hasPreferenceOverItem = false;
					logger.warn ("Problem with group member: "+ member.toString() + "(Cause: "+e.getMessage()+")");
				}
				
				stats.addUserInfo(member.getID(), result.getConcessionsMadeBy(agentRepresenterID),
						result.getProposalsRevealedPercentage(agentRepresenterID),
						result.getUtilitiesRevealedPercentage(agentRepresenterID),
						hasPreferenceOverItem,
						result.getProposalsMade(agentRepresenterID),
						userAcceptMap,
						userRejectMap
						);
		}
		return stats;
	}

	protected GRecResult<T> buildResult (GRecGroup group, List<NegotiationResult<T>> negotiationResults, 
			long recommendationTotalTime){
		GRecResult<T> recResult = new GRecResult<>(group, this.getClass().getSimpleName(), this.configs, recommendationTotalTime);

		for (NegotiationResult<T> result : negotiationResults){
			if (!result.wasConflict()){
				if (result.getResultProposal().getItemProposed() instanceof NegotiableItemSimple){ //TODO. Find a better solution: I really hate this but is the only way to do it right now (to be able to reject bad uses of this recommender - TEMPORAL
					//Build the group recommendation object
					List<T> items = ((NegotiableItemSimple<T>)result.getResultProposal().getItemProposed()).getItems(); //TODO change this
					T recommendedItem = items.get(0);
					GRecRecommendation<T> rec;
					try {
						rec = new GRecRecommendation<T>(group, recommendedItem, 
								this.estimateGroupRating(group, recommendedItem), true);
						recResult.addRecommendation(rec, buildRecStats(rec, result.getExecutionTimeInMillis(), result));
					} catch (SURException e) {
						logger.error ("Skipping result: "+result+". Cause: "+e.getMessage());
					}
					
				}
				else
					logger.error("The type of the item within the proposal produced as a result of the negotiation "
							+ "is not supported by this recommender. "
							+ "It should be 'NegotiableItemSimple' and it is: "
							+result.getResultProposal().getItemProposed().getClass().getSimpleName());
			}
		}
		return recResult;
	}
	
	//------- RECOMMEND
	
	//TODO IMPLEMENT THIS => this method maybe needs to run a thread so this way will be able to run concurrently
	public GRecResult<T> recommend (GRecGroup group, HashMap<SURUser, PUMASAgentProfile<T>> userAgProfiles, int howMany) 
			throws SURException {
		return this.recommend(group, userAgProfiles, howMany, null, null, null);
	}

	public GRecResult<T> recommend (GRecGroup group, HashMap<SURUser, PUMASAgentProfile<T>> userAgProfiles, int howMany, HashMap<SURUser, Double> assertivenessFactors, HashMap<SURUser, Double> cooperativenessFactors, HashMap<SURUser, HashMap<SURUser, Double>> relationshipsFactors) 
			throws SURException {

		long recommendationTimeTotal = 0;
		StopWatch timer = new StopWatch();
		timer.start();

		//Create the coordinator
		PUMASCoordinatorAg<T> coordinator = configs.getPUMASConfigs().buildNegotiationCoordinator();
		logger.info("=> Negotiation coordinator created ["+coordinator+"].");

		//Create the agents for the members of the group
		List<UserAg<T>> agents;
		if (userAgProfiles == null || userAgProfiles.isEmpty())
			agents = createAgents(group);
		else
			agents = createAgents(group, userAgProfiles, assertivenessFactors, cooperativenessFactors, relationshipsFactors);
		
		logger.info("=> Agents created  ["+agents+"].");

		//Add agents to Container
		coordinator.addAgents(agents);
		logger.info("=> Agents added to the coordinator.");

		//Use the coordinator to do the negotiation
		List<NegotiationResult<T>> results = new ArrayList<>();
		logger.info("=> Executing the protocol N times [N = "+howMany+"]");
		for (int i=1; i<=howMany; i++){
			logger.info("------------------------------------------------- NEGOTIATION N "+i+" -------------------------------------------------");
			NegotiationResult<T> negotiationResult;
			try {
				negotiationResult = coordinator.executeProtocol();
				logger.info(negotiationResult.toString());
				results.add(negotiationResult);
				coordinator.reset(false);
			} catch (ZeroAgentsInCoordinatorException e) {
				logger.error ("Negotiation aborted. Cause: "+e.getMessage());
			}
			logger.info("------------------------------------------------------------------------------------------------------------------------");
		}

		//Remove agents => not needed as this coordinator will die at the end of the execution of this method

		//Stop the timer
		timer.stop();
		recommendationTimeTotal = timer.getTime();

		//Tidy up
		coordinator.reset(true);
		
		//Build the result object and return it
		return buildResult(group, results, recommendationTimeTotal);
	}
	
	//----------------------------
	
	protected abstract List<UserAg<T>> createAgents (GRecGroup group);
	
	protected abstract List<UserAg<T>> createAgents (GRecGroup group, HashMap<SURUser, PUMASAgentProfile<T>> userAgProfiles);

	protected abstract List<UserAg<T>> createAgents (GRecGroup group, HashMap<SURUser, PUMASAgentProfile<T>> userAgProfiles, HashMap<SURUser, Double> assertivenessFactors, HashMap<SURUser, Double> cooperativenessFactors, HashMap<SURUser, HashMap<SURUser, Double>> relationshipsFactors);

	//----------------------------
	
	@Override
	public void forgetPastRecommendations() {
		singleUserRecommender.forgetPastRecommendations(); //TODO check if we need something else
	}



}
