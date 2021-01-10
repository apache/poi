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

package org.apache.poi.ss.formula.ptg;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.apache.poi.util.LittleEndianInput;
import org.junit.jupiter.api.Test;

/**
 * Tests for <tt>ArrayPtg</tt>
 */
final class TestArrayPtg {

	private static final byte[] ENCODED_PTG_DATA = {
		0x40,
		0, 0, 0, 0, 0, 0, 0,
	};
	private static final byte[] ENCODED_CONSTANT_DATA = {
		2,    // 3 columns
		1, 0, // 2 rows
		4, 1, 0, 0, 0, 0, 0, 0, 0, // TRUE
		2, 4, 0, 0, 65, 66, 67, 68, // "ABCD"
		2, 1, 0, 0, 69, // "E"
		1, 0, 0, 0, 0, 0, 0, 0, 0, // 0
		4, 0, 0, 0, 0, 0, 0, 0, 0, // FALSE
		2, 2, 0, 0, 70, 71, // "FG"
	};

	private static ArrayPtg create(byte[] initialData, byte[] constantData) {
		ArrayInitialPtg ptgInit = new ArrayInitialPtg(TestcaseRecordInputStream.createLittleEndian(initialData));
		return ptgInit.finishReading(TestcaseRecordInputStream.createLittleEndian(constantData));
	}

	/**
	 * Lots of problems with ArrayPtg's decoding and encoding of the element value data
	 */
	@Test
	void testReadWriteTokenValueBytes() {
		ArrayPtg ptg = create(ENCODED_PTG_DATA, ENCODED_CONSTANT_DATA);
		assertEquals(3, ptg.getColumnCount());
		assertEquals(2, ptg.getRowCount());
		Object[][] values = ptg.getTokenArrayValues();
		assertEquals(2, values.length);


		assertEquals(Boolean.TRUE, values[0][0]);
		assertEquals("ABCD", values[0][1]);
		assertEquals(0d, values[1][0]);
		assertEquals(Boolean.FALSE, values[1][1]);
		assertEquals("FG", values[1][2]);

		byte[] outBuf = new byte[ENCODED_CONSTANT_DATA.length];
		ptg.writeTokenValueBytes(new LittleEndianByteArrayOutputStream(outBuf, 0));

		assertNotEquals(4, outBuf[0], "Identified bug 42564b");
		assertArrayEquals(ENCODED_CONSTANT_DATA, outBuf);
	}


	/**
	 * Excel stores array elements column by column.  This test makes sure POI does the same.
	 */
	@Test
	void testElementOrdering() {
		ArrayPtg ptg = create(ENCODED_PTG_DATA, ENCODED_CONSTANT_DATA);
		assertEquals(3, ptg.getColumnCount());
		assertEquals(2, ptg.getRowCount());

		assertEquals(0, ptg.getValueIndex(0, 0));
		assertEquals(1, ptg.getValueIndex(1, 0));
		assertEquals(2, ptg.getValueIndex(2, 0));
		assertEquals(3, ptg.getValueIndex(0, 1));
		assertEquals(4, ptg.getValueIndex(1, 1));
		assertEquals(5, ptg.getValueIndex(2, 1));
	}

	/**
	 * Test for a bug which was temporarily introduced by the fix for bug 42564.
	 * A spreadsheet was added to make the ordering clearer.
	 */
	@Test
	void testElementOrderingInSpreadsheet() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("ex42564-elementOrder.xls");

		// The formula has an array with 3 rows and 5 columns
		String formula = wb.getSheetAt(0).getRow(0).getCell(0).getCellFormula();

		assertNotEquals("Identified bug 42564 b", "SUM({1,6,11;2,7,12;3,8,13;4,9,14;5,10,15})", formula);
		assertEquals("SUM({1,2,3,4,5;6,7,8,9,10;11,12,13,14,15})", formula);
	}

	@Test
	void testToFormulaString() {
		ArrayPtg ptg = create(ENCODED_PTG_DATA, ENCODED_CONSTANT_DATA);
		// bug 45380 - Unexpected constant class (java.lang.Boolean)
		String actualFormula = ptg.toFormulaString();
		assertEquals("{TRUE,\"ABCD\",\"E\";0,FALSE,\"FG\"}", actualFormula);
	}

	/**
	 * worth checking since AttrPtg.sid=0x20 and Ptg.CLASS_* = (0x00, 0x20, and 0x40)
	 */
	@Test
	void testOperandClassDecoding() {
		confirmOperandClassDecoding(Ptg.CLASS_REF);
		confirmOperandClassDecoding(Ptg.CLASS_VALUE);
		confirmOperandClassDecoding(Ptg.CLASS_ARRAY);
	}

	private static void confirmOperandClassDecoding(byte operandClass) {
		byte[] fullData = concat(ENCODED_PTG_DATA, ENCODED_CONSTANT_DATA);

		// Force encoded operand class for tArray
		fullData[0] = (byte) (ArrayPtg.sid + operandClass);

		LittleEndianInput in = TestcaseRecordInputStream.createLittleEndian(fullData);

		Ptg[] ptgs = Ptg.readTokens(ENCODED_PTG_DATA.length, in);
		assertEquals(1, ptgs.length);
		ArrayPtg aPtg = (ArrayPtg) ptgs[0];
		assertEquals(operandClass, aPtg.getPtgClass());
	}

	private static byte[] concat(byte[] a, byte[] b) {
		byte[] result = new byte[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}
}
