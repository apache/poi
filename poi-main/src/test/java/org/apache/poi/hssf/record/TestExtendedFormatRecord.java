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

package org.apache.poi.hssf.record;

import junit.framework.TestCase;
/**
 */
public final class TestExtendedFormatRecord extends TestCase {

	private static final byte[] data = new byte[] {
			00, 00, // Font 0
			00, 00, // Format 0
			0xF5 - 256, 0xFF - 256, // Cell opts ...
			0x20, 00, // Alignment 20
			00, 00, // Ident 0
			00, 00, // Border 0
			00, 00, // Palette 0
			00, 00, 00, 00, // ADTL Palette 0
			0xC0 - 256, 0x20 // Fill Palette 20c0
	};

	private static ExtendedFormatRecord createEFR() {
		return new ExtendedFormatRecord(TestcaseRecordInputStream.create(0x00E0, data));
	}

	public void testLoad() {
		ExtendedFormatRecord record = createEFR();
		assertEquals(0, record.getFontIndex());
		assertEquals(0, record.getFormatIndex());
		assertEquals(0xF5 - 256, record.getCellOptions());
		assertEquals(0x20, record.getAlignmentOptions());
		assertEquals(0, record.getIndentionOptions());
		assertEquals(0, record.getBorderOptions());
		assertEquals(0, record.getPaletteOptions());
		assertEquals(0, record.getAdtlPaletteOptions());
		assertEquals(0x20c0, record.getFillPaletteOptions());

		assertEquals(20 + 4, record.getRecordSize());
	}


	public void testStore() {
//    .fontindex       = 0
//    .formatindex     = 0
//    .celloptions     = fffffff5
//          .islocked  = true
//          .ishidden  = false
//          .recordtype= 1
//          .parentidx = fff
//    .alignmentoptions= 20
//          .alignment = 0
//          .wraptext  = false
//          .valignment= 2
//          .justlast  = 0
//          .rotation  = 0
//    .indentionoptions= 0
//          .indent    = 0
//          .shrinktoft= false
//          .mergecells= false
//          .readngordr= 0
//          .formatflag= false
//          .fontflag  = false
//          .prntalgnmt= false
//          .borderflag= false
//          .paternflag= false
//          .celloption= false
//    .borderoptns     = 0
//          .lftln     = 0
//          .rgtln     = 0
//          .topln     = 0
//          .btmln     = 0
//    .paleteoptns     = 0
//          .leftborder= 0
//          .rghtborder= 0
//          .diag      = 0
//    .paleteoptn2     = 0
//          .topborder = 0
//          .botmborder= 0
//          .adtldiag  = 0
//          .diaglnstyl= 0
//          .fillpattrn= 0
//    .fillpaloptn     = 20c0
//          .foreground= 40
//          .background= 41

		ExtendedFormatRecord record = new ExtendedFormatRecord();
		record.setFontIndex((short) 0);
		record.setFormatIndex((short) 0);

		record.setLocked(true);
		record.setHidden(false);
		record.setXFType((short) 1);
		record.setParentIndex((short) 0xfff);

		record.setVerticalAlignment((short) 2);

		record.setFillForeground((short) 0x40);
		record.setFillBackground((short) 0x41);

		byte[] recordBytes = record.serialize();
		assertEquals(recordBytes.length - 4, data.length);
		for (int i = 0; i < data.length; i++)
			assertEquals("At offset " + i, data[i], recordBytes[i + 4]);
	}

	public void testCloneOnto() {
		ExtendedFormatRecord base = createEFR();

		ExtendedFormatRecord other = new ExtendedFormatRecord();
		other.cloneStyleFrom(base);

		byte[] recordBytes = other.serialize();
		assertEquals(recordBytes.length - 4, data.length);
		for (int i = 0; i < data.length; i++)
			assertEquals("At offset " + i, data[i], recordBytes[i + 4]);
	}
}
