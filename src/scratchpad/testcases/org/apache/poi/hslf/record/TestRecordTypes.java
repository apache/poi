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


import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests that RecordTypes returns the right records and classes when asked
 */
public final class TestRecordTypes {
    @Test
	public void testPPTNameLookups() {
		assertEquals("MainMaster", RecordTypes.MainMaster.name());
		assertEquals("TextBytesAtom", RecordTypes.TextBytesAtom.name());
		assertEquals("VBAInfo", RecordTypes.VBAInfo.name());
	}

    @Test
	public void testEscherNameLookups() {
		assertEquals("EscherDggContainer", RecordTypes.EscherDggContainer.name());
		assertEquals("EscherClientTextbox", RecordTypes.EscherClientTextbox.name());
		assertEquals("EscherSelection", RecordTypes.EscherSelection.name());
	}

    @Test
	public void testPPTClassLookups() {
		// If this record is ever implemented, change to one that isn't!
		// This is checking the "unhandled default" stuff works
		assertEquals(RecordTypes.UnknownRecordPlaceholder, RecordTypes.forTypeID(-10));
	}

    @Test
    public void testEscherClassLookups() {
		// Should all come back with null, as DDF handles them
		assertEquals(null, RecordTypes.EscherDggContainer.recordConstructor);
		assertEquals(null, RecordTypes.EscherBStoreContainer.recordConstructor);
	}
}
