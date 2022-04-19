package edu.isistan.christian.recommenders.groups.commons.pumas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecConfigs;
import edu.isistan.christian.recommenders.sur.SingleUserRecommender;
import edu.isistan.christian.recommenders.sur.datatypes.SURItem;
import edu.isistan.pumas.framework.protocols.PUMASCoordinatorAg;
import edu.isistan.pumas.framework.protocols.PUMASCoordinatorAgTypes;
import edu.isistan.pumas.framework.protocols.commons.userAgents.arPunishmentStrategy.ARPSMinimumSatisfaction;
import edu.isistan.pumas.framework.protocols.commons.userAgents.arPunishmentStrategy.ARPStrategyEasyGoing;
import edu.isistan.pumas.framework.protocols.commons.userAgents.arPunishmentStrategy.ARPStrategyFlexible;
import edu.isistan.pumas.framework.protocols.commons.userAgents.arPunishmentStrategy.ARPStrategyFlexiblePlus;
import edu.isistan.pumas.framework.protocols.commons.userAgents.arPunishmentStrategy.ARPStrategyTaboo;
import edu.isistan.pumas.framework.protocols.commons.userAgents.arPunishmentStrategy.ARPunishmentStrategies;
import edu.isistan.pumas.framework.protocols.commons.userAgents.concessionCriterion.ConcessionCriteria;
import edu.isistan.pumas.framework.protocols.commons.userAgents.concessionCriterion.ConcessionCriterionCurrentProposalUtilityLossThreshold;
import edu.isistan.pumas.framework.protocols.commons.userAgents.concessionCriterion.ConcessionCriterionUtilityThreshold;
import edu.isistan.pumas.framework.protocols.commons.userAgents.initialProposalStrategy.InitialProposalStrategies;
import edu.isistan.pumas.framework.protocols.commons.userAgents.initialProposalStrategy.InitialProposalStrategy;
import edu.isistan.pumas.framework.protocols.commons.userAgents.proposalAcceptanceStrategy.ProposalAcceptanceStrategies;
import edu.isistan.pumas.framework.protocols.commons.userAgents.proposalAcceptanceStrategy.ProposalAcceptanceStrategyNext;
import edu.isistan.pumas.framework.protocols.commons.userAgents.proposalAcceptanceStrategy.ProposalAcceptanceStrategyRelaxed;
import edu.isistan.pumas.framework.protocols.commons.userAgents.proposalAcceptanceStrategy.ProposalAcceptanceStrategyRelaxedS;
import edu.isistan.pumas.framework.protocols.commons.userAgents.proposalAcceptanceStrategy.ProposalAcceptanceStrategyRelaxedS.RelaxLevel;
import edu.isistan.pumas.framework.protocols.commons.userAgents.proposalAcceptanceStrategy.ProposalAcceptanceStrategyStrict;
import edu.isistan.pumas.framework.protocols.commons.userAgents.utilityFunction.UtilityFunctionRecommenderBased;
import edu.isistan.pumas.framework.protocols.commons.userAgents.utilityFunction.UtilityFunctionTypes;
import edu.isistan.pumas.framework.protocols.monotonicConcession.MConcessionCoordinatorAg;
import edu.isistan.pumas.framework.protocols.monotonicConcession.agreementStrategy.AgreementStrategies;
import edu.isistan.pumas.framework.protocols.monotonicConcession.agreementStrategy.AgreementStrategy;
import edu.isistan.pumas.framework.protocols.monotonicConcession.multilateralConcessionStrategy.MultilateralConcessionStrategies;
import edu.isistan.pumas.framework.protocols.monotonicConcession.multilateralConcessionStrategy.MultilateralConcessionStrategy;
import edu.isistan.pumas.framework.protocols.monotonicConcession.negotiationStrategy.NegotiationStrategies;
import edu.isistan.pumas.framework.protocols.monotonicConcession.negotiationStrategy.NegotiationStrategy;
import edu.isistan.pumas.framework.protocols.oneStep.OneStepCoordinatorAg;

public abstract class PUMASConfigs<T extends SURItem> extends GRecConfigs{
	private static final Logger logger = LogManager.getLogger(PUMASConfigs.class);

	protected enum ProtocolProperties {
		MULTILATERAL_CONCESSION_STRATEGY ("pumas.protocol.monotonicConcession.multilateralConcessionStrategy"),
		AGREEMENT_STRATEGY ("pumas.protocol.monotonicConcession.agreementStrategy"),
		NEGOTIATION_STRATEGY ("pumas.protocol.monotonicConcession.negotiationStrategy"),
		COORDINATOR_TYPE ("pumas.protocol.monotonicConcession.coordinatorType");


		private String propertyName;
		ProtocolProperties (String propertyName){
			this.propertyName = propertyName;
		}

		public String getPropertyName(){
			return propertyName;
		}
	}

	protected enum UsersProperties {
		PPOOL_MAX_PROPOSALS_ADDED_ON_REFILL ("pumas.users.proposalsPool.maxProposalsAddedOnRefill"),
		PPOOL_IS_REFILL_ALLOWED ("pumas.users.proposalsPool.isRefillAllowed"),
		PPOOL_MAX_REFILLS_ALLOWED ("pumas.users.proposalsPool.maxRefillsAllowed"),
		PPOOL_IS_RECYCLING_ALLOWED ("pumas.users.proposalsPool.isRecyclingAllowed"),
		OPTIMIZATION_REUSE_NOT_USED_PROPOSALS_ENABLED ("pumas.users.optimizations.reuseNotUsedProposalsEnabled"),
		OPTIMIZATION_UTILITY_CACHE_ENABLED ("pumas.users.optimizations.utilityCacheEnabled"),
		INITIAL_PROPOSAL_STRATEGY ("pumas.users.monotonicConcession.initialProposalStrategy"),
		CONCESSION_CRITERION ("pumas.users.monotonicConcession.concessionCriterion"),
		CONCESSION_CRITERION_UTILITY_THRESHOLD_THRESHOLDVALUE ("users.monotonicConcession.concessionCriterion.utilityThreshold.threshold"),
		CONCESSION_CRITERION_CURRENT_PROPOSAL_UTILITY_LOSS_THRESHOLD_THRESHOLDVALUE ("users.monotonicConcession.concessionCriterion.currentProposalUtilityLossThreshold.threshold"),
		UTILITY_FUNCTION_TYPE ("pumas.users.monotonicConcession.utilityFunctionType"),
		ALREADY_RATED_PUNISHMENT_STRATEGY ("pumas.users.monotonicConcession.AlreadyRatedPunishmentStrategy"),
		ALREADY_RATED_PUNISHMENT_FLEXIBILITYVALUE ("pumas.users.monotonicConcession.AlreadyRatedPunishmentStrategy.flexibilityValue"),
		ALREADY_RATED_PUNISHMENT_MINSATISFACTIONVALUE ("pumas.users.monotonicConcession.AlreadyRatedPunishmentStrategy.minSatisfactionValue"),
		PROPOSAL_ACCEPTANCE_STRATEGY ("pumas.users.monotonicConcession.proposalAcceptanceStrategy"),
		PROPOSAL_ACCEPTANCE_RELAX_LEVEL ("pumas.users.monotonicConcession.proposalAcceptanceStrategy.relaxed_s.relaxLevel"),
		PROPOSAL_ACCEPTANCE_RELAX_PERCENTAGE ("pumas.users.monotonicConcession.proposalAcceptanceStrategy.relaxed.relaxPercentage"),
		PROPOSAL_ACCEPTANCE_PF_BETA ("pumas.users.monotonicConcession.proposalAcceptanceStrategy.pf.beta"),
		PROPOSAL_ACCEPTANCE_PF_GAMMA ("pumas.users.monotonicConcession.proposalAcceptanceStrategy.pf.gamma"),
		PROPOSAL_ACCEPTANCE_PF_DELTA ("pumas.users.monotonicConcession.proposalAcceptanceStrategy.pf.delta");


		private String propertyName;
		UsersProperties (String propertyName){
			this.propertyName = propertyName;
		}

		public String getPropertyName(){
			return propertyName;
		}
	}

	/** Protocol Configs */
	protected HashMap<ProtocolProperties, Object> protocolConfigs;

	/** User configs (agents default profile) */
//	protected HashMap<UsersProperties, Object> userConfigs;
	protected PUMASAgentProfile<T> agDefaultProfile;

	/** SUR used by the agents */
	protected SingleUserRecommender<T> singleUserRecommender;

	
	public PUMASConfigs(String configsPath) throws ConfigurationException {
		protocolConfigs = new HashMap<>();

		loadProtocolConfigs(configsPath);
		loadUserConfigs(configsPath);
	}

	private void loadProtocolConfigs(String configsPath) throws ConfigurationException {
		//Build from configs
		Configuration config = new PropertiesConfiguration(configsPath);
		try{
			protocolConfigs.put(ProtocolProperties.MULTILATERAL_CONCESSION_STRATEGY, 
					MultilateralConcessionStrategies.valueOf(config.getString (ProtocolProperties.MULTILATERAL_CONCESSION_STRATEGY.getPropertyName())).get());

			PUMASCoordinatorAgTypes coordinatorType = PUMASCoordinatorAgTypes.valueOf(config.getString(ProtocolProperties.COORDINATOR_TYPE.getPropertyName()));
			//Save the type of coordinator to use it later for building the coordinators when required
			protocolConfigs.put(ProtocolProperties.COORDINATOR_TYPE, coordinatorType);
			switch (coordinatorType){
			case MONOTONIC_CONCESSION:
				protocolConfigs.put(ProtocolProperties.AGREEMENT_STRATEGY, 
						AgreementStrategies.valueOf(config.getString(ProtocolProperties.AGREEMENT_STRATEGY.getPropertyName())).get());
				protocolConfigs.put(ProtocolProperties.NEGOTIATION_STRATEGY, 
						NegotiationStrategies.valueOf(config.getString(ProtocolProperties.NEGOTIATION_STRATEGY.getPropertyName())).get());
				break;
			case ONE_STEP:
				//Do nothing. because there's no configurations needed by this kind of coordinator right now (other than the multilateral concession strategy which was already loaded)
				break; 
			default:
				break; //do nothing.. if the coordinator doesn't match with the ones in the enum => an IllegalArgumentException will be thrown when calling the valueOf method of the enum
			}

			logger.info("File loaded: PUMAS PROTOCOL CONFIGS ["+protocolConfigs+"]");
		} catch (IllegalArgumentException | NullPointerException e){ //IllegalArgumentException  and NullPointerException can be thrown by the ENUMs when using the value of method
			throw new ConfigurationException(e);
		}
	}

	private void loadUserConfigs(String configsPath) throws ConfigurationException {
		//Build recommender from configs
		singleUserRecommender = buildSUR(configsPath); //can be required by the utility Function as well as by the Agent
		
		this.agDefaultProfile = new PUMASAgentProfile<>();

		//Build aggregationStrategy from configs
		Configuration config = new PropertiesConfiguration(configsPath);
		try{
			//Users configs (the same for all the users!)
			/// Proposals Pool configs
			this.agDefaultProfile.setPpoolMaxProposalsAddedOnRefill(config.getInt(UsersProperties.PPOOL_MAX_PROPOSALS_ADDED_ON_REFILL.getPropertyName()));
			this.agDefaultProfile.setPpoolMaxRefillsAllowed(config.getInt(UsersProperties.PPOOL_MAX_REFILLS_ALLOWED.getPropertyName()));
			this.agDefaultProfile.setPpoolRefillsAllowed(config.getBoolean(UsersProperties.PPOOL_IS_REFILL_ALLOWED.getPropertyName()));
			this.agDefaultProfile.setPpoolRecyclingAllowed(config.getBoolean(UsersProperties.PPOOL_IS_RECYCLING_ALLOWED.getPropertyName()));

			///Optimizations configs
			this.agDefaultProfile.setOptimizationUtilityCacheEnabled(config.getBoolean(UsersProperties.OPTIMIZATION_REUSE_NOT_USED_PROPOSALS_ENABLED.getPropertyName()));
			this.agDefaultProfile.setOptimizationReuseUnusedProposalsEnabled(config.getBoolean(UsersProperties.OPTIMIZATION_UTILITY_CACHE_ENABLED.getPropertyName()));

			/// Monotonic Concesssion configs
			@SuppressWarnings("unchecked") //because the enum get() method return a InitialProposalStrategy<SURItem> as the enum can't be parametrized
			InitialProposalStrategy<T> iPS = (InitialProposalStrategy<T>) InitialProposalStrategies.valueOf(config.getString(UsersProperties.INITIAL_PROPOSAL_STRATEGY.getPropertyName())).get();
			this.agDefaultProfile.setInitialProposalStrategy(iPS);

			//* Concession Criteria (config each criterion depending on its type)
			ConcessionCriteria concessionCriterionTypes = ConcessionCriteria.valueOf(config.getString(UsersProperties.CONCESSION_CRITERION.getPropertyName()));
			switch (concessionCriterionTypes){
			case UTILITY_THRESHOLD:
				@SuppressWarnings("unchecked")
				ConcessionCriterionUtilityThreshold<T> cCriterionUT = (ConcessionCriterionUtilityThreshold<T>) concessionCriterionTypes.get();
				cCriterionUT.setUtilityThreshold(config.getDouble(UsersProperties.CONCESSION_CRITERION_UTILITY_THRESHOLD_THRESHOLDVALUE.getPropertyName()));
				this.agDefaultProfile.setConcessionCriterion(cCriterionUT);
				break;
			case CURRENT_PROPOSAL_UTILITY_LOSS_THRESHOLD:
				@SuppressWarnings("unchecked")
				ConcessionCriterionCurrentProposalUtilityLossThreshold<T> cCriterionCPULT = (ConcessionCriterionCurrentProposalUtilityLossThreshold<T>) concessionCriterionTypes.get();
				cCriterionCPULT.setUtilityThreshold(config.getDouble(UsersProperties.CONCESSION_CRITERION_CURRENT_PROPOSAL_UTILITY_LOSS_THRESHOLD_THRESHOLDVALUE.getPropertyName()));
				this.agDefaultProfile.setConcessionCriterion(cCriterionCPULT);
				break;
			default:
				break;
			}
			
			//* Utility Function (config each function depending on its type)
			UtilityFunctionTypes uFunction = UtilityFunctionTypes.valueOf(config.getString(UsersProperties.UTILITY_FUNCTION_TYPE.getPropertyName()));
			switch (uFunction){
			case RECOMMENDER_BASED:
				@SuppressWarnings("unchecked")
				UtilityFunctionRecommenderBased<T> uF = (UtilityFunctionRecommenderBased<T>) uFunction.get();
				uF.setItemRecSys(singleUserRecommender); //needs to be built first, that's why we do it at the beginning of the method!
				
				this.agDefaultProfile.setUtilityFunction(uF);
				break;
			default:
				break;
			
			}
			
			//* Already Rated Punishment Strategy (config each strategy depending on its type)
			ARPunishmentStrategies arPunishmentStrategy = ARPunishmentStrategies.valueOf(config.getString(UsersProperties.ALREADY_RATED_PUNISHMENT_STRATEGY.getPropertyName()));
			switch(arPunishmentStrategy){
			case EASYGOING:
				@SuppressWarnings("unchecked")
				ARPStrategyEasyGoing<T> arPStrategyEG = (ARPStrategyEasyGoing<T>)arPunishmentStrategy.get();
				
				this.agDefaultProfile.setARPunishmentStrategy(arPStrategyEG);
				break;
			case FLEXIBLE:
				double flexible_flexibility = config.getDouble(UsersProperties.ALREADY_RATED_PUNISHMENT_FLEXIBILITYVALUE.getPropertyName());
				@SuppressWarnings("unchecked")
				ARPStrategyFlexible<T> arPStrategyFlex = (ARPStrategyFlexible<T>)arPunishmentStrategy.get();
				arPStrategyFlex.setItemRecSys(singleUserRecommender);
				arPStrategyFlex.setFlexibilityLevel(flexible_flexibility);

				this.agDefaultProfile.setARPunishmentStrategy(arPStrategyFlex);
				break;
			case FLEXIBLE_PLUS:
				double flexiblePlus_flexibility = config.getDouble(UsersProperties.ALREADY_RATED_PUNISHMENT_FLEXIBILITYVALUE.getPropertyName());
				double flexiblePlus_minSat = config.getDouble(UsersProperties.ALREADY_RATED_PUNISHMENT_MINSATISFACTIONVALUE.getPropertyName());
				@SuppressWarnings("unchecked")
				ARPStrategyFlexiblePlus<T> arPStrategyFlexPlus = (ARPStrategyFlexiblePlus<T>)arPunishmentStrategy.get(); 
				arPStrategyFlexPlus.setFlexibilityLevel(flexiblePlus_flexibility);
				arPStrategyFlexPlus.setItemRecSys(singleUserRecommender);
				arPStrategyFlexPlus.setMinimumSatisfactionLevel(flexiblePlus_minSat);
				
				this.agDefaultProfile.setARPunishmentStrategy(arPStrategyFlexPlus);
				break;
			case MINIMUM_SATISFACTION:
				double minSat_value = config.getDouble(UsersProperties.ALREADY_RATED_PUNISHMENT_MINSATISFACTIONVALUE.getPropertyName());
				@SuppressWarnings("unchecked")
				ARPSMinimumSatisfaction<T> arPStrategyMinSat = (ARPSMinimumSatisfaction<T>) arPunishmentStrategy.get();
				arPStrategyMinSat.setMinimumSatisfactionLevel(minSat_value);
				this.agDefaultProfile.setARPunishmentStrategy(arPStrategyMinSat);
				break;
			case TABOO:
				@SuppressWarnings("unchecked")
				ARPStrategyTaboo<T> arPStrategyTaboo = (ARPStrategyTaboo<T>) arPunishmentStrategy.get();
				arPStrategyTaboo.setItemRecSys(singleUserRecommender);
				
				this.agDefaultProfile.setARPunishmentStrategy(arPStrategyTaboo);
				break;
			case NONE: default:
				this.agDefaultProfile.setARPunishmentStrategy(null); //just to make sure is null, it should not be necessary
				break;
			}
			
			double pf_beta = config.getDouble(UsersProperties.PROPOSAL_ACCEPTANCE_PF_BETA.getPropertyName());
			double pf_gamma = config.getDouble(UsersProperties.PROPOSAL_ACCEPTANCE_PF_GAMMA.getPropertyName());
			double pf_delta = config.getDouble(UsersProperties.PROPOSAL_ACCEPTANCE_PF_DELTA.getPropertyName());
			
			if (pf_beta + pf_gamma + pf_delta >= 1)
					throw new IllegalArgumentException ("beta, gamma and delta parameters must add up to less than 1");
			
			//* Proposal Acceptance Strategy
			ProposalAcceptanceStrategies propAcceptStrategy = ProposalAcceptanceStrategies.valueOf(config.getString(UsersProperties.PROPOSAL_ACCEPTANCE_STRATEGY.getPropertyName()));
			switch (propAcceptStrategy){
			case STRICT:
				@SuppressWarnings("unchecked")
				ProposalAcceptanceStrategyStrict<T> propAcceptStrict = (ProposalAcceptanceStrategyStrict<T>) propAcceptStrategy.get();
				propAcceptStrict.setPfBeta(pf_beta);
				propAcceptStrict.setPfGamma(pf_gamma);
				propAcceptStrict.setPfDelta(pf_delta);

				this.agDefaultProfile.setPropAcceptanceStrategy(propAcceptStrict);
				break;
			case RELAXED:
				@SuppressWarnings("unchecked") ProposalAcceptanceStrategyRelaxed<T> prARelaxed = (ProposalAcceptanceStrategyRelaxed<T>) propAcceptStrategy.get();
				double relaxPercentage = config.getDouble(UsersProperties.PROPOSAL_ACCEPTANCE_RELAX_PERCENTAGE.getPropertyName());
				prARelaxed.setRelaxationLevel(relaxPercentage);
				
				this.agDefaultProfile.setPropAcceptanceStrategy(prARelaxed);
				break;
			case RELAXED_S:
				@SuppressWarnings("unchecked")
				ProposalAcceptanceStrategyRelaxedS<T> prARelaxed_S = (ProposalAcceptanceStrategyRelaxedS<T>) propAcceptStrategy.get();
				RelaxLevel relaxLevel = RelaxLevel.valueOf(config.getString(UsersProperties.PROPOSAL_ACCEPTANCE_RELAX_LEVEL.getPropertyName()));
				prARelaxed_S.setRelaxationLevel(relaxLevel);
				
				this.agDefaultProfile.setPropAcceptanceStrategy(prARelaxed_S);
				break;
			case NEXT:
				@SuppressWarnings("unchecked") ProposalAcceptanceStrategyNext<T> propAcceptNext = (ProposalAcceptanceStrategyNext<T>) propAcceptStrategy.get();
				this.agDefaultProfile.setPropAcceptanceStrategy(propAcceptNext);
				break;
			default:
				break;
			}
			logger.info("File loaded: PUMAS USER AGENTS CONFIGS ["+agDefaultProfile+"]");

		} catch (IllegalArgumentException | NullPointerException e){ //IllegalArgumentException  and NullPointerException can be thrown by the ENUMs when using the value of method
			throw new ConfigurationException(e);
		}
	}

	protected abstract SingleUserRecommender<T> buildSUR(String configsPath) throws ConfigurationException;
	
	public abstract SingleUserRecommender<T> getSUR();

	//--------------------------------

	/**
	 * 
	 * @return
	 */
	public PUMASCoordinatorAg<T> buildNegotiationCoordinator() {
		PUMASCoordinatorAg<T> coordinator = null;		
		switch (getNegotiationCoordinatorType()){
		case MONOTONIC_CONCESSION:
			coordinator = new MConcessionCoordinatorAg<T>(getAgreementStrategy(), getNegotiationStrategy(), getMultilateralConcessionStrategy());
			break;
		case ONE_STEP:
			coordinator = new OneStepCoordinatorAg<T>(getMultilateralConcessionStrategy());
			break; 
		default:
			break; //do nothing.. should never happen
		}

		return coordinator;
	}

	protected PUMASCoordinatorAgTypes getNegotiationCoordinatorType(){
		return (PUMASCoordinatorAgTypes) protocolConfigs.get(ProtocolProperties.COORDINATOR_TYPE);
	}

	@SuppressWarnings("unchecked")
	public AgreementStrategy<T> getAgreementStrategy() {
		return (AgreementStrategy<T>) protocolConfigs.get(ProtocolProperties.AGREEMENT_STRATEGY);
	}

	@SuppressWarnings("unchecked")
	public NegotiationStrategy<T> getNegotiationStrategy() {
		return (NegotiationStrategy<T>) protocolConfigs.get(ProtocolProperties.NEGOTIATION_STRATEGY);
	}

	@SuppressWarnings("unchecked")
	public MultilateralConcessionStrategy<T> getMultilateralConcessionStrategy(){
		return (MultilateralConcessionStrategy<T>) protocolConfigs.get(ProtocolProperties.MULTILATERAL_CONCESSION_STRATEGY);
	}

	//--------------------------------

	public PUMASAgentProfile<T> getAgentsDefaultProfile(){
		return this.agDefaultProfile;
	}

	//--------------------------------

	@Override
	public List<String> getAsStringList() {
		List<String> configsList = new ArrayList<>(); //change the strings!

		configsList.add("PUMAS CONFIGS");

		//Protocol Configs
		configsList.add("");
		configsList.add("Protocol configs:");
		configsList.add(" => Coordinator: "+this.buildNegotiationCoordinator().toString());
//		configsList.add(" => Coordinator Type: "+ this.getNegotiationCoordinatorType().toString());
//		configsList.add(" => Agreement Strategy: "+ ((this.getAgreementStrategy() != null)? this.getAgreementStrategy().toString() : "N/A"));
//		configsList.add(" => Negotiation Strategy: "+ ((this.getNegotiationStrategy() != null)? this.getNegotiationStrategy().toString() : "N/A"));
//		configsList.add(" => Multilateral Concession Strategy: "+this.getMultilateralConcessionStrategy().toString());

		//User configs
		configsList.add("");
		configsList.add("Users: (same configs for all the users)");
		configsList.add(" => Single User Recommender (shared by all the users): "+ this.getSUR().toString());
		configsList.add(" => ProposalsPool Configs: ["+UsersProperties.PPOOL_MAX_PROPOSALS_ADDED_ON_REFILL.getPropertyName()+"= "+this.agDefaultProfile.getPPoolMaxProposalsAddedOnRefill()
				+" | "+UsersProperties.PPOOL_IS_REFILL_ALLOWED.getPropertyName()+"= "+this.agDefaultProfile.isPPoolRefillAllowed()
				+" | "+UsersProperties.PPOOL_MAX_REFILLS_ALLOWED.getPropertyName()+"= "+this.agDefaultProfile.getPPoolMaxRefillsAllowed()
				+" | "+UsersProperties.PPOOL_IS_RECYCLING_ALLOWED.getPropertyName()+"= "+this.agDefaultProfile.isPPoolAllowsRecycling()+"]");
		configsList.add(" => Optimizations: ["+UsersProperties.OPTIMIZATION_REUSE_NOT_USED_PROPOSALS_ENABLED.getPropertyName()+"= "+this.agDefaultProfile.isOptReuseUnusedProposalsEnabled()
				+" | "+UsersProperties.OPTIMIZATION_UTILITY_CACHE_ENABLED.getPropertyName()+"= "+this.agDefaultProfile.isOptUtilityCacheEnabled()+"]"); 
		configsList.add(" => Initial Proposal Strategy: "+ this.agDefaultProfile.getInitPropStrategy().toString());
		configsList.add(" => Concession Criterion: "+ this.agDefaultProfile.getConcessionCriterion().toString());
		configsList.add(" => Utility Function: "+ this.agDefaultProfile.getUtilityFunction().toString());
		configsList.add(" => Already Rated Punishment Strategy: "+ this.agDefaultProfile.getARPunishmentStrategy().toString());
		configsList.add(" => Proposals Acceptance Strategy: "+ this.agDefaultProfile.getPropAcceptanceStrategy().toString());
		

		return configsList;
	}

	@Override
	public List<String> getRequiredProperties() { //add everything but SUR from the USER AG
		List<String> rProp = new ArrayList<>();
		
		for (ProtocolProperties p : ProtocolProperties.values())
			rProp.add(p.getPropertyName());

		for (UsersProperties p : UsersProperties.values())
			rProp.add(p.getPropertyName());

		return rProp;
	}
}
