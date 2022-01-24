package edu.isistan.christian.recommenders.groups.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class CSVExporter {

	public enum DefaultConfigs {
		COMMA_DELIMITER (";"),
		NEW_LINE_SEPARATOR ("\n"),
		DEFAULT_DECIMAL_DELIMITER ("."),
		DECIMAL_DELIMITER (",");

		private String configValue;
		DefaultConfigs (String configValue){
			this.configValue = configValue;
		}

		public String getConfigValue(){
			return configValue;
		}
	}

	protected String COMMA_DELIMITER, NEW_LINE_SEPARATOR, DEFAULT_DECIMAL_DELIMITER, DECIMAL_DELIMITER;

	public CSVExporter(){
		COMMA_DELIMITER = DefaultConfigs.COMMA_DELIMITER.getConfigValue();
		NEW_LINE_SEPARATOR = DefaultConfigs.NEW_LINE_SEPARATOR.getConfigValue();
		DEFAULT_DECIMAL_DELIMITER = DefaultConfigs.DEFAULT_DECIMAL_DELIMITER.getConfigValue();
		DECIMAL_DELIMITER = DefaultConfigs.DECIMAL_DELIMITER.getConfigValue();
	}

	public CSVExporter(String configsPath) throws ConfigurationException{
		//Read properties File
		Configuration config = new PropertiesConfiguration(configsPath);

		//READ configs
		COMMA_DELIMITER = config.getString("csvExp.commaDelimiter");
		NEW_LINE_SEPARATOR = config.getString("csvExp.NewLineSeparator");
		DEFAULT_DECIMAL_DELIMITER = config.getString("csvExp.DefaultDecimalSeparator");
		DECIMAL_DELIMITER = config.getString("csvExp.decimalDelimiter");
	}

	public CSVExporter(String commaDelimiter,
			String newLineSeparator, String defaultDecimalDelimiter,
			String decimalDelimiter) {
		super();
		COMMA_DELIMITER = commaDelimiter;
		NEW_LINE_SEPARATOR = newLineSeparator;
		DEFAULT_DECIMAL_DELIMITER = defaultDecimalDelimiter;
		DECIMAL_DELIMITER = decimalDelimiter;
	}
	
	public String getCommaDelimiter(){
		return COMMA_DELIMITER;
	}
	
	public String getNewLineSeparator(){
		return NEW_LINE_SEPARATOR;
	}
	
	public String getDefaultDecimalDelimiter(){
		return DEFAULT_DECIMAL_DELIMITER;
	}
	
	public String getDecimalDelimiter(){
		return DECIMAL_DELIMITER;
	}
	
	public void exportToCSV (String outFolderPath, String fileName, String header, String body) throws IOException{
		FileWriter fileWriter = null;
		try {
			File file = new File(outFolderPath);
			file.mkdirs(); //create the directories in the path if they don't exist
			fileWriter = new FileWriter(file.getAbsolutePath()+"\\"+fileName);

			//Write the CSV file header
			fileWriter.append(header);
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(body);	

		} catch (IOException e) {
			throw new IOException("Error in CsvFileWriter while exporting ["+fileName+"]: "+e.getMessage(),e.getCause());
		} finally {
			if (fileWriter != null){
				try{
					fileWriter.flush();
					fileWriter.close();
				} catch (IOException e){
					throw new IOException("Error while flushing/closing fileWriter while exporting ["+fileName+"]: "+e.getMessage(),e.getCause());
				}
			}
		}
	}
}
