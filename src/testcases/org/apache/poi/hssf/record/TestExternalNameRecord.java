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

package org.apache.poi.hssf.record;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
/**
 * 
 * @author Josh Micich
 */
public final class TestExternalNameRecord extends TestCase {

	private static final byte[] dataFDS = {
		0, 0, 0, 0, 0, 0, 3, 0, 70, 68, 83, 0, 0,
	};
	
	// data taken from bugzilla 44774 att 21790
	private static final byte[] dataAutoDocName = {
		-22, 127, 0, 0, 0, 0, 29, 0, 39, 49, 57, 49, 50, 49, 57, 65, 87, 52, 32, 67, 111, 114,
			112, 44, 91, 87, 79, 82, 75, 79, 85, 84, 95, 80, 88, 93, 39,
	};
	
	// data taken from bugzilla 44774 att 21790
	private static final byte[] dataPlainName = {
		0, 0, 0, 0, 0, 0, 9, 0, 82, 97, 116, 101, 95, 68, 97, 116, 101, 9, 0, 58, 0, 0, 0, 0, 4, 0, 8, 0
		// TODO - the last 2 bytes of formula data (8,0) seem weird.  They encode to ConcatPtg, UnknownPtg
		// UnknownPtg is otherwise not created by any other test cases
	};
	
	private static ExternalNameRecord createSimpleENR(byte[] data) {
		return new ExternalNameRecord(TestcaseRecordInputStream.create(0x0023, data));
	}
	public void testBasicDeserializeReserialize() {
		
		ExternalNameRecord enr = createSimpleENR(dataFDS);
		assertEquals("FDS", enr.getText());
	 
		try {
			TestcaseRecordInputStream.confirmRecordEncoding(0x0023, dataFDS, enr.serialize());
		} catch (ArrayIndexOutOfBoundsException e) {
			if(e.getMessage().equals("15")) {
				throw new AssertionFailedError("Identified bug 44695");
			}
		}
	}

	public void testBasicSize() {
		ExternalNameRecord enr = createSimpleENR(dataFDS);
		if(enr.getRecordSize() == 13) {
			throw new AssertionFailedError("Identified bug 44695");
		}
		assertEquals(17, enr.getRecordSize());
	}
	
	public void testAutoStdDocName() {

		ExternalNameRecord enr;
		try {
			enr = createSimpleENR(dataAutoDocName);
		} catch (ArrayIndexOutOfBoundsException e) {
			if(e.getMessage() == null) {
				throw new AssertionFailedError("Identified bug XXXX");
			}
			throw e;
		}
		assertEquals("'191219AW4 Corp,[WORKOUT_PX]'", enr.getText());
		assertTrue(enr.isAutomaticLink());
		assertFalse(enr.isBuiltInName());
		assertFalse(enr.isIconifiedPictureLink());
		assertFalse(enr.isOLELink());
		assertFalse(enr.isPicureLink());
		assertTrue(enr.isStdDocumentNameIdentifier());

		TestcaseRecordInputStream.confirmRecordEncoding(0x0023, dataAutoDocName, enr.serialize());
	}

	public void testPlainName() {

		ExternalNameRecord enr = createSimpleENR(dataPlainName);
		assertEquals("Rate_Date", enr.getText());
		assertFalse(enr.isAutomaticLink());
		assertFalse(enr.isBuiltInName());
		assertFalse(enr.isIconifiedPictureLink());
		assertFalse(enr.isOLELink());
		assertFalse(enr.isPicureLink());
		assertFalse(enr.isStdDocumentNameIdentifier());

		TestcaseRecordInputStream.confirmRecordEncoding(0x0023, dataPlainName, enr.serialize());
	}
}
