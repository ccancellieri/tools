/*
 * Copyright (C) 2011 - 2012  GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.geosolutions.tools.commons.listener;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the interface {@link Progress}
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public class DefaultProgress implements
		Progress<String, DefaultProgress.Warning> {

	

	private final Logger LOGGER;
	
	private final String name;
	
	private volatile float progress = 0;
	private volatile boolean completed = false;
	private volatile boolean started = false;
	private volatile boolean canceled = false;

	/** List of warnings occurred during the execution. **/
	private final List<Warning> warnings = Collections
			.synchronizedList(new LinkedList<Warning>());

	/** List of exceptions that were caught during executiong. **/
	private final List<Throwable> exceptions = Collections
			.synchronizedList(new LinkedList<Throwable>());

	public void setTask(String task) {
		if (LOGGER.isInfoEnabled())
			LOGGER.info(new StringBuilder(this.getClass().getSimpleName())
					.append(" [completed=").append(completed)
					.append(", progress=").append(progress)
					.append(", started=").append(started).append(", task=")
					.append(task).append("]").toString());
		started = false;
		canceled = false;
		completed = false;
	}
	
	public DefaultProgress(String name) {
		super();
		LOGGER = LoggerFactory.getLogger(name);
		this.name = name;
	}
	
	public DefaultProgress() {
		super();
		this.name = this.getClass().getSimpleName();
		LOGGER = LoggerFactory.getLogger(name);
		
	}

	/**
	 * {@link Progress#setStarted()}
	 */
	public void setStarted() {
		started = true;
		canceled = false;
		completed = false;
	}

	/**
	 * {@link Progress#setProgress(float)}
	 */
	public void setProgress(float percent) {
		if (LOGGER.isInfoEnabled()){
			LOGGER.info(new StringBuilder(name)
					.append(" [old_progress=").append(progress)
					.append(", new_progress=").append(percent).append("]")
					.toString());
		}
		progress = percent;
	}

	/**
	 * {@link Progress#getProgress(float)}
	 */
	public float getProgress() {
		return progress;
	}

	/**
	 * {@link Progress#setCompleted()}
	 */
	public void setCompleted() {
		if (LOGGER.isInfoEnabled()){
			LOGGER.info(new StringBuilder(name)
					.append(" [completed=").append("true").append("]")
					.toString());
		}
		completed = true;
	}

	/**
	 * {@link Progress#dispose()}
	 */
	public void dispose() {
		if (LOGGER.isInfoEnabled()){
			LOGGER.info(new StringBuilder(name)
					.append("[dispose").append("]")
					.toString());
		}
		exceptions.clear();
		warnings.clear();
	}

	/**
	 * {@link Progress#isCanceled()}
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * {@link Progress#cancel()}
	 */
	public void cancel() {
		canceled = true;
	}

	/**
	 * {@link Progress#exceptionOccurred(Throwable)}
	 */
	public void exceptionOccurred(Throwable exception) {
		exceptions.add(exception);
	}

	/**
	 * {@link Progress#warningOccurred(String, String, String)}
	 */
	public void warningOccurred(String source, String location, String warning) {
		final Warning w = new Warning(source, location, warning);
		warnings.add(w);
	}

	/**
	 * 
	 * 
	 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
	 * 
	 */
	public static class Warning implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private final String source;
		private final String location;
		private final String warning;

		public Warning(String source, String location, String warning) {
			super();
			this.source = source;
			this.location = location;
			this.warning = warning;
		}

		@Override
		public String toString() {
			return "Warning [location=" + location + ", source=" + source
					+ ", warning=" + warning + "]";
		}

		public String getSource() {
			return source;
		}

		public String getLocation() {
			return location;
		}

		public String getWarning() {
			return warning;
		}
	}

	/**
	 * {@link Progress#getWarnings()}
	 */
	public List<Warning> getWarnings() {
		return Collections.unmodifiableList(warnings);
	}

	/**
	 * {@link Progress#getExceptions()}
	 */
	public List<Throwable> getExceptions() {
		return Collections.unmodifiableList(exceptions);
	}

}
