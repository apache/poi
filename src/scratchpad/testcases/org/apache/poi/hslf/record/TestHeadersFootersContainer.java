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
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Tests that {@link HeadersFootersContainer} works properly
 *
 * @author Yegor Kozlov
 */
public final class TestHeadersFootersContainer extends TestCase {
	// SlideHeadersFootersContainer
	private byte[] slideData = new byte[] {
            0x3F, 0x00, (byte)0xD9, 0x0F, 0x2E, 0x00, 0x00, 0x00,
            0x00, 0x00, (byte)0xDA, 0x0F, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x23, 0x00,
            0x20, 0x00, (byte)0xBA, 0x0F, 0x1A, 0x00, 0x00, 0x00,
            0x4D, 0x00, 0x79, 0x00, 0x20, 0x00, 0x46, 0x00, 0x6F, 0x00, 0x6F, 0x00, 0x74,
            0x00, 0x65, 0x00, 0x72, 0x00, 0x20, 0x00, 0x2D, 0x00, 0x20, 0x00, 0x31, 0x00

    };

    // NotesHeadersFootersContainer
    private byte[] notesData = new byte[] {
            0x4F, 0x00, (byte)0xD9, 0x0F, 0x48, 0x00, 0x00, 0x00,
            0x00, 0x00, (byte)0xDA, 0x0F, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3D, 0x00,
            0x10, 0x00, (byte)0xBA, 0x0F, 0x16, 0x00, 0x00, 0x00,
            0x4E, 0x00, 0x6F, 0x00, 0x74, 0x00, 0x65, 0x00, 0x20, 0x00, 0x48, 0x00,
            0x65, 0x00, 0x61, 0x00, 0x64, 0x00, 0x65, 0x00, 0x72, 0x00,
            0x20, 0x00, (byte)0xBA, 0x0F, 0x16, 0x00, 0x00, 0x00,
            0x4E, 0x00, 0x6F, 0x00, 0x74, 0x00, 0x65, 0x00, 0x20, 0x00, 0x46, 0x00,
            0x6F, 0x00, 0x6F, 0x00, 0x74, 0x00, 0x65, 0x00, 0x72, 0x00
    };

    public void testReadSlideHeadersFootersContainer() {
		HeadersFootersContainer record = new HeadersFootersContainer(slideData, 0, slideData.length);
		assertEquals(RecordTypes.HeadersFooters.typeID, record.getRecordType());
        assertEquals(HeadersFootersContainer.SlideHeadersFootersContainer, record.getOptions());
        assertEquals(2, record.getChildRecords().length);

        HeadersFootersAtom hdd = record.getHeadersFootersAtom();
        assertNotNull(hdd);

        CString csFooter = record.getFooterAtom();
        assertNotNull(csFooter);
        assertEquals(HeadersFootersContainer.FOOTERATOM, csFooter.getOptions() >> 4);

        assertEquals("My Footer - 1", csFooter.getText());

        assertNull(record.getUserDateAtom());
        assertNull(record.getHeaderAtom());
    }

	public void testWriteSlideHeadersFootersContainer() throws Exception {
		HeadersFootersContainer record = new HeadersFootersContainer(slideData, 0, slideData.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		record.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertTrue(Arrays.equals(slideData, b));
	}

    public void testNewSlideHeadersFootersContainer() throws Exception {
        HeadersFootersContainer record = new HeadersFootersContainer(HeadersFootersContainer.SlideHeadersFootersContainer);

        assertNotNull(record.getHeadersFootersAtom());
        assertNull(record.getUserDateAtom());
        assertNull(record.getHeaderAtom());
        assertNull(record.getFooterAtom());

        HeadersFootersAtom hd = record.getHeadersFootersAtom();
        hd.setFlag(HeadersFootersAtom.fHasDate, true);
        hd.setFlag(HeadersFootersAtom.fHasTodayDate, true);
        hd.setFlag(HeadersFootersAtom.fHasFooter, true);

        CString csFooter = record.addFooterAtom();
        assertNotNull(csFooter);
        assertEquals(HeadersFootersContainer.FOOTERATOM, csFooter.getOptions() >> 4);
        csFooter.setText("My Footer - 1");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        record.writeOut(baos);
        byte[] b = baos.toByteArray();

        assertTrue(Arrays.equals(slideData, b));
    }

    public void testReadNotesHeadersFootersContainer() {
		HeadersFootersContainer record = new HeadersFootersContainer(notesData, 0, notesData.length);
		assertEquals(RecordTypes.HeadersFooters.typeID, record.getRecordType());
        assertEquals(HeadersFootersContainer.NotesHeadersFootersContainer, record.getOptions());
        assertEquals(3, record.getChildRecords().length);

        HeadersFootersAtom hdd = record.getHeadersFootersAtom();
        assertNotNull(hdd);

        CString csHeader = record.getHeaderAtom();
        assertNotNull(csHeader);
        assertEquals(HeadersFootersContainer.HEADERATOM, csHeader.getOptions() >> 4);
        assertEquals("Note Header", csHeader.getText());

        CString csFooter = record.getFooterAtom();
        assertNotNull(csFooter);
        assertEquals(HeadersFootersContainer.FOOTERATOM, csFooter.getOptions() >> 4);
        assertEquals("Note Footer", csFooter.getText());
    }

	public void testWriteNotesHeadersFootersContainer() throws Exception {
		HeadersFootersContainer record = new HeadersFootersContainer(notesData, 0, notesData.length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
		record.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertTrue(Arrays.equals(notesData, b));
	}

    public void testNewNotesHeadersFootersContainer() throws Exception {
        HeadersFootersContainer record = new HeadersFootersContainer(HeadersFootersContainer.NotesHeadersFootersContainer);

        assertNotNull(record.getHeadersFootersAtom());
        assertNull(record.getUserDateAtom());
        assertNull(record.getHeaderAtom());
        assertNull(record.getFooterAtom());

        HeadersFootersAtom hd = record.getHeadersFootersAtom();
        hd.setFlag(HeadersFootersAtom.fHasDate, true);
        hd.setFlag(HeadersFootersAtom.fHasTodayDate, false);
        hd.setFlag(HeadersFootersAtom.fHasUserDate, true);
        hd.setFlag(HeadersFootersAtom.fHasSlideNumber, true);
        hd.setFlag(HeadersFootersAtom.fHasHeader, true);
        hd.setFlag(HeadersFootersAtom.fHasFooter, true);

        CString csHeader = record.addHeaderAtom();
        assertNotNull(csHeader);
        assertEquals(HeadersFootersContainer.HEADERATOM, csHeader.getOptions() >> 4);
        csHeader.setText("Note Header");

        CString csFooter = record.addFooterAtom();
        assertNotNull(csFooter);
        assertEquals(HeadersFootersContainer.FOOTERATOM, csFooter.getOptions() >> 4);
        csFooter.setText("Note Footer");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        record.writeOut(baos);
        byte[] b = baos.toByteArray();

        assertTrue(Arrays.equals(notesData, b));
    }

}
