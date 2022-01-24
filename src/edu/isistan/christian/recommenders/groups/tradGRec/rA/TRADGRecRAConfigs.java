package edu.isistan.christian.recommenders.groups.tradGRec.rA;

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
import edu.isistan.christian.recommenders.groups.tradGRec.rA.recAggregationStrategy.RAStrategySimple;
import edu.isistan.christian.recommenders.groups.tradGRec.rA.recAggregationStrategy.RecAggregationStrategies;
import edu.isistan.christian.recommenders.groups.tradGRec.rA.recAggregationStrategy.RecAggregationStrategy;
import edu.isistan.christian.recommenders.groups.tradGRec.rA.recsPerUserStrategy.RecsPerUserStrategies;
import edu.isistan.christian.recommenders.groups.tradGRec.rA.recsPerUserStrategy.RecsPerUserStrategy;
import edu.isistan.christian.recommenders.sur.SingleUserRecommender;
import edu.isistan.christian.recommenders.sur.datatypes.SURItem;

public abstract class TRADGRecRAConfigs <T extends SURItem> extends GRecConfigs{

	private static final Logger logger = LogManager.getLogger(TRADGRecRAConfigs.class);

	protected enum GeneralProperties {
		REC_AGGREGATION_STRATEGY ("grec.RAStrategy"),
		GROUP_RATING_ESTIMATION_STRATEGY ("grec.groupRatingEstimationStrategy"), //the one used to estimate the group ratings
		RA_RECS_PER_USER_STRATEGY ("grec.RecsPerUserStrategy"),
		RA_SIMPLE_RATING_AGGREGATION_STRATEGY ("grec.RAStrategy.simple.ratingAggregationStrategy"),
		RA_SIMPLE_RATING_AGGREGATION_STRATEGY_AV_THRESHOLD ("grec.RAStrategy.simple.ratingAggregationStrategy.av.threshold"),
		RA_SIMPLE_RATING_AGGREGATION_STRATEGY_UL_ALPHA ("grec.RAStrategy.simple.ratingAggregationStrategy.ul.alpha"),
		RA_SIMPLE_RATING_AGGREGATION_STRATEGY_UL_BETA ("grec.RAStrategy.simple.ratingAggregationStrategy.ul.beta"),
		RA_SIMPLE_RATING_AGGREGATION_STRATEGY_UL_GAMMA ("grec.RAStrategy.simple.ratingAggregationStrategy.ul.gamma"),
		RA_SIMPLE_RATING_AGGREGATION_STRATEGY_UL_AV_THRESHOLD ("grec.RAStrategy.simple.ratingAggregationStrategy.ul.av_threshold")
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
	protected RecAggregationStrategy<T> recAggregationStrategy;
	protected AggregationStrategy groupRatingEstimationStrategy;
	protected RecsPerUserStrategy recsPerUserStrategy;

	public TRADGRecRAConfigs(String configsPath) throws ConfigurationException {
		loadGeneralConfigs(configsPath);
	}

	@SuppressWarnings("unchecked")
	private void loadGeneralConfigs(String configsPath) throws ConfigurationException {
		//Build recommender from configs
		singleUserRecommender = buildSUR(configsPath);

		//Build aggregationStrategy from configs
		Configuration config = new PropertiesConfiguration(configsPath);
		try{
			this.recsPerUserStrategy = RecsPerUserStrategies.valueOf(config.getString(GeneralProperties.RA_RECS_PER_USER_STRATEGY.getPropertyName())).get();
			
			RecAggregationStrategies raStrategyType = RecAggregationStrategies.valueOf(config.getString(GeneralProperties.REC_AGGREGATION_STRATEGY.getPropertyName()));
			this.recAggregationStrategy = (RecAggregationStrategy<T>) raStrategyType.get();
			
			double maxRating = 1.0;
			switch (raStrategyType) {
			case SIMPLE:
				AggregationStrategies ratingAggregationStrategyType = AggregationStrategies.valueOf(config.getString(GeneralProperties.RA_SIMPLE_RATING_AGGREGATION_STRATEGY.getPropertyName()));
				AggregationStrategy ratingAggregationStrategy = ratingAggregationStrategyType.get();
				//set the parameters
				switch(ratingAggregationStrategyType) {
					case APPROVAL_VOTING:
						double approvalThreshold = config.getDouble(GeneralProperties.RA_SIMPLE_RATING_AGGREGATION_STRATEGY_AV_THRESHOLD.getPropertyName());
						((AggregationStrategyApprovalVoting) ratingAggregationStrategy).setApprovalThreshold(approvalThreshold);
						((AggregationStrategyApprovalVoting) ratingAggregationStrategy).setMaxRating(maxRating);
						break;
					case AVERAGE: case LEAST_MISERY: case MOST_PLEASURE: //there are no parameters to set 
						break;
					case UPWARD_LEVELING:
						double alpha = config.getDouble(GeneralProperties.RA_SIMPLE_RATING_AGGREGATION_STRATEGY_UL_ALPHA.getPropertyName());
						double beta = config.getDouble(GeneralProperties.RA_SIMPLE_RATING_AGGREGATION_STRATEGY_UL_BETA.getPropertyName());
						double gamma = config.getDouble(GeneralProperties.RA_SIMPLE_RATING_AGGREGATION_STRATEGY_UL_GAMMA.getPropertyName());
						
						if (alpha + beta + gamma != 1)
							throw new IllegalArgumentException ("alpha, beta and gamma parameters must add up 1");
						
						double av_Threshold = config.getDouble(GeneralProperties.RA_SIMPLE_RATING_AGGREGATION_STRATEGY_UL_AV_THRESHOLD.getPropertyName());
						((AggregationStrategyUpwardLeveling) ratingAggregationStrategy).setAvgStrategy(new AggregationStrategyAverage());
						((AggregationStrategyUpwardLeveling) ratingAggregationStrategy).setAvStrategy(new AggregationStrategyApprovalVoting(av_Threshold, maxRating));
						((AggregationStrategyUpwardLeveling) ratingAggregationStrategy).setMaxRating(maxRating);
						((AggregationStrategyUpwardLeveling) ratingAggregationStrategy).setAlpha(alpha);
						((AggregationStrategyUpwardLeveling) ratingAggregationStrategy).setBeta(beta);
						((AggregationStrategyUpwardLeveling) ratingAggregationStrategy).setGamma(gamma);

						break;
					default: break;
				}
				((RAStrategySimple<SURItem>) recAggregationStrategy).setRatingAggregationStrategy(ratingAggregationStrategy);
				break;
				
			default:
				break;
			}
			
			AggregationStrategies groupRatingEstimationStrategyType = AggregationStrategies.valueOf(config.getString(GeneralProperties.GROUP_RATING_ESTIMATION_STRATEGY.getPropertyName()));
			switch(groupRatingEstimationStrategyType) {
			case APPROVAL_VOTING: case UPWARD_LEVELING:
				throw new ConfigurationException("APPROVAL_VOTING and UPWARD_LEVELING are not valid values for the property"
							+ GeneralProperties.GROUP_RATING_ESTIMATION_STRATEGY.getPropertyName()+" (check the properties file of TRADGRecRA)");
			case AVERAGE: case LEAST_MISERY: case MOST_PLEASURE:
				break;
			default:
				//do nothing. If it does not exist then the AggregationStrategies.valueOf has thrown an exception so this portion of the code is unreachable
				break;
			}
			
			groupRatingEstimationStrategy = AggregationStrategies.valueOf(config.getString(GeneralProperties.GROUP_RATING_ESTIMATION_STRATEGY.getPropertyName())).get();

			logger.info("File loaded: TRADGRecRAConfigs [ grec.singleUserRecommender= "+singleUserRecommender.toString()
					+" | "+GeneralProperties.REC_AGGREGATION_STRATEGY.getPropertyName()+"= "+recAggregationStrategy.toString()
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
	
	public RecsPerUserStrategy getRecsPerUserStrategy() {
		return recsPerUserStrategy;
	}

	public RecAggregationStrategy<T> getRecAggregationStrategy(){
		return recAggregationStrategy;
	}
	
	public AggregationStrategy getGroupRatingEstimationStrategy(){
		return groupRatingEstimationStrategy;
	}

	@Override
	public List<String> getAsStringList() {
		List<String> configsList = new ArrayList<>();

		configsList.add("TRADGRecRA CONFIGS");
		configsList.add("");
		configsList.add("General configs: ");
		configsList.add(" => Single User Recommender: "+ getSUR().toString());
		configsList.add(" => Recommendation Aggregation Strategy: "+ getRecAggregationStrategy().toString());
		configsList.add(" => Recs Per User Strategy: "+ getRecsPerUserStrategy().toString());
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
