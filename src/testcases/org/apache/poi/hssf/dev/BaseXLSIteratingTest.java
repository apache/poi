package org.apache.poi.hssf.dev;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Base class for integration-style tests which iterate over all test-files
 * and execute the same action to find out if any change breaks these applications.
 */
public abstract class BaseXLSIteratingTest {
    protected static final OutputStream NULL_OUTPUT_STREAM = new NullOutputStream();

	protected static final List<String> EXCLUDED = new ArrayList<String>();
	protected static final List<String> SILENT_EXCLUDED = new ArrayList<String>();

	@Test
	public void testMain() throws Exception {
		int count = runWithDir("test-data/spreadsheet");
		count += runWithDir("test-data/hpsf");
		
		System.out.println("Had " + count + " files");
	}

	private int runWithDir(String dir) {
		List<String> failed = new ArrayList<String>();

		String[] files = new File(dir).list(new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {
				return arg1.toLowerCase().endsWith(".xls");
			}
		});
		
		runWithArrayOfFiles(files, dir, failed);

		assertTrue("Expected to have no failed except the ones excluded, but had: " + failed, 
				failed.isEmpty());
		
		return files.length;
	}

	private void runWithArrayOfFiles(String[] files, String dir, List<String> failed) {
		for(String file : files) {
			try {
				runOneFile(dir, file, failed);
			} catch (Exception e) {
				System.out.println("Failed: " + file);
				if(SILENT_EXCLUDED.contains(file)) {
					continue;
				}

				e.printStackTrace();
				if(!EXCLUDED.contains(file)) {
					failed.add(file);
				}
			}
		}
	}

	abstract void runOneFile(String dir, String file, List<String> failed) throws Exception;

	/**
	 * Implementation of an OutputStream which does nothing, used
	 * to redirect stdout to avoid spamming the console with output
	 */
	private static class NullOutputStream extends OutputStream {
	    @Override
	    public void write(byte[] b, int off, int len) {
	    }

	    @Override
	    public void write(int b) {
	    }

	    @Override
	    public void write(byte[] b) throws IOException {
	    }
	}
}
