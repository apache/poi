/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xssf.streaming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.namespace.QName;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.BaseTestXCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlCursor;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests various functionality having to do with {@link SXSSFCell}.  For instance support for
 * particular datatypes, etc.
 */
public class TestSXSSFCell extends BaseTestXCell {

    public TestSXSSFCell() {
        super(SXSSFITestDataProvider.instance);
    }

    @AfterClass
    public static void tearDownClass() {
        SXSSFITestDataProvider.instance.cleanup();
    }

    @Test
    public void testPreserveSpaces() throws IOException {
        String[] samplesWithSpaces = {
                " POI",
                "POI ",
                " POI ",
                "\nPOI",
                "\n\nPOI \n",
        };
        for (String str : samplesWithSpaces) {
            Workbook swb = _testDataProvider.createWorkbook();
            Cell sCell = swb.createSheet().createRow(0).createCell(0);
            sCell.setCellValue(str);
            assertEquals(sCell.getStringCellValue(), str);

            // read back as XSSF and check that xml:spaces="preserve" is set
            XSSFWorkbook xwb = (XSSFWorkbook) _testDataProvider.writeOutAndReadBack(swb);
            XSSFCell xCell = xwb.getSheetAt(0).getRow(0).getCell(0);

            CTRst is = xCell.getCTCell().getIs();
            assertNotNull(is);
            XmlCursor c = is.newCursor();
            c.toNextToken();
            String t = c.getAttributeText(new QName("http://www.w3.org/XML/1998/namespace", "space"));
            c.dispose();
            assertEquals("expected xml:spaces=\"preserve\" \"" + str + "\"", "preserve", t);
            xwb.close();
            swb.close();
        }
    }

    @Test
    public void getCellTypeEnumDelegatesToGetCellType() {
        SXSSFCell instance = spy(new SXSSFCell(null, CellType.BLANK));
        CellType result = instance.getCellTypeEnum();
        verify(instance).getCellType();
        assertEquals(CellType.BLANK, result);
    }

    @Test
    public void getCachedFormulaResultTypeEnum_delegatesTo_getCachedFormulaResultType() {
        SXSSFCell instance = spy(new SXSSFCell(null, CellType.BLANK));
        instance.setCellFormula("");
        instance.getCachedFormulaResultTypeEnum();
        verify(instance).getCachedFormulaResultType();
    }

    @Test(expected = IllegalStateException.class)
    public void getCachedFormulaResultType_throwsISE_whenNotAFormulaCell() {
        SXSSFCell instance = new SXSSFCell(null, CellType.BLANK);
        instance.getCachedFormulaResultType();
    }


    @Test(expected = IllegalArgumentException.class)
    public void setCellValue_withTooLongRichTextString_throwsIAE() {
        Cell cell = spy(new SXSSFCell(null, CellType.BLANK));
        int length = SpreadsheetVersion.EXCEL2007.getMaxTextLength() + 1;
        String string = new String(new byte[length], StandardCharsets.UTF_8).replace("\0", "x");
        RichTextString richTextString = new XSSFRichTextString(string);
        cell.setCellValue(richTextString);
    }

    @Test
    public void getArrayFormulaRange_returnsNull() {
        Cell cell = new SXSSFCell(null, CellType.BLANK);
        CellRangeAddress result = cell.getArrayFormulaRange();
        assertNull(result);
    }

    @Test
    public void isPartOfArrayFormulaGroup_returnsFalse() {
        Cell cell = new SXSSFCell(null, CellType.BLANK);
        boolean result = cell.isPartOfArrayFormulaGroup();
        assertFalse(result);
    }

    @Test
    public void getErrorCellValue_returns0_onABlankCell() {
        Cell cell = new SXSSFCell(null, CellType.BLANK);
        assertEquals(CellType.BLANK, cell.getCellType());
        byte result = cell.getErrorCellValue();
        assertEquals(0, result);
    }

    /**
     * For now, {@link SXSSFCell} doesn't support array formulas.
     * However, this test should be enabled if array formulas are implemented for SXSSF.
     */
    @Override
    @Ignore
    public void setBlank_removesArrayFormula_ifCellIsPartOfAnArrayFormulaGroupContainingOnlyThisCell() {
    }

    /**
     * For now, {@link SXSSFCell} doesn't support array formulas.
     * However, this test should be enabled if array formulas are implemented for SXSSF.
     */
    @Override
    @Ignore
    @Test // <- annotation is necessary to override expected exception
    public void setBlank_throwsISE_ifCellIsPartOfAnArrayFormulaGroupContainingOtherCells() {
    }

    @Override
    @Ignore
    @Test
    public void setCellFormula_throwsISE_ifCellIsPartOfAnArrayFormulaGroupContainingOtherCells() {
    }

    @Override
    @Ignore
    public void removeFormula_turnsCellToBlank_whenFormulaWasASingleCellArrayFormula() {
    }

    @Override
    @Ignore
    public void setCellFormula_onASingleCellArrayFormulaCell_preservesTheValue() {
    }

    @Test
    @Ignore
    public void setCellFormula_isExceptionSafe_onBlankCell() {
    }

    @Test
    @Ignore
    public void setCellType_FORMULA_onAnArrayFormulaCell_doesNothing() {
    }
}
