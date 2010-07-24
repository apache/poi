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

package org.apache.poi.hssf.usermodel;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.RecordFormatException;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;

/**
 * @author aviks
 * 
 * This testcase contains tests for bugs that are yet to be fixed. Therefore,
 * the standard ant test target does not run these tests. Run this testcase with
 * the single-test target. The names of the tests usually correspond to the
 * Bugzilla id's PLEASE MOVE tests from this class to TestBugs once the bugs are
 * fixed, so that they are then run automatically.
 */
public final class TestUnfixedBugs extends TestCase {

	public void test43493() {
		// Has crazy corrupt sub-records on
		// a EmbeddedObjectRefSubRecord
		try {
			HSSFTestDataSamples.openSampleWorkbook("43493.xls");
		} catch (RecordFormatException e) {
			if (e.getCause().getCause() instanceof ArrayIndexOutOfBoundsException) {
				throw new AssertionFailedError("Identified bug 43493");
			}
			throw e;
		}
	}

    public void test49612() throws IOException {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("49612.xls");
        HSSFSheet sh = wb.getSheetAt(0);
        HSSFRow row = sh.getRow(0);
        HSSFCell c1 = row.getCell(2);
        HSSFCell d1 = row.getCell(3);
        HSSFCell e1 = row.getCell(2);

        assertEquals("SUM(BOB+JIM)", c1.getCellFormula());

        // Problem 1: java.lang.ArrayIndexOutOfBoundsException in org.apache.poi.hssf.model.LinkTable$ExternalBookBlock.getNameText
        assertEquals("SUM('49612.xls'!BOB+'49612.xls'!JIM)", d1.getCellFormula());

        //Problem 2
        //junit.framework.ComparisonFailure:
        //Expected :SUM('49612.xls'!BOB+'49612.xls'!JIM)
        //Actual   :SUM(BOB+JIM)
        assertEquals("SUM('49612.xls'!BOB+'49612.xls'!JIM)", e1.getCellFormula());

        HSSFFormulaEvaluator eval = new HSSFFormulaEvaluator(wb);
        assertEquals("evaluating c1", 30., eval.evaluate(c1).getNumberValue());

        //Problem 3:  java.lang.RuntimeException: Unexpected arg eval type (org.apache.poi.hssf.record.formula.eval.NameXEval)
        assertEquals("evaluating d1", 30., eval.evaluate(d1).getNumberValue());

        assertEquals("evaluating e1", 30., eval.evaluate(e1).getNumberValue());
    }
    
}
