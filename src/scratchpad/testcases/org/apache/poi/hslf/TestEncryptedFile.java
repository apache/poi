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
import org.apache.poi.hslf.record.*;

/**
 * Tests that HSLFSlideShow does the right thing with an encrypted file
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestEncryptedFile extends TestCase {
	// A non encrypted file
	private String ss_ne;
	// An encrypted file, with encrypted properties
	private String ss_e;
	// An encrypted file, without encrypted properties
	private String ss_np_e;
	// An encrypted file, with a 56 bit key
	private String ss_56_e;


    public TestEncryptedFile() throws Exception {
		String dirname = System.getProperty("HSLF.testdata.path");

		ss_ne = dirname + "/basic_test_ppt_file.ppt";
		ss_e = dirname + "/Password_Protected-hello.ppt";
		ss_np_e = dirname + "/Password_Protected-np-hello.ppt";
		ss_56_e = dirname + "/Password_Protected-56-hello.ppt";
    }

    public void testLoadNonEncrypted() throws Exception {
    	HSLFSlideShow hss = new HSLFSlideShow(ss_ne);

    	assertNotNull(hss);
    }

    public void testLoadEncrypted() throws Exception {
    	try {
    		new HSLFSlideShow(ss_e);
    		fail();
    	} catch(EncryptedPowerPointFileException e) {
    		// Good
    	}

    	try {
    		new HSLFSlideShow(ss_np_e);
    		fail();
    	} catch(EncryptedPowerPointFileException e) {
    		// Good
    	}

    	try {
    		new HSLFSlideShow(ss_56_e);
    		fail();
    	} catch(EncryptedPowerPointFileException e) {
    		// Good
    	}
    }
}
