/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.hssf.dev;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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

	private int runWithDir(String dir) throws IOException {
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

	private void runWithArrayOfFiles(String[] files, String dir, List<String> failed) throws IOException {
		for(String file : files) {
			try {
				runOneFile(dir, file, failed);
			} catch (Exception e) {
				System.out.println("Failed: " + file);
				if(SILENT_EXCLUDED.contains(file)) {
					continue;
				}

				e.printStackTrace();
				
				// try to read it in HSSFWorkbook to quickly fail if we cannot read the file there at all and thus probably can use SILENT_EXCLUDED instead
				FileInputStream stream = new FileInputStream(new File(dir, file));
				try {
					assertNotNull(new HSSFWorkbook(stream));
				} finally {
					stream.close();
				}
				
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
