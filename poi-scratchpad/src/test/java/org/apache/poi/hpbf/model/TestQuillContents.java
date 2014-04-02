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

package org.apache.poi.hpbf.model;

import org.apache.poi.hpbf.HPBFDocument;
import org.apache.poi.hpbf.model.qcbits.QCTextBit;
import org.apache.poi.hpbf.model.qcbits.QCPLCBit.Type12;
import org.apache.poi.hpbf.model.qcbits.QCPLCBit.Type0;
import org.apache.poi.hpbf.model.qcbits.QCPLCBit.Type4;
import org.apache.poi.hpbf.model.qcbits.QCPLCBit.Type8;
import org.apache.poi.POIDataSamples;

import junit.framework.TestCase;

public final class TestQuillContents extends TestCase {
    private static final POIDataSamples _samples = POIDataSamples.getPublisherInstance();

	public void testBasics() throws Exception {
		HPBFDocument doc = new HPBFDocument(
		    _samples.openResourceAsStream("Sample.pub")
		);

		QuillContents qc = doc.getQuillContents();
		assertEquals(20, qc.getBits().length);
		for(int i=0; i<19; i++) {
			assertNotNull(qc.getBits()[i]);
		}
		// Last one is blank
		assertNull(qc.getBits()[19]);

		// Should be text, then three STSHs
		assertEquals("TEXT", qc.getBits()[0].getThingType());
		assertEquals("TEXT", qc.getBits()[0].getBitType());
		assertEquals(0, qc.getBits()[0].getOptA());

		assertEquals("STSH", qc.getBits()[1].getThingType());
		assertEquals("STSH", qc.getBits()[1].getBitType());
		assertEquals(0, qc.getBits()[1].getOptA());

		assertEquals("STSH", qc.getBits()[2].getThingType());
		assertEquals("STSH", qc.getBits()[2].getBitType());
		assertEquals(1, qc.getBits()[2].getOptA());

		assertEquals("STSH", qc.getBits()[3].getThingType());
		assertEquals("STSH", qc.getBits()[3].getBitType());
		assertEquals(2, qc.getBits()[3].getOptA());
	}

	public void testText() throws Exception {
		HPBFDocument doc = new HPBFDocument(
                _samples.openResourceAsStream("Sample.pub")
		);

		QuillContents qc = doc.getQuillContents();
		assertEquals(20, qc.getBits().length);

		QCTextBit text = (QCTextBit)qc.getBits()[0];
		String t = text.getText();
		assertTrue(t.startsWith("This is some text on the first page"));
		assertTrue(t.endsWith("Within doc to page 1\r"));
	}

	public void testPLC() throws Exception {
		HPBFDocument doc = new HPBFDocument(
                _samples.openResourceAsStream("Simple.pub")
		);

		QuillContents qc = doc.getQuillContents();
		assertEquals(20, qc.getBits().length);

		assertTrue(qc.getBits()[9] instanceof Type4);
		assertTrue(qc.getBits()[10] instanceof Type4);
		assertTrue(qc.getBits()[12] instanceof Type8);

		Type4 plc9 = (Type4)qc.getBits()[9];
		Type4 plc10 = (Type4)qc.getBits()[10];
		Type8 plc12 = (Type8)qc.getBits()[12];


		assertEquals(1, plc9.getNumberOfPLCs());
		assertEquals(4, plc9.getPreData().length);
		assertEquals(1, plc9.getPlcValA().length);
		assertEquals(1, plc9.getPlcValB().length);

		assertEquals(0, plc9.getPreData()[0]);
		assertEquals(0, plc9.getPreData()[1]);
		assertEquals(0, plc9.getPreData()[2]);
		assertEquals(0, plc9.getPreData()[3]);
		assertEquals(0x356, plc9.getPlcValA()[0]);
		assertEquals(0x600, plc9.getPlcValB()[0]);


		assertEquals(1, plc10.getNumberOfPLCs());
		assertEquals(4, plc10.getPreData().length);
		assertEquals(1, plc10.getPlcValA().length);
		assertEquals(1, plc10.getPlcValB().length);

		assertEquals(0, plc10.getPreData()[0]);
		assertEquals(0, plc10.getPreData()[1]);
		assertEquals(0, plc10.getPreData()[2]);
		assertEquals(0, plc10.getPreData()[3]);
		assertEquals(0x356, plc10.getPlcValA()[0]);
		assertEquals(0x800, plc10.getPlcValB()[0]);

		assertEquals(2, plc12.getNumberOfPLCs());
		assertEquals(7, plc12.getPreData().length);
		assertEquals(2, plc12.getPlcValA().length);
		assertEquals(2, plc12.getPlcValB().length);

		assertEquals(0xff, plc12.getPreData()[0]);
		assertEquals(0, plc12.getPreData()[1]);
		assertEquals(0x3d, plc12.getPreData()[2]);
		assertEquals(0, plc12.getPreData()[3]);
		assertEquals(0x6e, plc12.getPreData()[4]);
		assertEquals(0, plc12.getPreData()[5]);
		assertEquals(0, plc12.getPreData()[6]);
		assertEquals(0xa0000, plc12.getPlcValA()[0]);
		assertEquals(0x22000000, plc12.getPlcValB()[0]);
		assertEquals(0x05, plc12.getPlcValA()[1]);
		assertEquals(0x04, plc12.getPlcValB()[1]);
	}

	public void testComplexPLC() throws Exception {
		HPBFDocument doc = new HPBFDocument(
                _samples.openResourceAsStream("Sample.pub")
		);

		QuillContents qc = doc.getQuillContents();
		assertEquals(20, qc.getBits().length);

		assertTrue(qc.getBits()[10] instanceof Type4);
		assertTrue(qc.getBits()[11] instanceof Type4);
		assertTrue(qc.getBits()[13] instanceof Type0);
		assertTrue(qc.getBits()[14] instanceof Type12);
		assertTrue(qc.getBits()[15] instanceof Type12);
		assertTrue(qc.getBits()[16] instanceof Type8);

		Type4 plc10 = (Type4)qc.getBits()[10];
		Type4 plc11 = (Type4)qc.getBits()[11];
		Type0 plc13 = (Type0)qc.getBits()[13];
		Type12 plc14 = (Type12)qc.getBits()[14];
		Type12 plc15 = (Type12)qc.getBits()[15];
		Type8 plc16 = (Type8)qc.getBits()[16];


		assertEquals(1, plc10.getNumberOfPLCs());
		assertEquals(4, plc10.getPreData().length);
		assertEquals(1, plc10.getPlcValA().length);
		assertEquals(1, plc10.getPlcValB().length);

		assertEquals(0, plc10.getPreData()[0]);
		assertEquals(0, plc10.getPreData()[1]);
		assertEquals(0, plc10.getPreData()[2]);
		assertEquals(0, plc10.getPreData()[3]);
		assertEquals(0x5d0, plc10.getPlcValA()[0]);
		assertEquals(0x800, plc10.getPlcValB()[0]);


		assertEquals(2, plc11.getNumberOfPLCs());
		assertEquals(4, plc11.getPreData().length);
		assertEquals(2, plc11.getPlcValA().length);
		assertEquals(2, plc11.getPlcValB().length);

		assertEquals(0, plc11.getPreData()[0]);
		assertEquals(0, plc11.getPreData()[1]);
		assertEquals(0, plc11.getPreData()[2]);
		assertEquals(0, plc11.getPreData()[3]);
		assertEquals(0x53a, plc11.getPlcValA()[0]);
		assertEquals(0x5d0, plc11.getPlcValB()[0]);
		assertEquals(0xa00, plc11.getPlcValA()[1]);
		assertEquals(0xc00, plc11.getPlcValB()[1]);


		assertEquals(5, plc13.getNumberOfPLCs());
		assertEquals(4, plc13.getPreData().length);
		assertEquals(5, plc13.getPlcValA().length);
		assertEquals(5, plc13.getPlcValB().length);

		assertEquals(0xff00, plc13.getPreData()[0]);
		assertEquals(0, plc13.getPreData()[1]);
		assertEquals(0xf, plc13.getPreData()[2]);
		assertEquals(0, plc13.getPreData()[3]);
		assertEquals(0x19, plc13.getPlcValA()[0]);
		assertEquals(0x00, plc13.getPlcValB()[0]);
		assertEquals(0x27, plc13.getPlcValA()[1]);
		assertEquals(0x00, plc13.getPlcValB()[1]);
		assertEquals(0x36, plc13.getPlcValA()[2]);
		assertEquals(0x00, plc13.getPlcValB()[2]);
		assertEquals(0x42, plc13.getPlcValA()[3]);
		assertEquals(0x00, plc13.getPlcValB()[3]);
		assertEquals(0x50, plc13.getPlcValA()[4]);
		assertEquals(0x00, plc13.getPlcValB()[4]);


		// TODO - test the type 12s


		assertEquals(6, plc16.getNumberOfPLCs());
		assertEquals(7, plc16.getPreData().length);
		assertEquals(6, plc16.getPlcValA().length);
		assertEquals(6, plc16.getPlcValB().length);

		assertEquals(0xff, plc16.getPreData()[0]);
		assertEquals(0, plc16.getPreData()[1]);
		assertEquals(0x56, plc16.getPreData()[2]);
		assertEquals(0, plc16.getPreData()[3]);
		assertEquals(0x62, plc16.getPreData()[4]);
		assertEquals(0, plc16.getPreData()[5]);
		assertEquals(0x3e, plc16.getPreData()[6]);
		assertEquals(0x500000, plc16.getPlcValA()[0]);
		assertEquals(0x570000, plc16.getPlcValB()[0]);
		assertEquals(0x4b0000, plc16.getPlcValA()[1]);
		assertEquals(0x000000, plc16.getPlcValB()[1]);
		assertEquals(0x0a0000, plc16.getPlcValA()[2]);
		assertEquals(0x22000000, plc16.getPlcValB()[2]);
		assertEquals(0x000005, plc16.getPlcValA()[3]);
		assertEquals(0x000004, plc16.getPlcValB()[3]);
		assertEquals(0x000004, plc16.getPlcValA()[4]);
		assertEquals(0x000004, plc16.getPlcValB()[4]);
		assertEquals(0x000004, plc16.getPlcValA()[5]);
		assertEquals(0x000004, plc16.getPlcValB()[5]);
	}

	public void testNoHyperlinks() throws Exception {
		HPBFDocument doc = new HPBFDocument(
                _samples.openResourceAsStream("SampleNewsletter.pub")
		);

		QuillContents qc = doc.getQuillContents();
		assertEquals(20, qc.getBits().length);

		Type12 plc18 = (Type12)qc.getBits()[18];

		assertEquals(1, plc18.getNumberOfPLCs());
		assertEquals(0, plc18.getNumberOfHyperlinks());
		assertEquals(0, plc18.getTextStartAt(0));
		assertEquals(601, plc18.getAllTextEndAt());
	}

	public void testSimpleHyperlink() throws Exception {
		HPBFDocument doc;
		QuillContents qc;
		Type12 hlBit;

		// Link at 10
		doc = new HPBFDocument(
                _samples.openResourceAsStream("LinkAt10.pub")
		);
		qc = doc.getQuillContents();

		hlBit = (Type12)qc.getBits()[12];
		assertEquals(1, hlBit.getNumberOfPLCs());
		assertEquals(1, hlBit.getNumberOfHyperlinks());

		assertEquals(10, hlBit.getTextStartAt(0));
		assertEquals(15, hlBit.getAllTextEndAt());
		assertEquals("http://poi.apache.org/", hlBit.getHyperlink(0));

		// Longer link at 10
		doc = new HPBFDocument(
                _samples.openResourceAsStream("LinkAt10Longer.pub")
		);
		qc = doc.getQuillContents();

		hlBit = (Type12)qc.getBits()[12];
		assertEquals(1, hlBit.getNumberOfPLCs());
		assertEquals(1, hlBit.getNumberOfHyperlinks());

		assertEquals(10, hlBit.getTextStartAt(0));
		assertEquals(15, hlBit.getAllTextEndAt());
		assertEquals("http://poi.apache.org/hpbf/", hlBit.getHyperlink(0));

		// Link at 20
		doc = new HPBFDocument(
                _samples.openResourceAsStream("LinkAt20.pub")
		);
		qc = doc.getQuillContents();

		hlBit = (Type12)qc.getBits()[12];
		assertEquals(1, hlBit.getNumberOfPLCs());
		assertEquals(1, hlBit.getNumberOfHyperlinks());

		assertEquals(20, hlBit.getTextStartAt(0));
		assertEquals(25, hlBit.getAllTextEndAt());
		assertEquals("http://poi.apache.org/", hlBit.getHyperlink(0));
	}

	public void testManyHyperlinks() throws Exception {
		HPBFDocument doc;
		QuillContents qc;
		Type12 hlBit;

		// Link at 10
		doc = new HPBFDocument(
                _samples.openResourceAsStream("LinkAt10.pub")
		);
		qc = doc.getQuillContents();

		hlBit = (Type12)qc.getBits()[12];
		assertEquals(1, hlBit.getNumberOfPLCs());
		assertEquals(1, hlBit.getNumberOfHyperlinks());

		assertEquals(10, hlBit.getTextStartAt(0));
		assertEquals(15, hlBit.getAllTextEndAt());
		assertEquals("http://poi.apache.org/", hlBit.getHyperlink(0));

	}

	public void testHyperlinkDifferentVersions() throws Exception {
		HPBFDocument doc;
		QuillContents qc;
		Type12 hlBitA;
		Type12 hlBitB;

		// Latest version
		doc = new HPBFDocument(
                _samples.openResourceAsStream("Sample.pub")
		);
		qc = doc.getQuillContents();

		hlBitA = (Type12)qc.getBits()[14];
		assertEquals(2, hlBitA.getNumberOfPLCs());
		assertEquals(2, hlBitA.getNumberOfHyperlinks());

		assertEquals(25, hlBitA.getTextStartAt(0));
		assertEquals(72, hlBitA.getTextStartAt(1));
		assertEquals(87, hlBitA.getAllTextEndAt());
		assertEquals("http://poi.apache.org/", hlBitA.getHyperlink(0));
		assertEquals("C:\\Documents and Settings\\Nick\\My Documents\\Booleans.xlsx", hlBitA.getHyperlink(1));

		hlBitB = (Type12)qc.getBits()[15];
		assertEquals(3, hlBitB.getNumberOfPLCs());
		assertEquals(3, hlBitB.getNumberOfHyperlinks());

		assertEquals(27, hlBitB.getTextStartAt(0));
		assertEquals(37, hlBitB.getTextStartAt(1));
		assertEquals(54, hlBitB.getTextStartAt(2));
		assertEquals(75, hlBitB.getAllTextEndAt());
		assertEquals("", hlBitB.getHyperlink(0));
		assertEquals("mailto:dev@poi.apache.org?subject=HPBF", hlBitB.getHyperlink(1));
		assertEquals("mailto:dev@poi.apache.org?subject=HPBF", hlBitB.getHyperlink(2));

		// 2000 version
		doc = new HPBFDocument(
                _samples.openResourceAsStream("Sample2000.pub")
		);
		qc = doc.getQuillContents();

		hlBitA = (Type12)qc.getBits()[13];
		assertEquals(2, hlBitA.getNumberOfPLCs());
		assertEquals(2, hlBitA.getNumberOfHyperlinks());

		assertEquals(25, hlBitA.getTextStartAt(0));
		assertEquals(72, hlBitA.getTextStartAt(1));
		assertEquals(87, hlBitA.getAllTextEndAt());
		assertEquals("http://poi.apache.org/", hlBitA.getHyperlink(0));
		assertEquals("C:\\Documents and Settings\\Nick\\My Documents\\Booleans.xlsx", hlBitA.getHyperlink(1));

		hlBitB = (Type12)qc.getBits()[14];
		assertEquals(3, hlBitB.getNumberOfPLCs());
		assertEquals(3, hlBitB.getNumberOfHyperlinks());

		assertEquals(27, hlBitB.getTextStartAt(0));
		assertEquals(37, hlBitB.getTextStartAt(1));
		assertEquals(54, hlBitB.getTextStartAt(2));
		assertEquals(75, hlBitB.getAllTextEndAt());
		assertEquals("", hlBitB.getHyperlink(0));
		assertEquals("mailto:dev@poi.apache.org?subject=HPBF", hlBitB.getHyperlink(1));
		assertEquals("mailto:dev@poi.apache.org?subject=HPBF", hlBitB.getHyperlink(2));

		// 98 version
		doc = new HPBFDocument(
                _samples.openResourceAsStream("Sample98.pub")
		);
		qc = doc.getQuillContents();

		hlBitA = (Type12)qc.getBits()[13];
		assertEquals(2, hlBitA.getNumberOfPLCs());
		assertEquals(2, hlBitA.getNumberOfHyperlinks());

		assertEquals(25, hlBitA.getTextStartAt(0));
		assertEquals(72, hlBitA.getTextStartAt(1));
		assertEquals(87, hlBitA.getAllTextEndAt());
		assertEquals("http://poi.apache.org/", hlBitA.getHyperlink(0));
		assertEquals("C:\\Documents and Settings\\Nick\\My Documents\\Booleans.xlsx", hlBitA.getHyperlink(1));

		hlBitB = (Type12)qc.getBits()[14];
		assertEquals(3, hlBitB.getNumberOfPLCs());
		assertEquals(3, hlBitB.getNumberOfHyperlinks());

		assertEquals(27, hlBitB.getTextStartAt(0));
		assertEquals(37, hlBitB.getTextStartAt(1));
		assertEquals(54, hlBitB.getTextStartAt(2));
		assertEquals(75, hlBitB.getAllTextEndAt());
		assertEquals("", hlBitB.getHyperlink(0));
		assertEquals("mailto:dev@poi.apache.org?subject=HPBF", hlBitB.getHyperlink(1));
		assertEquals("mailto:dev@poi.apache.org?subject=HPBF", hlBitB.getHyperlink(2));
	}
}
