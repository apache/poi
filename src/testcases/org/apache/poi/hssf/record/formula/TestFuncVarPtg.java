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

import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
/**
 * @author Josh Micich
 */
public final class TestFuncVarPtg extends TestCase {

	/**
	 * The first fix for bugzilla 44675 broke the encoding of SUM formulas (and probably others).
	 * The operand classes of the parameters to SUM() should be coerced to 'reference' not 'value'.
	 * In the case of SUM, Excel evaluates the formula to '#VALUE!' if a parameter operand class is
	 * wrong.  In other cases Excel seems to tolerate bad operand classes.</p>
	 * This functionality is related to the setParameterRVA() methods of <tt>FormulaParser</tt>
	 */
	public void testOperandClass() {
		HSSFWorkbook book = new HSSFWorkbook();
		Ptg[] ptgs = HSSFFormulaParser.parse("sum(A1:A2)", book);
		assertEquals(2, ptgs.length);
		assertEquals(AreaPtg.class, ptgs[0].getClass());
		
		switch(ptgs[0].getPtgClass()) {
			case Ptg.CLASS_REF:
				// correct behaviour
				break;
			case Ptg.CLASS_VALUE:
				throw new AssertionFailedError("Identified bug 44675b");
			default:
				throw new RuntimeException("Unexpected operand class");
		}
	}
}
