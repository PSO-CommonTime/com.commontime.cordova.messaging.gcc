package com.commontime.mdesign.plugins.base;

import java.io.IOException;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.helpers.LogLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.mindpipe.android.logging.log4j.LogCatAppender;

public class CTLogConfigurator {

	private Level rootLevel = Level.DEBUG;
	private String filePattern = "%d - [%p::%c::%C] - %m%n";
	private String logCatPattern = "%m%n";
	private String fileName = "android-log4j.log";
	private String logName = null;
	private int maxBackupSize = 5;
	private long maxFileSize = 512 * 1024;
	private boolean immediateFlush = true;
	private boolean useLogCatAppender = true;
	private boolean useFileAppender = true;
	private boolean resetConfiguration = true;
	private boolean internalDebugging = false;
	private boolean encrypt = false;
	
	public CTLogConfigurator(final String logName) {
		this.logName = logName;
	}

	
	public void configure() {
		final Logger root = Logger.getRootLogger();
		
		if(isResetConfiguration()) {
			LogManager.getLoggerRepository().resetConfiguration();
		}

		LogLog.setInternalDebugging(isInternalDebugging());
		
		if(isUseFileAppender()) {
			configureFileAppender();
		}
		
		if(isUseLogCatAppender()) {
			configureLogCatAppender();
		}
		
		root.setLevel(getRootLevel());
	}
	
	/**
	 * Sets the level of logger with name <code>loggerName</code>.
	 * Corresponds to log4j.properties <code>log4j.logger.org.apache.what.ever=ERROR</code>
	 * @param loggerName
	 * @param level
	 */
	public static void setLevel(final String loggerName, final Level level) {
		Logger.getLogger(loggerName).setLevel(level);
	}

	public static String getLevel(final String loggerName) {
		return Logger.getLogger(loggerName).getLevel().toString();
	}

	public void configureFileAppender() {
		final Logger l = Logger.getLogger(logName);
		final RollingFileAppender rollingFileAppender;
		final Layout fileLayout = new PatternLayout(getFilePattern());

		try {
			if( encrypt ) {
				rollingFileAppender = new RollingFileEncryptedAppender(fileLayout, getFileName());
			} else {
				rollingFileAppender = new RollingFileAppender(fileLayout, getFileName());
			}
		} catch (final IOException e) {
			throw new RuntimeException("Exception configuring log system", e);
		}

		rollingFileAppender.setName("FileAppender");
		rollingFileAppender.setMaxBackupIndex(getMaxBackupSize());
		rollingFileAppender.setMaximumFileSize(getMaxFileSize());
		rollingFileAppender.setImmediateFlush(isImmediateFlush());

		rollingFileAppender.setThreshold(Priority.toPriority(Priority.ALL_INT));
		
		l.addAppender(rollingFileAppender);
	}
	
	public void configureLogCatAppender() {
		final Logger l = Logger.getLogger(logName);
		final Layout logCatLayout = new PatternLayout(getLogCatPattern());
		final LogCatAppender logCatAppender;
		
		if( encrypt ) {
			logCatAppender = new LogCatEncryptedAppender(logCatLayout);
		} else { 
			logCatAppender = new LogCatAppender(logCatLayout);
		}
		
		logCatAppender.setName("LogCatAppender");

		l.addAppender(logCatAppender);
	}
	
	/*
	public void configureNotifyLogAppender(JSONObject jso) {
		final Logger l = Logger.getLogger(logName);
		final NotifyLogAppender notifyLogAppender;
		if( encrypt ) {
			notifyLogAppender = new NotifyLogEncryptedAppender(jso);
		} else {
			notifyLogAppender = new NotifyLogAppender(jso);
		}		 
		notifyLogAppender.setName("NotifyLogAppender");
		notifyLogAppender.setThreshold(Priority.DEBUG);

		l.addAppender(notifyLogAppender);
	}
	*/
	
	public JSONArray getAppenders() throws JSONException {
		JSONArray appenders = new JSONArray();
		final Logger l = Logger.getLogger(logName);
		Enumeration e = l.getAllAppenders();
		for (int i = 0; e.hasMoreElements(); i += 1) {
			Appender app = (Appender)e.nextElement();
			appenders.put(i, app.getName());
		}
		return appenders;
	}

	/**
	 * Return the log level of the root logger
	 * @return Log level of the root logger
	 */
	public Level getRootLevel() {
		return rootLevel;
	}

	/**
	 * Sets log level for the root logger
	 * @param level Log level for the root logger
	 */
	public void setRootLevel(final Level level) {
		this.rootLevel = level;
	}

	public String getFilePattern() {
		return filePattern;
	}

	public void setFilePattern(final String filePattern) {
		this.filePattern = filePattern;
	}

	public String getLogCatPattern() {
		return logCatPattern;
	}

	public void setLogCatPattern(final String logCatPattern) {
		this.logCatPattern = logCatPattern;
	}

	/**
	 * Returns the name of the log file
	 * @return the name of the log file
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the name of the log file
	 * @param fileName Name of the log file
	 */
	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Returns the maximum number of backed up log files
	 * @return Maximum number of backed up log files
	 */
	public int getMaxBackupSize() {
		return maxBackupSize;
	}

	/**
	 * Sets the maximum number of backed up log files
	 * @param maxBackupSize Maximum number of backed up log files
	 */
	public void setMaxBackupSize(final int maxBackupSize) {
		this.maxBackupSize = maxBackupSize;
	}

	/**
	 * Returns the maximum size of log file until rolling
	 * @return Maximum size of log file until rolling
	 */
	public long getMaxFileSize() {
		return maxFileSize;
	}

	/**
	 * Sets the maximum size of log file until rolling
	 * @param maxFileSize Maximum size of log file until rolling
	 */
	public void setMaxFileSize(final long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public boolean isImmediateFlush() {
		return immediateFlush;
	}

	public void setImmediateFlush(final boolean immediateFlush) {
		this.immediateFlush = immediateFlush;
	}

	/**
	 * Returns true, if FileAppender is used for logging
	 * @return True, if FileAppender is used for logging
	 */
	public boolean isUseFileAppender() {
		return useFileAppender;
	}

	/**
	 * @param useFileAppender the useFileAppender to set
	 */
	public void setUseFileAppender(final boolean useFileAppender) {
		this.useFileAppender = useFileAppender;
	}

	/**
	 * Returns true, if LogcatAppender should be used
	 * @return True, if LogcatAppender should be used
	 */
	public boolean isUseLogCatAppender() {
		return useLogCatAppender;
	}

	/**
	 * If set to true, LogCatAppender will be used for logging
	 * @param useLogCatAppender If true, LogCatAppender will be used for logging
	 */
	public void setUseLogCatAppender(final boolean useLogCatAppender) {
		this.useLogCatAppender = useLogCatAppender;
	}
	
	public void setEncrypt(final boolean encrypt) {
		this.encrypt = encrypt;
	}

	public void setResetConfiguration(boolean resetConfiguration) {
		this.resetConfiguration = resetConfiguration;
	}

	/**
	 * Resets the log4j configuration before applying this configuration. Default is true.
	 * @return True, if the log4j configuration should be reset before applying this configuration. 
	 */
	public boolean isResetConfiguration() {
		return resetConfiguration;
	}

	public void setInternalDebugging(boolean internalDebugging) {
		this.internalDebugging = internalDebugging;
	}

	public boolean isInternalDebugging() {
		return internalDebugging;
	}


}