package com.mc.saas.offer.picker.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author suqiang.song
 *
 */
public class ConfigHelper {
	private static final ConfigHelper INSTANCE = new ConfigHelper();
	private static Properties appProps = new Properties();
	private static final Logger log = LoggerFactory.getLogger(ConfigHelper.class);

	private ConfigHelper() {

	}

	public void init(String appConfigPath) {
		if (null == appConfigPath || "".equals(appConfigPath)) {
			appProps.setProperty("offerCount", "30");
			appProps.setProperty("userCount", "60034");
			appProps.setProperty("userGroupCount", "1000");
			appProps.setProperty("maxOfferPerUser", "20");
			appProps.setProperty("minOfferPerUser", "5");
			appProps.setProperty("parallelExecution", "false");
			appProps.setProperty("diverseWeight", "0.50");
			appProps.setProperty("persist", "false");
			appProps.setProperty("fi", "test");
			appProps.setProperty("dataSource", "simulation");
			appProps.setProperty("rootPath", "/dev/shm/");
			appProps.setProperty("userOfferScoreFilePath",
					appProps.getProperty("rootPath") + appProps.getProperty("fi") + "/" + "userOfferScore.csv");
			appProps.setProperty("offerBurdgetFilePath",
					appProps.getProperty("rootPath") + appProps.getProperty("fi") + "/" + "offerBurdget.csv");

		} else {
			log.info(" Loading the Proprity file:" + appConfigPath);
			try {
				appProps.load(new FileInputStream(appConfigPath));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		log.info(" offerCount:" + appProps.getProperty("offerCount"));
		log.info(" userCount:" + appProps.getProperty("userCount"));
		log.info(" userGroupCount:" + appProps.getProperty("userGroupCount"));
		log.info(" maxOfferPerUser:" + appProps.getProperty("maxOfferPerUser"));
		log.info(" minOfferPerUser:" + appProps.getProperty("minOfferPerUser"));
		log.info(" parallelExecution:" + appProps.getProperty("parallelExecution"));
		log.info(" diverseWeight:" + appProps.getProperty("diverseWeight"));
		log.info(" persist:" + appProps.getProperty("persist"));
		log.info(" fi:" + appProps.getProperty("fi"));
		log.info(" dataSource:" + appProps.getProperty("dataSource"));
		if (!"simulation".equals(appProps.getProperty("dataSource"))) {
			log.info(" userOfferScoreFilePath:" + appProps.getProperty("userOfferScoreFilePath"));
			log.info(" offerBurdgetFilePath:" + appProps.getProperty("offerBurdgetFilePath"));
		}
	}

	public static ConfigHelper getInstance() {
		return ConfigHelper.INSTANCE;
	}

	public String getProperty(String key) {
		return appProps.getProperty(key);
	}

	public void setProperty(String key, String value) {
		appProps.setProperty(key, value);
	}

}
