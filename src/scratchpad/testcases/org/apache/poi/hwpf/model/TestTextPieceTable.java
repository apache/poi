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

package org.apache.poi.hwpf.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.apache.poi.hwpf.HWPFDocFixture;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.hwpf.model.io.HWPFFileSystem;


public final class TestTextPieceTable extends TestCase {
  private HWPFDocFixture _hWPFDocFixture;
  //private String dirname;

  public void testReadWrite()
    throws Exception
  {
    FileInformationBlock fib = _hWPFDocFixture._fib;
    byte[] mainStream = _hWPFDocFixture._mainStream;
    byte[] tableStream = _hWPFDocFixture._tableStream;
    int fcMin = fib.getFcMin();

    ComplexFileTable cft = new ComplexFileTable(mainStream, tableStream, fib.getFcClx(), fcMin);


    HWPFFileSystem fileSys = new HWPFFileSystem();

    cft.writeTo(fileSys);
    ByteArrayOutputStream tableOut = fileSys.getStream("1Table");
    ByteArrayOutputStream mainOut =  fileSys.getStream("WordDocument");

    byte[] newTableStream = tableOut.toByteArray();
    byte[] newMainStream = mainOut.toByteArray();

    ComplexFileTable newCft = new ComplexFileTable(newMainStream, newTableStream, 0,0);

    TextPieceTable oldTextPieceTable = cft.getTextPieceTable();
    TextPieceTable newTextPieceTable = newCft.getTextPieceTable();

    assertEquals(oldTextPieceTable, newTextPieceTable);
  }

	/**
	 * Check that we do the positions correctly when
	 *  working with pure-ascii
	 */
	public void testAsciiParts() throws Exception {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("ThreeColHeadFoot.doc");
		TextPieceTable tbl = doc.getTextTable();

		// All ascii, so stored in one big lump
		assertEquals(1, tbl.getTextPieces().size());
		TextPiece tp = (TextPiece)tbl.getTextPieces().get(0);

		assertEquals(0, tp.getStart());
		assertEquals(339, tp.getEnd());
		assertEquals(339, tp.characterLength());
		assertEquals(339, tp.bytesLength());
		assertTrue(tp.getStringBuffer().toString().startsWith("This is a sample word document"));


		// Save and re-load
		HWPFDocument docB = saveAndReload(doc);
		tbl = docB.getTextTable();

		assertEquals(1, tbl.getTextPieces().size());
		tp = (TextPiece)tbl.getTextPieces().get(0);

		assertEquals(0, tp.getStart());
		assertEquals(339, tp.getEnd());
		assertEquals(339, tp.characterLength());
		assertEquals(339, tp.bytesLength());
		assertTrue(tp.getStringBuffer().toString().startsWith("This is a sample word document"));
	}

	/**
	 * Check that we do the positions correctly when
	 *  working with a mix ascii, unicode file
	 */
	public void testUnicodeParts() throws Exception {
		HWPFDocument doc = HWPFTestDataSamples.openSampleFile("HeaderFooterUnicode.doc");
		TextPieceTable tbl = doc.getTextTable();

		// In three bits, split every 512 bytes
		assertEquals(3, tbl.getTextPieces().size());
		TextPiece tpA = (TextPiece)tbl.getTextPieces().get(0);
		TextPiece tpB = (TextPiece)tbl.getTextPieces().get(1);
		TextPiece tpC = (TextPiece)tbl.getTextPieces().get(2);

		assertTrue(tpA.isUnicode());
		assertTrue(tpB.isUnicode());
		assertTrue(tpC.isUnicode());

		assertEquals(256, tpA.characterLength());
		assertEquals(256, tpB.characterLength());
		assertEquals(19, tpC.characterLength());

		assertEquals(512, tpA.bytesLength());
		assertEquals(512, tpB.bytesLength());
		assertEquals(38, tpC.bytesLength());

		assertEquals(0, tpA.getStart());
		assertEquals(256, tpA.getEnd());
		assertEquals(256, tpB.getStart());
		assertEquals(512, tpB.getEnd());
		assertEquals(512, tpC.getStart());
		assertEquals(531, tpC.getEnd());


		// Save and re-load
		HWPFDocument docB = saveAndReload(doc);
		tbl = docB.getTextTable();

		assertEquals(3, tbl.getTextPieces().size());
		tpA = (TextPiece)tbl.getTextPieces().get(0);
		tpB = (TextPiece)tbl.getTextPieces().get(1);
		tpC = (TextPiece)tbl.getTextPieces().get(2);

		assertTrue(tpA.isUnicode());
		assertTrue(tpB.isUnicode());
		assertTrue(tpC.isUnicode());

		assertEquals(256, tpA.characterLength());
		assertEquals(256, tpB.characterLength());
		assertEquals(19, tpC.characterLength());

		assertEquals(512, tpA.bytesLength());
		assertEquals(512, tpB.bytesLength());
		assertEquals(38, tpC.bytesLength());

		assertEquals(0, tpA.getStart());
		assertEquals(256, tpA.getEnd());
		assertEquals(256, tpB.getStart());
		assertEquals(512, tpB.getEnd());
		assertEquals(512, tpC.getStart());
		assertEquals(531, tpC.getEnd());
	}

	protected HWPFDocument saveAndReload(HWPFDocument doc) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		doc.write(baos);

		return new HWPFDocument(
				new ByteArrayInputStream(baos.toByteArray())
		);
	}

  protected void setUp()
    throws Exception
  {
    super.setUp();

    _hWPFDocFixture = new HWPFDocFixture(this);
    _hWPFDocFixture.setUp();
  }

  protected void tearDown()
    throws Exception
  {
    _hWPFDocFixture.tearDown();

    _hWPFDocFixture = null;
    super.tearDown();
  }

}
