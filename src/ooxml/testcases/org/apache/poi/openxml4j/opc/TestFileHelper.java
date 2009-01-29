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

package org.apache.poi.openxml4j.opc;

import java.io.File;
import java.util.TreeMap;

import org.apache.poi.openxml4j.opc.internal.FileHelper;

import junit.framework.TestCase;

/**
 * Test TestFileHelper class.
 * 
 * @author Julien Chable
 */
public class TestFileHelper extends TestCase {

	public void testGetDirectory() {
		TreeMap<String, String> expectedValue = new TreeMap<String, String>();
		expectedValue.put("c:\\test\\test.doc", "c:\\test");
		expectedValue.put("d:\\test\\test2\\test.doc.xml", "d:\\test\\test2");

		for (String filename : expectedValue.keySet()) {
			assertTrue(expectedValue.get(filename).equalsIgnoreCase(
					FileHelper.getDirectory(new File(filename))
							.getAbsolutePath()));
		}
	}
}
