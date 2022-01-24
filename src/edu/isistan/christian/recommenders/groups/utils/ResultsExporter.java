package edu.isistan.christian.recommenders.groups.utils;

import java.io.IOException;

import edu.isistan.christian.recommenders.groups.commons.datatypes.GRecResult;
import edu.isistan.christian.recommenders.sur.datatypes.SURItem;


public interface ResultsExporter <T extends SURItem> {
	
	public void export (GRecResult<T> result, String outfolderPath, boolean buildExportPath) throws IOException;
	
}
