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

package org.apache.poi.hssf.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.FormulaExtractor;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.formula.ptg.AttrPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.CellType;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests 'operand class' transformation performed by
 * <tt>OperandClassTransformer</tt> by comparing its results with those
 * directly produced by Excel (in a sample spreadsheet).
 */
@RunWith(Parameterized.class)
public final class TestRVA {

	private static final String NEW_LINE = System.getProperty("line.separator");
	private static POIFSFileSystem poifs;
    private static HSSFWorkbook workbook;


	@Parameter(value = 0)
    public HSSFCell formulaCell;
    @Parameter(value = 1)
    public String formula;

    @AfterClass
    public static void closeResource() throws Exception {
        workbook.close();
        poifs.close();
    }

    @Parameters(name="{1}")
    public static Collection<Object[]> data() throws Exception {
        poifs = new POIFSFileSystem(HSSFTestDataSamples.getSampleFile("testRVA.xls"), true);
        workbook = new HSSFWorkbook(poifs);
		HSSFSheet sheet = workbook.getSheetAt(0);

        List<Object[]> data = new ArrayList<>();
        
        for (int rowIdx = 0; true; rowIdx++) {
            HSSFRow row = sheet.getRow(rowIdx);
            if (row == null) {
                break;
            }
            HSSFCell cell = row.getCell(0);
            if (cell == null || cell.getCellType() == CellType.BLANK) {
                break;
            }

            String formula = cell.getCellFormula();
            data.add(new Object[]{cell,formula});
        }
        
        return data;
    }
	
    @Test
	public void confirmCell() {
		Ptg[] excelPtgs = FormulaExtractor.getPtgs(formulaCell);
		Ptg[] poiPtgs = HSSFFormulaParser.parse(formula, workbook);
		int nExcelTokens = excelPtgs.length;
		int nPoiTokens = poiPtgs.length;
		if (nExcelTokens != nPoiTokens) {
			if (nExcelTokens == nPoiTokens + 1 && excelPtgs[0].getClass() == AttrPtg.class) {
				// compensate for missing tAttrVolatile, which belongs in any formula 
				// involving OFFSET() et al. POI currently does not insert where required
				Ptg[] temp = new Ptg[nExcelTokens];
				temp[0] = excelPtgs[0];
				System.arraycopy(poiPtgs, 0, temp, 1, nPoiTokens);
				poiPtgs = temp;
			} else {
				fail("Expected " + nExcelTokens + " tokens but got " + nPoiTokens);
			}
		}
		boolean hasMismatch = false;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nExcelTokens; i++) {
			Ptg poiPtg = poiPtgs[i];
			Ptg excelPtg = excelPtgs[i];
			if (excelPtg.getClass() != poiPtg.getClass()) {
				hasMismatch = true;
				sb.append("  mismatch token type[").append(i).append("] ").append(getShortClassName(excelPtg)).append(" ").append(excelPtg.getRVAType()).append(" - ").append(getShortClassName(poiPtg)).append(" ").append(poiPtg.getRVAType());
				sb.append(NEW_LINE);
				continue;
			}
			if (poiPtg.isBaseToken()) {
				continue;
			}
			sb.append("  token[").append(i).append("] ").append(excelPtg).append(" ").append(excelPtg.getRVAType());

			if (excelPtg.getPtgClass() != poiPtg.getPtgClass()) {
				hasMismatch = true;
				sb.append(" - was ").append(poiPtg.getRVAType());
			}
			sb.append(NEW_LINE);
		}
		assertFalse(hasMismatch);
	}

	private String getShortClassName(Object o) {
		String cn = o.getClass().getName();
		int pos = cn.lastIndexOf('.');
		return cn.substring(pos + 1);
	}
}
