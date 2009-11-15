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

package org.apache.poi.hssf.record.formula;

import java.util.Arrays;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.TestcaseRecordInputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.LittleEndianByteArrayOutputStream;
import org.apache.poi.util.LittleEndianInput;
/**
 * Tests for <tt>ArrayPtg</tt>
 *
 * @author Josh Micich
 */
public final class TestArrayPtg extends TestCase {

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
		ArrayPtg.Initial ptgInit = new ArrayPtg.Initial(TestcaseRecordInputStream.createLittleEndian(initialData));
		return ptgInit.finishReading(TestcaseRecordInputStream.createLittleEndian(constantData));
	}

	/**
	 * Lots of problems with ArrayPtg's decoding and encoding of the element value data
	 */
	public void testReadWriteTokenValueBytes() {
		ArrayPtg ptg = create(ENCODED_PTG_DATA, ENCODED_CONSTANT_DATA);
		assertEquals(3, ptg.getColumnCount());
		assertEquals(2, ptg.getRowCount());
		Object[][] values = ptg.getTokenArrayValues();
		assertEquals(2, values.length);


		assertEquals(Boolean.TRUE, values[0][0]);
		assertEquals("ABCD", values[0][1]);
		assertEquals(new Double(0), values[1][0]);
		assertEquals(Boolean.FALSE, values[1][1]);
		assertEquals("FG", values[1][2]);

		byte[] outBuf = new byte[ENCODED_CONSTANT_DATA.length];
		ptg.writeTokenValueBytes(new LittleEndianByteArrayOutputStream(outBuf, 0));

		if(outBuf[0] == 4) {
			throw new AssertionFailedError("Identified bug 42564b");
		}
		assertTrue(Arrays.equals(ENCODED_CONSTANT_DATA, outBuf));
	}


	/**
	 * Excel stores array elements column by column.  This test makes sure POI does the same.
	 */
	public void testElementOrdering() {
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
	public void testElementOrderingInSpreadsheet() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("ex42564-elementOrder.xls");

		// The formula has an array with 3 rows and 5 columns
		String formula = wb.getSheetAt(0).getRow(0).getCell(0).getCellFormula();
		// TODO - These number literals should not have '.0'. Excel has different number rendering rules

		if (formula.equals("SUM({1.0,6.0,11.0;2.0,7.0,12.0;3.0,8.0,13.0;4.0,9.0,14.0;5.0,10.0,15.0})")) {
			throw new AssertionFailedError("Identified bug 42564 b");
		}
		assertEquals("SUM({1.0,2.0,3.0,4.0,5.0;6.0,7.0,8.0,9.0,10.0;11.0,12.0,13.0,14.0,15.0})", formula);
	}

	public void testToFormulaString() {
		ArrayPtg ptg = create(ENCODED_PTG_DATA, ENCODED_CONSTANT_DATA);
		String actualFormula;
		try {
			actualFormula = ptg.toFormulaString();
		} catch (IllegalArgumentException e) {
			if (e.getMessage().equals("Unexpected constant class (java.lang.Boolean)")) {
				throw new AssertionFailedError("Identified bug 45380");
			}
			throw e;
		}
		assertEquals("{TRUE,\"ABCD\",\"E\";0.0,FALSE,\"FG\"}", actualFormula);
	}

	/**
	 * worth checking since AttrPtg.sid=0x20 and Ptg.CLASS_* = (0x00, 0x20, and 0x40)
	 */
	public void testOperandClassDecoding() {
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
