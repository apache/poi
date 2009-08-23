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


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.POIDataSamples;

/**
 * Tests that ExHyperlink works properly.
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestExHyperlink extends TestCase {
	// From a real file
	private byte[] data_a = new byte[] {
		0x0F, 00, 0xD7-256, 0x0F, 0xA8-256, 00, 00, 00,

		00, 00, 0xD3-256, 0x0F, 04, 00, 00, 00,
		03, 00, 00, 00,

		00, 00, 0xBA-256, 0x0F, 0x46, 00, 00, 00,
		0x68, 00, 0x74, 00, 0x74, 00, 0x70, 00,
		0x3A, 00, 0x2F, 00, 0x2F, 00, 0x6A, 00,
		0x61, 00, 0x6B, 00, 0x61, 00, 0x72, 00,
		0x74, 00, 0x61, 00, 0x2E, 00, 0x61, 00,
		0x70, 00, 0x61, 00, 0x63, 00, 0x68, 00,
		0x65, 00, 0x2E, 00, 0x6F, 00, 0x72, 00,
		0x67, 00, 0x2F, 00, 0x70, 00, 0x6F, 00,
		0x69, 00, 0x2F, 00, 0x68, 00, 0x73, 00,
		0x73, 00, 0x66, 00, 0x2F, 00,

		0x10, 00, 0xBA-256, 0x0F, 0x46, 00, 00, 00,
		0x68, 00, 0x74, 00, 0x74, 00, 0x70, 00,
		0x3A, 00, 0x2F, 00, 0x2F, 00, 0x6A, 00,
		0x61, 00, 0x6B, 00, 0x61, 00, 0x72, 00,
		0x74, 00, 0x61, 00, 0x2E, 00, 0x61, 00,
		0x70, 00, 0x61, 00, 0x63, 00, 0x68, 00,
		0x65, 00, 0x2E, 00, 0x6F, 00, 0x72, 00,
		0x67, 00, 0x2F, 00, 0x70, 00, 0x6F, 00,
		0x69, 00, 0x2F, 00, 0x68, 00, 0x73, 00,
		0x73, 00, 0x66, 00, 0x2F, 00
	};

    public void testRecordType() {
    	ExHyperlink eh = new ExHyperlink(data_a, 0, data_a.length);
		assertEquals(4055l, eh.getRecordType());
	}

    public void testNumber() {
    	ExHyperlink eh = new ExHyperlink(data_a, 0, data_a.length);
		assertEquals(3, eh.getExHyperlinkAtom().getNumber());
    }

	public void testLinkURL() {
    	ExHyperlink eh = new ExHyperlink(data_a, 0, data_a.length);
    	assertEquals("http://jakarta.apache.org/poi/hssf/", eh.getLinkURL());
	}
	public void testDetails() {
    	ExHyperlink eh = new ExHyperlink(data_a, 0, data_a.length);
    	assertEquals("http://jakarta.apache.org/poi/hssf/", eh._getDetailsA());
    	assertEquals("http://jakarta.apache.org/poi/hssf/", eh._getDetailsB());
	}

	public void testWrite() throws Exception {
    	ExHyperlink eh = new ExHyperlink(data_a, 0, data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		eh.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}
	}

	public void testRealFile() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
		HSLFSlideShow hss = new HSLFSlideShow(slTests.openResourceAsStream("WithLinks.ppt"));
		SlideShow ss = new SlideShow(hss);

		// Get the document
		Document doc = ss.getDocumentRecord();
		// Get the ExObjList
		ExObjList exObjList = null;
		for(int i=0; i<doc._children.length; i++) {
			if(doc._children[i] instanceof ExObjList) {
				exObjList = (ExObjList)doc._children[i];
			}
		}
		if (exObjList == null) {
			throw new AssertionFailedError("exObjList must not be null");
		}

		// Within that, grab out the Hyperlink atoms
		List<ExHyperlink> linksA = new ArrayList<ExHyperlink>();
		for(int i=0; i<exObjList._children.length; i++) {
			Record ch = exObjList._children[i];
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

	}
}
