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

package org.apache.poi.hssf.record.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.ExpPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;

final class TestFormulaRecordAggregate {

	@Test
	void testBasic() {
		FormulaRecord f = new FormulaRecord();
		f.setCachedResultTypeString();
		StringRecord s = new StringRecord();
		s.setString("abc");
		FormulaRecordAggregate fagg = new FormulaRecordAggregate(f, s, SharedValueManager.createEmpty());
		assertEquals("abc", fagg.getStringValue());
		assertFalse(fagg.isPartOfArrayFormula());
	}

	/**
	 * Sometimes a {@link StringRecord} appears after a {@link FormulaRecord} even though the
	 * formula has evaluated to a text value.  This might be more likely to occur when the formula
	 * <i>can</i> evaluate to a text value.<br>
	 * Bug 46213 attachment 22874 has such an extra {@link StringRecord} at stream offset 0x5765.
	 * This file seems to open in Excel (2007) with no trouble.  When it is re-saved, Excel omits
	 * the extra record.  POI should do the same.
	 */
	@Test
	void testExtraStringRecord_bug46213() {
		FormulaRecord fr = new FormulaRecord();
		fr.setValue(2.0);
		StringRecord sr = new StringRecord();
		sr.setString("NA");
		SharedValueManager svm = SharedValueManager.createEmpty();
		// bug 46213 -> String record was  supplied but formula record flag is not  set
		FormulaRecordAggregate fra = new FormulaRecordAggregate(fr, sr, svm);
		List<org.apache.poi.hssf.record.Record> vraRecs = new ArrayList<>();
		fra.visitContainedRecords(vraRecs::add);
		assertEquals(1, vraRecs.size());
		assertEquals(fr, vraRecs.get(0));
	}

	@Test
	void testArrayFormulas() {
		int rownum = 4;
		int colnum = 4;

		FormulaRecord fr = new FormulaRecord();
		fr.setRow(rownum);
		fr.setColumn((short)colnum);

		FormulaRecordAggregate agg = new FormulaRecordAggregate(fr, null, SharedValueManager.createEmpty());
		Ptg[] ptgsForCell = {new ExpPtg(rownum, colnum)};
		agg.setParsedExpression(ptgsForCell);

		String formula = "SUM(A1:A3*B1:B3)";
		Ptg[] ptgs = HSSFFormulaParser.parse(formula, null, FormulaType.ARRAY, 0);
		agg.setArrayFormula(new CellRangeAddress(rownum, rownum, colnum, colnum), ptgs);

		assertTrue(agg.isPartOfArrayFormula());
		assertEquals("E5", agg.getArrayFormulaRange().formatAsString());
		Ptg[] ptg = agg.getFormulaTokens();
		String fmlaSer = FormulaRenderer.toFormulaString(null, ptg);
		assertEquals(formula, fmlaSer);

        agg.removeArrayFormula(rownum, colnum);
        assertFalse(agg.isPartOfArrayFormula());
    }
}
