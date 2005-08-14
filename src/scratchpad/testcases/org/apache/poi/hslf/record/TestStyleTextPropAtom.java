
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        


package org.apache.poi.hslf.record;

import org.apache.poi.hslf.record.StyleTextPropAtom.*;

import junit.framework.TestCase;
import java.io.ByteArrayOutputStream;
import java.util.LinkedList;

/**
 * Tests that StyleTextPropAtom works properly
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public class TestStyleTextPropAtom extends TestCase {
	// From a real file
	private byte[] data_a = new byte[] { 0, 0, 0xA1-256, 0x0F, 0x2A, 0, 0, 0,
      0x36, 00, 00, 00, 00, 00, 00, 00, 
	  00, 00, 0x15, 00, 00, 00, 00, 00,
      00, 00, 0x11, 00, 00, 00, 00, 00,
	  0x04, 00, 00, 00, 00, 0x05, 0x10, 00,
      00, 00, 00, 00, 0x04, 00, 0xFF-256, 0x33, 00, 0xFE-256
	};
	private int data_a_text_len = 54;

	private byte[] data_b = new byte[] { 0, 0, 0xA1-256, 0x0F, 0x2E, 0, 0, 0, 
	  0x53, 0, 0, 0, 0, 0, 0, 0,
	  0, 0, 0x1E, 0, 0, 0, 01, 0, 
	  0, 0, 01, 0, 0x1C, 0, 0, 0, 
	  02, 0, 04, 0, 02, 0, 0, 0,
	  0, 05, 0x19, 0, 0, 0, 0, 0,
	  04, 0, 0xFF-256, 0x33, 0, 0xFE-256
	};
	private int data_b_text_len = 83;

    public void testRecordType() throws Exception {
		StyleTextPropAtom stpa = new StyleTextPropAtom(data_a,0,data_a.length);
		StyleTextPropAtom stpb = new StyleTextPropAtom(data_b,0,data_b.length);
		assertEquals(4001l, stpa.getRecordType());
		assertEquals(4001l, stpb.getRecordType());
	}


	public void testCharacterStyleCounts() throws Exception {
		StyleTextPropAtom stpa = new StyleTextPropAtom(data_a,0,data_a.length);
		StyleTextPropAtom stpb = new StyleTextPropAtom(data_b,0,data_b.length);

		// Set for the appropriate text sizes
		stpa.setParentTextSize(data_a_text_len);
		stpb.setParentTextSize(data_b_text_len);

		// In both cases, we should only have 1 paragraph styling
		assertEquals(1, stpa.getParagraphStyles().size());
		assertEquals(1, stpb.getParagraphStyles().size());
	}

	public void testParagraphStyleCounts() throws Exception {
		StyleTextPropAtom stpa = new StyleTextPropAtom(data_a,0,data_a.length);
		StyleTextPropAtom stpb = new StyleTextPropAtom(data_b,0,data_b.length);

		// Set for the appropriate text sizes
		stpa.setParentTextSize(data_a_text_len);
		stpb.setParentTextSize(data_b_text_len);

		// In both cases, we should have three different character stylings
		assertEquals(3, stpa.getCharacterStyles().size());
		assertEquals(3, stpb.getCharacterStyles().size());
	}


	public void testCharacterStyleLengths() throws Exception {
		StyleTextPropAtom stpa = new StyleTextPropAtom(data_a,0,data_a.length);
		StyleTextPropAtom stpb = new StyleTextPropAtom(data_b,0,data_b.length);

		// Set for the appropriate text sizes
		stpa.setParentTextSize(data_a_text_len);
		stpb.setParentTextSize(data_b_text_len);

		// 54 chars, 21 + 17 + 16
		LinkedList a_ch_l = stpa.getCharacterStyles();
		TextPropCollection a_ch_1 = (TextPropCollection)a_ch_l.get(0);
		TextPropCollection a_ch_2 = (TextPropCollection)a_ch_l.get(1);
		TextPropCollection a_ch_3 = (TextPropCollection)a_ch_l.get(2);
		assertEquals(21, a_ch_1.getCharactersCovered());
		assertEquals(17, a_ch_2.getCharactersCovered());
		assertEquals(16, a_ch_3.getCharactersCovered());

		// 83 chars, 30 + 28 + 25
		LinkedList b_ch_l = stpb.getCharacterStyles();
		TextPropCollection b_ch_1 = (TextPropCollection)b_ch_l.get(0);
		TextPropCollection b_ch_2 = (TextPropCollection)b_ch_l.get(1);
		TextPropCollection b_ch_3 = (TextPropCollection)b_ch_l.get(2);
		assertEquals(30, b_ch_1.getCharactersCovered());
		assertEquals(28, b_ch_2.getCharactersCovered());
		assertEquals(25, b_ch_3.getCharactersCovered());
	}

	public void testWrite() throws Exception {
		StyleTextPropAtom stpa = new StyleTextPropAtom(data_a,0,data_a.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		stpa.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}
	}

	public void testLoadWrite() throws Exception {
		StyleTextPropAtom stpa = new StyleTextPropAtom(data_a,0,data_a.length);
		stpa.setParentTextSize(data_a_text_len);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		stpa.writeOut(baos);
		byte[] b = baos.toByteArray();

		assertEquals(data_a.length, b.length);
		for(int i=0; i<data_a.length; i++) {
			assertEquals(data_a[i],b[i]);
		}
	}
}
