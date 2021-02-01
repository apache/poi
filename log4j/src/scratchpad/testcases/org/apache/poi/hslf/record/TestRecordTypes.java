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


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.ddf.EscherRecordTypes;
import org.junit.jupiter.api.Test;

/**
 * Tests that RecordTypes returns the right records and classes when asked
 */
public final class TestRecordTypes {
    @Test
	void testPPTNameLookups() {
		assertEquals("MainMaster", RecordTypes.MainMaster.name());
		assertEquals("TextBytesAtom", RecordTypes.TextBytesAtom.name());
		assertEquals("VBAInfo", RecordTypes.VBAInfo.name());
	}

    @Test
	void testEscherNameLookups() {
		assertEquals("DGG_CONTAINER", EscherRecordTypes.DGG_CONTAINER.name());
		assertEquals("CLIENT_TEXTBOX", EscherRecordTypes.CLIENT_TEXTBOX.name());
		assertEquals("SELECTION", EscherRecordTypes.SELECTION.name());
	}

    @Test
	void testPPTClassLookups() {
		// If this record is ever implemented, change to one that isn't!
		// This is checking the "unhandled default" stuff works
		assertEquals(RecordTypes.UnknownRecordPlaceholder, RecordTypes.forTypeID(-10));
	}
}
