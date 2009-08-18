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

package org.apache.poi.hslf.record;


import junit.framework.TestCase;

/**
 * Tests that RecordTypes returns the right records and classes when asked
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestRecordTypes extends TestCase {
	public void testPPTNameLookups() {
		assertEquals("MainMaster", RecordTypes.recordName(1016));
		assertEquals("TextBytesAtom", RecordTypes.recordName(4008));
		assertEquals("VBAInfo", RecordTypes.recordName(1023));
	}

	public void testEscherNameLookups() {
		assertEquals("EscherDggContainer", RecordTypes.recordName(0xf000));
		assertEquals("EscherClientTextbox", RecordTypes.recordName(0xf00d));
		assertEquals("EscherSelection", RecordTypes.recordName(0xf119));
	}

	public void testPPTClassLookups() {
		assertEquals(Slide.class, RecordTypes.recordHandlingClass(1006));
		assertEquals(TextCharsAtom.class, RecordTypes.recordHandlingClass(4000));
		assertEquals(TextBytesAtom.class, RecordTypes.recordHandlingClass(4008));
		assertEquals(SlideListWithText.class, RecordTypes.recordHandlingClass(4080));

		// If this record is ever implemented, change to one that isn't!
		// This is checking the "unhandled default" stuff works
		assertEquals(UnknownRecordPlaceholder.class, RecordTypes.recordHandlingClass(2019));
	}

	public void testEscherClassLookups() {
		// Should all come back with null, as DDF handles them
		assertEquals(null, RecordTypes.recordHandlingClass(0xf000));
		assertEquals(null, RecordTypes.recordHandlingClass(0xf001));
	}
}
