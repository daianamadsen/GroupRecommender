package edu.isistan.christian.recommenders.groups.commons.pumas;

import edu.isistan.christian.recommenders.sur.datatypes.SURItem;
import edu.isistan.pumas.framework.protocols.commons.userAgents.arPunishmentStrategy.AlreadyRatedPunishmentStrategy;
import edu.isistan.pumas.framework.protocols.commons.userAgents.concessionCriterion.ConcessionCriterion;
import edu.isistan.pumas.framework.protocols.commons.userAgents.initialProposalStrategy.InitialProposalStrategy;
import edu.isistan.pumas.framework.protocols.commons.userAgents.proposalAcceptanceStrategy.ProposalAcceptanceStrategy;
import edu.isistan.pumas.framework.protocols.commons.userAgents.utilityFunction.UtilityFunction;

public class PUMASAgentProfile<T extends SURItem> {
	
	private ConcessionCriterion<T> concessionCriterion;
	private InitialProposalStrategy<T> initialProposalStrategy;
	private ProposalAcceptanceStrategy<T> prAStrategy;
	private UtilityFunction<T> utilityFunction;
	private AlreadyRatedPunishmentStrategy<T> aRPStrategy;
	
	private int ppoolMaxProposalsAddedOnRefill, ppoolMaxRefillsAllowed;
	private boolean ppoolRefillsAllowed, ppoolRecyclingAllowed;
	
	private boolean optimizationReuseUnusedProposalsEnabled, optimizationUtilityCacheEnabled;
	
	public PUMASAgentProfile(ConcessionCriterion<T> concessionCriterion,
			InitialProposalStrategy<T> initialProposalStrategy, ProposalAcceptanceStrategy<T> prAStrategy,
			AlreadyRatedPunishmentStrategy<T> aRPStrategy, UtilityFunction<T> utilityFunction,
			int ppoolMaxProposalsAddedOnRefill, int ppoolMaxRefillsAllowed, boolean ppoolRefillsAllowed,
			boolean ppoolRecyclingAllowed, boolean optimizationReuseUnusedProposalsEnabled,
			boolean optimizationUtilityCacheEnabled) {
		super();
		this.concessionCriterion = concessionCriterion;
		this.initialProposalStrategy = initialProposalStrategy;
		this.prAStrategy = prAStrategy;
		this.aRPStrategy = aRPStrategy;
		this.utilityFunction = utilityFunction;
		this.ppoolMaxProposalsAddedOnRefill = ppoolMaxProposalsAddedOnRefill;
		this.ppoolMaxRefillsAllowed = ppoolMaxRefillsAllowed;
		this.ppoolRefillsAllowed = ppoolRefillsAllowed;
		this.ppoolRecyclingAllowed = ppoolRecyclingAllowed;
		this.optimizationReuseUnusedProposalsEnabled = optimizationReuseUnusedProposalsEnabled;
		this.optimizationUtilityCacheEnabled = optimizationUtilityCacheEnabled;
	}
	
	public PUMASAgentProfile () {}

	//Gets

	public int getPPoolMaxProposalsAddedOnRefill() {
		return this.ppoolMaxProposalsAddedOnRefill;
	}

	public int getPPoolMaxRefillsAllowed() {
		return this.ppoolMaxRefillsAllowed;
	}

	public boolean isPPoolRefillAllowed() {
		return this.ppoolRefillsAllowed;
	}

	public boolean isPPoolAllowsRecycling() {
		return this.ppoolRecyclingAllowed;
	}

	public boolean isOptReuseUnusedProposalsEnabled() {
		return this.optimizationReuseUnusedProposalsEnabled;
	}

	public boolean isOptUtilityCacheEnabled() {
		return this.optimizationUtilityCacheEnabled;
	}

	public ConcessionCriterion<T> getConcessionCriterion() {
		return this.concessionCriterion;
	}

	public InitialProposalStrategy<T> getInitPropStrategy() {
		return this.initialProposalStrategy;
	}
	
	public ProposalAcceptanceStrategy<T> getPropAcceptanceStrategy(){
		return this.prAStrategy;
	}
	
	public UtilityFunction<T> getUtilityFunction(){
		return this.utilityFunction;
	}
	
	public AlreadyRatedPunishmentStrategy<T> getARPunishmentStrategy(){
		return this.aRPStrategy;
	}
	
	//Sets

	public void setPpoolMaxProposalsAddedOnRefill(int ppoolMaxProposalsAddedOnRefill) {
		this.ppoolMaxProposalsAddedOnRefill = ppoolMaxProposalsAddedOnRefill;
	}

	public void setPpoolMaxRefillsAllowed(int ppoolMaxRefillsAllowed) {
		this.ppoolMaxRefillsAllowed = ppoolMaxRefillsAllowed;
	}

	public void setPpoolRefillsAllowed(boolean ppoolRefillsAllowed) {
		this.ppoolRefillsAllowed = ppoolRefillsAllowed;
	}

	public void setPpoolRecyclingAllowed(boolean ppoolRecyclingAllowed) {
		this.ppoolRecyclingAllowed = ppoolRecyclingAllowed;
	}

	public void setOptimizationReuseUnusedProposalsEnabled(boolean optimizationReuseUnusedProposalsEnabled) {
		this.optimizationReuseUnusedProposalsEnabled = optimizationReuseUnusedProposalsEnabled;
	}

	public void setOptimizationUtilityCacheEnabled(boolean optimizationUtilityCacheEnabled) {
		this.optimizationUtilityCacheEnabled = optimizationUtilityCacheEnabled;
	}

	public void setConcessionCriterion(ConcessionCriterion<T> concessionCriterion) {
		this.concessionCriterion = concessionCriterion;
	}

	public void setInitialProposalStrategy(InitialProposalStrategy<T> initialProposalStrategy) {
		this.initialProposalStrategy = initialProposalStrategy;
	}

	public void setPropAcceptanceStrategy(ProposalAcceptanceStrategy<T> prAStrategy) {
		this.prAStrategy = prAStrategy;
	}

	public void setUtilityFunction(UtilityFunction<T> utilityFunction) {
		this.utilityFunction = utilityFunction;
	}

	public void setARPunishmentStrategy(AlreadyRatedPunishmentStrategy<T> aRPStrategy) {
		this.aRPStrategy = aRPStrategy;
	}

	@Override
	public String toString() {
		return "PUMASGRecAgentProfile [concessionCriterion=" + concessionCriterion + ", initialProposalStrategy="
				+ initialProposalStrategy + ", prAStrategy=" + prAStrategy + ", utilityFunction=" + utilityFunction
				+ ", aRPStrategy=" + aRPStrategy + ", ppoolMaxProposalsAddedOnRefill=" + ppoolMaxProposalsAddedOnRefill
				+ ", ppoolMaxRefillsAllowed=" + ppoolMaxRefillsAllowed + ", ppoolRefillsAllowed=" + ppoolRefillsAllowed
				+ ", ppoolRecyclingAllowed=" + ppoolRecyclingAllowed + ", optimizationReuseUnusedProposalsEnabled="
				+ optimizationReuseUnusedProposalsEnabled + ", optimizationUtilityCacheEnabled="
				+ optimizationUtilityCacheEnabled + "]";
	}
	
}
