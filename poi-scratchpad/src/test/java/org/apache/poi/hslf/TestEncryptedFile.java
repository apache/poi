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

package org.apache.poi.hslf;


import junit.framework.TestCase;

import org.apache.poi.hslf.exceptions.EncryptedPowerPointFileException;
import org.apache.poi.POIDataSamples;

/**
 * Tests that HSLFSlideShow does the right thing with an encrypted file
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestEncryptedFile extends TestCase {
    private static POIDataSamples slTests = POIDataSamples.getSlideShowInstance();

	public void testLoadNonEncrypted() throws Exception {
		HSLFSlideShow hss = new HSLFSlideShow(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));

		assertNotNull(hss);
	}

	public void testLoadEncrypted() throws Exception {
		try {
            new HSLFSlideShow(slTests.openResourceAsStream("Password_Protected-hello.ppt"));
			fail();
		} catch(EncryptedPowerPointFileException e) {
			// Good
		}

		try {
            new HSLFSlideShow(slTests.openResourceAsStream("Password_Protected-np-hello.ppt"));
			fail();
		} catch(EncryptedPowerPointFileException e) {
			// Good
		}

		try {
            new HSLFSlideShow(slTests.openResourceAsStream("Password_Protected-56-hello.ppt"));
			fail();
		} catch(EncryptedPowerPointFileException e) {
			// Good
		}
	}
}
