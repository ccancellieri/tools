package it.geosolutions.tools.commons.logging;

import java.util.Enumeration;

import org.apache.log4j.LogManager;
import org.slf4j.Logger;

public class Log4JCheck {

	/**
	 * Returns true if it appears that log4j have been previously configured.
	 * This code checks to see if there are any appenders defined for log4j
	 * which is the definitive way to tell if log4j is already initialized
	 * see also:
	 * http://www.basilv.com/psd/blog/2007/how-to-add-logging-to-ant-builds
	 */
	public static boolean isConfigured() {
		Enumeration<Logger> appenders = LogManager.getRootLogger().getAllAppenders();
		if (appenders.hasMoreElements()) {
			return true;
		} else {
			Enumeration<Logger> loggers = LogManager.getCurrentLoggers();
			while (loggers.hasMoreElements()) {
				Logger c = (Logger) loggers.nextElement();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * print info about registered appenders
	 */
	public static void printInfo() {
		final Enumeration<Logger> appenders = LogManager.getRootLogger().getAllAppenders();
		if (appenders.hasMoreElements()) {
			info(appenders);
		} else {
			info(LogManager.getCurrentLoggers());
		}
	}
	
	private static void info(final Enumeration<Logger> loggers){
		while (loggers.hasMoreElements()) {
			Logger c = (Logger) loggers.nextElement();
			if (c.isInfoEnabled()){
				c.info("Configured logger: "+c.getName()+" -> "+c.toString());
			}
		}
	}
}
