package com.mc.saas.offer.picker.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mc.saas.offer.picker.Knapsack;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.BeanToCsv;
import com.opencsv.bean.ColumnPositionMappingStrategy;
/**
 * @author suqiang.song
 *
 */
public class PersistHelper {
	private static final Logger log = LoggerFactory.getLogger(PersistHelper.class);
	private static final PersistHelper INSTANCE = new PersistHelper();
	private static Map<String, CSVWriter> fileWriterMap = new ConcurrentHashMap<String, CSVWriter>();
	private static Map<String, CSVReader> fileReaderMap = new ConcurrentHashMap<String, CSVReader>();


	private PersistHelper() {
	}

	public static PersistHelper getInstance() {
		return PersistHelper.INSTANCE;
	}

	public void clean() {
		fileWriterMap.clear();
		fileReaderMap.clear();
	}

	public void registerWriter(String instanceName, String path, String suffix) {
		try {
			// prepare the file
			prepareFile(path + instanceName + suffix);
			fileWriterMap.put(instanceName, new CSVWriter(new FileWriter(path + instanceName + suffix), ','));
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	private File prepareFile(String path) {
		File file = new File(path);

		// Create the file
		try {
			// delete the file if exsits
			if (file.isFile()) {
				FileUtils.forceDelete(file);
			}
			file.getParentFile().mkdirs();
			if (file.createNewFile()) {
				log.info("File is created-->" + path);
			} else {
				log.info("File already exists.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;

	}

	public void registerReader(String instanceName, String path, String suffix) {
		try {
			fileReaderMap.put(instanceName, new CSVReader(new FileReader(path + instanceName + suffix), ','));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public CSVWriter getWriter(String instanceName) {
		return fileWriterMap.get(instanceName);
	}

	public CSVReader getReader(String instanceName) {
		return fileReaderMap.get(instanceName);
	}

	public void closeWriter(String instanceName) {
		try {
			getWriter(instanceName).close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeReader(String instanceName) {
		try {
			getReader(instanceName).close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeNext(String instanceName, String[] nextLine) {
		getWriter(instanceName).writeNext(nextLine);
	}

	public void writeOfferMetrics(String instanceName,String[] columns,List<OfferMetrics> lines) {
		BeanToCsv bc = new BeanToCsv();
		ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
		mappingStrategy.setType(OfferMetrics.class);
		// Setting the colums for mappingStrategy
		mappingStrategy.setColumnMapping(columns);
		// Writing bags to csv file
		bc.write(mappingStrategy, getWriter(instanceName), lines);
		closeWriter(instanceName);
	}
	
	
	public void writePickerResults(String instanceName,String[] columns,List<Knapsack> lines) {

		BeanToCsv bc = new BeanToCsv();
		ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
		mappingStrategy.setType(Knapsack.class);
		// Setting the colums for mappingStrategy
		mappingStrategy.setColumnMapping(columns);
		// Writing bags to csv file
		bc.write(mappingStrategy, getWriter(instanceName), lines);
		closeWriter(instanceName);
	}

	public void writeAll(String instanceName, List<String[]> allLines) {
		getWriter(instanceName).writeAll(allLines);
	}

	public void forceDeleteFile(File file) {
		try {
			if (file.isFile()) {
				FileUtils.forceDelete(file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}