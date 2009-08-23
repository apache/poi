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
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Centralises logic for finding/opening sample files in the src/testcases/org/apache/poi/hssf/hssf/data folder.
 *
 * @author Josh Micich
 */
public final class HSSFTestDataSamples {

	private static final POIDataSamples _inst = POIDataSamples.getSpreadSheetInstance();

	public static InputStream openSampleFileStream(String sampleFileName) {
		return _inst.openResourceAsStream(sampleFileName);
	}
	public static byte[] getTestDataFileContent(String fileName) {
		return _inst.readFile(fileName);
	}

	public static HSSFWorkbook openSampleWorkbook(String sampleFileName) {
		try {
			return new HSSFWorkbook(_inst.openResourceAsStream(sampleFileName));
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
}
