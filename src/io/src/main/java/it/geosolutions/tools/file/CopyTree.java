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
package it.geosolutions.tools.file;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy a Tree recursively and asynchronously
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class CopyTree extends DirectoryWalker<Future<File>> {
	private final static Logger LOGGER = LoggerFactory
			.getLogger(CopyTree.class);

	private final File sourceDir;
	private final File destDir;
	private volatile boolean cancelled = false;

	private final CompletionService<File> cs;

	public CopyTree(FileFilter filter, final CompletionService<File> cs,
			File sourceDir, File destDir) {
		super(filter, -1);
		if (sourceDir == null || destDir == null || cs == null) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Invalid null argument");
			}
			throw new IllegalArgumentException("Invalid null argument");
		}

		this.sourceDir = sourceDir;
		this.destDir = destDir;
		this.cs = cs;

	}

	/**
	 * @param filter
	 *            the filter to apply, null means visit all files
	 * @param deep
	 *            controls how deep the hierarchy is navigated to (less than 0
	 *            means unlimited)
	 */
	public CopyTree(FileFilter filter, final CompletionService<File> cs,
			int deep, File sourceDir, File destDir) {
		super(filter, deep);
		if (sourceDir == null || destDir == null || cs == null) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Invalid null argument");
			}
			throw new IllegalArgumentException("Invalid null argument");
		}
		this.sourceDir = sourceDir;
		this.destDir = destDir;
		this.cs = cs;
	}

	 /**
	 * @return the cancelled
	 */
	 public final boolean isCancelled() {
	 return cancelled;
	 }

	/**
	 * @param cancelled
	 *            the cancelled to set
	 */
	public final void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	/**
	 * 
	 * Copy the entire tree recursively (depending from the passed filter)
	 * can be interrupted using setCancelled(true)
	 * @param root
	 * @return list of file (can be null if interrupted)
	 * @throws IOException
	 */
	public Collection<Future<File>> copy() {

		final Collection<Future<File>> res = new ArrayList<Future<File>>();
		try {
			super.walk(sourceDir, res);

			return res;
		} catch (CancelException ioe) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info(ioe.getLocalizedMessage());
			}
		} catch (IOException ioe) {
			// if an I/O Error occurs
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(ioe.getLocalizedMessage(),ioe);
			}
		}
		return null;
	}

	@Override
	protected boolean handleIsCancelled(File file, int depth,
			Collection<Future<File>> results) throws IOException {
		return cancelled;
	}

	@Override
	protected void handleCancelled(File startDirectory, Collection<Future<File>> results,
			CancelException cancel) throws IOException {
		// cancell all pending calls
		final Iterator<Future<File>> it=results.iterator();
		while (it.hasNext()){
			Future<File> future=it.next();
			// cancel the call
			future.cancel(true);
		}
		// remove partially copied files
		FileUtils.deleteQuietly(destDir);
		
		// re-throw exception
		throw cancel;
	}

	@Override
	protected boolean handleDirectory(File directory, int depth,
			Collection<Future<File>> results) throws IOException {
		return true; // process ALL directory
	}

	@Override
	protected File[] filterDirectoryContents(File directory, int depth,
			File[] files) throws IOException {
		return files;
	}

	@Override
	protected void handleFile(File file, int depth,
			Collection<Future<File>> results) throws IOException {
		if (!cancelled){
			results.add(asyncCopyTree(cs, file, sourceDir, destDir));
		}
	}

	/**
	 * 
	 * @param cs
	 *            CompletionService
	 * @param source
	 *            file to copy
	 * @param sourceDir
	 *            where source is mounted
	 * @param destinationDir
	 *            mount point where to copy source
	 * @return
	 * @throws RejectedExecutionException
	 *             - if this task cannot be accepted for execution.
	 * @throws IllegalArgumentException
	 *             - if executor is null or terminated.
	 */
	public static Future<File> asyncCopyTree(
			final CompletionService<File> cs, final File source,
			final File sourceDir, final File destinationDir)
			throws RejectedExecutionException, IllegalArgumentException {

		final Callable<File> call = new Callable<File>() {
			public File call() throws Exception {
				// FilenameUtils.
				File destFile = Path.rebaseFile(sourceDir, destinationDir, source);
				// try to build the directory tree
				destFile.getParentFile().mkdirs();
				// start copy
				FileUtils.copyFile(source, destFile);
				// return the rebased and copied file
				return destFile;
			}
		};
		return cs.submit(call);
	}

}
