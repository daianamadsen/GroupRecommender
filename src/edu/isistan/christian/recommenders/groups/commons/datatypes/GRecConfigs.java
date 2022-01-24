package edu.isistan.christian.recommenders.groups.commons.datatypes;

import java.io.IOException;
import java.util.List;

import edu.isistan.christian.recommenders.sur.utils.TextFileExporter;

public abstract class GRecConfigs {
	
	public abstract List<String> getRequiredProperties();

	public abstract List<String> getAsStringList();
	
	public void export (String folderPath, String fileName) throws IOException{
		TextFileExporter.saveTextToFile(folderPath, fileName, getAsStringList());
	}
	
	
}
