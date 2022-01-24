package edu.isistan.christian.recommenders.groups.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecGroup;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecRecommendation;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecRecommendationStats;
import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecResult;
import edu.isistan.christian.recommenders.sur.datatypes.SURItem;
import edu.isistan.christian.recommenders.sur.datatypes.SURUser;

public abstract class CSVResultsExporter<T extends SURItem> implements ResultsExporter<T>{

	private static final Logger logger = LogManager.getLogger(CSVResultsExporter.class);

	public static final String EMPTY_COLUMN = "EMPTY_COLUMN";
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	protected CSVExporter csvExporter;

	protected String COMMA_DELIMITER, NEW_LINE_SEPARATOR, DEFAULT_DECIMAL_DELIMITER, DECIMAL_DELIMITER;

	public CSVResultsExporter(){
		csvExporter = new CSVExporter();
		this.configDelimiters();
	}

	public CSVResultsExporter(String configsPath) throws ConfigurationException{
		csvExporter = new CSVExporter(configsPath);
		this.configDelimiters();
	}

	public CSVResultsExporter(String commaDelimiter,
			String newLineSeparator, String defaultDecimalDelimiter,
			String decimalDelimiter) {
		csvExporter = new CSVExporter(commaDelimiter, newLineSeparator, defaultDecimalDelimiter, decimalDelimiter);
		this.configDelimiters();
	}
	
	private void configDelimiters(){
		COMMA_DELIMITER = csvExporter.getCommaDelimiter();
		NEW_LINE_SEPARATOR = csvExporter.getNewLineSeparator();
		DEFAULT_DECIMAL_DELIMITER = csvExporter.getDefaultDecimalDelimiter();
		DECIMAL_DELIMITER = csvExporter.getDecimalDelimiter();
	}

	//-------------------------------------------

	protected void export(GRecResult<T> result, String resultsFolderPath) throws IOException{
		logger.info("Exporting results...");
		
		//Export run results to CSV
//		String runResultsFileName = result.getRecommenderName()+"_RESULTS [Users= "+buildGroupMembersIDsList(result.getGroup())+"].csv";
		String runResultsFileName = result.getRecommenderName()+"_RESULTS.csv";
		String runResultsBody = createRunResultsRows(result) + NEW_LINE_SEPARATOR + createRunResultsSummary(result);
		csvExporter.exportToCSV(resultsFolderPath, runResultsFileName, createRunResultsHeader(result), runResultsBody);
		logger.info("=> File Created: \""+runResultsFileName+"\" in the folder \""+resultsFolderPath+"\"");

		//Export run stats to CSV
//		String runStatsFileName = result.getRecommenderName()+"_STATS [Users= "+buildGroupMembersIDsList(result.getGroup())+"].csv";
		String runStatsFileName = result.getRecommenderName()+"_STATS.csv";
		csvExporter.exportToCSV(resultsFolderPath, runStatsFileName, createRunStatsHeader(result), createRunStatsData(result));
		logger.info("=> File Created: \""+runStatsFileName+"\" in the folder \""+resultsFolderPath+"\"");

		String runConfigsFileName = result.getRecommenderName()+"_CONFIGS.txt";
		result.getConfigs().export(resultsFolderPath, runConfigsFileName);
		logger.info("=> File Created: \""+runConfigsFileName+"\" in the folder \""+resultsFolderPath+"\"");
	}
	
	/**
	 * 
	 * @param result
	 * @param outFolderPath
	 * @param buildExportPath if TRUE the exporter will build a new export path taking the outFolderPath as base: the folder in which the files will be exported
	 * will be inside the folder designated by outFolderPath. If FALSE, it will export the results to files inside the outFolderPath without creating new folders
	 * @throws IOException
	 */
	public void export(GRecResult<T> result, String outFolderPath, boolean buildExportPath) throws IOException{
		logger.info("Exporting results...");
		
		String resultsFolderPath = outFolderPath;
		if (buildExportPath)
			resultsFolderPath = buildExportFolderPath(outFolderPath, result);
		
		export(result, resultsFolderPath);
	}
	
	//--------------- RUN RESULTS HEADER BUILDER METHOD
	
	private String createRunResultsHeader(GRecResult<T> result){
		String header = "Recommendation Nº"+COMMA_DELIMITER;

		//PART OF THE HEADER RELATED TO ITEMS DATA (ID, etc)
		for (String itemHeaderData : getItemHeaderData(result))
			if (itemHeaderData.equals(EMPTY_COLUMN))
				header+= COMMA_DELIMITER;
			else
				header+= itemHeaderData+COMMA_DELIMITER;

		//Fixed (allways stays the same)
		header+= "Recommendation time (in ms)"+COMMA_DELIMITER+"Predicted Group Utility"+COMMA_DELIMITER;

		//PART OF THE HEADER RELATED TO USERS DATA (UTILITY, etc)
		for (String userHeaderData : getUserHeaderData(result))
			if (userHeaderData.equals(EMPTY_COLUMN))
				header+= COMMA_DELIMITER;
			else
				header+= userHeaderData+COMMA_DELIMITER;

		return header;
	}
	
	//--------------- RUN RESULTS ROWS BUILDER METHOD
	private String createRunResultsRows(GRecResult<T> result){
		String data = "";
		int recNumber = 1;
		for (GRecRecommendation<T> rec : result.getRecommendations()){
			data+= String.valueOf(recNumber)+COMMA_DELIMITER;

			//Add items data
			for (String itemRowData : getItemRowData(rec))
				if (itemRowData.equals(EMPTY_COLUMN))
					data+= COMMA_DELIMITER;
				else
					data+= itemRowData+COMMA_DELIMITER;

			GRecRecommendationStats stats = result.getRecommendationStats(rec);
			data+= getStringFrom(stats.getRecommendationTime())+COMMA_DELIMITER;
			data+= getStringFrom(rec.getPredictedGroupRating())+COMMA_DELIMITER;

			//Add user data
			for (String userRowData : getUserRowData(rec, result))
				if (userRowData.equals(EMPTY_COLUMN))
					data+= COMMA_DELIMITER;
				else
					data+= userRowData+COMMA_DELIMITER;

			data += NEW_LINE_SEPARATOR;
			recNumber++;
		}

		return data;
	}
	
	//--------------- RUN RESULTS SUMMARY ROW BUILDER METHOD
	private String createRunResultsSummary(GRecResult<T> result){
		String summary = "RESULTS AVERAGE"+COMMA_DELIMITER;

		//Add blank columns as a placeholder for the item data
		int itemDataCount = getItemHeaderData(result).size(); 
		for (int i=0; i<itemDataCount;i++){
			summary+=COMMA_DELIMITER;
		}

		for (String summaryRowData : getSummaryRowData(result))
			if (summaryRowData.equals(EMPTY_COLUMN))
				summary+= COMMA_DELIMITER;
			else
				summary+= summaryRowData+COMMA_DELIMITER;

		return summary;
	}


	///----------------------- RUN RESULTS ITEM HEADER & ROW DATA 
	/**
	 * By overriding/extending this method it is possible to add more information related 
	 * to the items in the header. If this method is overriden/extended => the method 
	 * {@link #getItemRowData(GRecRecommendation)} should be also extended as it is
	 * the responsible for creating the items related information in the rows
	 * @param result 
	 * @return a list of string related to the item data in the header. 
	 * By default is a list with the string "Item ID" as each GRecRecommendation
	 * (of the list of recommendations held by the "result" parameter) contains an
	 * object which extends the type {@link SURItem} which ensures to have at least an ID.
	 */
	protected List<String> getItemHeaderData(GRecResult<T> result){
		List<String> itemHeaderData = new ArrayList<>();
		itemHeaderData.add("Item ID");
		return itemHeaderData;
	}
	
	protected List<String> getItemRowData(GRecRecommendation<T> rec){
		List<String> itemRowData = new ArrayList<>();
		itemRowData.add(rec.getRecommendedItem().getID());
		return itemRowData;
	}
	
	///----------------------- RUN RESULTS USER HEADER & ROW DATA 
	/**
	 * By overriding/extending this method it is possible to add more information related 
	 * to the users in the header. If this method is overriden/extended => the method 
	 * {@link #getUserRowData(GRecRecommendation, GRecResult)} should be also extended as it is
	 * the responsible for creating the users related information in the rows
	 * @param result 
	 * @return a list of string related to the user data in the header. The list may contain the special
	 * string {@link #EMPTY_COLUMN} which will make the  {@link #createRunResultsHeader(GRecResult)} to add
	 * an empty column in the header. 
	 * By default is a list with an {@link #EMPTY_COLUMN} and then a list of the headers of the user 
	 * utilities columns as the {@link GRecResult} contains a {@link GRecRecommendationStats} for every
	 * recommendation which at least contains the user's utilities for the related recommendation. 
	 * the string "Item ID" as each GRecRecommendation.
	 */
	protected List<String> getUserHeaderData(GRecResult<T> result){
		List<String> userHeaderData = new ArrayList<>(); 
		/*
		 * We want to add a blank column before the user data
		 */
		userHeaderData.add(EMPTY_COLUMN);

		for (SURUser member : result.getGroup()){
			userHeaderData.add("USER [ID= "+member.getID()+"] Utility Value");
		}

		return userHeaderData;
	}

	protected List<String> getUserRowData(GRecRecommendation<T> rec, GRecResult<T> result){
		List<String> userRowData = new ArrayList<>();
		GRecRecommendationStats stats = result.getRecommendationStats(rec);

		userRowData.add(EMPTY_COLUMN);

		for (SURUser member : result.getGroup()){
			userRowData.add(getStringFrom(stats.getItemRatingForUser(member.getID())));
		}

		return userRowData;
	}

	///----------------------- RUN RESULTS SUMMARIZED ROW DATA

	protected List<String> getSummaryRowData (GRecResult<T> result){
		List<String> summaryRowData = new ArrayList<>();		

		int recsCount = result.getRecommendations().size(); 
		if (recsCount == 0){
			summaryRowData.add(this.getStringFrom(0.0)); //avg recommendation time in 0
			summaryRowData.add(this.getStringFrom(0.0)); //avg predicted group utility in 0
			summaryRowData.add(EMPTY_COLUMN); //spacing (blank column) respect to the user utilities columns

			for (int i=0; i<result.getGroup().size(); i++){ //avg utility for every member is 0
				summaryRowData.add(this.getStringFrom(0.0));
			}
			return summaryRowData;
		}

		//Compute average recommendation time
		double avgTime = 0.0;
		for (GRecRecommendation<T> rec : result.getRecommendations()){
			avgTime+= result.getRecommendationStats(rec).getRecommendationTime();
		}

		avgTime /= recsCount;
		summaryRowData.add(this.getStringFrom(avgTime)); //add column to csv

		//Compute average predicted group utility
		double avgGroupUtility = 0.0;
		for (GRecRecommendation<T> rec : result.getRecommendations()){
			avgGroupUtility+= rec.getPredictedGroupRating();
		}
		avgGroupUtility/= recsCount;
		summaryRowData.add(this.getStringFrom(avgGroupUtility)); //add column to csv

		summaryRowData.add(EMPTY_COLUMN); //spacing (blank column) respect to the user utilities columns

		//Compute average utility for every member of the group
		for (SURUser member : result.getGroup()){
			double avgSatisfaction = 0.0;
			for (GRecRecommendation<T> rec : result.getRecommendations()){
				avgSatisfaction+= result.getRecommendationStats(rec).getItemRatingForUser(member.getID());
			}
			avgSatisfaction /= recsCount;
			summaryRowData.add(this.getStringFrom(avgSatisfaction));
		}

		return summaryRowData;
	}
	
	//------------------------------------------- RUN STATS INFORMATION

	protected String createRunStatsHeader(GRecResult<T> result){
		return "STAT NAME"+COMMA_DELIMITER+"STAT VALUE";
	}

	protected String createRunStatsData(GRecResult<T> result){
		String statsData = "";
//		
//		statsData+="Recommender init/loading time on the last initialization (in ms)"+COMMA_DELIMITER
//				+this.getStringFrom(recommenderInitTime)+NEW_LINE_SEPARATOR;
		statsData+="Recommendation total time (in ms)"+COMMA_DELIMITER
				+this.getStringFrom(result.getRecommendationTotalTime())+NEW_LINE_SEPARATOR;
		
		return statsData;
	}

	//------------------------------------------- UTILS

	protected String getStringFrom (double d){
		String stringForm = String.valueOf(d);
		return (stringForm.replace(DEFAULT_DECIMAL_DELIMITER, DECIMAL_DELIMITER));
	}

	protected String getStringFrom (float f){
		String stringForm = String.valueOf(f);
		return (stringForm.replace(DEFAULT_DECIMAL_DELIMITER, DECIMAL_DELIMITER));
	}

	protected String buildGroupMembersIDsList (GRecGroup group){
		String groupMembers = "";
		for (int i=0; i<group.size(); i++){
			groupMembers+= group.get(i).getID();
			if (i!= group.size()-1)
				groupMembers+= ", ";
		}
		return groupMembers;
	}

	protected String buildExportFolderPath (String baseFolderPath, GRecResult<T> result){	
		if (!baseFolderPath.endsWith(FILE_SEPARATOR))
			baseFolderPath+= FILE_SEPARATOR;
		return baseFolderPath+result.getGroup().getID()+" [MembersIDs= "+buildGroupMembersIDsList(result.getGroup())+"]"+FILE_SEPARATOR;
	}

}
