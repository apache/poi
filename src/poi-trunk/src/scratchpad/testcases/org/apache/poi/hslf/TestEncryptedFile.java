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


import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.exceptions.EncryptedPowerPointFileException;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.junit.Test;

/**
 * Tests that HSLFSlideShow does the right thing with an encrypted file
 */
public final class TestEncryptedFile {
    private static final POIDataSamples slTests = POIDataSamples.getSlideShowInstance();

    @Test
	public void testLoadNonEncrypted() throws IOException {
        InputStream is = slTests.openResourceAsStream("basic_test_ppt_file.ppt");
		HSLFSlideShowImpl hss = new HSLFSlideShowImpl(is);
		assertNotNull(hss);
		hss.close();
		is.close();
	}

    @Test(expected=EncryptedPowerPointFileException.class)
	public void testLoadEncrypted1() throws IOException {
        InputStream is = slTests.openResourceAsStream("Password_Protected-hello.ppt");
		try {
            new HSLFSlideShowImpl(is).close();
		} finally {
		    is.close();
		}
    }
    
    @Test(expected=EncryptedPowerPointFileException.class)
    public void testLoadEncrypted2() throws IOException {
        InputStream is = slTests.openResourceAsStream("Password_Protected-np-hello.ppt");
		try {
            new HSLFSlideShowImpl(is).close();
		} finally {
		    is.close();
		}
    }
    
    @Test(expected=EncryptedPowerPointFileException.class)
    public void testLoadEncrypted3() throws IOException {
        InputStream is = slTests.openResourceAsStream("Password_Protected-56-hello.ppt");
		try {
            new HSLFSlideShowImpl(is).close();
		} finally {
		    is.close();
		}
	}
}
