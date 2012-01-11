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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * List of Progress Listener can be used to propagate events to all the registered list of progress listeners
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 * @param <T> the serializable type for tasks
 * @param <W> the warning type
 * 
 * @see DefaultProgress
 */
public final class ProgressList<T extends Serializable,W> implements Progress<T, W>{
	
	private final List<Progress<T,W>> listeners=Collections.synchronizedList(new LinkedList<Progress<T,W>>());

    /**
     * 
     * @param listener the listener to add
     * @return as specified {@link Collection#add(Object)}
     */
    public boolean addListener(Progress<T, W> listener){
    	return listeners.add(listener);
    }

	public void setTask(T task) {
		for (Progress<T, W> p: listeners){
			p.setTask(task);
		}
	}

	public void setStarted() {
		for (Progress<T, W> p: listeners){
			p.setStarted();
		}
	}

	public void setProgress(float percent) {
		for (Progress<T, W> p: listeners){
			p.setProgress(percent);
		}
	}

	public void setCompleted() {
		for (Progress<T, W> p: listeners){
			p.setCompleted();
		}
	}

	public void dispose() {
		for (Progress<T, W> p: listeners){
			p.dispose();
		}
	}

//	public boolean isCanceled() {
//		boolean res=false;
//		for (Progress<T, W> p: listeners){
//			res=res||p.isCanceled();
//		}
//		return res;
//	}

	public void cancel() {
		for (Progress<T, W> p: listeners){
			p.cancel();
		}
	}

	public void warningOccurred(String source, String location,
			String warning) {
		for (Progress<T, W> p: listeners){
			p.warningOccurred(source, location, warning);
		}
		
	}

	public void exceptionOccurred(Throwable exception) {
		for (Progress<T, W> p: listeners){
			p.exceptionOccurred(exception);
		}
	}
	
}