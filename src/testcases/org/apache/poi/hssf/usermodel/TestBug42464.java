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

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.ss.usermodel.FormulaEvaluator.CellValue;
import org.apache.poi.hssf.util.CellReference;

/**
 * 
 */
public final class TestBug42464 extends TestCase {

	public void testOKFile() throws Exception {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("42464-ExpPtg-ok.xls");
		process(wb);
	}
	public void testExpSharedBadFile() throws Exception {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("42464-ExpPtg-bad.xls");
		process(wb);
	}
	
	private static void process(HSSFWorkbook wb) {
		for(int i=0; i<wb.getNumberOfSheets(); i++) {
			HSSFSheet s = wb.getSheetAt(i);
			HSSFFormulaEvaluator eval =
				new HSSFFormulaEvaluator(s, wb);
			
			Iterator it = s.rowIterator();
			while(it.hasNext()) {
				HSSFRow r = (HSSFRow)it.next();
				process(r, eval);
			}
		}
	}
	
	private static void process(HSSFRow row, HSSFFormulaEvaluator eval) {
		Iterator it = row.cellIterator();
		while(it.hasNext()) {
			HSSFCell cell = (HSSFCell)it.next();
			if(cell.getCellType() != HSSFCell.CELL_TYPE_FORMULA) {
			    continue;
			}
			FormulaRecordAggregate record = (FormulaRecordAggregate) cell.getCellValueRecord();
			FormulaRecord r = record.getFormulaRecord();
			List ptgs = r.getParsedExpression();
			
			String cellRef = new CellReference(row.getRowNum(), cell.getCellNum(), false, false).formatAsString();
			if(false && cellRef.equals("BP24")) { // TODO - replace System.out.println()s with asserts
				System.out.print(cellRef);
				System.out.println(" - has " + r.getNumberOfExpressionTokens() 
				        + " ptgs over " + r.getExpressionLength()  + " tokens:");
				for(int i=0; i<ptgs.size(); i++) {
					String c = ptgs.get(i).getClass().toString();
					System.out.println("\t" + c.substring(c.lastIndexOf('.')+1) );
				}
				System.out.println("-> " + cell.getCellFormula());
			}
			
			CellValue evalResult = eval.evaluate(cell);
			assertNotNull(evalResult);
		}
	}
}
