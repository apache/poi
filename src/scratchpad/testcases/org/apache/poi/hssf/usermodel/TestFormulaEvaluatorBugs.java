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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;

import junit.framework.TestCase;

public final class TestFormulaEvaluatorBugs extends TestCase {
	private String dirName;
	private String tmpDirName;

	protected void setUp() throws Exception {
		super.setUp();
		dirName = System.getProperty("HSSF.testdata.path");
		tmpDirName = System.getProperty("java.io.tmpdir");
	}
	
	/**
	 * An odd problem with evaluateFormulaCell giving the
	 *  right values when file is opened, but changes
	 *  to the source data in some versions of excel 
	 *  doesn't cause them to be updated. However, other
	 *  versions of excel, and gnumeric, work just fine
	 * WARNING - tedious bug where you actually have to
	 *  open up excel
	 */
	public void test44636() throws Exception {
		// Open the existing file, tweak one value and
		//  re-calculate
		FileInputStream in = new FileInputStream(new File(dirName,"44636.xls"));
		HSSFWorkbook wb = new HSSFWorkbook(in);
		HSSFSheet sheet = wb.getSheetAt (0);
		HSSFRow row = sheet.getRow (0);
		
		row.getCell((short)0).setCellValue(4.2);
		row.getCell((short)2).setCellValue(25);
		
		HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
		assertEquals(4.2*25, row.getCell((short)3).getNumericCellValue(), 0.0001);
		
		// Save
		File existing = new File(tmpDirName,"44636-existing.xls");
		FileOutputStream out = new FileOutputStream(existing);
		wb.write(out);
		out.close();
		System.err.println("Existing file for bug #44636 written to " + existing.toString());
		
		
		// Now, do a new file from scratch
		wb = new HSSFWorkbook();
		sheet = wb.createSheet();
		
		row = sheet.createRow(0);
		row.createCell((short)0).setCellValue(1.2);
		row.createCell((short)1).setCellValue(4.2);
		
		row = sheet.createRow(1);
		row.createCell((short)0).setCellFormula("SUM(A1:B1)");
		
		HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
		assertEquals(5.4, row.getCell((short)0).getNumericCellValue(), 0.0001);
				
		// Save
		File scratch = new File(tmpDirName,"44636-scratch.xls");
		out = new FileOutputStream(scratch);
		wb.write(out);
		out.close();
		System.err.println("New file for bug #44636 written to " + scratch.toString());
	}
}
