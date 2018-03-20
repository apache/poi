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

package org.apache.poi.xssf.usermodel;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.formula.WorkbookEvaluatorProvider;
import org.apache.poi.ss.usermodel.BaseTestDataFormat;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Test;

/**
 * Tests for {@link XSSFDataFormat}
 */
public final class TestXSSFDataFormat extends BaseTestDataFormat {

	public TestXSSFDataFormat() {
		super(XSSFITestDataProvider.instance);
	}

    /**
     * [Bug 49928] formatCellValue returns incorrect value for \u00a3 formatted cells
     */
	@Override
    @Test
    public void test49928() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("49928.xlsx");
        doTest49928Core(wb);
        
        DataFormat dataFormat = wb.createDataFormat();

        // As of 2015-12-27, there is no way to override a built-in number format with POI XSSFWorkbook
        // 49928.xlsx has been saved with a poundFmt that overrides the default value (dollar)
        short poundFmtIdx = wb.getSheetAt(0).getRow(0).getCell(0).getCellStyle().getDataFormat();
        assertEquals(poundFmtIdx, dataFormat.getFormat(poundFmt));

        // now create a custom format with Pound (\u00a3)
        
        String customFmt = "\u00a3##.00[Yellow]";
        assertNotBuiltInFormat(customFmt);
        short customFmtIdx = dataFormat.getFormat(customFmt);
        assertTrue(customFmtIdx >= BuiltinFormats.FIRST_USER_DEFINED_FORMAT_INDEX);
        assertEquals(customFmt, dataFormat.getFormat(customFmtIdx));
        
        wb.close();
    }
    
    /**
     * [Bug 58532] Handle formats that go numnum, numK, numM etc 
     */
	@Override
    @Test
    public void test58532() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("FormatKM.xlsx");
        doTest58532Core(wb);
        wb.close();
    }
    
    /**
     * [Bug 58778] Built-in number formats can be overridden with XSSFDataFormat.putFormat(int id, String fmt)
     */
	@Test
    public void test58778() throws IOException {
        XSSFWorkbook wb1 = new XSSFWorkbook();
        Cell cell = wb1.createSheet("bug58778").createRow(0).createCell(0);
        cell.setCellValue(5.25);
        CellStyle style = wb1.createCellStyle();
        
        XSSFDataFormat dataFormat = wb1.createDataFormat();
        
        short poundFmtIdx = 6;
        dataFormat.putFormat(poundFmtIdx, poundFmt);
        style.setDataFormat(poundFmtIdx);
        cell.setCellStyle(style);
        // Cell should appear as "<poundsymbol>5"
        
        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutCloseAndReadBack(wb1);
        cell = wb2.getSheet("bug58778").getRow(0).getCell(0);
        assertEquals(5.25, cell.getNumericCellValue(), 0);
        
        style = cell.getCellStyle();
        assertEquals(poundFmt, style.getDataFormatString());
        assertEquals(poundFmtIdx, style.getDataFormat());
        
        // manually check the file to make sure the cell is rendered as "<poundsymbol>5"
        // Verified with LibreOffice 4.2.8.2 on 2015-12-28
        wb2.close();
        wb1.close();
    }

    @Test
    public void testConditionalFormattingEvaluation() throws IOException {
        final Workbook wb = XSSFTestDataSamples.openSampleWorkbook("61060-conditional-number-formatting.xlsx");

        final DataFormatter formatter = new DataFormatter();
        final FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        final ConditionalFormattingEvaluator cfEvaluator = new ConditionalFormattingEvaluator(wb, (WorkbookEvaluatorProvider) evaluator);

        CellReference ref = new CellReference("A1");
        Cell cell = wb.getSheetAt(0).getRow(ref.getRow()).getCell(ref.getCol());
        assertEquals("0.10", formatter.formatCellValue(cell, evaluator, cfEvaluator));
        // verify cell format without the conditional rule applied
        assertEquals("0.1", formatter.formatCellValue(cell, evaluator));

        ref = new CellReference("A3");
        cell = wb.getSheetAt(0).getRow(ref.getRow()).getCell(ref.getCol());
        assertEquals("-2.00E+03", formatter.formatCellValue(cell, evaluator, cfEvaluator));
        // verify cell format without the conditional rule applied
        assertEquals("-2000", formatter.formatCellValue(cell, evaluator));
        
        ref = new CellReference("A4");
        cell = wb.getSheetAt(0).getRow(ref.getRow()).getCell(ref.getCol());
        assertEquals("100", formatter.formatCellValue(cell, evaluator, cfEvaluator));
        
        ref = new CellReference("A5");
        cell = wb.getSheetAt(0).getRow(ref.getRow()).getCell(ref.getCol());
        assertEquals("$1,000", formatter.formatCellValue(cell, evaluator, cfEvaluator));
        // verify cell format without the conditional rule applied
        assertEquals("1000", formatter.formatCellValue(cell, evaluator));
        
        wb.close();
    }
}
