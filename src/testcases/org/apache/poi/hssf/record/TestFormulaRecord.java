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
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.AttrPtg;
import org.apache.poi.hssf.record.formula.FuncVarPtg;
import org.apache.poi.hssf.record.formula.IntPtg;
import org.apache.poi.hssf.record.formula.ReferencePtg;

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
		//record.setRow((short)1);
		record.setRow(1);
		record.setXFIndex((short)4);
		
		assertEquals(record.getColumn(),(short)0);
		//assertEquals(record.getRow(),(short)1);
		assertEquals((short)record.getRow(),(short)1);
		assertEquals(record.getXFIndex(),(short)4);
	}
	
	/**
	 * Make sure a NAN value is preserved
	 * This formula record is a representation of =1/0 at row 0, column 0 
	 */
	public void testCheckNanPreserve() {
		byte[] formulaByte = new byte[29];

		formulaByte[4] = (byte)0x0F;
		formulaByte[6] = (byte)0x02;
		formulaByte[8] = (byte)0x07;
		formulaByte[12] = (byte)0xFF;
		formulaByte[13] = (byte)0xFF;
		formulaByte[18] = (byte)0xE0;
		formulaByte[19] = (byte)0xFC;
		formulaByte[20] = (byte)0x07;
		formulaByte[22] = (byte)0x1E;
		formulaByte[23] = (byte)0x01;
		formulaByte[25] = (byte)0x1E;
		formulaByte[28] = (byte)0x06;
		
		FormulaRecord record = new FormulaRecord(new TestcaseRecordInputStream(FormulaRecord.sid, (short)29, formulaByte));
		assertEquals("Row", 0, record.getRow());
		assertEquals("Column", 0, record.getColumn());		
		assertTrue("Value is not NaN", Double.isNaN(record.getValue()));
		
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
	
	public void testWithConcat()  throws Exception {
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
		
		List ptgs = fr.getParsedExpression();
		assertEquals(9, ptgs.size());
		assertEquals(IntPtg.class,	   ptgs.get(0).getClass());
		assertEquals(AttrPtg.class,	  ptgs.get(1).getClass());
		assertEquals(ReferencePtg.class, ptgs.get(2).getClass());
		assertEquals(AttrPtg.class,	  ptgs.get(3).getClass());
		assertEquals(ReferencePtg.class, ptgs.get(4).getClass());
		assertEquals(AttrPtg.class,	  ptgs.get(5).getClass());
		assertEquals(ReferencePtg.class, ptgs.get(6).getClass());
		assertEquals(AttrPtg.class,	  ptgs.get(7).getClass());
		assertEquals(FuncVarPtg.class,   ptgs.get(8).getClass());
		
		FuncVarPtg choose = (FuncVarPtg)ptgs.get(8);
		assertEquals("CHOOSE", choose.getName());
	}
	
	public static void main(String [] ignored_args) {
		junit.textui.TestRunner.run(TestFormulaRecord.class);
	}
}
