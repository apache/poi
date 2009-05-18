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

package org.apache.poi.hssf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Centralises logic for finding/opening sample files in the src/testcases/org/apache/poi/hssf/hssf/data folder.
 *
 * @author Josh Micich
 */
public final class HSSFTestDataSamples {

	private static final String TEST_DATA_DIR_SYS_PROPERTY_NAME = "HSSF.testdata.path";

	private static boolean _isInitialised;
	private static File _resolvedDataDir;
	/** <code>true</code> if standard system propery is not set,
	 * but the data is available on the test runtime classpath */
	private static boolean _sampleDataIsAvaliableOnClassPath;

	/**
	 * Opens a sample file from the standard HSSF test data directory
	 *
	 * @return an open <tt>InputStream</tt> for the specified sample file
	 */
	public static InputStream openSampleFileStream(String sampleFileName) {

		if(!_isInitialised) {
			try {
				initialise();
			} finally {
				_isInitialised = true;
			}
		}
		if (_sampleDataIsAvaliableOnClassPath) {
			InputStream result = openClasspathResource(sampleFileName);
			if(result == null) {
				throw new RuntimeException("specified test sample file '" + sampleFileName
						+ "' not found on the classpath");
			}
//			System.out.println("opening cp: " + sampleFileName);
			// wrap to avoid temp warning method about auto-closing input stream
			return new NonSeekableInputStream(result);
		}
		if (_resolvedDataDir == null) {
			throw new RuntimeException("Must set system property '"
					+ TEST_DATA_DIR_SYS_PROPERTY_NAME
					+ "' properly before running tests");
		}

		File f = new File(_resolvedDataDir, sampleFileName);
		if (!f.exists()) {
			throw new RuntimeException("Sample file '" + sampleFileName
					+ "' not found in data dir '" + _resolvedDataDir.getAbsolutePath() + "'");
		}
//		System.out.println("opening " + f.getAbsolutePath());
		try {
			return new FileInputStream(f);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static void initialise() {
		String dataDirName = System.getProperty(TEST_DATA_DIR_SYS_PROPERTY_NAME);
		if (dataDirName == null) {
			// check to see if we can just get the resources from the classpath
			InputStream is = openClasspathResource("SampleSS.xls");
			if (is != null) {
				try {
					is.close(); // be nice
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				_sampleDataIsAvaliableOnClassPath = true;
				return;
			}

			throw new RuntimeException("Must set system property '"
					+ TEST_DATA_DIR_SYS_PROPERTY_NAME + "' before running tests");
		}
		File dataDir = new File(dataDirName);
		if (!dataDir.exists()) {
			throw new RuntimeException("Data dir '" + dataDirName
					+ "' specified by system property '" + TEST_DATA_DIR_SYS_PROPERTY_NAME
					+ "' does not exist");
		}
		// convert to canonical file, to make any subsequent error messages
		// clearer.
		try {
			_resolvedDataDir = dataDir.getCanonicalFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Opens a test sample file from the 'data' sub-package of this class's package.
	 * @return <code>null</code> if the sample file is not deployed on the classpath.
	 */
	private static InputStream openClasspathResource(String sampleFileName) {
		return HSSFTestDataSamples.class.getResourceAsStream("data/" + sampleFileName);
	}

	private static final class NonSeekableInputStream extends InputStream {

		private final InputStream _is;

		public NonSeekableInputStream(InputStream is) {
			_is = is;
		}

		public int read() throws IOException {
			return _is.read();
		}
		public int read(byte[] b, int off, int len) throws IOException {
			return _is.read(b, off, len);
		}
		public boolean markSupported() {
			return false;
		}
		public void close() throws IOException {
			_is.close();
		}
	}

	public static HSSFWorkbook openSampleWorkbook(String sampleFileName) {
		try {
			return new HSSFWorkbook(openSampleFileStream(sampleFileName));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Writes a spreadsheet to a <tt>ByteArrayOutputStream</tt> and reads it back
	 * from a <tt>ByteArrayInputStream</tt>.<p/>
	 * Useful for verifying that the serialisation round trip
	 */
	public static HSSFWorkbook writeOutAndReadBack(HSSFWorkbook original) {

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
			original.write(baos);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			return new HSSFWorkbook(bais);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return byte array of sample file content from file found in standard hssf test data dir
	 */
	public static byte[] getTestDataFileContent(String fileName) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			InputStream fis = HSSFTestDataSamples.openSampleFileStream(fileName);

			byte[] buf = new byte[512];
			while (true) {
				int bytesRead = fis.read(buf);
				if (bytesRead < 1) {
					break;
				}
				bos.write(buf, 0, bytesRead);
			}
			fis.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return bos.toByteArray();
	}
}
