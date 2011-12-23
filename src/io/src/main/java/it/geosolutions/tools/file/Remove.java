package it.geosolutions.tools.file;

import it.geosolutions.tools.check.Objects;

import java.io.File;
import java.io.FilenameFilter;

public abstract class Remove {

	/**
	 * Delete all the files/dirs with matching the specified
	 * {@link FilenameFilter} in the specified directory. The method can work
	 * recursively.
	 * 
	 * @param sourceDirectory
	 *            the directory to delete files from.
	 * @param filter
	 *            the {@link FilenameFilter} to use for selecting files to
	 *            delete.
	 * @param recursive
	 *            boolean that specifies if we want to delete files recursively
	 *            or not.
	 * @return
	 */
	public static boolean deleteDirectory(File sourceDirectory,
			FilenameFilter filter, boolean recursive, boolean deleteItself) {
		Objects.notNull(sourceDirectory, filter);
		if (!sourceDirectory.exists() || !sourceDirectory.canRead()
				|| !sourceDirectory.isDirectory())
			throw new IllegalStateException("Source is not in a legal state.");
	
		final File[] files = (filter != null ? sourceDirectory
				.listFiles(filter) : sourceDirectory.listFiles());
		for (File file : files) {
			if (file.isDirectory()) {
				if (recursive)
					deleteDirectory(file, filter, recursive, deleteItself);
			} else {
				if (!file.delete())
					return false;
			}
		}
		return deleteItself ? sourceDirectory.delete() : true;
	
	}

	/**
	 * Delete asynchronously the specified File.
	 */
	public static Object deleteFile(File file) {
		Objects.notNull(file);
		if (!file.exists() || !file.canRead() || !file.isFile())
			throw new IllegalStateException("Source is not in a legal state.");
	
		Object obj = new Object();
		FileGarbageCollector.getFileCleaningTracker().track(file, obj);
		return obj;
	}

	/**
	 * Empty the specified directory. The method can work recursively.
	 * 
	 * @param sourceDirectory
	 *            the directory to delete files/dirs from.
	 * @param recursive
	 *            boolean that specifies if we want to delete files/dirs
	 *            recursively or not.
	 * @param deleteItself
	 *            boolean used if we want to delete the sourceDirectory itself
	 * @return
	 */
	public static boolean emptyDirectory(File sourceDirectory,
			boolean recursive, boolean deleteItself) {
		Objects.notNull(sourceDirectory);
		if (!sourceDirectory.exists() || !sourceDirectory.canRead()
				|| !sourceDirectory.isDirectory()) {
			throw new IllegalStateException("Source is not in a legal state.");
		}
	
		final File[] files = sourceDirectory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				if (recursive) {
					if (!emptyDirectory(file, recursive, true)) {// delete
						// subdirs
						// recursively
						return false;
					}
				}
			} else {
				if (!file.delete()) {
					return false;
				}
			}
		}
		return deleteItself ? sourceDirectory.delete() : true;
	}

}
