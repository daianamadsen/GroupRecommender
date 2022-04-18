package edu.isistan.christian.recommenders.groups.tradGRec.pA;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.isistan.christian.recommenders.groups.commons.aggregationStrategy.AggregationStrategies;
import edu.isistan.christian.recommenders.groups.commons.aggregationStrategy.AggregationStrategy;
import edu.isistan.christian.recommenders.groups.commons.aggregationStrategy.AggregationStrategyApprovalVoting;
import edu.isistan.christian.recommenders.groups.commons.aggregationStrategy.AggregationStrategyAverage;
import edu.isistan.christian.recommenders.groups.commons.aggregationStrategy.AggregationStrategyUpwardLeveling;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecConfigs;
import edu.isistan.christian.recommenders.sur.SingleUserRecommender;
import edu.isistan.christian.recommenders.sur.datatypes.SURItem;

public abstract class TRADGRecPAConfigs <T extends SURItem> extends GRecConfigs{

	private static final Logger logger = LogManager.getLogger(TRADGRecPAConfigs.class);

	protected enum GeneralProperties {
		AGGREGATION_STRATEGY ("grec.aggregationStrategy"),
		GROUP_RATING_ESTIMATION_STRATEGY ("grec.groupRatingEstimationStrategy"), //the one used to estimate the group ratings
		AGGREGATION_STRATEGY_AV_THRESHOLD ("grec.aggregationStrategy.av.threshold"),
		AGGREGATION_STRATEGY_UL_ALPHA ("grec.aggregationStrategy.ul.alpha"),
		AGGREGATION_STRATEGY_UL_BETA ("grec.aggregationStrategy.ul.beta"),
		AGGREGATION_STRATEGY_UL_GAMMA ("grec.aggregationStrategy.ul.gamma"),
		AGGREGATION_STRATEGY_UL_AV_THRESHOLD ("grec.aggregationStrategy.ul.av_threshold"),
		AGGREGATION_STRATEGY_PF_BETA ("grec.aggregationStrategy.pf.beta"),
		AGGREGATION_STRATEGY_PF_GAMMA ("grec.aggregationStrategy.pf.gamma"),
		AGGREGATION_STRATEGY_PF_DELTA ("grec.aggregationStrategy.pf.delta"),
		;

		private String propertyName;
		GeneralProperties (String propertyName){
			this.propertyName = propertyName;
		}

		public String getPropertyName(){
			return propertyName;
		}
	}

	protected SingleUserRecommender<T> singleUserRecommender;
	protected AggregationStrategy aggregationStrategy;
	protected AggregationStrategy groupRatingEstimationStrategy;

	protected double pf_beta, pf_gamma, pf_delta;

	public TRADGRecPAConfigs(String configsPath) throws ConfigurationException {
		loadGeneralConfigs(configsPath);
	}

	private void loadGeneralConfigs(String configsPath) throws ConfigurationException {
		//Build recommender from configs
		singleUserRecommender = buildSUR(configsPath);

		//Build aggregationStrategy from configs
		Configuration config = new PropertiesConfiguration(configsPath);
		try{
			AggregationStrategies aggregationStrategyType = AggregationStrategies.valueOf(config.getString(GeneralProperties.AGGREGATION_STRATEGY.getPropertyName()));
			this.aggregationStrategy = aggregationStrategyType.get();
			double maxRating = 1.0;
			switch (aggregationStrategyType) {
			case APPROVAL_VOTING:
				double approvalThreshold = config.getDouble(GeneralProperties.AGGREGATION_STRATEGY_AV_THRESHOLD.getPropertyName());
				((AggregationStrategyApprovalVoting) this.aggregationStrategy).setApprovalThreshold(approvalThreshold);
				((AggregationStrategyApprovalVoting) this.aggregationStrategy).setMaxRating(maxRating);
				break;
			case AVERAGE: case LEAST_MISERY: case MOST_PLEASURE: //there are no parameters to set 
				break;
			case UPWARD_LEVELING:
				double alpha = config.getDouble(GeneralProperties.AGGREGATION_STRATEGY_UL_ALPHA.getPropertyName());
				double beta = config.getDouble(GeneralProperties.AGGREGATION_STRATEGY_UL_BETA.getPropertyName());
				double gamma = config.getDouble(GeneralProperties.AGGREGATION_STRATEGY_UL_GAMMA.getPropertyName());
				
				if (alpha + beta + gamma != 1)
					throw new IllegalArgumentException ("alpha, beta and gamma parameters must add up 1");
				
				double av_Threshold = config.getDouble(GeneralProperties.AGGREGATION_STRATEGY_UL_AV_THRESHOLD.getPropertyName());
				((AggregationStrategyUpwardLeveling) this.aggregationStrategy).setAvgStrategy(new AggregationStrategyAverage());
				((AggregationStrategyUpwardLeveling) this.aggregationStrategy).setAvStrategy(new AggregationStrategyApprovalVoting(av_Threshold, maxRating));
				((AggregationStrategyUpwardLeveling) this.aggregationStrategy).setMaxRating(maxRating);
				((AggregationStrategyUpwardLeveling) this.aggregationStrategy).setAlpha(alpha);
				((AggregationStrategyUpwardLeveling) this.aggregationStrategy).setBeta(beta);
				((AggregationStrategyUpwardLeveling) this.aggregationStrategy).setGamma(gamma);

				break;
			default:
				break;
			}
			
			this.pf_beta = config.getDouble(GeneralProperties.AGGREGATION_STRATEGY_PF_BETA.getPropertyName());
			this.pf_gamma = config.getDouble(GeneralProperties.AGGREGATION_STRATEGY_PF_GAMMA.getPropertyName());
			this.pf_delta = config.getDouble(GeneralProperties.AGGREGATION_STRATEGY_PF_DELTA.getPropertyName());
			
			if (pf_beta + pf_gamma + pf_delta >= 1)
					throw new IllegalArgumentException ("beta, gamma and delta parameters must add up to less than 1");
				
			groupRatingEstimationStrategy = AggregationStrategies.valueOf(config.getString(GeneralProperties.GROUP_RATING_ESTIMATION_STRATEGY.getPropertyName())).get();

			logger.info("File loaded: TRADGRecPAConfigs [ grec.singleUserRecommender= "+singleUserRecommender.toString()
					+" | "+GeneralProperties.AGGREGATION_STRATEGY.getPropertyName()+"= "+aggregationStrategy.toString()
					+" | "+GeneralProperties.GROUP_RATING_ESTIMATION_STRATEGY.getPropertyName()+"= "+groupRatingEstimationStrategy.toString()
					+"]");
		} catch (IllegalArgumentException | NullPointerException e){ //IllegalArgumentException  and NullPointerException can be thrown by the ENUMs when using the value of method
			throw new ConfigurationException(e);
		}
	}

	protected abstract SingleUserRecommender<T> buildSUR(String configsPath) throws ConfigurationException;

	public SingleUserRecommender<T> getSUR(){
		return singleUserRecommender;
	}

	public AggregationStrategy getAggregationStrategy(){
		return aggregationStrategy;
	}
	
	public AggregationStrategy getGroupRatingEstimationStrategy(){
		return groupRatingEstimationStrategy;
	}

	public double getPfBeta(){
		return pf_beta;
	}

	public double getPfGamma(){
		return pf_gamma;
	}

	public double getPfDelta(){
		return pf_delta;
	}

	@Override
	public List<String> getAsStringList() {
		List<String> configsList = new ArrayList<>();

		configsList.add("TRADGRecPA CONFIGS");
		configsList.add("");
		configsList.add("General configs: ");
		configsList.add(" => Single User Recommender: "+ getSUR().toString());
		configsList.add(" => Rating Aggregation Strategy: "+ getAggregationStrategy().toString());
		configsList.add(" => Group Rating Estimation Strategy (for estimating the group ratings for a recommendation): "+ getGroupRatingEstimationStrategy().toString());

		return configsList;
	}

	@Override
	public List<String> getRequiredProperties() {
		List<String> rProp = new ArrayList<>();
		
		for (GeneralProperties p : GeneralProperties.values())
			rProp.add(p.getPropertyName());

		return rProp;
	}



}
