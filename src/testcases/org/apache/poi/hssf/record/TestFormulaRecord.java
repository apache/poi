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

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.AttrPtg;
import org.apache.poi.hssf.record.formula.FuncVarPtg;
import org.apache.poi.hssf.record.formula.IntPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.RefPtg;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFErrorConstants;

/**
 * Tests the serialization and deserialization of the FormulaRecord
 * class works correctly.
 *
 * @author Andrew C. Oliver
 */
public final class TestFormulaRecord extends TestCase {

	public void testCreateFormulaRecord () {
		FormulaRecord record = new FormulaRecord();
		record.setColumn((short)0);
		record.setRow(1);
		record.setXFIndex((short)4);

		assertEquals(record.getColumn(),0);
		assertEquals(record.getRow(), 1);
		assertEquals(record.getXFIndex(),4);
	}

	/**
	 * Make sure a NAN value is preserved
	 * This formula record is a representation of =1/0 at row 0, column 0
	 */
	public void testCheckNanPreserve() {
		byte[] formulaByte = {
			0, 0, 0, 0,
			0x0F, 0x00,

			// 8 bytes cached number is a 'special value' in this case
			0x02, // special cached value type 'error'
			0x00,
			HSSFErrorConstants.ERROR_DIV_0,
			0x00,
			0x00,
			0x00,
			(byte)0xFF,
			(byte)0xFF,

			0x00,
			0x00,
			0x00,
			0x00,

			(byte)0xE0, //18
			(byte)0xFC,
			// Ptgs
			0x07, 0x00, // encoded length
			0x1E, 0x01, 0x00, // IntPtg(1)
			0x1E, 0x00,	0x00, // IntPtg(0)
			0x06, // DividePtg

		};

		FormulaRecord record = new FormulaRecord(new TestcaseRecordInputStream(FormulaRecord.sid, (short)29, formulaByte));
		assertEquals("Row", 0, record.getRow());
		assertEquals("Column", 0, record.getColumn());
		assertEquals(HSSFCell.CELL_TYPE_ERROR, record.getCachedResultType());

		byte[] output = record.serialize();
		assertEquals("Output size", 33, output.length); //includes sid+recordlength

		for (int i = 5; i < 13;i++) {
			assertEquals("FormulaByte NaN doesn't match", formulaByte[i], output[i+4]);
		}
	}

	/**
	 * Tests to see if the shared formula cells properly reserialize the expPtg
	 *
	 */
	public void testExpFormula() {
		byte[] formulaByte = new byte[27];

		formulaByte[4] =(byte)0x0F;
		formulaByte[14]=(byte)0x08;
		formulaByte[18]=(byte)0xE0;
		formulaByte[19]=(byte)0xFD;
		formulaByte[20]=(byte)0x05;
		formulaByte[22]=(byte)0x01;
		FormulaRecord record = new FormulaRecord(new TestcaseRecordInputStream(FormulaRecord.sid, (short)27, formulaByte));
		assertEquals("Row", 0, record.getRow());
		assertEquals("Column", 0, record.getColumn());
		byte[] output = record.serialize();
		assertEquals("Output size", 31, output.length); //includes sid+recordlength
		assertEquals("Offset 22", 1, output[26]);
	}

	public void testWithConcat() {
		// =CHOOSE(2,A2,A3,A4)
		byte[] data = {
				6, 0, 68, 0,
				1, 0, 1, 0, 15, 0, 0, 0, 0, 0, 0, 0, 57,
				64, 0, 0, 12, 0, 12, -4, 46, 0,
				30, 2, 0,	// Int - 2
				25, 4, 3, 0, // Attr
					8, 0, 17, 0, 26, 0, // jumpTable
					35, 0, // chooseOffset
				36, 1, 0, 0, -64, // Ref - A2
				25, 8, 21, 0, // Attr
				36, 2, 0, 0, -64, // Ref - A3
				25,	8, 12, 0, // Attr
				36, 3, 0, 0, -64, // Ref - A4
				25, 8, 3, 0,  // Attr
				66, 4, 100, 0 // CHOOSE
		};
		RecordInputStream inp = new RecordInputStream( new ByteArrayInputStream(data));
		inp.nextRecord();

		FormulaRecord fr = new FormulaRecord(inp);

		Ptg[] ptgs = fr.getParsedExpression();
		assertEquals(9, ptgs.length);
		assertEquals(IntPtg.class,	   ptgs[0].getClass());
		assertEquals(AttrPtg.class,	  ptgs[1].getClass());
		assertEquals(RefPtg.class, ptgs[2].getClass());
		assertEquals(AttrPtg.class,	  ptgs[3].getClass());
		assertEquals(RefPtg.class, ptgs[4].getClass());
		assertEquals(AttrPtg.class,	  ptgs[5].getClass());
		assertEquals(RefPtg.class, ptgs[6].getClass());
		assertEquals(AttrPtg.class,	  ptgs[7].getClass());
		assertEquals(FuncVarPtg.class,   ptgs[8].getClass());

		FuncVarPtg choose = (FuncVarPtg)ptgs[8];
		assertEquals("CHOOSE", choose.getName());
	}
}
