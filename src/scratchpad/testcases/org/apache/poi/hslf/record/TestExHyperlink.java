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


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.junit.jupiter.api.Test;

/**
 * Tests that ExHyperlink works properly.
 */
public final class TestExHyperlink {
	@Test
    void testReadWrite() throws IOException {
        // From a real file
        byte[] exHyperlinkBytes = org.apache.poi.poifs.storage.RawDataUtil.decompress(
            "H4sIAAAAAAAAAONnuM6/ggEELvOzAElmMHsXvxuQzGAoAcICBisGfSDMYkhkyAbi"+
            "IqBYIoMeEBcAcTJQVSqQlw8UTweqKgCyMoF0BkMxEKYBWQJUNQ0A/k1x3rAAAAA="
        );
	    ExHyperlink exHyperlink = new ExHyperlink(exHyperlinkBytes, 0, exHyperlinkBytes.length);


	    assertEquals(4055l, exHyperlink.getRecordType());
        assertEquals(3, exHyperlink.getExHyperlinkAtom().getNumber());
        String expURL = "http://jakarta.apache.org/poi/hssf/";
        assertEquals(expURL, exHyperlink.getLinkURL());
        assertEquals(expURL, exHyperlink._getDetailsA());
        assertEquals(expURL, exHyperlink._getDetailsB());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exHyperlink.writeOut(baos);
        assertArrayEquals(exHyperlinkBytes, baos.toByteArray());
	}

	@Test
	void testRealFile() throws IOException {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
		HSLFSlideShowImpl hss = new HSLFSlideShowImpl(slTests.openResourceAsStream("WithLinks.ppt"));
		HSLFSlideShow ss = new HSLFSlideShow(hss);

		// Get the document
		Document doc = ss.getDocumentRecord();
		// Get the ExObjList
		ExObjList exObjList = null;
		for (final Record rec : doc._children) {
			if(rec instanceof ExObjList) {
				exObjList = (ExObjList)rec;
			}
		}

		assertNotNull(exObjList);

		// Within that, grab out the Hyperlink atoms
		List<ExHyperlink> linksA = new ArrayList<>();
		for (Record ch : exObjList._children) {
			if(ch instanceof ExHyperlink) {
				linksA.add((ExHyperlink) ch);
			}
		}

		// Should be 4 of them
		assertEquals(4, linksA.size());
		ExHyperlink[] links = new ExHyperlink[linksA.size()];
		linksA.toArray(links);

		assertEquals(4, exObjList.getExHyperlinks().length);

		// Check the other way

		// Check they have what we expect in them
		assertEquals(1, links[0].getExHyperlinkAtom().getNumber());
		assertEquals("http://jakarta.apache.org/poi/", links[0].getLinkURL());

		assertEquals(2, links[1].getExHyperlinkAtom().getNumber());
		assertEquals("http://slashdot.org/", links[1].getLinkURL());

		assertEquals(3, links[2].getExHyperlinkAtom().getNumber());
		assertEquals("http://jakarta.apache.org/poi/hssf/", links[2].getLinkURL());

		assertEquals(4, links[3].getExHyperlinkAtom().getNumber());
		assertEquals("http://jakarta.apache.org/hslf/", links[3].getLinkURL());

	    ss.close();
	}
}
