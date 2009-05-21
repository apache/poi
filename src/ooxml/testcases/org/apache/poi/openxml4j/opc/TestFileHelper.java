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
public final class TestFileHelper extends TestCase {

	/**
	 * TODO - use simple JDK methods on {@link File} instead:<br/>
	 * {@link File#getParentFile()} instead of {@link FileHelper#getDirectory(File)
	 * {@link File#getName()} instead of {@link FileHelper#getFilename(File)
	 */
	public void testGetDirectory() {
		TreeMap<String, String> expectedValue = new TreeMap<String, String>();
		expectedValue.put("/dir1/test.doc", "/dir1");
		expectedValue.put("/dir1/dir2/test.doc.xml", "/dir1/dir2");

		for (String filename : expectedValue.keySet()) {
			File f1 = new File(expectedValue.get(filename));
			File f2 = FileHelper.getDirectory(new File(filename));

			if (false) {
				// YK: The original version asserted expected values against File#getAbsolutePath():
				assertTrue(expectedValue.get(filename).equalsIgnoreCase(f2.getAbsolutePath()));
				// This comparison is platform dependent. A better approach is below
			}
			assertTrue(f1.equals(f2));
		}
	}
}
