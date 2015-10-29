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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Base class for integration-style tests which iterate over all test-files
 * and execute the same action to find out if any change breaks these applications.
 * 
 * This test uses {@link Parameterized} to run the test for each file separatedely.
 */
@RunWith(Parameterized.class)
public abstract class BaseXLSIteratingTest {
    protected static final OutputStream NULL_OUTPUT_STREAM = new NullOutputStream();

	protected static final List<String> EXCLUDED = new ArrayList<String>();

    @Parameters(name="{index}: {0}")
    public static Iterable<Object[]> files() {
        String dataDirName = System.getProperty(POIDataSamples.TEST_PROPERTY);
        if(dataDirName == null) {
            dataDirName = "test-data";
        }

        List<Object[]> files = new ArrayList<Object[]>();
        findFile(files, dataDirName + "/spreadsheet");
        findFile(files, dataDirName + "/hpsf");
        
        return files;
    }
	
    private static void findFile(List<Object[]> list, String dir) {
        String[] files = new File(dir).list(new FilenameFilter() {
            @Override
            public boolean accept(File arg0, String arg1) {
                return arg1.toLowerCase(Locale.ROOT).endsWith(".xls");
            }
        });
        
        assertNotNull("Did not find any xls files in directory " + dir, files);
        
        for(String file : files) {
            list.add(new Object[] { new File(dir, file) });
        }
    }
    
    @Parameter
    public File file;
    
	@Test
	public void testMain() throws Exception {
	    // we had intermittent problems when this was set differently somehow, let's try to set it here so it always is set correctly for these tests
	    Biff8EncryptionKey.setCurrentUserPassword(null);
	    
		try {
			runOneFile(file);
		} catch (Exception e) {
		    Assume.assumeFalse("File " + file + " is excluded currently",
		            EXCLUDED.contains(file.getName()));

			System.out.println("Failed: " + file);
			e.printStackTrace();
			
			// try to read it in HSSFWorkbook to quickly fail if we cannot read the file there at all and thus probably should use EXCLUDED instead
			FileInputStream stream = new FileInputStream(file);
			try {
				assertNotNull(new HSSFWorkbook(stream));
			} finally {
				stream.close();
			}
		}
	}

	abstract void runOneFile(File file) throws Exception;

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
