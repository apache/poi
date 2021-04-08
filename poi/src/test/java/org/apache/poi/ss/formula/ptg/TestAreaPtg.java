
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.AreaReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AreaPtg}.
 */
final class TestAreaPtg {

	AreaPtg relative;
	AreaPtg absolute;

	@BeforeEach
	void setUp() {
		short firstRow=5;
		short lastRow=13;
		short firstCol=7;
		short lastCol=17;
		relative = new AreaPtg(firstRow,lastRow,firstCol,lastCol,true,true,true,true);
		absolute = new AreaPtg(firstRow,lastRow,firstCol,lastCol,false,false,false,false);
	}

	@Test
	void testSortTopLeftToBottomRight() {
	    AreaPtg ptg = new AreaPtg(new AreaReference("A$1:$B5", SpreadsheetVersion.EXCEL2007));
	    assertEquals("A$1:$B5", ptg.toFormulaString());
	    ptg.setFirstColumn(3);
	    assertEquals("D$1:$B5", ptg.toFormulaString(),
			"Area Ptg should not implicitly re-sort itself (except during construction)");
	    ptg.sortTopLeftToBottomRight();
	    assertEquals("$B$1:D5", ptg.toFormulaString(),
			"Area Ptg should restore itself to top-left to lower-right order when explicitly asked");
	}

	@Test
	void testSetColumnsAbsolute() {
		resetColumns(absolute);
		validateReference(true, absolute);
	}

	@Test
	void testSetColumnsRelative() {
		resetColumns(relative);
		validateReference(false, relative);
	}

	private void validateReference(boolean abs, AreaPtg ref) {
		String isWrong = " reference is not "+(abs?"absolute":"relative");
		assertEquals(abs,!ref.isFirstColRelative(),"First column" + isWrong);
		assertEquals(abs,!ref.isLastColRelative(), "Last column" + isWrong);
		assertEquals(abs,!ref.isFirstRowRelative(),"First row" + isWrong);
		assertEquals(abs,!ref.isLastRowRelative(), "Last row" + isWrong);
	}


	private static void resetColumns(AreaPtg aptg) {
		int fc = aptg.getFirstColumn();
		int lc = aptg.getLastColumn();
		aptg.setFirstColumn(fc);
		aptg.setLastColumn(lc);
		assertEquals(fc , aptg.getFirstColumn() );
		assertEquals(lc , aptg.getLastColumn() );
	}

	@Test
    void testAbsoluteRelativeRefs() {
        AreaPtg sca1 = new AreaPtg(4, 5, 6, 7, true, false, true, false);
        AreaPtg sca2 = new AreaPtg(4, 5, 6, 7, false, true, false, true);
        AreaPtg sca3 = new AreaPtg(5, 5, 7, 7, true, false, true, false);
        AreaPtg sca4 = new AreaPtg(5, 5, 7, 7, false, true, false, true);

        assertEquals("G5:$H$6", sca1.toFormulaString(), "first rel., last abs.");
        assertEquals("$G$5:H6", sca2.toFormulaString(), "first abs., last rel.");
        assertEquals("H6:$H$6", sca3.toFormulaString(), "first rel., last abs.");
        assertEquals("$H$6:H6", sca4.toFormulaString(), "first abs., last rel.");

        AreaPtg cla1 = cloneArea(sca1);
        AreaPtg cla2 = cloneArea(sca2);
        AreaPtg cla3 = cloneArea(sca3);
        AreaPtg cla4 = cloneArea(sca4);

        assertEquals("G5:$H$6", cla1.toFormulaString(), "first rel., last abs.");
        assertEquals("$G$5:H6", cla2.toFormulaString(), "first abs., last rel.");
        assertEquals("H6:$H$6", cla3.toFormulaString(), "first rel., last abs.");
        assertEquals("$H$6:H6", cla4.toFormulaString(), "first abs., last rel.");
    }

    private AreaPtg cloneArea(AreaPtg a) {
        return new AreaPtg(
                a.getFirstRow(), a.getLastRow(), a.getFirstColumn(), a.getLastColumn(),
                a.isFirstRowRelative(), a.isLastRowRelative(), a.isFirstColRelative(), a.isLastColRelative()
        );
    }

	@Test
	void testFormulaParser() {
		String formula1="SUM($E$5:$E$6)";
		String expectedFormula1="SUM($F$5:$F$6)";
		String newFormula1 = shiftAllColumnsBy1(formula1);
		assertEquals(expectedFormula1, newFormula1, "Absolute references changed");

		String formula2="SUM(E5:E6)";
		String expectedFormula2="SUM(F5:F6)";
		String newFormula2 = shiftAllColumnsBy1(formula2);
		assertEquals(expectedFormula2, newFormula2, "Relative references changed");
	}

	private static String shiftAllColumnsBy1(String  formula) {
		int letUsShiftColumn1By1Column=1;
		Ptg[] ptgs = HSSFFormulaParser.parse(formula, null);
		for (Ptg ptg : ptgs) {
			if (ptg instanceof AreaPtg )
			{
				AreaPtg aptg = (AreaPtg)ptg;
				aptg.setFirstColumn((short)(aptg.getFirstColumn()+letUsShiftColumn1By1Column));
				aptg.setLastColumn((short)(aptg.getLastColumn()+letUsShiftColumn1By1Column));
			}
		}
        return HSSFFormulaParser.toFormulaString(null, ptgs);
	}
}
