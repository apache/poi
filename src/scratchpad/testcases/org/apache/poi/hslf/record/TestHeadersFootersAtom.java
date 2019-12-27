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


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

/**
 * Tests that {@link HeadersFootersAtom} works properly
 */
public final class TestHeadersFootersAtom {
	// From a real file
	private final byte[] data = new byte[] {
            0x00, 0x00, (byte)0xDA, 0x0F, 0x04, 0x00, 0x00, 0,
            0x00, 0x00, 0x23, 0x00 };

    @Test
    public void testRead() {
		HeadersFootersAtom record = new HeadersFootersAtom(data, 0, data.length);
		assertEquals(RecordTypes.HeadersFootersAtom.typeID, record.getRecordType());

        assertEquals(0, record.getFormatId());
        assertEquals(0x23, record.getMask());

        assertTrue(record.getFlag(HeadersFootersAtom.fHasDate));
        assertTrue(record.getFlag(HeadersFootersAtom.fHasTodayDate));
        assertFalse(record.getFlag(HeadersFootersAtom.fHasUserDate));
        assertFalse(record.getFlag(HeadersFootersAtom.fHasSlideNumber));
        assertFalse(record.getFlag(HeadersFootersAtom.fHasHeader));
        assertTrue(record.getFlag(HeadersFootersAtom.fHasFooter));
    }

    @Test
	public void testWrite() throws Exception {
		HeadersFootersAtom record = new HeadersFootersAtom(data, 0, data.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		record.writeOut(baos);
		assertArrayEquals(data, baos.toByteArray());
	}

    @Test
    public void testNewRecord() throws Exception {
        HeadersFootersAtom record = new HeadersFootersAtom();
        record.setFlag(HeadersFootersAtom.fHasDate, true);
        record.setFlag(HeadersFootersAtom.fHasTodayDate, true);
        record.setFlag(HeadersFootersAtom.fHasFooter, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        record.writeOut(baos);
        assertArrayEquals(data, baos.toByteArray());
    }

    @Test
    public void testFlags() {
        HeadersFootersAtom record = new HeadersFootersAtom();

        //in a new record all the bits are 0
        for(int i = 0; i < 6; i++) assertFalse(record.getFlag(1 << i));

        record.setFlag(HeadersFootersAtom.fHasTodayDate, true);
        assertTrue(record.getFlag(HeadersFootersAtom.fHasTodayDate));

        record.setFlag(HeadersFootersAtom.fHasTodayDate, true);
        assertTrue(record.getFlag(HeadersFootersAtom.fHasTodayDate));

        record.setFlag(HeadersFootersAtom.fHasTodayDate, false);
        assertFalse(record.getFlag(HeadersFootersAtom.fHasTodayDate));

        record.setFlag(HeadersFootersAtom.fHasTodayDate, false);
        assertFalse(record.getFlag(HeadersFootersAtom.fHasTodayDate));
    }
}
