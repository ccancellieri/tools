package it.geosolutions.tools.file.writer;

import it.geosolutions.tools.file.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public final class Writer {

	/**
	 * Open 'destination' file in append mode and append content of the
	 * 'toAppend' file
	 * 
	 * @param toAppend
	 * @param destination
	 * @throws IOException
	 */
	public static void appendFile(File toAppend, File destination)
			throws IOException {
		FileWriter fw = null;
		BufferedWriter bw = null;
		LineIterator it = null;
		try {
			fw = new FileWriter(destination, true);
			bw = new BufferedWriter(fw);
			it = FileUtils.lineIterator(toAppend);
			while (it.hasNext()) {
				bw.append(it.nextLine());
				bw.newLine();
			}
			bw.flush();
		} finally {
			if (it != null) {
				it.close();
			}
			if (bw !=null){
				IOUtils.closeQuietly(bw);
			}
			if (fw != null) {
				IOUtils.closeQuietly(fw);
			}
		}
	}
}
