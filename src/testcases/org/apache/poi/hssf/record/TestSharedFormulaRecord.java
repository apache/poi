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

import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.RefPtg;

/**
 * @author Josh Micich
 */
public final class TestSharedFormulaRecord extends TestCase {

	/**
	 * Binary data for an encoded formula.  Taken from attachment 22062 (bugzilla 45123/45421).
	 * The shared formula is in Sheet1!C6:C21, with text "SUMPRODUCT(--(End_Acct=$C6),--(End_Bal))"
	 * This data is found at offset 0x1A4A (within the shared formula record).
	 * The critical thing about this formula is that it contains shared formula tokens (tRefN*,
	 * tAreaN*) with operand class 'array'.
	 */
	private static final byte[] SHARED_FORMULA_WITH_REF_ARRAYS_DATA = {
		0x1A, 0x00,
		0x63, 0x02, 0x00, 0x00, 0x00,
		0x6C, 0x00, 0x00, 0x02, (byte)0x80,  // tRefNA
		0x0B,
		0x15,
		0x13,
		0x13,
		0x63, 0x03, 0x00, 0x00, 0x00,
		0x15,
		0x13,
		0x13,
		0x42, 0x02, (byte)0xE4, 0x00,
	};
	
	/**
	 * The method <tt>SharedFormulaRecord.convertSharedFormulas()</tt> converts formulas from
	 * 'shared formula' to 'single cell formula' format.  It is important that token operand
	 * classes are preserved during this transformation, because Excel may not tolerate the
	 * incorrect encoding.  The formula here is one such example (Excel displays #VALUE!).
	 */
	public void testConvertSharedFormulasOperandClasses_bug45123() {
		
		RecordInputStream in = TestcaseRecordInputStream.createWithFakeSid(SHARED_FORMULA_WITH_REF_ARRAYS_DATA);
		int encodedLen = in.readUShort();
		Ptg[] sharedFormula = Ptg.readTokens(encodedLen, in);
		
		Ptg[] convertedFormula = SharedFormulaRecord.convertSharedFormulas(sharedFormula, 100, 200);
		
		RefPtg refPtg = (RefPtg) convertedFormula[1];
		assertEquals("$C101", refPtg.toFormulaString());
		if (refPtg.getPtgClass() == Ptg.CLASS_REF) {
			throw new AssertionFailedError("Identified bug 45123");
		}
		
		confirmOperandClasses(sharedFormula, convertedFormula);
	}

	private static void confirmOperandClasses(Ptg[] originalPtgs, Ptg[] convertedPtgs) {
		assertEquals(originalPtgs.length, convertedPtgs.length);
		for (int i = 0; i < convertedPtgs.length; i++) {
			Ptg originalPtg = originalPtgs[i];
			Ptg convertedPtg = convertedPtgs[i];
			if (originalPtg.getPtgClass() != convertedPtg.getPtgClass()) {
				throw new ComparisonFailure("Different operand class for token[" + i + "]",
						String.valueOf(originalPtg.getPtgClass()), String.valueOf(convertedPtg.getPtgClass()));
			}
		}
	}
}
