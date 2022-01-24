package edu.isistan.christian.recommenders.groups.magres.pA;

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
import edu.isistan.christian.recommenders.groups.commons.pumas.PUMASConfigs;
import edu.isistan.christian.recommenders.groups.magres.pA.subsetGeneratorStrategy.SubsetGeneratorStrategies;
import edu.isistan.christian.recommenders.groups.magres.pA.subsetGeneratorStrategy.SubsetGeneratorStrategy;
import edu.isistan.christian.recommenders.sur.SingleUserRecommender;
import edu.isistan.christian.recommenders.sur.datatypes.SURItem;

public abstract class MAGReSPAConfigs <T extends SURItem> extends GRecConfigs{

	private static final Logger logger = LogManager.getLogger(MAGReSPAConfigs.class);

	protected enum GeneralProperties {
		AGGREGATION_STRATEGY ("grec.aggregationStrategy"),
		GROUP_RATING_ESTIMATION_STRATEGY ("grec.groupRatingEstimationStrategy"), //the one used to estimate the group ratings
		SUBSET_GENERATOR_STRATEGY ("magres.pa.subsetGeneratorStrategy"),
		AGGREGATION_STRATEGY_AV_THRESHOLD ("grec.aggregationStrategy.av.threshold"),
		AGGREGATION_STRATEGY_UL_ALPHA ("grec.aggregationStrategy.ul.alpha"),
		AGGREGATION_STRATEGY_UL_BETA ("grec.aggregationStrategy.ul.beta"),
		AGGREGATION_STRATEGY_UL_GAMMA ("grec.aggregationStrategy.ul.gamma"),
		AGGREGATION_STRATEGY_UL_AV_THRESHOLD ("grec.aggregationStrategy.ul.av_threshold")
		;

		private String propertyName;
		GeneralProperties (String propertyName){
			this.propertyName = propertyName;
		}

		public String getPropertyName(){
			return propertyName;
		}
	}
	
	protected AggregationStrategy aggregationStrategy;
	protected AggregationStrategy groupRatingEstimationStrategy;
	protected SubsetGeneratorStrategy<T> subsetGeneratorStrategy;
	
	protected PUMASConfigs<T> pumasConfigs;

	public MAGReSPAConfigs(String configsPath, PUMASConfigs<T> pumasConfigs) throws ConfigurationException {
		this.pumasConfigs = pumasConfigs;
		this.loadGeneralConfigs(configsPath);
	}

	@SuppressWarnings("unchecked")
	private void loadGeneralConfigs(String configsPath) throws ConfigurationException {
		//Build recommender from configs
//		singleUserRecommender = buildSUR(configsPath);//we use the one from pumasConfigs

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
			
			groupRatingEstimationStrategy = AggregationStrategies.valueOf(config.getString(GeneralProperties.GROUP_RATING_ESTIMATION_STRATEGY.getPropertyName())).get();
			subsetGeneratorStrategy = (SubsetGeneratorStrategy<T>) SubsetGeneratorStrategies.valueOf(config.getString(GeneralProperties.SUBSET_GENERATOR_STRATEGY.getPropertyName())).get();

			logger.info("File loaded: MAGReSPAConfigs [ "+"singleUserRecommender= "+ pumasConfigs.getSUR()
					+" | "+GeneralProperties.AGGREGATION_STRATEGY.getPropertyName()+"= "+aggregationStrategy.getClass().getSimpleName()
					+" | "+GeneralProperties.GROUP_RATING_ESTIMATION_STRATEGY.getPropertyName()+"= "+groupRatingEstimationStrategy.getClass().getSimpleName()
					+" | "+GeneralProperties.SUBSET_GENERATOR_STRATEGY.getPropertyName()+"= "+subsetGeneratorStrategy.getClass().getSimpleName()
					+"]");
		} catch (IllegalArgumentException | NullPointerException e){ //IllegalArgumentException  and NullPointerException can be thrown by the ENUMs when using the value of method
			throw new ConfigurationException(e);
		}
	}
	
	public SingleUserRecommender<T> getSUR(){
		return pumasConfigs.getSUR();
	}

	public AggregationStrategy getAggregationStrategy(){
		return aggregationStrategy;
	}
	
	public AggregationStrategy getGroupRatingEstimationStrategy(){
		return groupRatingEstimationStrategy;
	}
	
	public SubsetGeneratorStrategy<T> getSubsetGeneratorStrategy() {
		return subsetGeneratorStrategy;
	}
	
	public PUMASConfigs<T> getPUMASConfigs() {
		return pumasConfigs;
	}

	
	@Override
	public List<String> getAsStringList() {
		List<String> configsList = new ArrayList<>();

		configsList.add("MAGReSPA CONFIGS");
		configsList.add("");
		configsList.add("General configs: ");
		configsList.add(" => Single User Recommender: "+ pumasConfigs.getSUR().toString());
		configsList.add(" => Rating Aggregation Strategy: "+ getAggregationStrategy().toString());
		configsList.add(" => Group Rating Estimation Strategy (for estimating the group ratings for a recommendation): "+ getGroupRatingEstimationStrategy().toString());
		configsList.add(" => Subset generation Strategy: "+ getSubsetGeneratorStrategy().toString());
		
		configsList.addAll(this.pumasConfigs.getAsStringList());
		

		return configsList;
	}

	@Override
	public List<String> getRequiredProperties() {
		List<String> rProp = new ArrayList<>();
		
		for (GeneralProperties p : GeneralProperties.values())
			rProp.add(p.getPropertyName());
		
		rProp.addAll(this.pumasConfigs.getRequiredProperties());

		return rProp;
	}



}
