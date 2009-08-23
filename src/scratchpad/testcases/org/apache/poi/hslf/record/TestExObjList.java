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

import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.POIDataSamples;

/**
 * Tests that ExObjList works properly.
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public class TestExObjList extends TestCase {
	public void testRealFile() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
		HSLFSlideShow hss = new HSLFSlideShow(slTests.openResourceAsStream("WithLinks.ppt"));
		SlideShow ss = new SlideShow(hss);

		// Get the document
		Document doc = ss.getDocumentRecord();
		// Get the ExObjList
		ExObjList exObjList = doc.getExObjList();
		assertNotNull(exObjList);
		assertEquals(1033l, exObjList.getRecordType());

		// Check the atom
		assertNotNull(exObjList.getExObjListAtom());
		assertEquals(4, exObjList.getExObjListAtom().getObjectIDSeed());

		// Check the Hyperlinks
		assertEquals(4, exObjList.getExHyperlinks().length);

		// Check the contents
		ExHyperlink[] links = exObjList.getExHyperlinks();

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
