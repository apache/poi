
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

	private byte[] data_b = new byte[] { 0, 0, 0xA1-256, 0x0F, 0x2E, 0, 0, 0, 
	  0x53, 0, 0, 0, 0, 0, 0, 0,
	  0, 0, 0x1E, 0, 0, 0, 01, 0, 
	  0, 0, 01, 0, 0x1C, 0, 0, 0, 
	  02, 0, 04, 0, 02, 0, 0, 0,
	  0, 05, 0x19, 0, 0, 0, 0, 0,
	  04, 0, 0xFF-256, 0x33, 0, 0xFE-256
	};

    public void testRecordType() throws Exception {
		StyleTextPropAtom stpa = new StyleTextPropAtom(data_a,0,data_a.length);
		StyleTextPropAtom stpb = new StyleTextPropAtom(data_b,0,data_b.length);
		assertEquals(4001l, stpa.getRecordType());
		assertEquals(4001l, stpb.getRecordType());
	}

	public void testCharacterGroups() throws Exception {
		StyleTextPropAtom stpa = new StyleTextPropAtom(data_a,0,data_a.length);
		StyleTextPropAtom stpb = new StyleTextPropAtom(data_b,0,data_b.length);

		assertEquals(3, stpa.getCharacterStyles().length);
		assertEquals(3, stpb.getCharacterStyles().length);
	}

	public void testCharacterLengths() throws Exception {
		StyleTextPropAtom stpa = new StyleTextPropAtom(data_a,0,data_a.length);
		StyleTextPropAtom stpb = new StyleTextPropAtom(data_b,0,data_b.length);

		// 54 chars, 21 + 17 (+ 16)
		assertEquals(54, stpa.getParagraphStyleCharactersCoveredLength() );
		CharacterStyle[] csa = stpa.getCharacterStyles();
		assertEquals(21, csa[0].getCharactersCoveredLength() );
		assertEquals(17, csa[1].getCharactersCoveredLength() );
		assertEquals(0, csa[2].getCharactersCoveredLength() );

		// 83 chars, 30 + 28 (+ 25)
		assertEquals(83, stpb.getParagraphStyleCharactersCoveredLength() );
		CharacterStyle[] csb = stpb.getCharacterStyles();
		assertEquals(30, csb[0].getCharactersCoveredLength() );
		assertEquals(28, csb[1].getCharactersCoveredLength() );
		assertEquals(0, csb[2].getCharactersCoveredLength() );
	}

	public void testCharacterProps() throws Exception {
		StyleTextPropAtom stpa = new StyleTextPropAtom(data_a,0,data_a.length);
		StyleTextPropAtom stpb = new StyleTextPropAtom(data_b,0,data_b.length);

		// Set A has no styles
		CharacterStyle[] csa = stpa.getCharacterStyles();
		for(int i=0; i<csa.length; i++) {
			assertEquals(false, csa[i].isBold() );
			assertEquals(false, csa[i].isItalic() );
			assertEquals(false, csa[i].isUnderlined() );
		}

		// Set B - 1st bold, 2nd italic
		CharacterStyle[] csb = stpb.getCharacterStyles();
		assertEquals(true, csb[0].isBold() );
		assertEquals(true, csb[1].isItalic() );
	}

	public void testChangeCharacterProps() throws Exception {
		// Change from A to B
		StyleTextPropAtom stpa = new StyleTextPropAtom(data_a,0,data_a.length);
		CharacterStyle[] csa = stpa.getCharacterStyles();

		// Update paragraph length
		stpa.setParagraphStyleCharactersCoveredLength(83);

		// Update each of the Character Styles
		// First is 30 long and bold
		csa[0].setCharactersCoveredLength(30);
		csa[0].setBold(true);
		// Second is 28 long and italic
		csa[1].setCharactersCoveredLength(28);
		csa[1].setItalic(true);

		// Ensure now matches data from B
		// Disabled, as it currently doesn't, as we don't know about
		//  everything that needs updating, esp the S2 values
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		stpa.writeOut(baos);
//		byte[] b = baos.toByteArray();
//
//		assertEquals(data_b.length, b.length);
//		for(int i=0; i<data_b.length; i++) {
//			assertEquals(data_b[i],b[i]);
//		}
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
}
