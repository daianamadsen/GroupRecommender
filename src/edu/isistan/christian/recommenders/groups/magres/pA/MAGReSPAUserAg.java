package edu.isistan.christian.recommenders.groups.magres.pA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import edu.isistan.christian.recommenders.sur.datatypes.SURItem;
import edu.isistan.christian.recommenders.sur.datatypes.SURUser;
import edu.isistan.pumas.framework.dataTypes.NegotiableItemComposed;
import edu.isistan.pumas.framework.protocols.commons.exceptions.CacheRefillingException;
import edu.isistan.pumas.framework.protocols.commons.proposal.AgProposal;
import edu.isistan.pumas.framework.protocols.commons.proposal.comparators.ProposalUtilityComparator;
import edu.isistan.pumas.framework.protocols.commons.userAgents.UserAg;
import edu.isistan.pumas.framework.protocols.commons.userAgents.arPunishmentStrategy.AlreadyRatedPunishmentStrategy;
import edu.isistan.pumas.framework.protocols.commons.userAgents.concessionCriterion.ConcessionCriterion;
import edu.isistan.pumas.framework.protocols.commons.userAgents.initialProposalStrategy.InitialProposalStrategy;
import edu.isistan.pumas.framework.protocols.commons.userAgents.proposalAcceptanceStrategy.ProposalAcceptanceStrategy;
import edu.isistan.pumas.framework.protocols.commons.userAgents.utilityFunction.UtilityFunction;

public class MAGReSPAUserAg<T extends SURItem> extends UserAg<T>{
	
	protected List<AgProposal<T>> candidateProposals, proposalsUsed;
	
	public boolean lastCacheRefillSuccess = true;

	public MAGReSPAUserAg(SURUser myUser, InitialProposalStrategy<T> initialProposalStrategy,
			ConcessionCriterion<T> concessionCriterion, ProposalAcceptanceStrategy<T> proposalAcceptStrategy,
			UtilityFunction<T> utilityFunction, AlreadyRatedPunishmentStrategy<T> arPunishmentStrategy,
			boolean proposalsPoolAllowsRecycling, boolean optReuseNotUsedProposalsEnabled,
			boolean optUtilityCacheEnabled, Set<Set<T>> subsetsForProposals) {
		super(myUser, initialProposalStrategy, concessionCriterion, proposalAcceptStrategy, utilityFunction,
				arPunishmentStrategy, proposalsPoolAllowsRecycling, optReuseNotUsedProposalsEnabled, optUtilityCacheEnabled);
		this.candidateProposals = createAllProposals(subsetsForProposals);
		this.proposalsUsed = new ArrayList<>();
	}

	public MAGReSPAUserAg(SURUser myUser, InitialProposalStrategy<T> initialProposalStrategy,
			ConcessionCriterion<T> concessionCriterion, ProposalAcceptanceStrategy<T> proposalAcceptStrategy,
			UtilityFunction<T> utilityFunction, AlreadyRatedPunishmentStrategy<T> arPunishmentStrategy,
			int proposalsPoolMaxSize, int maxProposalsPoolRefillsAllowed, boolean proposalsPoolIsRefillAllowed,
			boolean proposalsPoolAllowsRecycling, boolean optNotUsedProposalsEnabled, boolean optUtilityCacheEnabled,
			Set<Set<T>> subsetsForProposals) {
		super(myUser, initialProposalStrategy, concessionCriterion, proposalAcceptStrategy, utilityFunction,
				arPunishmentStrategy, proposalsPoolMaxSize, maxProposalsPoolRefillsAllowed, proposalsPoolIsRefillAllowed,
				proposalsPoolAllowsRecycling, optNotUsedProposalsEnabled, optUtilityCacheEnabled);
		this.candidateProposals = createAllProposals(subsetsForProposals);
		this.proposalsUsed = new ArrayList<>();
	}
	
	private List<AgProposal<T>> createAllProposals (Set<Set<T>> subsets) {
		List<AgProposal<T>> proposals = new ArrayList<>();
		for (Set<T> subset : subsets) {
			NegotiableItemComposed<T> item = new NegotiableItemComposed<>(subset);
			double utility = this.utilityFunction.assessNegotiableItem(this, item);
			proposals.add(new AgProposal<>(this.getID(), item, utility));
		}
		
		Collections.sort(proposals, new ProposalUtilityComparator<T>(false)); //order in descending order according to the utility of each proposal
		
		return proposals;
	}

	@Override
	protected List<AgProposal<T>> buildCandidateProposals(int n) throws Exception {
		// TODO Auto-generated method stub
		List<AgProposal<T>> toReturn = new ArrayList<>();
		
		int end = (n < this.candidateProposals.size())? n : this.candidateProposals.size();
		toReturn = this.candidateProposals.subList(0, end);
		
		this.proposalsUsed.addAll(toReturn);
		this.candidateProposals.removeAll(toReturn);
		
		return toReturn;
	}
	
	@Override
	protected void refillProposalsPool() throws CacheRefillingException{
		if (lastCacheRefillSuccess){
			//Attempt to refill
			try{
				super.refillProposalsPool();
			} catch (CacheRefillingException e){
				//if refill failed => change the boolean and then throw the exception
				lastCacheRefillSuccess = false;
				throw e;
			}
		}
		else
			throw new CacheRefillingException();
	}

	@Override
	public double getProposalsRevealedPercentage() {
		double proposalsTotal = this.candidateProposals.size()+this.proposalsUsed.size();
		return this.statsManager.getProposalsRevealed()/proposalsTotal;
	}

	@Override
	public double getUtilitiesRevealedPercentage() {
		double proposalsTotal = this.candidateProposals.size()+this.proposalsUsed.size();
		return this.statsManager.getItemsWithUtilityRevealedCount()/proposalsTotal; //every proposal contains an item => total of items = total of proposals
	}
	
	public void reset(boolean fullReset){ //TODO revise this. It seems I forgot to override this method
		lastCacheRefillSuccess = true;
		super.reset(fullReset);
	}

	
}
