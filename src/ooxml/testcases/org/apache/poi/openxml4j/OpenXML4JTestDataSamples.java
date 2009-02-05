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

package org.apache.poi.openxml4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Centralises logic for finding/opening sample files for ooxml4j unit tests 
 * 
 * @author jmicich
 */
public final class OpenXML4JTestDataSamples {

	private static final String IN_DIR_PROP_NAME = "openxml4j.testdata.input";
	private static final String COMP_IN_DIR_PROP_NAME = "openxml4j.compliance.input";
	
	private static File _sampleInputDir;
	private static File _sampleOutputDir;
	private static File _complianceSampleInputDir;

	private OpenXML4JTestDataSamples() {
		// no instances of this class
	}
	
	public static InputStream openSampleStream(String sampleFileName) {
		File f = getSampleFile(sampleFileName);
		try {
			return new FileInputStream(f);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	public static String getSampleFileName(String sampleFileName) {
		// TODO - investigate allowing read/write access for package opened on stream
		return getSampleFile(sampleFileName).getAbsolutePath();
	}
	
	public static File getSampleFile(String sampleFileName) {
		File dir = getSampleInputDir();
		File f = new File(dir, sampleFileName);
		if (!f.exists()) {
			throw new RuntimeException("Specified sample file '" 
					+ f.getAbsolutePath() + "' does not exist");
		}
		if (f.isDirectory()) {
			throw new RuntimeException("Specified sample file '" 
					+ f.getAbsolutePath() + "' is a directory");
		}
		return f;
	}
	
	public static File getOutputFile(String outputFileName) {
		File dir = getSampleOutputDir();
		return new File(dir, outputFileName);
	}


	public static InputStream openComplianceSampleStream(String sampleFileName) {
		File f = getComplianceSampleFile(sampleFileName);
		try {
			return new FileInputStream(f);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	private static File getComplianceSampleFile(String sampleFileName) {
		File dir = getComplianceSampleInputDir();
		File f = new File(dir, sampleFileName);
		if (!f.exists()) {
			throw new RuntimeException("Specified sample file '" 
					+ f.getAbsolutePath() + "' does not exist");
		}
		if (f.isDirectory()) {
			throw new RuntimeException("Specified sample file '" 
					+ f.getAbsolutePath() + "' is a directory");
		}
		return f;
	}
	public static String getComplianceSampleFileName(String sampleFileName) {
		return getComplianceSampleFile(sampleFileName).getAbsolutePath();
	}
	private static File getComplianceSampleInputDir() {
		if (_complianceSampleInputDir == null) {
			_complianceSampleInputDir = getAndCheckDirByProperty(COMP_IN_DIR_PROP_NAME);
		}
		return _complianceSampleInputDir;
	}

	
	private static File getSampleInputDir() {
		if (_sampleInputDir == null) {
			_sampleInputDir = getAndCheckDirByProperty(IN_DIR_PROP_NAME);
		}
		return _sampleInputDir;
	}

	private static File getAndCheckDirByProperty(String propName) {
		String dirName = System.getProperty(propName);
		File dir = new File(dirName);
		if (!dir.exists()) {
			throw new RuntimeException("Specified '" + propName + "' directory: '"
					+ dirName + "' does not exist");
		}
		if (!dir.isDirectory()) {
			throw new RuntimeException("Specified '" + propName + "' directory: '"
					+ dirName + "' is a not a proper directory");
		}
		return dir;
	}

	private static File getSampleOutputDir() {
		if (_sampleOutputDir == null) {
            File dir = new File(System.getProperty("java.io.tmpdir"), "poifiles");
			if (dir.exists()) {
    			if (!dir.isDirectory()) {
    				throw new RuntimeException("Specified output directory: '"
    						+ dir.getAbsolutePath() + "' is a not a proper directory");
    			}
			} else {
				if (!dir.mkdirs()) {
					throw new RuntimeException("Failed to create directory: '"
							+ dir.getAbsolutePath() + "'");
				}
			}
			_sampleOutputDir = dir;
		}
		return _sampleOutputDir;
	}

}
