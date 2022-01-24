package edu.isistan.christian.recommenders.groups.magres.rA;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.isistan.christian.recommenders.groups.commons.aggregationStrategy.AggregationStrategies;
import edu.isistan.christian.recommenders.groups.commons.aggregationStrategy.AggregationStrategy;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecConfigs;
import edu.isistan.christian.recommenders.groups.commons.pumas.PUMASConfigs;
import edu.isistan.christian.recommenders.sur.datatypes.SURItem;

public class MAGReSRAConfigs<T extends SURItem> extends GRecConfigs{
	private static final Logger logger = LogManager.getLogger(MAGReSRAConfigs.class);

	protected enum GeneralProperties {
		GROUP_RATING_ESTIMATION_STRATEGY ("grec.groupRatingEstimationStrategy"); //the one used to estimate the group ratings

		private String propertyName;
		GeneralProperties (String propertyName){
			this.propertyName = propertyName;
		}

		public String getPropertyName(){
			return propertyName;
		}
	}
	
	protected PUMASConfigs<T> pumasConfigs;

	
	/** AggregationStrategy used by PUMAS for doing the estimation of the group rating for a recommendation */
	protected AggregationStrategy groupRatingEstimationStrategy;

	public MAGReSRAConfigs(String configsPath, PUMASConfigs<T> pumasConfigs) throws ConfigurationException {
		this.loadGeneralConfigs(configsPath);
		this.pumasConfigs = pumasConfigs;
	}

	private void loadGeneralConfigs(String configsPath) throws ConfigurationException {
		Configuration config = new PropertiesConfiguration(configsPath);
		try{
			groupRatingEstimationStrategy = AggregationStrategies.valueOf(config.getString(GeneralProperties.GROUP_RATING_ESTIMATION_STRATEGY.getPropertyName())).get();
		} catch (IllegalArgumentException | NullPointerException e){ //IllegalArgumentException  and NullPointerException can be thrown by the ENUMs when using the value of method
			throw new ConfigurationException(e);
		}
		
		logger.info("File loaded: MAGReS-RA GENERAL CONFIGS [ groupRatingEstimationStrategy= "+groupRatingEstimationStrategy+"]");
	}
	
	/**
	 * 
	 * @return the {@link AggregationStrategy} used by PUMAS for doing the estimation of the group rating for a recommendation
	 */
	public AggregationStrategy getGroupRatingEstimationStrategy(){
		return this.groupRatingEstimationStrategy;
	}
	
	public PUMASConfigs<T> getPUMASConfigs() {
		return this.pumasConfigs;
	}

	//--------------------------------

	@Override
	public List<String> getAsStringList() {
		List<String> configsList = new ArrayList<>(); //change the strings!

		configsList.add("MAGReS CONFIGS");
		configsList.add("");
		//General Configs
		configsList.add("General configs:");
		configsList.add(" => Group Rating Estimation Strategy (for estimating the group ratings for a recommendation): "+ this.getGroupRatingEstimationStrategy().toString());
		configsList.add("");
		configsList.addAll(this.pumasConfigs.getAsStringList());
		

		return configsList;
	}

	@Override
	public List<String> getRequiredProperties() { //add everything but SUR from the USER AG
		List<String> rProp = new ArrayList<>();

		for (GeneralProperties p : GeneralProperties.values())
			rProp.add(p.getPropertyName());
		
		rProp.addAll(this.pumasConfigs.getRequiredProperties());
		
		return rProp;
	}
}
