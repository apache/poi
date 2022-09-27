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

import static java.time.Duration.between;
import static java.time.Instant.now;
import static org.apache.commons.io.output.NullOutputStream.NULL_OUTPUT_STREAM;
import static org.apache.logging.log4j.util.Unbox.box;
import static org.apache.poi.extractor.ExtractorFactory.OOXML_PACKAGE;
import static org.apache.poi.openxml4j.opc.TestContentType.isOldXercesActive;
import static org.apache.poi.ss.util.Utils.addRow;
import static org.apache.poi.ss.util.Utils.assertDouble;
import static org.apache.poi.xssf.XSSFTestDataSamples.openSampleWorkbook;
import static org.apache.poi.xssf.XSSFTestDataSamples.writeOutAndReadBack;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.POIDataSamples;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLDocumentPart.RelationPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.sl.usermodel.ObjectShape;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.formula.EvaluationConditionalFormatRule;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.formula.WorkbookEvaluatorProvider;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.Function;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.*;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.TempFile;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.XLSBUnsupportedException;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.CalculationChain;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCalcCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDefinedName;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDefinedNames;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTMergeCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTMergeCells;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellFormulaType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTFontImpl;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public final class TestXSSFBugs extends BaseTestBugzillaIssues {
    private static final Logger LOG = LogManager.getLogger(TestXSSFBugs.class);

    public TestXSSFBugs() {
        super(XSSFITestDataProvider.instance);
    }

    /**
     * Named ranges had the right reference, but
     * the wrong sheet name
     */
    @Test
    void bug45430() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("45430.xlsx")) {
            assertFalse(wb.isMacroEnabled());
            assertEquals(3, wb.getNumberOfNames());

            assertEquals(0, wb.getName("SheetAA1").getCTName().getLocalSheetId());
            assertFalse(wb.getName("SheetAA1").getCTName().isSetLocalSheetId());
            assertEquals("SheetA!$A$1", wb.getName("SheetAA1").getRefersToFormula());
            assertEquals("SheetA", wb.getName("SheetAA1").getSheetName());

            assertEquals(0, wb.getName("SheetBA1").getCTName().getLocalSheetId());
            assertFalse(wb.getName("SheetBA1").getCTName().isSetLocalSheetId());
            assertEquals("SheetB!$A$1", wb.getName("SheetBA1").getRefersToFormula());
            assertEquals("SheetB", wb.getName("SheetBA1").getSheetName());

            assertEquals(0, wb.getName("SheetCA1").getCTName().getLocalSheetId());
            assertFalse(wb.getName("SheetCA1").getCTName().isSetLocalSheetId());
            assertEquals("SheetC!$A$1", wb.getName("SheetCA1").getRefersToFormula());
            assertEquals("SheetC", wb.getName("SheetCA1").getSheetName());

            // Save and re-load, still there
            try (XSSFWorkbook nwb = writeOutAndReadBack(wb)) {
                assertEquals(3, nwb.getNumberOfNames());
                assertEquals("SheetA!$A$1", nwb.getName("SheetAA1").getRefersToFormula());
            }
        }
    }

    /**
     * We should carry vba macros over after save
     */
    @Test
    void bug45431() throws IOException, InvalidFormatException {
        try (XSSFWorkbook wb1 = openSampleWorkbook("45431.xlsm");
             OPCPackage pkg1 = wb1.getPackage()) {
            assertTrue(wb1.isMacroEnabled());

            // Check the various macro related bits can be found
            PackagePart vba = pkg1.getPart(
                PackagingURIHelper.createPartName("/xl/vbaProject.bin")
            );
            assertNotNull(vba);
            // And the drawing bit
            PackagePart drw = pkg1.getPart(
                PackagingURIHelper.createPartName("/xl/drawings/vmlDrawing1.vml")
            );
            assertNotNull(drw);


            // Save and re-open, both still there
            try (XSSFWorkbook wb2 = writeOutAndReadBack(wb1);
                 OPCPackage pkg2 = wb2.getPackage()) {
                assertTrue(wb2.isMacroEnabled());

                vba = pkg2.getPart(
                    PackagingURIHelper.createPartName("/xl/vbaProject.bin")
                );
                assertNotNull(vba);
                drw = pkg2.getPart(
                    PackagingURIHelper.createPartName("/xl/drawings/vmlDrawing1.vml")
                );
                assertNotNull(drw);

                // And again, just to be sure
                try (XSSFWorkbook wb3 = writeOutAndReadBack(wb2);
                     OPCPackage pkg3 = wb3.getPackage()) {
                    assertTrue(wb3.isMacroEnabled());

                    vba = pkg3.getPart(
                        PackagingURIHelper.createPartName("/xl/vbaProject.bin")
                    );
                    assertNotNull(vba);
                    drw = pkg3.getPart(
                        PackagingURIHelper.createPartName("/xl/drawings/vmlDrawing1.vml")
                    );
                    assertNotNull(drw);
                }
            }
        }
    }

    @Test
    void bug47504() throws IOException {
        try (XSSFWorkbook wb1 = openSampleWorkbook("47504.xlsx")) {
            assertEquals(1, wb1.getNumberOfSheets());
            XSSFSheet sh = wb1.getSheetAt(0);
            XSSFDrawing drawing = sh.createDrawingPatriarch();
            List<RelationPart> rels = drawing.getRelationParts();
            assertEquals(1, rels.size());
            assertEquals("Sheet1!A1", rels.get(0).getRelationship().getTargetURI().getFragment());

            // And again, just to be sure
            try (XSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                assertEquals(1, wb2.getNumberOfSheets());
                sh = wb2.getSheetAt(0);
                drawing = sh.createDrawingPatriarch();
                rels = drawing.getRelationParts();
                assertEquals(1, rels.size());
                assertEquals("Sheet1!A1", rels.get(0).getRelationship().getTargetURI().getFragment());
            }
        }
    }

    /**
     * Excel will sometimes write a button with a textbox
     * containing &gt;br&lt; (not closed!).
     * Clearly Excel shouldn't do this, but test that we can
     * read the file despite the naughtiness
     */
    @Test
    void bug49020() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("BrNotClosed.xlsx")) {
            assertNotNull(wb);
        }
    }

    /**
     * ensure that CTPhoneticPr is loaded by the ooxml test suite so that it is included in poi-ooxml-lite
     */
    @Test
    void bug49325() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("49325.xlsx")) {
            CTWorksheet sh = wb.getSheetAt(0).getCTWorksheet();
            assertNotNull(sh.getPhoneticPr());
        }
    }

    /**
     * Names which are defined with a Sheet
     * should return that sheet index properly
     */
    @Test
    void bug48923() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("48923.xlsx")) {
            assertEquals(4, wb.getNumberOfNames());

            Name b1 = wb.getName("NameB1");
            Name b2 = wb.getName("NameB2");
            Name sheet2 = wb.getName("NameSheet2");
            Name test = wb.getName("Test");

            assertNotNull(b1);
            assertEquals("NameB1", b1.getNameName());
            assertEquals("Sheet1", b1.getSheetName());
            assertEquals(-1, b1.getSheetIndex());
            assertFalse(b1.isDeleted());
            assertFalse(b1.isHidden());

            assertNotNull(b2);
            assertEquals("NameB2", b2.getNameName());
            assertEquals("Sheet1", b2.getSheetName());
            assertEquals(-1, b2.getSheetIndex());
            assertFalse(b2.isDeleted());
            assertFalse(b2.isHidden());

            assertNotNull(sheet2);
            assertEquals("NameSheet2", sheet2.getNameName());
            assertEquals("Sheet2", sheet2.getSheetName());
            assertEquals(-1, sheet2.getSheetIndex());

            assertNotNull(test);
            assertEquals("Test", test.getNameName());
            assertEquals("Sheet1", test.getSheetName());
            assertEquals(-1, test.getSheetIndex());
        }
    }

    /**
     * Problem with evaluation formulas due to
     * NameXPtgs.
     * Blows up on:
     * IF(B6= (ROUNDUP(B6,0) + ROUNDDOWN(B6,0))/2, MROUND(B6,2),ROUND(B6,0))
     * <p>
     * TODO: delete this test case when MROUND and VAR are implemented
     */
    @Test
    void bug48539() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("48539.xlsx")) {
            assertEquals(3, wb.getNumberOfSheets());
            assertEquals(0, wb.getNumberOfNames());

            // Try each cell individually
            XSSFFormulaEvaluator eval = new XSSFFormulaEvaluator(wb);
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet s = wb.getSheetAt(i);
                for (Row r : s) {
                    for (Cell c : r) {
                        if (c.getCellType() == CellType.FORMULA) {
                            CellValue cv = eval.evaluate(c);

                            if (cv.getCellType() == CellType.NUMERIC) {
                                // assert that the calculated value agrees with
                                // the cached formula result calculated by Excel
                                String formula = c.getCellFormula();
                                double cachedFormulaResult = c.getNumericCellValue();
                                double evaluatedFormulaResult = cv.getNumberValue();
                                assertEquals(cachedFormulaResult, evaluatedFormulaResult, 1E-7, formula);
                            }
                        }
                    }
                }
            }

            // Now all of them
            XSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
        }
    }

    /**
     * Foreground colours should be found even if
     * a theme is used
     */
    @Test
    void bug48779() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("48779.xlsx")) {
            XSSFCell cell = wb.getSheetAt(0).getRow(0).getCell(0);
            XSSFCellStyle cs = cell.getCellStyle();

            assertNotNull(cs);
            assertEquals(1, cs.getIndex());

            // Look at the low level xml elements
            assertEquals(2, cs.getCoreXf().getFillId());
            assertEquals(0, cs.getCoreXf().getXfId());
            assertTrue(cs.getCoreXf().getApplyFill());

            XSSFCellFill fg = wb.getStylesSource().getFillAt(2);
            assertNotNull(fg.getFillForegroundColor());
            assertEquals(0, fg.getFillForegroundColor().getIndexed());
            assertEquals(0.0, fg.getFillForegroundColor().getTint(), 0);
            assertEquals("FFFF0000", fg.getFillForegroundColor().getARGBHex());
            assertNotNull(fg.getFillBackgroundColor());
            assertEquals(64, fg.getFillBackgroundColor().getIndexed());

            // Now look higher up
            assertNotNull(cs.getFillForegroundXSSFColor());
            assertEquals(0, cs.getFillForegroundColor());
            assertEquals("FFFF0000", cs.getFillForegroundXSSFColor().getARGBHex());
            assertEquals("FFFF0000", cs.getFillForegroundColorColor().getARGBHex());

            assertEquals(64, cs.getFillBackgroundColor());
            assertNull(cs.getFillBackgroundXSSFColor().getARGBHex());
            assertNull(cs.getFillBackgroundColorColor().getARGBHex());
        }
    }

    /**
     * Ensure General and @ format are working properly
     * for integers
     */
    @Test
    void bug47490() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("GeneralFormatTests.xlsx")) {
            Sheet s = wb.getSheetAt(1);
            Row r;
            DataFormatter df = new DataFormatter();

            r = s.getRow(1);
            assertEquals(1.0, r.getCell(2).getNumericCellValue(), 0);
            assertEquals("General", r.getCell(2).getCellStyle().getDataFormatString());
            assertEquals("1", df.formatCellValue(r.getCell(2)));
            assertEquals("1", df.formatRawCellContents(1.0, -1, "@"));
            assertEquals("1", df.formatRawCellContents(1.0, -1, "General"));

            r = s.getRow(2);
            assertEquals(12.0, r.getCell(2).getNumericCellValue(), 0);
            assertEquals("General", r.getCell(2).getCellStyle().getDataFormatString());
            assertEquals("12", df.formatCellValue(r.getCell(2)));
            assertEquals("12", df.formatRawCellContents(12.0, -1, "@"));
            assertEquals("12", df.formatRawCellContents(12.0, -1, "General"));

            r = s.getRow(3);
            assertEquals(123.0, r.getCell(2).getNumericCellValue(), 0);
            assertEquals("General", r.getCell(2).getCellStyle().getDataFormatString());
            assertEquals("123", df.formatCellValue(r.getCell(2)));
            assertEquals("123", df.formatRawCellContents(123.0, -1, "@"));
            assertEquals("123", df.formatRawCellContents(123.0, -1, "General"));
        }
    }

    /**
     * A problem file from a non-standard source (a scientific instrument that saves its
     * output as an .xlsx file) that have two issues:
     * 1. The Content Type part name is lower-case:  [content_types].xml
     * 2. The file appears to use backslashes as path separators
     * <p>
     * The OPC spec tolerates both of these peculiarities, so does POI
     */
    @Test
    void bug49609() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("49609.xlsx")) {
            assertEquals("FAM", wb.getSheetName(0));
            assertEquals("Cycle", wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue());
        }

    }

    @Test
    void bug49783() throws IOException {
        try (Workbook wb = openSampleWorkbook("49783.xlsx")) {
            Sheet sheet = wb.getSheetAt(0);
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            Cell cell;

            cell = sheet.getRow(0).getCell(0);
            assertEquals("#REF!*#REF!", cell.getCellFormula());
            assertEquals(CellType.ERROR, evaluator.evaluateInCell(cell).getCellType());
            assertEquals("#REF!", FormulaError.forInt(cell.getErrorCellValue()).getString());

            Name nm1 = wb.getName("sale_1");
            assertNotNull(nm1, "name sale_1 should be present");
            assertEquals("Sheet1!#REF!", nm1.getRefersToFormula());
            Name nm2 = wb.getName("sale_2");
            assertNotNull(nm2, "name sale_2 should be present");
            assertEquals("Sheet1!#REF!", nm2.getRefersToFormula());

            cell = sheet.getRow(1).getCell(0);
            assertEquals("sale_1*sale_2", cell.getCellFormula());
            assertEquals(CellType.ERROR, evaluator.evaluateInCell(cell).getCellType());
            assertEquals("#REF!", FormulaError.forInt(cell.getErrorCellValue()).getString());
        }
    }

    /**
     * Creating a rich string of "hello world" and applying
     * a font to characters 1-5 means we have two strings,
     * "hello" and " world". As such, we need to apply
     * preserve spaces to the 2nd bit, lest we end up
     * with something like "helloworld" !
     */
    @Test
    void bug49941() throws IOException {
        try (XSSFWorkbook wb1 = new XSSFWorkbook()) {
            XSSFSheet s = wb1.createSheet();
            XSSFRow r = s.createRow(0);
            XSSFCell c = r.createCell(0);

            // First without fonts
            c.setCellValue(
                new XSSFRichTextString(" with spaces ")
            );
            assertEquals(" with spaces ", c.getRichStringCellValue().toString());
            assertEquals(0, c.getRichStringCellValue().getCTRst().sizeOfRArray());
            assertTrue(c.getRichStringCellValue().getCTRst().isSetT());
            // Should have the preserve set
            assertEquals(
                1,
                c.getRichStringCellValue().getCTRst().xgetT().getDomNode().getAttributes().getLength()
            );
            assertEquals(
                "preserve",
                c.getRichStringCellValue().getCTRst().xgetT().getDomNode().getAttributes().item(0).getNodeValue()
            );

            // Save and check
            try (XSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                s = wb2.getSheetAt(0);
                r = s.getRow(0);
                c = r.getCell(0);
                assertEquals(" with spaces ", c.getRichStringCellValue().toString());
                assertEquals(0, c.getRichStringCellValue().getCTRst().sizeOfRArray());
                assertTrue(c.getRichStringCellValue().getCTRst().isSetT());

                // Change the string
                c.setCellValue(
                    new XSSFRichTextString("hello world")
                );
                assertEquals("hello world", c.getRichStringCellValue().toString());
                // Won't have preserve
                assertEquals(
                    0,
                    c.getRichStringCellValue().getCTRst().xgetT().getDomNode().getAttributes().getLength()
                );

                // Apply a font
                XSSFFont f = wb2.createFont();
                f.setBold(true);
                c.getRichStringCellValue().applyFont(0, 5, f);
                assertEquals("hello world", c.getRichStringCellValue().toString());
                // Does need preserving on the 2nd part
                assertEquals(2, c.getRichStringCellValue().getCTRst().sizeOfRArray());
                assertEquals(
                    0,
                    c.getRichStringCellValue().getCTRst().getRArray(0).xgetT().getDomNode().getAttributes().getLength()
                );
                assertEquals(
                    1,
                    c.getRichStringCellValue().getCTRst().getRArray(1).xgetT().getDomNode().getAttributes().getLength()
                );
                assertEquals(
                    "preserve",
                    c.getRichStringCellValue().getCTRst().getRArray(1).xgetT().getDomNode().getAttributes().item(0).getNodeValue()
                );

                // Save and check
                try (XSSFWorkbook wb3 = writeOutAndReadBack(wb2)) {
                    s = wb3.getSheetAt(0);
                    r = s.getRow(0);
                    c = r.getCell(0);
                    assertEquals("hello world", c.getRichStringCellValue().toString());
                }
            }
        }
    }

    /**
     * Repeatedly writing the same file which has styles
     */
    @Test
    void bug49940() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("styles.xlsx")) {
            assertEquals(3, wb.getNumberOfSheets());
            assertEquals(10, wb.getStylesSource().getNumCellStyles());

            for (int i=0; i<3; i++) {
                try (XSSFWorkbook wb2 = writeOutAndReadBack(wb)) {
                    assertEquals(3, wb2.getNumberOfSheets());
                    assertEquals(10, wb2.getStylesSource().getNumCellStyles());
                }
            }
        }
    }

    /**
     * Various ways of removing a cell formula should all zap the calcChain
     * entry.
     */
    @Test
    void bug49966() throws IOException {
        try (XSSFWorkbook wb1 = openSampleWorkbook("shared_formulas.xlsx")) {
            XSSFSheet sheet = wb1.getSheetAt(0);

            writeOutAndReadBack(wb1).close();

            // CalcChain has lots of entries
            CalculationChain cc = wb1.getCalculationChain();
            assertEquals("A2", cc.getCTCalcChain().getCArray(0).getR());
            assertEquals("A3", cc.getCTCalcChain().getCArray(1).getR());
            assertEquals("A4", cc.getCTCalcChain().getCArray(2).getR());
            assertEquals("A5", cc.getCTCalcChain().getCArray(3).getR());
            assertEquals("A6", cc.getCTCalcChain().getCArray(4).getR());
            assertEquals("A7", cc.getCTCalcChain().getCArray(5).getR());
            assertEquals("A8", cc.getCTCalcChain().getCArray(6).getR());
            assertEquals(40, cc.getCTCalcChain().sizeOfCArray());

            writeOutAndReadBack(wb1).close();

            // Try various ways of changing the formulas
            // If it stays a formula, chain entry should remain
            // Otherwise should go
            sheet.getRow(1).getCell(0).setCellFormula("A1"); // stay
            sheet.getRow(2).getCell(0).setCellFormula(null); // go
            sheet.getRow(3).getCell(0).setCellFormula("14"); // stay
            writeOutAndReadBack(wb1).close();

            sheet.getRow(4).getCell(0).setBlank(); // go
            writeOutAndReadBack(wb1).close();

            validateCells(sheet);
            sheet.getRow(5).removeCell(sheet.getRow(5).getCell(0)); // go
            validateCells(sheet);
            writeOutAndReadBack(wb1).close();

            sheet.getRow(6).getCell(0).setBlank(); // go
            writeOutAndReadBack(wb1).close();

            sheet.getRow(7).getCell(0).setCellValue((String) null); // go

            writeOutAndReadBack(wb1).close();

            // Save and check
            try (XSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                wb1.close();
                assertEquals(35, cc.getCTCalcChain().sizeOfCArray());

                cc = wb2.getCalculationChain();
                assertEquals("A2", cc.getCTCalcChain().getCArray(0).getR());
                assertEquals("A4", cc.getCTCalcChain().getCArray(1).getR());
                assertEquals("A9", cc.getCTCalcChain().getCArray(2).getR());
            }
        }
    }

    @Test
    void bug49966Row() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("shared_formulas.xlsx")) {
            XSSFSheet sheet = wb.getSheetAt(0);

            validateCells(sheet);
            sheet.getRow(5).removeCell(sheet.getRow(5).getCell(0)); // go
            validateCells(sheet);
        }
    }

    private void validateCells(XSSFSheet sheet) {
        for (Row row : sheet) {
            // trigger handling
            ((XSSFRow) row).onDocumentWrite();
        }
    }

    @Test
    void bug49156() throws IOException {
        try (Workbook wb = openSampleWorkbook("49156.xlsx")) {
            FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();

            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.FORMULA) {
                        // caused NPE on some cells
                        assertDoesNotThrow(() -> formulaEvaluator.evaluateInCell(cell));
                    }
                }
            }
        }
    }

    /**
     * Newlines are valid characters in a formula
     */
    @Test
    void bug50440And51875() throws IOException {
        try (Workbook wb = openSampleWorkbook("NewlineInFormulas.xlsx")) {
            Sheet s = wb.getSheetAt(0);
            Cell c = s.getRow(0).getCell(0);

            assertEquals("SUM(\n1,2\n)", c.getCellFormula());
            assertEquals(3.0, c.getNumericCellValue(), 0);

            FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
            formulaEvaluator.evaluateFormulaCell(c);

            assertEquals("SUM(\n1,2\n)", c.getCellFormula());
            assertEquals(3.0, c.getNumericCellValue(), 0);

            // For 51875
            Cell b3 = s.getRow(2).getCell(1);
            formulaEvaluator.evaluateFormulaCell(b3);
            assertEquals("B1+B2", b3.getCellFormula()); // The newline is lost for shared formulas
            assertEquals(3.0, b3.getNumericCellValue(), 0);
        }
    }

    /**
     * Moving a cell comment from one cell to another
     */
    @Test
    void bug50795() throws IOException {
        try (XSSFWorkbook wb1 = openSampleWorkbook("50795.xlsx")) {
            XSSFSheet sheet = wb1.getSheetAt(0);
            XSSFRow row = sheet.getRow(0);

            XSSFCell cellWith = row.getCell(0);
            XSSFCell cellWithoutComment = row.getCell(1);

            assertNotNull(cellWith.getCellComment());
            assertNull(cellWithoutComment.getCellComment());

            String exp = "\u0410\u0432\u0442\u043e\u0440:\ncomment";
            XSSFComment comment = cellWith.getCellComment();
            assertEquals(exp, comment.getString().getString());


            // Check we can write it out and read it back as-is
            try (XSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                sheet = wb2.getSheetAt(0);
                row = sheet.getRow(0);
                cellWith = row.getCell(0);
                cellWithoutComment = row.getCell(1);

                // Double check things are as expected
                assertNotNull(cellWith.getCellComment());
                assertNull(cellWithoutComment.getCellComment());
                comment = cellWith.getCellComment();
                assertEquals(exp, comment.getString().getString());


                // Move the comment
                cellWithoutComment.setCellComment(comment);


                // Write out and re-check
                try (XSSFWorkbook wb3 = writeOutAndReadBack(wb2)) {
                    sheet = wb3.getSheetAt(0);
                    row = sheet.getRow(0);

                    // Ensure it swapped over
                    cellWith = row.getCell(0);
                    cellWithoutComment = row.getCell(1);
                    assertNull(cellWith.getCellComment());
                    assertNotNull(cellWithoutComment.getCellComment());

                    comment = cellWithoutComment.getCellComment();
                    assertEquals(exp, comment.getString().getString());
                }
            }
        }
    }

    /**
     * When the cell background colour is set with one of the first
     * two columns of the theme colour palette, the colours are
     * shades of white or black.
     * For those cases, ensure we don't break on reading the colour
     */
    @Test
    void bug50299() throws IOException {
        try (Workbook wb = openSampleWorkbook("50299.xlsx")) {

            // Check all the colours
            for (int sn = 0; sn < wb.getNumberOfSheets(); sn++) {
                Sheet s = wb.getSheetAt(sn);
                for (Row r : s) {
                    for (Cell c : r) {
                        CellStyle cs = c.getCellStyle();
                        if (cs != null) {
                            cs.getFillForegroundColor();
                        }
                    }
                }
            }

            // Check one bit in detail
            // Check that we get back foreground=0 for the theme colours,
            //  and background=64 for the auto colouring
            Sheet s = wb.getSheetAt(0);
            assertEquals(0, s.getRow(0).getCell(8).getCellStyle().getFillForegroundColor());
            assertEquals(64, s.getRow(0).getCell(8).getCellStyle().getFillBackgroundColor());
            assertEquals(0, s.getRow(1).getCell(8).getCellStyle().getFillForegroundColor());
            assertEquals(64, s.getRow(1).getCell(8).getCellStyle().getFillBackgroundColor());
        }
    }

    /**
     * Excel .xls style indexed colours in a .xlsx file
     */
    @Test
    void bug50786() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("50786-indexed_colours.xlsx")) {
            XSSFSheet s = wb.getSheetAt(0);
            XSSFRow r = s.getRow(2);

            // Check we have the right cell
            XSSFCell c = r.getCell(1);
            assertEquals("test\u00a0", c.getRichStringCellValue().getString());

            // It should be light green
            XSSFCellStyle cs = c.getCellStyle();
            assertEquals(42, cs.getFillForegroundColor());
            assertEquals(42, cs.getFillForegroundColorColor().getIndexed());
            assertNotNull(cs.getFillForegroundColorColor().getRGB());
            assertEquals("FFCCFFCC", cs.getFillForegroundColorColor().getARGBHex());
        }
    }

    /**
     * If the border colours are set with themes, then we
     * should still be able to get colours
     */
    @Test
    void bug50846() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("50846-border_colours.xlsx")) {

            XSSFSheet sheet = wb.getSheetAt(0);
            XSSFRow row = sheet.getRow(0);

            // Border from a theme, brown
            XSSFCell cellT = row.getCell(0);
            XSSFCellStyle styleT = cellT.getCellStyle();
            XSSFColor colorT = styleT.getBottomBorderXSSFColor();

            assertEquals(5, colorT.getTheme());
            assertEquals("FFC0504D", colorT.getARGBHex());

            // Border from a style direct, red
            XSSFCell cellS = row.getCell(1);
            XSSFCellStyle styleS = cellS.getCellStyle();
            XSSFColor colorS = styleS.getBottomBorderXSSFColor();

            assertEquals(0, colorS.getTheme());
            assertEquals("FFFF0000", colorS.getARGBHex());
        }
    }

    /**
     * Fonts where their colours come from the theme rather
     * then being set explicitly still should allow the
     * fetching of the RGB.
     */
    @Test
    void bug50784() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("50784-font_theme_colours.xlsx")) {
            XSSFSheet s = wb.getSheetAt(0);
            XSSFRow r = s.getRow(0);

            // Column 1 has a font with regular colours
            XSSFCell cr = r.getCell(1);
            XSSFFont fr = wb.getFontAt(cr.getCellStyle().getFontIndex());
            XSSFColor colr = fr.getXSSFColor();
            // No theme, has colours
            assertEquals(0, colr.getTheme());
            assertNotNull(colr.getRGB());

            // Column 0 has a font with colours from a theme
            XSSFCell ct = r.getCell(0);
            XSSFFont ft = wb.getFontAt(ct.getCellStyle().getFontIndex());
            XSSFColor colt = ft.getXSSFColor();
            // Has a theme, which has the colours on it
            assertEquals(9, colt.getTheme());
            XSSFColor themeC = wb.getTheme().getThemeColor(colt.getTheme());
            assertNotNull(themeC.getRGB());
            assertNotNull(colt.getRGB());
            assertEquals(themeC.getARGBHex(), colt.getARGBHex()); // The same colour
        }
    }

    /**
     * New lines were being eaten when setting a font on
     * a rich text string
     */
    @Test
    void bug48877() throws IOException {
        String text = "Use \n with word wrap on to create a new line.\n" +
                "This line finishes with two trailing spaces.  ";

        try (XSSFWorkbook wb1 = new XSSFWorkbook()) {
            XSSFSheet sheet = wb1.createSheet();

            Font font1 = wb1.createFont();
            font1.setColor((short) 20);
            Font font2 = wb1.createFont();
            font2.setColor(Font.COLOR_RED);
            Font font3 = wb1.getFontAt(0);

            XSSFRow row = sheet.createRow(2);
            XSSFCell cell = row.createCell(2);

            XSSFRichTextString richTextString =
                wb1.getCreationHelper().createRichTextString(text);

            // Check the text has the newline
            assertEquals(text, richTextString.getString());

            // Apply the font
            richTextString.applyFont(font3);
            richTextString.applyFont(0, 3, font1);
            cell.setCellValue(richTextString);

            // To enable newlines you need set a cell styles with wrap=true
            CellStyle cs = wb1.createCellStyle();
            cs.setWrapText(true);
            cell.setCellStyle(cs);

            // Check the text has the
            assertEquals(text, cell.getStringCellValue());

            // Save the file and re-read it
            try (XSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                sheet = wb2.getSheetAt(0);
                row = sheet.getRow(2);
                cell = row.getCell(2);
                assertEquals(text, cell.getStringCellValue());

                // Now add a 2nd, and check again
                int fontAt = text.indexOf('\n', 6);
                cell.getRichStringCellValue().applyFont(10, fontAt + 1, font2);
                assertEquals(text, cell.getStringCellValue());

                assertEquals(4, cell.getRichStringCellValue().numFormattingRuns());
                assertEquals("Use", cell.getRichStringCellValue().getCTRst().getRArray(0).getT());

                String r3 = cell.getRichStringCellValue().getCTRst().getRArray(2).getT();
                assertEquals("line.\n", r3.substring(r3.length() - 6));

                // Save and re-check
                try (XSSFWorkbook wb3 = writeOutAndReadBack(wb2)) {

                    sheet = wb3.getSheetAt(0);
                    row = sheet.getRow(2);
                    cell = row.getCell(2);
                    assertEquals(text, cell.getStringCellValue());
                }
            }
        }
    }

    /**
     * Adding sheets when one has a table, then re-ordering
     */
    @Test
    void bug50867() throws IOException {
        try (XSSFWorkbook wb1 = openSampleWorkbook("50867_with_table.xlsx")) {
            assertEquals(3, wb1.getNumberOfSheets());

            XSSFSheet s1 = wb1.getSheetAt(0);
            XSSFSheet s2 = wb1.getSheetAt(1);
            XSSFSheet s3 = wb1.getSheetAt(2);
            assertEquals(1, s1.getTables().size());
            assertEquals(0, s2.getTables().size());
            assertEquals(0, s3.getTables().size());

            XSSFTable t = s1.getTables().get(0);
            assertEquals("Tabella1", t.getName());
            assertEquals("Tabella1", t.getDisplayName());
            assertEquals("A1:C3", t.getCTTable().getRef());

            // Add a sheet and re-order
            XSSFSheet s4 = wb1.createSheet("NewSheet");
            wb1.setSheetOrder(s4.getSheetName(), 0);

            // Check on tables
            assertEquals(1, s1.getTables().size());
            assertEquals(0, s2.getTables().size());
            assertEquals(0, s3.getTables().size());
            assertEquals(0, s4.getTables().size());

            // Refetch to get the new order
            s1 = wb1.getSheetAt(0);
            s2 = wb1.getSheetAt(1);
            s3 = wb1.getSheetAt(2);
            s4 = wb1.getSheetAt(3);
            assertEquals(0, s1.getTables().size());
            assertEquals(1, s2.getTables().size());
            assertEquals(0, s3.getTables().size());
            assertEquals(0, s4.getTables().size());

            // Save and re-load
            try (XSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                s1 = wb2.getSheetAt(0);
                s2 = wb2.getSheetAt(1);
                s3 = wb2.getSheetAt(2);
                s4 = wb2.getSheetAt(3);
                assertEquals(0, s1.getTables().size());
                assertEquals(1, s2.getTables().size());
                assertEquals(0, s3.getTables().size());
                assertEquals(0, s4.getTables().size());

                t = s2.getTables().get(0);
                assertEquals("Tabella1", t.getName());
                assertEquals("Tabella1", t.getDisplayName());
                assertEquals("A1:C3", t.getCTTable().getRef());


                // Add some more tables, and check
                t = s2.createTable(null);
                t.setName("New 2");
                t.setDisplayName("New 2");
                t = s3.createTable(null);
                t.setName("New 3");
                t.setDisplayName("New 3");

                try (XSSFWorkbook wb3 = writeOutAndReadBack(wb2)) {
                    s1 = wb3.getSheetAt(0);
                    s2 = wb3.getSheetAt(1);
                    s3 = wb3.getSheetAt(2);
                    s4 = wb3.getSheetAt(3);
                    assertEquals(0, s1.getTables().size());
                    assertEquals(2, s2.getTables().size());
                    assertEquals(1, s3.getTables().size());
                    assertEquals(0, s4.getTables().size());

                    t = s2.getTables().get(0);
                    assertEquals("Tabella1", t.getName());
                    assertEquals("Tabella1", t.getDisplayName());
                    assertEquals("A1:C3", t.getCTTable().getRef());

                    t = s2.getTables().get(1);
                    assertEquals("New 2", t.getName());
                    assertEquals("New 2", t.getDisplayName());

                    t = s3.getTables().get(0);
                    assertEquals("New 3", t.getName());
                    assertEquals("New 3", t.getDisplayName());

                    // Check the relationships
                    assertEquals(0, s1.getRelations().size());
                    assertEquals(3, s2.getRelations().size());
                    assertEquals(1, s3.getRelations().size());
                    assertEquals(0, s4.getRelations().size());

                    assertEquals(
                        XSSFRelation.PRINTER_SETTINGS.getContentType(),
                        s2.getRelations().get(0).getPackagePart().getContentType()
                    );
                    assertEquals(
                        XSSFRelation.TABLE.getContentType(),
                        s2.getRelations().get(1).getPackagePart().getContentType()
                    );
                    assertEquals(
                        XSSFRelation.TABLE.getContentType(),
                        s2.getRelations().get(2).getPackagePart().getContentType()
                    );
                    assertEquals(
                        XSSFRelation.TABLE.getContentType(),
                        s3.getRelations().get(0).getPackagePart().getContentType()
                    );
                    assertEquals(
                        "/xl/tables/table3.xml",
                        s3.getRelations().get(0).getPackagePart().getPartName().toString()
                    );
                }
            }
        }
    }

    /**
     * Setting repeating rows and columns shouldn't break
     * any print settings that were there before
     */
    @Test
    void bug49253() throws IOException {
        try (XSSFWorkbook wb1 = new XSSFWorkbook();
             XSSFWorkbook wb2 = new XSSFWorkbook()) {
            CellRangeAddress cra = CellRangeAddress.valueOf("C2:D3");

            // No print settings before repeating
            XSSFSheet s1 = wb1.createSheet();
            assertFalse(s1.getCTWorksheet().isSetPageSetup());
            assertTrue(s1.getCTWorksheet().isSetPageMargins());
            s1.setRepeatingColumns(cra);
            s1.setRepeatingRows(cra);

            assertTrue(s1.getCTWorksheet().isSetPageSetup());
            assertTrue(s1.getCTWorksheet().isSetPageMargins());

            PrintSetup ps1 = s1.getPrintSetup();
            assertFalse(ps1.getValidSettings());
            assertFalse(ps1.getLandscape());


            // Had valid print settings before repeating
            XSSFSheet s2 = wb2.createSheet();
            PrintSetup ps2 = s2.getPrintSetup();
            assertTrue(s2.getCTWorksheet().isSetPageSetup());
            assertTrue(s2.getCTWorksheet().isSetPageMargins());

            ps2.setLandscape(false);
            assertTrue(ps2.getValidSettings());
            assertFalse(ps2.getLandscape());
            s2.setRepeatingColumns(cra);
            s2.setRepeatingRows(cra);

            ps2 = s2.getPrintSetup();
            assertTrue(s2.getCTWorksheet().isSetPageSetup());
            assertTrue(s2.getCTWorksheet().isSetPageMargins());
            assertTrue(ps2.getValidSettings());
            assertFalse(ps2.getLandscape());
        }
    }

    /**
     * Default Column style
     */
    @Test
    void bug51037() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet s = wb.createSheet();

            CellStyle defaultStyle = wb.getCellStyleAt((short) 0);
            assertEquals(0, defaultStyle.getIndex());

            CellStyle blueStyle = wb.createCellStyle();
            blueStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
            blueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            assertEquals(1, blueStyle.getIndex());

            CellStyle pinkStyle = wb.createCellStyle();
            pinkStyle.setFillForegroundColor(IndexedColors.PINK.getIndex());
            pinkStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            assertEquals(2, pinkStyle.getIndex());

            // Starts empty
            assertEquals(1, s.getCTWorksheet().sizeOfColsArray());
            CTCols cols = s.getCTWorksheet().getColsArray(0);
            assertEquals(0, cols.sizeOfColArray());

            // Add some rows and columns
            XSSFRow r1 = s.createRow(0);
            XSSFRow r2 = s.createRow(1);
            r1.createCell(0);
            r1.createCell(2);
            r2.createCell(0);
            r2.createCell(3);

            // Check no style is there
            assertEquals(1, s.getCTWorksheet().sizeOfColsArray());
            assertEquals(0, cols.sizeOfColArray());

            assertEquals(defaultStyle, s.getColumnStyle(0));
            assertEquals(defaultStyle, s.getColumnStyle(2));
            assertEquals(defaultStyle, s.getColumnStyle(3));


            // Apply the styles
            s.setDefaultColumnStyle(0, pinkStyle);
            s.setDefaultColumnStyle(3, blueStyle);

            // Check
            assertEquals(pinkStyle, s.getColumnStyle(0));
            assertEquals(defaultStyle, s.getColumnStyle(2));
            assertEquals(blueStyle, s.getColumnStyle(3));

            assertEquals(1, s.getCTWorksheet().sizeOfColsArray());
            assertEquals(2, cols.sizeOfColArray());

            assertEquals(1, cols.getColArray(0).getMin());
            assertEquals(1, cols.getColArray(0).getMax());
            assertEquals(pinkStyle.getIndex(), cols.getColArray(0).getStyle());

            assertEquals(4, cols.getColArray(1).getMin());
            assertEquals(4, cols.getColArray(1).getMax());
            assertEquals(blueStyle.getIndex(), cols.getColArray(1).getStyle());


            // Save, re-load and re-check
            try (XSSFWorkbook wbBack = writeOutAndReadBack(wb)) {
                s = wbBack.getSheetAt(0);
                defaultStyle = wbBack.getCellStyleAt(defaultStyle.getIndex());
                blueStyle = wbBack.getCellStyleAt(blueStyle.getIndex());
                pinkStyle = wbBack.getCellStyleAt(pinkStyle.getIndex());

                assertEquals(pinkStyle, s.getColumnStyle(0));
                assertEquals(defaultStyle, s.getColumnStyle(2));
                assertEquals(blueStyle, s.getColumnStyle(3));
            }
        }
    }

    /**
     * Repeatedly writing a file.
     * Something with the SharedStringsTable currently breaks...
     */
    @Test
    void bug46662() throws IOException {
        for (int i=0; i<2; i++) {
            try (XSSFWorkbook wb1 = (i == 0) ? new XSSFWorkbook() : openSampleWorkbook("sample.xlsx")) {
                for (int j=0; j<3; j++) {
                    try (XSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                        assertEquals(wb1.getNumberOfSheets(), wb2.getNumberOfSheets());
                    }
                }
            }
        }

        // TODO: Complex file
    }

    /**
     * Colours and styles when the list has gaps in it
     */
    @Test
    void bug51222() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("51222.xlsx")) {
            XSSFSheet s = wb.getSheetAt(0);

            XSSFCell cA4_EEECE1 = s.getRow(3).getCell(0);
            XSSFCell cA5_1F497D = s.getRow(4).getCell(0);

            // Check the text
            assertEquals("A4", cA4_EEECE1.getRichStringCellValue().getString());
            assertEquals("A5", cA5_1F497D.getRichStringCellValue().getString());

            // Check the styles assigned to them
            assertEquals(4, cA4_EEECE1.getCTCell().getS());
            assertEquals(5, cA5_1F497D.getCTCell().getS());

            // Check we look up the correct style
            assertEquals(4, cA4_EEECE1.getCellStyle().getIndex());
            assertEquals(5, cA5_1F497D.getCellStyle().getIndex());

            // Check the fills on them at the low level
            assertEquals(5, cA4_EEECE1.getCellStyle().getCoreXf().getFillId());
            assertEquals(6, cA5_1F497D.getCellStyle().getCoreXf().getFillId());

            // These should reference themes 2 and 3
            assertEquals(2, wb.getStylesSource().getFillAt(5).getCTFill().getPatternFill().getFgColor().getTheme());
            assertEquals(3, wb.getStylesSource().getFillAt(6).getCTFill().getPatternFill().getFgColor().getTheme());

            // Ensure we get the right colours for these themes
            // TODO fix
            // assertEquals("FFEEECE1", wb.getTheme().getThemeColor(2).getARGBHex());
            // assertEquals("FF1F497D", wb.getTheme().getThemeColor(3).getARGBHex());

            // Finally check the colours on the styles
            // TODO fix
            // assertEquals("FFEEECE1", cA4_EEECE1.getCellStyle().getFillForegroundXSSFColor().getARGBHex());
            // assertEquals("FF1F497D", cA5_1F497D.getCellStyle().getFillForegroundXSSFColor().getARGBHex());
        }
    }

    @Test
    void bug51470() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("51470.xlsx")) {
            XSSFSheet sh0 = wb.getSheetAt(0);
            XSSFSheet sh1 = wb.cloneSheet(0);
            List<RelationPart> rels0 = sh0.getRelationParts();
            List<RelationPart> rels1 = sh1.getRelationParts();
            assertEquals(1, rels0.size());
            assertEquals(1, rels1.size());

            PackageRelationship pr0 = rels0.get(0).getRelationship();
            PackageRelationship pr1 = rels1.get(0).getRelationship();

            assertEquals(pr0.getTargetMode(), pr1.getTargetMode());
            assertEquals(pr0.getTargetURI(), pr1.getTargetURI());
            POIXMLDocumentPart doc0 = rels0.get(0).getDocumentPart();
            POIXMLDocumentPart doc1 = rels1.get(0).getDocumentPart();

            assertEquals(doc0, doc1);
        }
    }

    /**
     * Add comments to Sheet 1, when Sheet 2 already has
     * comments (so /xl/comments1.xml is taken)
     */
    @Test
    void bug51850() throws IOException {
        try (XSSFWorkbook wb1 = openSampleWorkbook("51850.xlsx")) {
            XSSFSheet sh1 = wb1.getSheetAt(0);
            XSSFSheet sh2 = wb1.getSheetAt(1);

            // Sheet 2 has comments
            assertNotNull(sh2.getCommentsTable(false));
            assertEquals(1, sh2.getCommentsTable(false).getNumberOfComments());

            // Sheet 1 doesn't (yet)
            assertNull(sh1.getCommentsTable(false));

            // Try to add comments to Sheet 1
            CreationHelper factory = wb1.getCreationHelper();
            Drawing<?> drawing = sh1.createDrawingPatriarch();

            ClientAnchor anchor = factory.createClientAnchor();
            anchor.setCol1(0);
            anchor.setCol2(4);
            anchor.setRow1(0);
            anchor.setRow2(1);

            Comment comment1 = drawing.createCellComment(anchor);
            comment1.setString(
                factory.createRichTextString("I like this cell. It's my favourite."));
            comment1.setAuthor("Bob T. Fish");

            anchor = factory.createClientAnchor();
            anchor.setCol1(0);
            anchor.setCol2(4);
            anchor.setRow1(1);
            anchor.setRow2(1);
            Comment comment2 = drawing.createCellComment(anchor);
            comment2.setString(
                factory.createRichTextString("This is much less fun..."));
            comment2.setAuthor("Bob T. Fish");

            Cell c1 = sh1.getRow(0).createCell(4);
            c1.setCellValue(2.3);
            c1.setCellComment(comment1);

            Cell c2 = sh1.getRow(0).createCell(5);
            c2.setCellValue(2.1);
            c2.setCellComment(comment2);


            // Save and re-load
            try (XSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                sh1 = wb2.getSheetAt(0);
                sh2 = wb2.getSheetAt(1);

                // Check the comments
                assertNotNull(sh2.getCommentsTable(false));
                assertEquals(1, sh2.getCommentsTable(false).getNumberOfComments());

                assertNotNull(sh1.getCommentsTable(false));
                assertEquals(2, sh1.getCommentsTable(false).getNumberOfComments());
            }
        }
    }

    /**
     * Sheet names with a , in them
     */
    @Test
    void bug51963() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("51963.xlsx")) {
            Sheet sheet = wb.getSheetAt(0);
            assertEquals("Abc,1", sheet.getSheetName());

            Name name = wb.getName("Intekon.ProdCodes");
            assertEquals("'Abc,1'!$A$1:$A$2", name.getRefersToFormula());

            AreaReference ref = wb.getCreationHelper().createAreaReference(name.getRefersToFormula());
            assertEquals(0, ref.getFirstCell().getRow());
            assertEquals(0, ref.getFirstCell().getCol());
            assertEquals(1, ref.getLastCell().getRow());
            assertEquals(0, ref.getLastCell().getCol());
        }
    }

    /**
     * Sum across multiple workbooks
     * eg =SUM($Sheet1.C1:$Sheet4.C1)
     */
    @Test
    void bug48703() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("48703.xlsx")) {
            XSSFSheet sheet = wb.getSheetAt(0);

            // Contains two forms, one with a range and one a list
            XSSFRow r1 = sheet.getRow(0);
            XSSFRow r2 = sheet.getRow(1);
            XSSFCell c1 = r1.getCell(1);
            XSSFCell c2 = r2.getCell(1);

            assertEquals(20.0, c1.getNumericCellValue(), 0);
            assertEquals("SUM(Sheet1!C1,Sheet2!C1,Sheet3!C1,Sheet4!C1)", c1.getCellFormula());

            assertEquals(20.0, c2.getNumericCellValue(), 0);
            assertEquals("SUM(Sheet1:Sheet4!C1)", c2.getCellFormula());

            // Try evaluating both
            XSSFFormulaEvaluator eval = new XSSFFormulaEvaluator(wb);
            eval.evaluateFormulaCell(c1);
            eval.evaluateFormulaCell(c2);

            assertEquals(20.0, c1.getNumericCellValue(), 0);
            assertEquals(20.0, c2.getNumericCellValue(), 0);
        }
    }

    /**
     * Bugzilla 51710: problems reading shared formuals from .xlsx
     */
    @Test
    void bug51710() throws IOException {
        try (Workbook wb = openSampleWorkbook("51710.xlsx")) {

            final String[] columns = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"};
            final int rowMax = 500; // bug triggers on row index 59

            Sheet sheet = wb.getSheetAt(0);


            // go through all formula cells
            for (int rInd = 2; rInd <= rowMax; rInd++) {
                Row row = sheet.getRow(rInd);

                for (int cInd = 1; cInd <= 12; cInd++) {
                    Cell cell = row.getCell(cInd);
                    String formula = cell.getCellFormula();
                    CellReference ref = new CellReference(cell);

                    //simulate correct answer
                    String correct = "$A" + (rInd + 1) + "*" + columns[cInd] + "$2";

                    assertEquals(correct, formula, "Incorrect formula in " + ref.formatAsString());
                }

            }
        }
    }

    /**
     * Bug 53101:
     */
    @Test
    void bug5301() throws IOException {
        try (Workbook wb = openSampleWorkbook("53101.xlsx")) {
            FormulaEvaluator evaluator =
                    wb.getCreationHelper().createFormulaEvaluator();
            // A1: SUM(B1: IZ1)
            double a1Value =
                    evaluator.evaluate(wb.getSheetAt(0).getRow(0).getCell(0)).getNumberValue();

            // Assert
            assertEquals(259.0, a1Value, 0.0);

            // KY: SUM(B1: IZ1)
            /*double ky1Value =*/
            assertEquals(259.0, evaluator.evaluate(wb.getSheetAt(0).getRow(0).getCell(310)).getNumberValue(), 0.0001);

            // Assert
            assertEquals(259.0, a1Value, 0.0);
        }
    }

    @Test
    void bug54436() throws IOException {
        try (Workbook wb = openSampleWorkbook("54436.xlsx")) {
            if (!WorkbookEvaluator.getSupportedFunctionNames().contains("GETPIVOTDATA")) {
                Function func = (args, srcRowIndex, srcColumnIndex) -> ErrorEval.NA;

                WorkbookEvaluator.registerFunction("GETPIVOTDATA", func);
            }
            FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
            assertDoesNotThrow(fe::evaluateAll);
        }
    }

    /**
     * Password Protected .xlsx files are now (as of 4.0.0) tested for the default password
     * when opened via WorkbookFactory, so there's no EncryptedDocumentException thrown anymore
     */
    @Test
    void bug55692_poifs() throws IOException {
        // Via a POIFSFileSystem
        try (POIFSFileSystem fsP = new POIFSFileSystem(
                POIDataSamples.getPOIFSInstance().openResourceAsStream("protect.xlsx"));
            Workbook wb = WorkbookFactory.create(fsP)) {
            assertNotNull(wb);
            assertEquals(3, wb.getNumberOfSheets());
        }
    }

    @Test
    void bug55692_stream() throws IOException {
        // Directly on a Stream, will go via POIFS and spot it's
        //  actually a .xlsx file encrypted with the default password, and open
        try (Workbook wb = WorkbookFactory.create(
                POIDataSamples.getPOIFSInstance().openResourceAsStream("protect.xlsx"))) {
            assertNotNull(wb);
            assertEquals(3, wb.getNumberOfSheets());
        }
    }

    @Test
    void bug55692_poifs2() throws IOException {
        // Via a POIFSFileSystem, will spot it's actually a .xlsx file
        //  encrypted with the default password, and open
        try (POIFSFileSystem fsNP = new POIFSFileSystem(
                POIDataSamples.getPOIFSInstance().openResourceAsStream("protect.xlsx"))) {
            Workbook wb = WorkbookFactory.create(fsNP);
            assertNotNull(wb);
            assertEquals(3, wb.getNumberOfSheets());
            wb.close();
        }
    }

    @Test
    void bug53282() throws IOException {
        try (Workbook wb = openSampleWorkbook("53282b.xlsx")) {
            Cell c = wb.getSheetAt(0).getRow(1).getCell(0);
            assertEquals("#@_#", c.getStringCellValue());
            assertEquals("http://invalid.uri", c.getHyperlink().getAddress());
        }
    }

    /**
     * Was giving NullPointerException
     * at org.apache.poi.xssf.usermodel.XSSFWorkbook.onDocumentRead
     * due to a lack of Styles Table
     */
    @Test
    void bug56278() throws IOException {
        try (Workbook wb = openSampleWorkbook("56278.xlsx")) {
            assertEquals(0, wb.getSheetIndex("Market Rates"));

            // Save and re-check
            Workbook nwb = writeOutAndReadBack(wb);
            assertEquals(0, nwb.getSheetIndex("Market Rates"));
            nwb.close();
        }
    }

    @Test
    void bug56315() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("56315.xlsx")) {
            Cell c = wb.getSheetAt(0).getRow(1).getCell(0);
            CellValue cv = wb.getCreationHelper().createFormulaEvaluator().evaluate(c);
            double rounded = cv.getNumberValue();
            assertEquals(0.1, rounded, 0.0);
        }
    }

    @Test
    void bug56468() throws IOException, InterruptedException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell = row.createCell(0);
            cell.setCellValue("Hi");
            sheet.setRepeatingRows(new CellRangeAddress(0, 0, 0, 0));

            // small hack to try to make this test stable, previously it failed whenever the two written ZIP files had
            // different file-creation dates stored. We try to do a loop until the current second changes in order to
            // avoid problems with some date information that is written to the ZIP and thus causes differences
            long start = System.currentTimeMillis() / 1000;
            while (System.currentTimeMillis() / 1000 == start) {
                Thread.sleep(10);
            }

            UnsynchronizedByteArrayOutputStream bos = new UnsynchronizedByteArrayOutputStream(8096);
            wb.write(bos);
            byte[] firstSave = bos.toByteArray();
            bos.reset();
            wb.write(bos);
            byte[] secondSave = bos.toByteArray();

            assertArrayEquals(firstSave, secondSave,
                "Had: \n" + Arrays.toString(firstSave) + " and \n" + Arrays.toString(secondSave));
        }
    }

    /**
     * ISO-8601 style cell formats with a T in them, eg
     * cell format of "yyyy-MM-ddTHH:mm:ss"
     */
    @Test
    void bug54034() throws IOException {
        TimeZone tz = LocaleUtil.getUserTimeZone();
        LocaleUtil.setUserTimeZone(TimeZone.getTimeZone("CET"));
        try (Workbook wb = openSampleWorkbook("54034.xlsx")) {
            Sheet sheet = wb.getSheet("Sheet1");
            Row row = sheet.getRow(1);
            Cell cell = row.getCell(2);
            assertTrue(DateUtil.isCellDateFormatted(cell));

            DataFormatter fmt = new DataFormatter();
            assertEquals("yyyy\\-mm\\-dd\\Thh:mm", cell.getCellStyle().getDataFormatString());
            assertEquals("2012-08-08T22:59", fmt.formatCellValue(cell));
        } finally {
            LocaleUtil.setUserTimeZone(tz);
        }
    }


    @Test
    void testBug53798XLSX() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("53798_shiftNegative_TMPL.xlsx")) {
            File xlsOutput = TempFile.createTempFile("testBug53798", ".xlsx");
            bug53798Work(wb, xlsOutput);
        }
    }

    @Disabled("Shifting rows is not yet implemented in SXSSFSheet")
    @Test
    void testBug53798XLSXStream() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("53798_shiftNegative_TMPL.xlsx")) {
            File xlsOutput = TempFile.createTempFile("testBug53798", ".xlsx");
            SXSSFWorkbook wb2 = new SXSSFWorkbook(wb);
            bug53798Work(wb2, xlsOutput);
            wb2.close();
        }
    }

    @Test
    void testBug53798XLS() throws IOException {
        try (Workbook wb = HSSFTestDataSamples.openSampleWorkbook("53798_shiftNegative_TMPL.xls")) {
            File xlsOutput = TempFile.createTempFile("testBug53798", ".xls");
            bug53798Work(wb, xlsOutput);
        }
    }

    /**
     * SUMIF was throwing a NPE on some formulas
     */
    @Test
    void testBug56420SumIfNPE() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("56420.xlsx")) {

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

            Sheet sheet = wb.getSheetAt(0);
            Row r = sheet.getRow(2);
            Cell c = r.getCell(2);
            assertEquals("SUMIF($A$1:$A$4,A3,$B$1:$B$4)", c.getCellFormula());
            Cell eval = evaluator.evaluateInCell(c);
            assertEquals(0.0, eval.getNumericCellValue(), 0.0001);
        }
    }

    private void bug53798Work(Workbook wb, File xlsOutput) throws IOException {
        Sheet testSheet = wb.getSheetAt(0);

        testSheet.shiftRows(2, 2, 1);

        saveAndReloadReport(wb, xlsOutput);

        // 1) corrupted xlsx (unreadable data in the first row of a shifted group) already comes about
        // when shifted by less than -1 negative amount (try -2)
        testSheet.shiftRows(3, 3, -1);

        saveAndReloadReport(wb, xlsOutput);

        testSheet.shiftRows(2, 2, 1);

        saveAndReloadReport(wb, xlsOutput);

        // 2) attempt to create a new row IN PLACE of a removed row by a negative shift causes corrupted
        // xlsx file with  unreadable data in the negative shifted row.
        // NOTE it's ok to create any other row.
        Row newRow = testSheet.createRow(3);

        saveAndReloadReport(wb, xlsOutput);

        Cell newCell = newRow.createCell(0);

        saveAndReloadReport(wb, xlsOutput);

        newCell.setCellValue("new Cell in row " + newRow.getRowNum());

        saveAndReloadReport(wb, xlsOutput);

        // 3) once a negative shift has been made any attempt to shift another group of rows
        // (note: outside of previously negative shifted rows) by a POSITIVE amount causes POI exception:
        // org.apache.xmlbeans.impl.values.XmlValueDisconnectedException.
        // NOTE: another negative shift on another group of rows is successful, provided no new rows in
        // place of previously shifted rows were attempted to be created as explained above.
        testSheet.shiftRows(6, 7, 1);   // -- CHANGE the shift to positive once the behaviour of
        // the above has been tested

        saveAndReloadReport(wb, xlsOutput);
    }

    /**
     * XSSFCell.typeMismatch on certain blank cells when formatting
     * with DataFormatter
     */
    @Test
    void bug56702() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("56702.xlsx")) {

            Sheet sheet = wb.getSheetAt(0);

            // Get wrong cell by row 8 & column 7
            Cell cell = sheet.getRow(8).getCell(7);
            assertEquals(CellType.NUMERIC, cell.getCellType());

            // Check the value - will be zero as it is <c><v/></c>
            assertEquals(0.0, cell.getNumericCellValue(), 0.001);

            // Try to format
            DataFormatter formatter = new DataFormatter();
            formatter.formatCellValue(cell);

            // Check the formatting
            assertEquals("0", formatter.formatCellValue(cell));
        }
    }

    /**
     * Formulas which reference named ranges, either in other
     * sheets, or workbook scoped but in other workbooks.
     * Used to fail with with errors like
     * org.apache.poi.ss.formula.FormulaParseException: Cell reference expected after sheet name at index 9
     * org.apache.poi.ss.formula.FormulaParseException: Parse error near char 0 '[' in specified formula '[0]!NR_Global_B2'. Expected number, string, or defined name
     */
    @Test
    void bug56737() throws IOException {
        try (Workbook wb = openSampleWorkbook("56737.xlsx")) {

            // Check the named range definitions
            Name nSheetScope = wb.getName("NR_To_A1");
            Name nWBScope = wb.getName("NR_Global_B2");

            assertNotNull(nSheetScope);
            assertNotNull(nWBScope);

            assertEquals("Defines!$A$1", nSheetScope.getRefersToFormula());
            assertEquals("Defines!$B$2", nWBScope.getRefersToFormula());

            // Check the different kinds of formulas
            Sheet s = wb.getSheetAt(0);
            Cell cRefSName = s.getRow(1).getCell(3);
            Cell cRefWName = s.getRow(2).getCell(3);

            assertEquals("Defines!NR_To_A1", cRefSName.getCellFormula());
            // Note the formula, as stored in the file, has the external name index not filename
            // TODO Provide a way to get the one with the filename
            assertEquals("[0]!NR_Global_B2", cRefWName.getCellFormula());

            // Try to evaluate them
            FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();
            assertEquals("Test A1", eval.evaluate(cRefSName).getStringValue());
            assertEquals(142, (int) eval.evaluate(cRefWName).getNumberValue());

            // Try to evaluate everything
            eval.evaluateAll();
        }
    }

    private void saveAndReloadReport(Workbook wb, File outFile) throws IOException {
        // run some method on the font to verify if it is "disconnected" already
        //for(short i = 0;i < 256;i++)
        {
            Font font = wb.getFontAt(0);
            if (font instanceof XSSFFont) {
                XSSFFont xfont = (XSSFFont) wb.getFontAt(0);
                CTFontImpl ctFont = (CTFontImpl) xfont.getCTFont();
                assertEquals(0, ctFont.sizeOfBArray());
            }
        }

        try (FileOutputStream fileOutStream = new FileOutputStream(outFile)) {
            wb.write(fileOutStream);
        }

        try (FileInputStream is = new FileInputStream(outFile)) {
            Workbook newWB = null;
            try {
                if (wb instanceof XSSFWorkbook) {
                    newWB = new XSSFWorkbook(is);
                } else if (wb instanceof HSSFWorkbook) {
                    newWB = new HSSFWorkbook(is);
                } else if (wb instanceof SXSSFWorkbook) {
                    newWB = new SXSSFWorkbook(new XSSFWorkbook(is));
                } else {
                    throw new IllegalStateException("Unknown workbook: " + wb);
                }
                assertNotNull(newWB.getSheet("test"));
            } finally {
                if (newWB != null) {
                    newWB.close();
                }
            }
        }
    }

    @ParameterizedTest
    @CsvSource({
        /* Not 0.0 because POI sees date "0" minus one month as invalid date, which is -1! */
        "56688_1.xlsx, -1.0",
        "56688_2.xlsx, #VALUE!",
        "56688_3.xlsx, #VALUE!",
        "56688_4.xlsx, date"
    })
    void testBug56688(String fileName, String expect) throws IOException {
        if ("date".equals(expect)) {
            Calendar calendar = LocaleUtil.getLocaleCalendar();
            calendar.add(Calendar.MONTH, 2);
            double excelDate = DateUtil.getExcelDate(calendar.getTime());
            NumberEval eval = new NumberEval(Math.floor(excelDate));
            expect = eval.getStringValue() + ".0";
        }

        try (XSSFWorkbook excel = openSampleWorkbook(fileName)) {
            XSSFFormulaEvaluator evaluator = new XSSFFormulaEvaluator(excel);
            evaluator.evaluateAll();

            XSSFCell cell = excel.getSheetAt(0).getRow(1).getCell(1);
            CellValue value = evaluator.evaluate(cell);

            assertEquals(expect, value.formatAsString());
        }
    }

    /**
     * New hyperlink with no initial cell reference, still need
     * to be able to change it
     */
    @Test
    void testBug56527() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            XSSFCreationHelper creationHelper = wb.getCreationHelper();

            // Try with a cell reference
            XSSFHyperlink hyperlink1 = creationHelper.createHyperlink(HyperlinkType.URL);
            sheet.addHyperlink(hyperlink1);
            hyperlink1.setAddress("http://myurl");
            hyperlink1.setCellReference("B4");
            assertEquals(3, hyperlink1.getFirstRow());
            assertEquals(1, hyperlink1.getFirstColumn());
            assertEquals(3, hyperlink1.getLastRow());
            assertEquals(1, hyperlink1.getLastColumn());

            // Try with explicit rows / columns
            XSSFHyperlink hyperlink2 = creationHelper.createHyperlink(HyperlinkType.URL);
            sheet.addHyperlink(hyperlink2);
            hyperlink2.setAddress("http://myurl");
            hyperlink2.setFirstRow(5);
            hyperlink2.setFirstColumn(3);

            assertEquals(5, hyperlink2.getFirstRow());
            assertEquals(3, hyperlink2.getFirstColumn());
            assertEquals(5, hyperlink2.getLastRow());
            assertEquals(3, hyperlink2.getLastColumn());

            assertTrue(sheet.getHyperlinkList().contains(hyperlink1), "sheet contains hyperlink1");
            assertTrue(sheet.getHyperlinkList().contains(hyperlink2), "sheet contains hyperlink2");

            sheet.removeHyperlink(hyperlink1);
            assertFalse(sheet.getHyperlinkList().contains(hyperlink1), "sheet no longer contains hyperlink1");
            assertTrue(sheet.getHyperlinkList().contains(hyperlink2), "sheet still contains hyperlink2");
        }
    }

    /**
     * Shifting rows with a formula that references a
     * function in another file
     */
    @Test
    void bug56502() throws IOException {
        try (Workbook wb = openSampleWorkbook("56502.xlsx")) {
            Sheet sheet = wb.getSheetAt(0);

            Cell cFunc = sheet.getRow(3).getCell(0);
            assertEquals("[1]!LUCANET(\"Ist\")", cFunc.getCellFormula());
            Cell cRef = sheet.getRow(3).createCell(1);
            cRef.setCellFormula("A3");

            // Shift it down one row
            sheet.shiftRows(1, sheet.getLastRowNum(), 1);

            // Check the new formulas: Function won't change, Reference will
            cFunc = sheet.getRow(4).getCell(0);
            assertEquals("[1]!LUCANET(\"Ist\")", cFunc.getCellFormula());
            cRef = sheet.getRow(4).getCell(1);
            assertEquals("A4", cRef.getCellFormula());
        }
    }

    @Test
    void bug54764() throws IOException, OpenXML4JException, XmlException {
        try (OPCPackage pkg = XSSFTestDataSamples.openSamplePackage("54764.xlsx")) {
            // Check the core properties - will be found but empty, due
            //  to the expansion being too much to be considered valid
            POIXMLProperties props = new POIXMLProperties(pkg);
            assertNull(props.getCoreProperties().getTitle());
            assertNull(props.getCoreProperties().getSubject());
            assertNull(props.getCoreProperties().getDescription());

            // Now check the spreadsheet itself
            assertThrows(POIXMLException.class, () -> new XSSFWorkbook(pkg), "Should fail as too much expansion occurs");
        }

        // Try with one with the entities in the Content Types
        assertThrows(Exception.class, () -> XSSFTestDataSamples.openSamplePackage("54764-2.xlsx"),
            "Should fail as too much expansion occurs");

        // Check we can still parse valid files after all that
        try (Workbook wb = openSampleWorkbook("sample.xlsx")) {
            assertEquals(3, wb.getNumberOfSheets());
        }
    }

    @Test
    void test54764WithSAXHelper() throws Exception {
        File testFile = XSSFTestDataSamples.getSampleFile("54764.xlsx");
        try (ZipFile zip = new ZipFile(testFile)) {
            ZipArchiveEntry ze = zip.getEntry("xl/sharedStrings.xml");
            XMLReader reader = XMLHelper.newXMLReader();
            SAXParseException e = assertThrows(SAXParseException.class,
                () -> reader.parse(new InputSource(zip.getInputStream(ze))));
            assertNotNull(e.getMessage());
            assertNotEquals(isOldXercesActive(), e.getMessage().contains("DOCTYPE is disallowed when the feature"));
        }
    }

    @Test
    void test54764WithDocumentHelper() throws Exception {
        File testFile = XSSFTestDataSamples.getSampleFile("54764.xlsx");
        try (ZipFile zip = new ZipFile(testFile)) {
            ZipArchiveEntry ze = zip.getEntry("xl/sharedStrings.xml");
            SAXParseException e = assertThrows(SAXParseException.class,
                () -> DocumentHelper.readDocument(zip.getInputStream(ze)));
            assertNotNull(e.getMessage());
            assertNotEquals(isOldXercesActive(), e.getMessage().contains("DOCTYPE is disallowed when the feature"));
        }
    }

    /**
     * CTDefinedNamesImpl should be included in the smaller
     * poi-ooxml-lite jar
     */
    @Test
    void bug57176() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("57176.xlsx")) {
            CTDefinedNames definedNames = wb.getCTWorkbook().getDefinedNames();
            List<CTDefinedName> definedNameList = definedNames.getDefinedNameList();
            for (CTDefinedName defName : definedNameList) {
                assertNotNull(defName.getName());
                assertNotNull(defName.getStringValue());
            }
            assertEquals("TestDefinedName", definedNameList.get(0).getName());
        }
    }

    /**
     * .xlsb files are not supported, but we should generate a helpful
     * error message if given one
     */
    @Test
    void bug56800_xlsb() throws IOException {
        // Can be opened at the OPC level
        try (OPCPackage pkg = XSSFTestDataSamples.openSamplePackage("Simple.xlsb")) {
            // XSSF Workbook gives helpful error
            assertThrows(XLSBUnsupportedException.class, () -> new XSSFWorkbook(pkg), ".xlsb files not supported");

            // Workbook Factory gives helpful error on package
            assertThrows(XLSBUnsupportedException.class, () -> XSSFWorkbookFactory.createWorkbook(pkg), ".xlsb files not supported");
        }

        // Workbook Factory gives helpful error on file
        File xlsbFile = HSSFTestDataSamples.getSampleFile("Simple.xlsb");
        assertThrows(XLSBUnsupportedException.class, () -> WorkbookFactory.create(xlsbFile), ".xlsb files not supported");
    }

    @Test
    void testBug57196() throws IOException {
        try (Workbook wb = openSampleWorkbook("57196.xlsx")) {
            Sheet sheet = wb.getSheet("Feuil1");
            Row mod = sheet.getRow(1);
            mod.getCell(1).setCellValue(3);
            mod = sheet.getRow(2);
            mod.createCell(0).setCellValue(10);
            XSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
            assertEquals(256, mod.getCell(2).getNumericCellValue());
        }
    }

    @Test
    void test57196_Detail() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Sheet1");
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell = row.createCell(0);
            cell.setCellFormula("DEC2HEX(HEX2DEC(O8)-O2+D2)");
            XSSFFormulaEvaluator fe = new XSSFFormulaEvaluator(wb);
            CellValue cv = fe.evaluate(cell);

            assertNotNull(cv);
        }
    }

    @Test
    void test57196_Detail2() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Sheet1");
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell = row.createCell(0);
            cell.setCellFormula("DEC2HEX(O2+D2)");
            XSSFFormulaEvaluator fe = new XSSFFormulaEvaluator(wb);
            CellValue cv = fe.evaluate(cell);

            assertNotNull(cv);
        }
    }

    @ParameterizedTest
    @CsvSource({
        // simple formula worked
        "DEC2HEX(O2+D2), org.apache.poi.ss.formula.eval.StringEval [0]",
        // this already failed! Hex2Dec did not correctly handle RefEval
        "HEX2DEC(O8), org.apache.poi.ss.formula.eval.NumberEval [0]",
        // slightly more complex one failed
        "HEX2DEC(O8)-O2+D2, org.apache.poi.ss.formula.eval.NumberEval [0]",
        // more complicated failed
        "DEC2HEX(HEX2DEC(O8)-O2+D2), org.apache.poi.ss.formula.eval.StringEval [0]",
        // what other similar functions
        "DEC2BIN(O8)-O2+D2, org.apache.poi.ss.formula.eval.ErrorEval [#VALUE!]",
        // what other similar functions
        "DEC2BIN(A1), org.apache.poi.ss.formula.eval.StringEval [0]"
    })
    void test57196_WorkbookEvaluator(String formula, String expValue) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Sheet1");
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell = row.createCell(0);
            cell.setCellValue("0");
            cell = row.createCell(1);
            cell.setCellValue(0);
            cell = row.createCell(2);
            cell.setCellValue(0);

            cell.setCellFormula(formula);

            WorkbookEvaluator workbookEvaluator = new WorkbookEvaluator(XSSFEvaluationWorkbook.create(wb), null, null);
            workbookEvaluator.setDebugEvaluationOutputForNextEval(true);
            ValueEval ve = workbookEvaluator.evaluate(new XSSFEvaluationCell(cell));

            assertEquals(expValue, ve.toString());
        }
    }

    /**
     * A .xlsx file with no Shared Strings table should open fine
     * in read-only mode
     */
    @ParameterizedTest
    @EnumSource(value = PackageAccess.class, names = {"READ_WRITE", "READ"})
    void bug57482(PackageAccess access) throws IOException, InvalidFormatException {
        File file = HSSFTestDataSamples.getSampleFile("57482-OnlyNumeric.xlsx");
        try (OPCPackage pkg = OPCPackage.open(file, access);
             XSSFWorkbook wb1 = new XSSFWorkbook(pkg)) {
            // Try to open it and read the contents

            assertNotNull(wb1.getSharedStringSource());
            assertEquals(0, wb1.getSharedStringSource().getCount());

            DataFormatter fmt = new DataFormatter();
            XSSFSheet s = wb1.getSheetAt(0);
            assertEquals("1", fmt.formatCellValue(s.getRow(0).getCell(0)));
            assertEquals("11", fmt.formatCellValue(s.getRow(0).getCell(1)));
            assertEquals("5", fmt.formatCellValue(s.getRow(4).getCell(0)));

            // Add a text cell
            s.getRow(0).createCell(3).setCellValue("Testing");
            assertEquals("Testing", fmt.formatCellValue(s.getRow(0).getCell(3)));

            // Try to write-out and read again, should only work
            //  in read-write mode, not read-only mode
            try (XSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                assertNotEquals(PackageAccess.READ, access, "Shouln't be able to write from read-only mode");

                // Check again
                s = wb2.getSheetAt(0);
                assertEquals("1", fmt.formatCellValue(s.getRow(0).getCell(0)));
                assertEquals("11", fmt.formatCellValue(s.getRow(0).getCell(1)));
                assertEquals("5", fmt.formatCellValue(s.getRow(4).getCell(0)));
                assertEquals("Testing", fmt.formatCellValue(s.getRow(0).getCell(3)));

                wb2.getPackage().revert();
            } catch (InvalidOperationException e) {
                if (access == PackageAccess.READ_WRITE) {
                    // Shouldn't occur in write-mode
                    throw e;
                }
            }

            pkg.revert();
        }
    }

    /**
     * "Unknown error type: -60" fetching formula error value
     */
    @Test
    void bug57535() throws IOException {
        try (Workbook wb = openSampleWorkbook("57535.xlsx")) {
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            evaluator.clearAllCachedResultValues();

            Sheet sheet = wb.getSheet("Sheet1");
            Cell cell = sheet.getRow(5).getCell(4);
            assertEquals(CellType.FORMULA, cell.getCellType());
            assertEquals("E4+E5", cell.getCellFormula());

            CellValue value = evaluator.evaluate(cell);
            assertEquals(CellType.ERROR, value.getCellType());
            assertEquals(-60, value.getErrorValue());
            assertEquals("~CIRCULAR~REF~", FormulaError.forInt(value.getErrorValue()).getString());
            assertEquals("CIRCULAR_REF", FormulaError.forInt(value.getErrorValue()).toString());
        }
    }


    @Test
    void test57165() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("57171_57163_57165.xlsx")) {
            removeAllSheetsBut(3, wb);
            // Throws exception here
            assertDoesNotThrow(() -> wb.cloneSheet(0));
            wb.setSheetName(1, "New Sheet");

            try (XSSFWorkbook wbBack = writeOutAndReadBack(wb)) {
                assertNotNull(wbBack.getSheet("New Sheet"));
            }
        }
    }

    @Test
    void test57165_create() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("57171_57163_57165.xlsx")) {
            removeAllSheetsBut(3, wb);
            // Throws exception here
            assertDoesNotThrow(() -> wb.createSheet("newsheet"));
            wb.setSheetName(1, "New Sheet");

            try (XSSFWorkbook wbBack = writeOutAndReadBack(wb)) {
                assertNotNull(wbBack.getSheet("New Sheet"));
            }
        }
    }

    private static void removeAllSheetsBut(@SuppressWarnings("SameParameterValue") int sheetIndex, Workbook wb) {
        int sheetNb = wb.getNumberOfSheets();
        // Move this sheet at the first position
        wb.setSheetOrder(wb.getSheetName(sheetIndex), 0);
        for (int sn = sheetNb - 1; sn > 0; sn--) {
            wb.removeSheetAt(sn);
        }
    }

    /**
     * Sums 2 plus the cell at the left, indirectly to avoid reference
     * problems when deleting columns, conditionally to stop recursion
     */
    private static final String FORMULA1 =
            "IF( INDIRECT( ADDRESS( ROW(), COLUMN()-1 ) ) = 0, 0, "
                    + "INDIRECT( ADDRESS( ROW(), COLUMN()-1 ) ) ) + 2";

    /**
     * Sums 2 plus the upper cell, indirectly to avoid reference
     * problems when deleting rows, conditionally to stop recursion
     */
    private static final String FORMULA2 =
            "IF( INDIRECT( ADDRESS( ROW()-1, COLUMN() ) ) = 0, 0, "
                    + "INDIRECT( ADDRESS( ROW()-1, COLUMN() ) ) ) + 2";

    /**
     * Expected:
     * <p>
     * [  0][  2][  4]
     */
    @Test
    void testBug56820_Formula1() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            Sheet sh = wb.createSheet();

            sh.createRow(0).createCell(0).setCellValue(0.0d);
            Cell formulaCell1 = sh.getRow(0).createCell(1);
            Cell formulaCell2 = sh.getRow(0).createCell(2);
            formulaCell1.setCellFormula(FORMULA1);
            formulaCell2.setCellFormula(FORMULA1);

            double A1 = evaluator.evaluate(formulaCell1).getNumberValue();
            double A2 = evaluator.evaluate(formulaCell2).getNumberValue();

            assertEquals(2, A1, 0);
            assertEquals(4, A2, 0);  //<-- FAILS EXPECTATIONS
        }
    }

    /**
     * Expected:
     * <p>
     * [  0] <- number
     * [  2] <- formula
     * [  4] <- formula
     */
    @Test
    void testBug56820_Formula2() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            Sheet sh = wb.createSheet();

            sh.createRow(0).createCell(0).setCellValue(0.0d);
            Cell formulaCell1 = sh.createRow(1).createCell(0);
            Cell formulaCell2 = sh.createRow(2).createCell(0);
            formulaCell1.setCellFormula(FORMULA2);
            formulaCell2.setCellFormula(FORMULA2);

            double A1 = evaluator.evaluate(formulaCell1).getNumberValue();
            double A2 = evaluator.evaluate(formulaCell2).getNumberValue(); //<-- FAILS EVALUATION

            assertEquals(2, A1, 0);
            assertEquals(4, A2, 0);
        }
    }

    @Test
    void test56467() throws IOException {
        try (Workbook wb = openSampleWorkbook("picture.xlsx")) {
            Sheet orig = wb.getSheetAt(0);
            assertNotNull(orig);

            Sheet sheet = wb.cloneSheet(0);
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            for (XSSFShape shape : ((XSSFDrawing) drawing).getShapes()) {
                if (shape instanceof XSSFPicture) {
                    XSSFPictureData pictureData = ((XSSFPicture) shape).getPictureData();
                    assertNotNull(pictureData);
                }
            }

        }
    }

    /**
     * OOXML-Strict files
     * Not currently working - namespace mis-match from XMLBeans
     */
    @Test
    @Disabled("XMLBeans namespace mis-match on ooxml-strict files")
    void test57699() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("sample.strict.xlsx")) {
            assertEquals(3, wb.getNumberOfSheets());
            // TODO Check sheet contents
            // TODO Check formula evaluation

            try (XSSFWorkbook wbBack = writeOutAndReadBack(wb)) {
                assertEquals(3, wbBack.getNumberOfSheets());
                // TODO Re-check sheet contents
                // TODO Re-check formula evaluation
            }
        }
    }

    @Test
    void testBug56295_MergeXlslsWithStyles() throws IOException {
        try (XSSFWorkbook xlsToAppendWorkbook = openSampleWorkbook("56295.xlsx");
             XSSFWorkbook targetWorkbook = new XSSFWorkbook()) {
            XSSFSheet sheet = xlsToAppendWorkbook.getSheetAt(0);
            XSSFRow srcRow = sheet.getRow(0);
            XSSFCell oldCell = srcRow.getCell(0);
            XSSFCellStyle cellStyle = oldCell.getCellStyle();

            checkStyle(cellStyle);

//        StylesTable table = xlsToAppendWorkbook.getStylesSource();
//        List<XSSFCellFill> fills = table.getFills();
//        System.out.println("Having " + fills.size() + " fills");
//        for(XSSFCellFill fill : fills) {
//          System.out.println("Fill: " + fill.getFillBackgroundColor() + "/" + fill.getFillForegroundColor());
//        }
            xlsToAppendWorkbook.close();

            XSSFSheet newSheet = targetWorkbook.createSheet(sheet.getSheetName());
            XSSFRow destRow = newSheet.createRow(0);
            XSSFCell newCell = destRow.createCell(0);

            //newCell.getCellStyle().cloneStyleFrom(cellStyle);
            CellStyle newCellStyle = targetWorkbook.createCellStyle();
            newCellStyle.cloneStyleFrom(cellStyle);
            newCell.setCellStyle(newCellStyle);
            checkStyle(newCell.getCellStyle());
            newCell.setCellValue(oldCell.getStringCellValue());

            try (XSSFWorkbook wbBack = writeOutAndReadBack(targetWorkbook)) {
                XSSFCellStyle styleBack = wbBack.getSheetAt(0).getRow(0).getCell(0).getCellStyle();
                checkStyle(styleBack);

            }
        }
    }

    /**
     * Paragraph with property BuFont but none of the properties
     * BuNone, BuChar, and BuAutoNum, used to trigger a NPE
     * Excel treats this as not-bulleted, so now do we
     */
    @Test
    void testBug57826() throws IOException {
        try (XSSFWorkbook workbook = openSampleWorkbook("57826.xlsx")) {
            assertTrue(workbook.getNumberOfSheets() >= 1, "no sheets in workbook");
            XSSFSheet sheet = workbook.getSheetAt(0);

            XSSFDrawing drawing = sheet.getDrawingPatriarch();
            assertNotNull(drawing);

            List<XSSFShape> shapes = drawing.getShapes();
            assertEquals(1, shapes.size());
            assertTrue(shapes.get(0) instanceof XSSFSimpleShape);

            XSSFSimpleShape shape = (XSSFSimpleShape) shapes.get(0);

            // Used to throw a NPE
            String text = shape.getText();

            // No bulleting info included
            assertEquals("test ok", text);
        }
    }

    private void checkStyle(XSSFCellStyle cellStyle) {
        assertNotNull(cellStyle);
        assertEquals(0, cellStyle.getFillForegroundColor());
        assertNotNull(cellStyle.getFillForegroundXSSFColor());
        XSSFColor fgColor = cellStyle.getFillForegroundColorColor();
        assertNotNull(fgColor);
        assertEquals("FF00FFFF", fgColor.getARGBHex());

        assertEquals(0, cellStyle.getFillBackgroundColor());
        assertNotNull(cellStyle.getFillBackgroundXSSFColor());
        XSSFColor bgColor = cellStyle.getFillBackgroundColorColor();
        assertNotNull(bgColor);
        assertEquals("FF00FFFF", fgColor.getARGBHex());
    }

    @Test
    void bug57642() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet s = wb.createSheet("TestSheet");
            XSSFCell c = s.createRow(0).createCell(0);
            c.setCellFormula("ISERROR(TestSheet!A1)");
            c = s.createRow(1).createCell(1);
            c.setCellFormula("ISERROR(B2)");

            wb.setSheetName(0, "CSN");
            c = s.getRow(0).getCell(0);
            assertEquals("ISERROR(CSN!A1)", c.getCellFormula());
            c = s.getRow(1).getCell(1);
            assertEquals("ISERROR(B2)", c.getCellFormula());
        }
    }

    /**
     * .xlsx supports 64000 cell styles, the style indexes after
     * 32,767 must not be -32,768, then -32,767, -32,766
     */
    @Test
    void bug57880() throws IOException {
        int numStyles = 33000;
        File file;
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            for (int i = 1; i < numStyles; i++) {
                // Create a style and use it
                XSSFCellStyle style = wb.createCellStyle();
                assertEquals(i, style.getUIndex());
            }
            assertEquals(numStyles, wb.getNumCellStyles());

            // avoid OOM in Gump run
            file = XSSFTestDataSamples.writeOutAndClose(wb, "bug57880");
        }

        // avoid zip bomb detection
        double ratio = ZipSecureFile.getMinInflateRatio();
        ZipSecureFile.setMinInflateRatio(0.00005);
        try (XSSFWorkbook wb = XSSFTestDataSamples.readBackAndDelete(file)) {
            ZipSecureFile.setMinInflateRatio(ratio);

            //Assume identical cell styles aren't consolidated
            //If XSSFWorkbooks ever implicitly optimize/consolidate cell styles (such as when the workbook is written to disk)
            //then this unit test should be updated
            assertEquals(numStyles, wb.getNumCellStyles());
            for (int i = 1; i < numStyles; i++) {
                XSSFCellStyle style = wb.getCellStyleAt(i);
                assertNotNull(style);
                assertEquals(i, style.getUIndex());
            }
        }
    }


    @ParameterizedTest
    @ValueSource(booleans = { false, true})
    void test56574(boolean createRow) throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("56574.xlsx")) {

            Sheet sheet = wb.getSheet("Func");
            assertNotNull(sheet);

            Map<String, Object[]> data;
            data = new TreeMap<>();
            data.put("1", new Object[]{"ID", "NAME", "LASTNAME"});
            data.put("2", new Object[]{2, "Amit", "Shukla"});
            data.put("3", new Object[]{1, "Lokesh", "Gupta"});
            data.put("4", new Object[]{4, "John", "Adwards"});
            data.put("5", new Object[]{2, "Brian", "Schultz"});

            int rownum = 1;
            for (Map.Entry<String, Object[]> me : data.entrySet()) {
                final Row row;
                if (createRow) {
                    row = sheet.createRow(rownum++);
                } else {
                    row = sheet.getRow(rownum++);
                }
                assertNotNull(row);

                int cellnum = 0;
                for (Object obj : me.getValue()) {
                    Cell cell = row.getCell(cellnum);
                    if (cell == null) {
                        cell = row.createCell(cellnum);
                    } else {
                        if (cell.getCellType() == CellType.FORMULA) {
                            cell.setCellFormula(null);
                            cell.getCellStyle().setDataFormat((short) 0);
                        }
                    }
                    if (obj instanceof String) {
                        cell.setCellValue((String) obj);
                    } else if (obj instanceof Integer) {
                        cell.setCellValue((Integer) obj);
                    }
                    cellnum++;
                }
            }

            XSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
            wb.getCreationHelper().createFormulaEvaluator().evaluateAll();

            CalculationChain chain = wb.getCalculationChain();
            checkCellsAreGone(chain);

            try (XSSFWorkbook wbBack = writeOutAndReadBack(wb)) {
                Sheet sheetBack = wbBack.getSheet("Func");
                assertNotNull(sheetBack);

                chain = wbBack.getCalculationChain();
                checkCellsAreGone(chain);
            }
        }
    }

    private void checkCellsAreGone(CalculationChain chain) {
        for (CTCalcCell calc : chain.getCTCalcChain().getCList()) {
            // A2 to A6 should be gone
            assertNotEquals("A2", calc.getR());
            assertNotEquals("A3", calc.getR());
            assertNotEquals("A4", calc.getR());
            assertNotEquals("A5", calc.getR());
            assertNotEquals("A6", calc.getR());
        }
    }

    /**
     * Excel 2007 generated Macro-Enabled .xlsm file
     */
    @Test
    void bug57181() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("57181.xlsm")) {
            assertEquals(9, wb.getNumberOfSheets());
        }
    }

    @Test
    void bug52111() throws IOException {
        try (Workbook wb = openSampleWorkbook("Intersection-52111-xssf.xlsx")) {
            Sheet s = wb.getSheetAt(0);
            assertFormula(wb, s.getRow(2).getCell(0), "(C2:D3 D3:E4)", "4.0");
            assertFormula(wb, s.getRow(6).getCell(0), "Tabelle2!E:E Tabelle2!11:11", "5.0");
            assertFormula(wb, s.getRow(8).getCell(0), "Tabelle2!E:F Tabelle2!11:12", null);
        }
    }

    @Test
    void test48962() throws IOException {
        try (Workbook wb = openSampleWorkbook("48962.xlsx")) {
            Sheet sh = wb.getSheetAt(0);
            Row row = sh.getRow(1);
            Cell cell = row.getCell(0);

            CellStyle style = cell.getCellStyle();
            assertNotNull(style);

            // color index
            assertEquals(64, style.getFillBackgroundColor());
            XSSFColor color = ((XSSFCellStyle) style).getFillBackgroundXSSFColor();
            assertNotNull(color);

            // indexed color
            assertEquals(64, color.getIndexed());
            assertEquals(64, color.getIndex());

            // not an RGB color
            assertFalse(color.isRGB());
            assertNull(color.getRGB());
        }
    }

    @Test
    void test50755_workday_formula_example() throws IOException {
        try (Workbook wb = openSampleWorkbook("50755_workday_formula_example.xlsx")) {
            Sheet sheet = wb.getSheet("Sheet1");
            for (Row aRow : sheet) {
                Cell cell = aRow.getCell(1);
                if (cell.getCellType() == CellType.FORMULA) {
                    String formula = cell.getCellFormula();
                    assertNotNull(formula);
                    assertTrue(formula.contains("WORKDAY"));
                } else {
                    assertNotNull(cell.toString());
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "51626.xlsx", "51626_contact.xlsx"})
    void test51626(String fileName) throws IOException {
        try (Workbook wb = openSampleWorkbook(fileName)) {
            assertNotNull(wb);
        }

        try (InputStream stream = HSSFTestDataSamples.openSampleFileStream(fileName);
             Workbook wb = WorkbookFactory.create(stream)) {
            assertNotNull(wb);
        }
    }

    @Disabled("this test is only for manual verification, as we can't test if the cell is visible in Excel")
    void test51451() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet();

            Row row = sh.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue(239827342);

            CellStyle style = wb.createCellStyle();
            //style.setHidden(false);
            DataFormat excelFormat = wb.createDataFormat();
            style.setDataFormat(excelFormat.getFormat("#,##0"));
            sh.setDefaultColumnStyle(0, style);
        }
    }

    @Test
    void test53105() throws IOException {
        try (Workbook wb = openSampleWorkbook("53105.xlsx")) {
            assertNotNull(wb);


            // Act
            // evaluate SUM('Skye Lookup Input'!A4:XFD4), cells in range each contain "1"
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            double numericValue = evaluator.evaluate(wb.getSheetAt(0).getRow(1).getCell(0)).getNumberValue();

            // Assert
            assertEquals(16384.0, numericValue, 0.0);
        }
    }


    @Test
    void test58315() throws IOException {
        try (Workbook wb = openSampleWorkbook("58315.xlsx")) {
            Cell cell = wb.getSheetAt(0).getRow(0).getCell(0);
            assertNotNull(cell);
            StringBuilder tmpCellContent = new StringBuilder(cell.getStringCellValue());
            XSSFRichTextString richText = (XSSFRichTextString) cell.getRichStringCellValue();

            for (int i = richText.length() - 1; i >= 0; i--) {
                Font f = richText.getFontAtIndex(i);
                if (f != null && f.getStrikeout()) {
                    tmpCellContent.deleteCharAt(i);
                }
            }
            String result = tmpCellContent.toString();
            assertEquals("320 350", result);
        }
    }

    @Test
    void test55406() throws IOException {
        try (Workbook wb = openSampleWorkbook("55406_Conditional_formatting_sample.xlsx")) {
            Sheet sheet = wb.getSheetAt(0);
            Cell cellA1 = sheet.getRow(0).getCell(0);
            Cell cellA2 = sheet.getRow(1).getCell(0);

            assertEquals(0, cellA1.getCellStyle().getFillForegroundColor());
            assertEquals("FFFDFDFD", ((XSSFColor) cellA1.getCellStyle().getFillForegroundColorColor()).getARGBHex());
            assertEquals(0, cellA2.getCellStyle().getFillForegroundColor());
            assertEquals("FFFDFDFD", ((XSSFColor) cellA2.getCellStyle().getFillForegroundColorColor()).getARGBHex());

            SheetConditionalFormatting cond = sheet.getSheetConditionalFormatting();
            assertEquals(2, cond.getNumConditionalFormattings());

            assertEquals(1, cond.getConditionalFormattingAt(0).getNumberOfRules());
            assertEquals(64, cond.getConditionalFormattingAt(0).getRule(0).getPatternFormatting().getFillForegroundColor());
            assertEquals("ISEVEN(ROW())", cond.getConditionalFormattingAt(0).getRule(0).getFormula1());
            assertNull(((XSSFColor) cond.getConditionalFormattingAt(0).getRule(0).getPatternFormatting().getFillForegroundColorColor()).getARGBHex());

            assertEquals(1, cond.getConditionalFormattingAt(1).getNumberOfRules());
            assertEquals(64, cond.getConditionalFormattingAt(1).getRule(0).getPatternFormatting().getFillForegroundColor());
            assertEquals("ISEVEN(ROW())", cond.getConditionalFormattingAt(1).getRule(0).getFormula1());
            assertNull(((XSSFColor) cond.getConditionalFormattingAt(1).getRule(0).getPatternFormatting().getFillForegroundColorColor()).getARGBHex());
        }
    }

    @Test
    void test51998() throws IOException {
        try (Workbook wb = openSampleWorkbook("51998.xlsx")) {
            Set<String> sheetNames = new HashSet<>();

            for (int sheetNum = 0; sheetNum < wb.getNumberOfSheets(); sheetNum++) {
                sheetNames.add(wb.getSheetName(sheetNum));
            }

            for (String sheetName : sheetNames) {
                int sheetIndex = wb.getSheetIndex(sheetName);

                wb.removeSheetAt(sheetIndex);

                Sheet newSheet = wb.createSheet();
                //Sheet newSheet = wb.createSheet(sheetName);
                int newSheetIndex = wb.getSheetIndex(newSheet);
                wb.setSheetName(newSheetIndex, sheetName);
                wb.setSheetOrder(sheetName, sheetIndex);
            }

            try (Workbook wbBack = writeOutAndReadBack(wb)) {
                assertNotNull(wbBack);
            }
        }
    }

    @Test
    void test58731() throws IOException {
        Object[][] bookData = {
            {"Head First Java", "Kathy Serria", 79},
            {"Effective Java", "Joshua Bloch", 36},
            {"Clean Code", "Robert martin", 42},
            {"Thinking in Java", "Bruce Eckel", 35},
        };

        try (Workbook wb = openSampleWorkbook("58731.xlsx")) {
            Sheet sheet = wb.createSheet("Java Books");

            int rowCount = 0;
            for (Object[] aBook : bookData) {
                Row row = sheet.createRow(rowCount++);

                int columnCount = 0;
                for (Object field : aBook) {
                    Cell cell = row.createCell(columnCount++);
                    if (field instanceof String) {
                        cell.setCellValue((String) field);
                    } else if (field instanceof Integer) {
                        cell.setCellValue((Integer) field);
                    }
                }
            }

            try (Workbook wb2 = writeOutAndReadBack(wb)) {
                sheet = wb2.getSheet("Java Books");
                assertNotNull(sheet.getRow(0));
                assertNotNull(sheet.getRow(0).getCell(0));
                assertEquals(bookData[0][0], sheet.getRow(0).getCell(0).getStringCellValue());
            }
        }
    }

    /**
     * Regression between 3.10.1 and 3.13 -
     * org.apache.poi.openxml4j.exceptions.InvalidFormatException:
     * The part /xl/sharedStrings.xml does not have any content type
     * ! Rule: Package require content types when retrieving a part from a package. [M.1.14]
     */
    @Test
    void test58760() throws IOException {
        try (Workbook wb1 = openSampleWorkbook("58760.xlsx")) {
            assertEquals(1, wb1.getNumberOfSheets());
            assertEquals("Sheet1", wb1.getSheetName(0));
            try (Workbook wb2 = writeOutAndReadBack(wb1)) {
                assertEquals(1, wb2.getNumberOfSheets());
                assertEquals("Sheet1", wb2.getSheetName(0));
            }
        }
    }

    @Test
    void test57236() throws IOException {
        // Having very small numbers leads to different formatting, Excel uses the scientific notation, but POI leads to "0"

        /*
        DecimalFormat format = new DecimalFormat("#.##########", new DecimalFormatSymbols(Locale.getDefault()));
        double d = 3.0E-104;
        assertEquals("3.0E-104", format.format(d));
         */

        DataFormatter formatter = new DataFormatter(true);

        try (XSSFWorkbook wb = openSampleWorkbook("57236.xlsx")) {
            for (int sheetNum = 0; sheetNum < wb.getNumberOfSheets(); sheetNum++) {
                Sheet sheet = wb.getSheetAt(sheetNum);
                for (int rowNum = sheet.getFirstRowNum(); rowNum < sheet.getLastRowNum(); rowNum++) {
                    Row row = sheet.getRow(rowNum);
                    for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
                        Cell cell = row.getCell(cellNum);
                        String fmtCellValue = formatter.formatCellValue(cell);
                        assertNotNull(fmtCellValue);
                        assertNotEquals("0", fmtCellValue);
                    }
                }
            }
        }
    }

    @ParameterizedTest()
    @CsvSource({ "false, rotated.xls", "true, rotated.xlsx" })
    @Disabled("Creates files for checking results manually, actual values are tested in Test*CellStyle")
    void test58043(boolean xssf, String fileName) throws IOException {
        try (Workbook wb = WorkbookFactory.create(xssf)) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow((short) 0);
            Cell cell = row.createCell(0);
            cell.setCellValue("Unsuccessful rotated text.");

            CellStyle style = wb.createCellStyle();
            style.setRotation((short) -90);
            cell.setCellStyle(style);
            assertEquals(180, style.getRotation());

            XSSFTestDataSamples.writeOut(wb, fileName);
        }
    }

    @Test
    void test59132() throws IOException {
        try (Workbook workbook = openSampleWorkbook("59132.xlsx")) {
            Sheet worksheet = workbook.getSheet("sheet1");

            // B3
            Row row = worksheet.getRow(2);
            Cell cell = row.getCell(1);

            cell.setCellValue((String) null);

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            // B3
            row = worksheet.getRow(2);
            cell = row.getCell(1);

            assertEquals(CellType.BLANK, cell.getCellType());
            assertEquals(CellType._NONE, evaluator.evaluateFormulaCell(cell));

            // A3
            row = worksheet.getRow(2);
            cell = row.getCell(0);

            assertEquals(CellType.FORMULA, cell.getCellType());
            assertEquals("IF(ISBLANK(B3),\"\",B3)", cell.getCellFormula());
            assertEquals(CellType.STRING, evaluator.evaluateFormulaCell(cell));
            CellValue value = evaluator.evaluate(cell);
            assertEquals("", value.getStringValue());

            // A5
            row = worksheet.getRow(4);
            cell = row.getCell(0);

            assertEquals(CellType.FORMULA, cell.getCellType());
            assertEquals("COUNTBLANK(A1:A4)", cell.getCellFormula());
            assertEquals(CellType.NUMERIC, evaluator.evaluateFormulaCell(cell));
            value = evaluator.evaluate(cell);
            assertEquals(1.0, value.getNumberValue(), 0.1);
        }
    }

    @Disabled("bug 59442")
    @Test
    void testSetRGBBackgroundColor() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFCell cell = workbook.createSheet().createRow(0).createCell(0);

            XSSFColor color = new XSSFColor(java.awt.Color.RED, workbook.getStylesSource().getIndexedColors());
            XSSFCellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(color);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cell.setCellStyle(style);

            // Everything is fine at this point, cell is red
            XSSFColor actual = cell.getCellStyle().getFillBackgroundColorColor();
            assertNull(actual);
            actual = cell.getCellStyle().getFillForegroundColorColor();
            assertNotNull(actual);
            assertEquals(color.getARGBHex(), actual.getARGBHex());

            Map<String, Object> properties = new HashMap<>();
            properties.put(CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperties(cell, properties);

            // Now the cell is all black
            actual = cell.getCellStyle().getFillBackgroundColorColor();
            assertNotNull(actual);
            assertNull(actual.getARGBHex());
            actual = cell.getCellStyle().getFillForegroundColorColor();
            assertNotNull(actual);
            assertEquals(color.getARGBHex(), actual.getARGBHex());

            try (XSSFWorkbook nwb = writeOutAndReadBack(workbook)) {
                XSSFCell ncell = nwb.getSheetAt(0).getRow(0).getCell(0);
                XSSFColor ncolor = new XSSFColor(java.awt.Color.RED, workbook.getStylesSource().getIndexedColors());

                // Now the cell is all black
                XSSFColor nactual = ncell.getCellStyle().getFillBackgroundColorColor();
                assertNotNull(nactual);
                assertEquals(ncolor.getARGBHex(), nactual.getARGBHex());

            }
        }
    }

    @Disabled("currently fails on POI 3.15 beta 2")
    @Test
    void test55273() throws IOException {
        try (Workbook wb = openSampleWorkbook("ExcelTables.xlsx")) {
            Sheet sheet = wb.getSheet("ExcelTable");

            Name name = wb.getName("TableAsRangeName");
            assertEquals("TableName[#All]", name.getRefersToFormula());
            // POI 3.15-beta 2 (2016-06-15): getSheetName throws IllegalArgumentException: Invalid CellReference: TableName[#All]
            assertEquals("TableName", name.getSheetName());

            XSSFSheet xsheet = (XSSFSheet) sheet;
            List<XSSFTable> tables = xsheet.getTables();
            assertEquals(2, tables.size()); //FIXME: how many tables are there in this spreadsheet?
            assertEquals("Table1", tables.get(0).getName()); //FIXME: what is the table name?
            assertEquals("Table2", tables.get(1).getName()); //FIXME: what is the table name?
        }
    }

    @Test
    void test57523() throws IOException {
        try (Workbook wb = openSampleWorkbook("57523.xlsx")) {
            Sheet sheet = wb.getSheet("Attribute Master");
            Row row = sheet.getRow(15);

            int N = CellReference.convertColStringToIndex("N");
            Cell N16 = row.getCell(N);
            assertEquals(500.0, N16.getNumericCellValue(), 0.00001);

            int P = CellReference.convertColStringToIndex("P");
            Cell P16 = row.getCell(P);
            assertEquals(10.0, P16.getNumericCellValue(), 0.00001);
        }
    }

    /**
     * Files produced by some scientific equipment neglect
     * to include the row number on the row tags
     */
    @Test
    void noRowNumbers59746() throws IOException {
        try (Workbook wb = openSampleWorkbook("59746_NoRowNums.xlsx")) {
            Sheet sheet = wb.getSheetAt(0);
            assertTrue(sheet.getLastRowNum() > 20, "Last row num: " + sheet.getLastRowNum());
            assertEquals("Checked", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Checked", sheet.getRow(9).getCell(2).getStringCellValue());
            assertFalse(sheet.getRow(70).getCell(8).getBooleanCellValue());
            assertEquals(71, sheet.getPhysicalNumberOfRows());
            assertEquals(70, sheet.getLastRowNum());
            assertEquals(70, sheet.getRow(sheet.getLastRowNum()).getRowNum());
        }
    }

    @Test
    void testWorkdayFunction() throws IOException {
        try (XSSFWorkbook workbook = openSampleWorkbook("59106.xlsx")) {
            XSSFSheet sheet = workbook.getSheet("Test");
            Row row = sheet.getRow(1);
            Cell cell = row.getCell(0);
            DataFormatter form = new DataFormatter();
            FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
            String result = form.formatCellValue(cell, evaluator);

            assertEquals("09 Mar 2016", result);
        }
    }

    // This bug is currently open. When this bug is fixed, it should not throw an AssertionError
    @Test
    void test55076_collapseColumnGroups() throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet();

            // this column collapsing bug only occurs when the grouped columns are different widths
            sheet.setColumnWidth(1, 400);
            sheet.setColumnWidth(2, 600);
            sheet.setColumnWidth(3, 800);

            assertEquals(400, sheet.getColumnWidth(1));
            assertEquals(600, sheet.getColumnWidth(2));
            assertEquals(800, sheet.getColumnWidth(3));

            sheet.groupColumn(1, 3);
            sheet.setColumnGroupCollapsed(1, true);

            assertEquals(0, sheet.getColumnOutlineLevel(0));
            assertEquals(1, sheet.getColumnOutlineLevel(1));
            assertEquals(1, sheet.getColumnOutlineLevel(2));
            assertEquals(1, sheet.getColumnOutlineLevel(3));
            assertEquals(0, sheet.getColumnOutlineLevel(4));

            // none of the columns should be hidden
            // column group collapsing is a different concept
            for (int c = 0; c < 5; c++) {
                // if this fails for c == 1 in the future, then the implementation will be correct
                // and we need to adapt this test accordingly
                assertEquals(c == 1, sheet.isColumnHidden(c), "Column " + c);
            }

            assertEquals(400, sheet.getColumnWidth(1));
            // 600 is the correct value! ... 2048 is just for pacifying the test (see above comment)
            assertEquals(2048, sheet.getColumnWidth(2));
            assertEquals(800, sheet.getColumnWidth(3));

        }
    }

    /**
     * Other things, including charts, may end up taking drawing part
     * numbers. (Uses a test file hand-crafted with an extra non-drawing
     * part with a part number)
     */
    @Test
    void drawingNumbersAlreadyTaken_60255() throws IOException {
        try (Workbook wb = openSampleWorkbook("60255_extra_drawingparts.xlsx")) {
            assertEquals(4, wb.getNumberOfSheets());

            // Sheet 3 starts with a drawing
            Sheet sheet = wb.getSheetAt(0);
            assertNull(sheet.getDrawingPatriarch());
            sheet = wb.getSheetAt(1);
            assertNull(sheet.getDrawingPatriarch());
            sheet = wb.getSheetAt(2);
            assertNotNull(sheet.getDrawingPatriarch());
            sheet = wb.getSheetAt(3);
            assertNull(sheet.getDrawingPatriarch());

            // Add another sheet, and give it a drawing
            sheet = wb.createSheet();
            assertNull(sheet.getDrawingPatriarch());
            sheet.createDrawingPatriarch();
            assertNotNull(sheet.getDrawingPatriarch());

            // Save and check
            Workbook wbBack = writeOutAndReadBack(wb);
            assertEquals(5, wbBack.getNumberOfSheets());

            // Sheets 3 and 5 now
            sheet = wbBack.getSheetAt(0);
            assertNull(sheet.getDrawingPatriarch());
            sheet = wbBack.getSheetAt(1);
            assertNull(sheet.getDrawingPatriarch());
            sheet = wbBack.getSheetAt(2);
            assertNotNull(sheet.getDrawingPatriarch());
            sheet = wbBack.getSheetAt(3);
            assertNull(sheet.getDrawingPatriarch());
            sheet = wbBack.getSheetAt(4);
            assertNotNull(sheet.getDrawingPatriarch());
        }
    }

    @Test
    void test53611() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("test");
            Row row = sheet.createRow(1);
            Cell cell = row.createCell(1);
            cell.setCellValue("blabla");

            //0 1 2 3 4 5 6 7
            //A B C D E F G H
            row = sheet.createRow(4);
            cell = row.createCell(7);
            cell.setCellValue("blabla");

            // we currently only populate the dimension during writing out
            // to avoid having to iterate all rows/cells in each add/remove of a row or cell
            wb.write(NULL_OUTPUT_STREAM);

            assertEquals("B2:H5", ((XSSFSheet) sheet).getCTWorksheet().getDimension().getRef());
        }
    }

    @Test
    void test61798() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("test");
            Row row = sheet.createRow(1);
            Cell cell = row.createCell(1);
            cell.setCellValue("blabla");

            row = sheet.createRow(4);
            // Allowable column range for EXCEL2007 is (0..16383) or ('A'..'XDF')
            cell = row.createCell(16383);
            cell.setCellValue("blabla");

            // we currently only populate the dimension during writing out
            // to avoid having to iterate all rows/cells in each add/remove of a row or cell
            wb.write(NULL_OUTPUT_STREAM);

            assertEquals("B2:XFD5", ((XSSFSheet) sheet).getCTWorksheet().getDimension().getRef());
        }
    }

    @Test
    void bug61063() throws Exception {
        try (Workbook wb = openSampleWorkbook("61063.xlsx")) {

            FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();
            Sheet s = wb.getSheetAt(0);

            Row r = s.getRow(3);
            Cell c = r.getCell(0);
            assertEquals(CellType.FORMULA, c.getCellType());
            eval.setDebugEvaluationOutputForNextEval(true);
            CellValue cv = eval.evaluate(c);
            assertNotNull(cv);
            assertEquals(2.0, cv.getNumberValue(), 0.00001, "Had: " + cv);
        }
    }

    @Test
    void bug61516() throws IOException {
        final String initialFormula = "A1";
        final String expectedFormula = "#REF!"; // from ms excel

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("sheet1");
            sheet.createRow(0).createCell(0).setCellValue(1); // A1 = 1

            {
                Cell c3 = sheet.createRow(2).createCell(2);
                c3.setCellFormula(initialFormula); // C3 = =A1
                FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
                CellValue cellValue = evaluator.evaluate(c3);
                assertEquals(1, cellValue.getNumberValue(), 0.0001);
            }

            {
                FormulaShifter formulaShifter = FormulaShifter.createForRowCopy(0, "sheet1", 2/*firstRowToShift*/, 2/*lastRowToShift*/
                    , -1/*step*/, SpreadsheetVersion.EXCEL2007);    // parameters 2, 2, -1 should mean : move row range [2-2] one level up
                XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
                Ptg[] ptgs = FormulaParser.parse(initialFormula, fpb, FormulaType.CELL, 0); // [A1]
                formulaShifter.adjustFormula(ptgs, 0);    // adjusted to [A]
                String shiftedFmla = FormulaRenderer.toFormulaString(fpb, ptgs);    //A
                //System.out.println(String.format("initial formula : A1; expected formula value after shifting up : #REF!; actual formula value : %s", shiftedFmla));
                assertEquals(expectedFormula, shiftedFmla,
                    "On copy we expect the formula to be adjusted, in this case it would point to row -1, which is an invalid REF");
            }

            {
                FormulaShifter formulaShifter = FormulaShifter.createForRowShift(0, "sheet1", 2/*firstRowToShift*/, 2/*lastRowToShift*/
                    , -1/*step*/, SpreadsheetVersion.EXCEL2007);    // parameters 2, 2, -1 should mean : move row range [2-2] one level up
                XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
                Ptg[] ptgs = FormulaParser.parse(initialFormula, fpb, FormulaType.CELL, 0); // [A1]
                formulaShifter.adjustFormula(ptgs, 0);    // adjusted to [A]
                String shiftedFmla = FormulaRenderer.toFormulaString(fpb, ptgs);    //A
                //System.out.println(String.format("initial formula : A1; expected formula value after shifting up : #REF!; actual formula value : %s", shiftedFmla));
                assertEquals(initialFormula, shiftedFmla,
                    "On move we expect the formula to stay the same, thus expecting the initial formula A1 here");
            }

            sheet.shiftRows(2, 2, -1);
            {
                Cell c2 = sheet.getRow(1).getCell(2);
                assertNotNull(c2, "cell C2 needs to exist now");
                assertEquals(CellType.FORMULA, c2.getCellType());
                assertEquals(initialFormula, c2.getCellFormula());
                FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
                CellValue cellValue = evaluator.evaluate(c2);
                assertEquals(1, cellValue.getNumberValue(), 0.0001);
            }

        }
    }

    @Test
    void test61652() throws IOException {
        try (Workbook wb = openSampleWorkbook("61652.xlsx")) {
            Sheet sheet = wb.getSheet("IRPPCalc");
            Row row = sheet.getRow(11);
            Cell cell = row.getCell(18);
            WorkbookEvaluatorProvider fe = (WorkbookEvaluatorProvider) wb.getCreationHelper().createFormulaEvaluator();
            ConditionalFormattingEvaluator condfmt = new ConditionalFormattingEvaluator(wb, fe);

            assertEquals("[]", condfmt.getConditionalFormattingForCell(cell).toString(),
                "Conditional formatting is not triggered for this cell");

            // but we can read the conditional formatting itself
            List<EvaluationConditionalFormatRule> rules = condfmt.getFormatRulesForSheet(sheet);
            assertEquals(1, rules.size());
            assertEquals("AND($A1>=EDATE($D$6,3),$B1>0)", rules.get(0).getFormula1());
        }
    }

    @Test
    void test61543() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            XSSFTable table1 = sheet.createTable(null);
            XSSFTable table2 = sheet.createTable(null);
            XSSFTable table3 = sheet.createTable(null);

            assertDoesNotThrow(() -> sheet.removeTable(table1));

            assertDoesNotThrow(() -> sheet.createTable(null));

            assertDoesNotThrow(() -> sheet.removeTable(table2));
            assertDoesNotThrow(() -> sheet.removeTable(table3));

            assertDoesNotThrow(() -> sheet.createTable(null));
        }
    }

    /**
     * Auto column sizing failed when there were loads of fonts with
     *  errors like ArrayIndexOutOfBoundsException: -32765
     */
    @Test
    void test62108() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            XSSFRow row = sheet.createRow(0);

            // Create lots of fonts
            XSSFDataFormat formats = wb.createDataFormat();
            XSSFFont[] fonts = new XSSFFont[50000];
            for (int i = 0; i < fonts.length; i++) {
                XSSFFont font = wb.createFont();
                font.setFontHeight(i);
                fonts[i] = font;
            }

            // Create a moderate number of columns, which use
            //  fonts from the start and end of the font list
            final int numCols = 125;
            for (int i = 0; i < numCols; i++) {
                XSSFCellStyle cs = wb.createCellStyle();
                cs.setDataFormat(formats.getFormat("'Test " + i + "' #,###"));

                XSSFFont font = fonts[i];
                if (i % 2 == 1) {
                    font = fonts[fonts.length - i];
                }
                cs.setFont(font);

                XSSFCell c = row.createCell(i);
                c.setCellValue(i);
                c.setCellStyle(cs);
            }

            // Do the auto-size
            for (int i = 0; i < numCols; i++) {
                int i2 = i;
                assertDoesNotThrow(() -> sheet.autoSizeColumn(i2));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void test61905(boolean xssf) throws IOException {
        ITestDataProvider instance = xssf ?  XSSFITestDataProvider.instance : HSSFITestDataProvider.instance;
        try (Workbook wb = instance.createWorkbook()) {
            Sheet sheet = wb.createSheet("new sheet");
            sheet.setActiveCell(new CellAddress("E11"));
            assertEquals("E11", sheet.getActiveCell().formatAsString());

            try (Workbook wbBack = instance.writeOutAndReadBack(wb)) {
                sheet = wbBack.getSheetAt(0);
                assertEquals("E11", sheet.getActiveCell().formatAsString());
            }
        }
    }

    @Test
    void testBug54084Unicode() throws IOException {
        // sample XLSX with the same text-contents as the text-file above
        try (XSSFWorkbook wb = openSampleWorkbook("54084 - Greek - beyond BMP.xlsx")) {

            verifyBug54084Unicode(wb);

            //XSSFTestDataSamples.writeOut(wb, "bug 54084 for manual review");

            // now write the file and read it back in
            try (XSSFWorkbook wbWritten = writeOutAndReadBack(wb)) {
                verifyBug54084Unicode(wbWritten);
            }

            // finally also write it out via the streaming interface and verify that we still can read it back in
            try (SXSSFWorkbook swb = new SXSSFWorkbook(wb);
                 Workbook wbStreamingWritten = SXSSFITestDataProvider.instance.writeOutAndReadBack(swb)) {
                verifyBug54084Unicode(wbStreamingWritten);
            }
        }
    }

    private void verifyBug54084Unicode(Workbook wb) {
        // expected data is stored in UTF-8 in a text-file
        byte[] data = HSSFTestDataSamples.getTestDataFileContent("54084 - Greek - beyond BMP.txt");
        String testData = new String(data, StandardCharsets.UTF_8).trim();

        Sheet sheet = wb.getSheetAt(0);
        Row row = sheet.getRow(0);
        Cell cell = row.getCell(0);

        String value = cell.getStringCellValue();
        //System.out.println(value);

        assertEquals(testData, value, "The data in the text-file should exactly match the data that we read from the workbook");
    }


    @Test
    void bug63371() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            CellRangeAddress region = new CellRangeAddress(1, 1, 1, 2);
            assertEquals(0, sheet.addMergedRegion(region));

            final CTMergeCells ctMergeCells = sheet.getCTWorksheet().getMergeCells();

            assertEquals(1, sheet.getMergedRegions().size());
            assertEquals(1, sheet.getNumMergedRegions());
            assertEquals(1, ctMergeCells.getCount());
            assertEquals(1, ctMergeCells.getMergeCellList().size());

        }
    }

    @Test
    void bug60397() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();

            CellRangeAddress region = new CellRangeAddress(1, 1, 1, 2);
            assertEquals(0, sheet.addMergedRegion(region));

            List<CellRangeAddress> ranges = sheet.getMergedRegions();
            int numMergedRegions = sheet.getNumMergedRegions();
            CTWorksheet ctSheet = sheet.getCTWorksheet();
            CTMergeCells ctMergeCells = ctSheet.getMergeCells();
            List<CTMergeCell> ctMergeCellList = ctMergeCells.getMergeCellList();
            long ctMergeCellCount = ctMergeCells.getCount();
            int ctMergeCellListSize = ctMergeCellList.size();

            assertEquals(1, ranges.size());
            assertEquals(1, numMergedRegions);
            assertEquals(1, ctMergeCellCount);
            assertEquals(1, ctMergeCellListSize);

            CellRangeAddress region2 = new CellRangeAddress(1, 2, 4, 6);
            assertEquals(1, sheet.addMergedRegion(region2));

            ranges = sheet.getMergedRegions();
            numMergedRegions = sheet.getNumMergedRegions();
            ctSheet = sheet.getCTWorksheet();
            ctMergeCells = ctSheet.getMergeCells();
            ctMergeCellList = ctMergeCells.getMergeCellList();
            ctMergeCellCount = ctMergeCells.getCount();
            ctMergeCellListSize = ctMergeCellList.size();

            assertEquals(2, ranges.size());
            assertEquals(2, numMergedRegions);
            assertEquals(2, ctMergeCellCount);
            assertEquals(2, ctMergeCellListSize);
        }
    }

    @Test
    void testBug63509() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            XSSFSheet sheet = workbook.createSheet("sheet1");

            Row row = sheet.createRow(0);

            Cell cell = row.createCell(0);
            cell.setCellValue("1000");

            // This causes the error
            sheet.addIgnoredErrors(new CellReference(cell), IgnoredErrorType.NUMBER_STORED_AS_TEXT);

            // Workaround
            // sheet.addIgnoredErrors(new CellReference(cell.getRowIndex(), cell.getColumnIndex(), false, false),
            //        IgnoredErrorType.NUMBER_STORED_AS_TEXT);

            String sqref = sheet.getCTWorksheet().getIgnoredErrors().getIgnoredErrorArray(0).getSqref().get(0).toString();
            assertEquals("A1", sqref);
        }
    }

    @Test
    void test64045() {
        File file = XSSFTestDataSamples.getSampleFile("xlsx-corrupted.xlsx");
        assertThrows(POIXMLException.class, () -> new XSSFWorkbook(file), "Should catch exception as the file is corrupted");
    }

    @Test
    void test58896WithFile() throws IOException {
        try (Workbook wb = openSampleWorkbook("58896.xlsx")) {
            Sheet sheet = wb.getSheetAt(0);
            Instant start = now();

            LOG.atInfo().log("Autosizing columns...");

            for (int i = 0; i < 3; ++i) {
                LOG.atInfo().log("Autosize {} - {}", box(i), between(start, now()));
                sheet.autoSizeColumn(i);
            }

            for (int i = 0; i < 69 - 35 + 1; ++i)
                for (int j = 0; j < 8; ++j) {
                    int col = 3 + 2 + i * (8 + 2) + j;
                    LOG.atInfo().log("Autosize {} - {}", box(col), between(start, now()));
                    sheet.autoSizeColumn(col);
                }
            LOG.atInfo().log(between(start, now()));

            assertTrue(between(start, now()).getSeconds() < 25);
        }
    }

    @Test
    void testBug63845() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);

            Cell cell = row.createCell(0, CellType.FORMULA);
            cell.setCellFormula("SUM(B1:E1)");

            assertNull(((XSSFCell) cell).getCTCell().getV(),
                "Element 'v' should not be set for formulas unless the value was calculated");

            try (Workbook wbBack = writeOutAndReadBack(wb)) {
                Cell cellBack = wbBack.getSheetAt(0).getRow(0).getCell(0);
                assertNull(((XSSFCell) cellBack).getCTCell().getV(),
                    "Element 'v' should not be set for formulas unless the value was calculated");
                assertNotNull(((XSSFCell) cellBack).getCTCell().getF(),
                    "Formula should be set internally now");

                wbBack.getCreationHelper().createFormulaEvaluator().evaluateInCell(cellBack);

                assertEquals("0.0", ((XSSFCell) cellBack).getCTCell().getV(),
                    "Element 'v' should be set now as the formula was calculated manually");

                cellBack.setCellValue("123");
                assertEquals("123", cellBack.getStringCellValue(),
                    "String value should be set now");
                assertNull(((XSSFCell) cellBack).getCTCell().getF(), "No formula should be set any more");
            }
        }
    }

    @Test
    void testBug63845_2() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("test");
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue(2);
            row.createCell(1).setCellValue(5);
            row.createCell(2).setCellFormula("A1+B1");

            try (Workbook wbBack = writeOutAndReadBack(wb)) {
                Cell cellBack = wbBack.getSheetAt(0).getRow(0).getCell(2);

                assertNull(((XSSFCell) cellBack).getCTCell().getV(),
                    "Element 'v' should not be set for formulas unless the value was calculated");

                wbBack.getCreationHelper().createFormulaEvaluator().evaluateInCell(cellBack);

                assertEquals("7.0", ((XSSFCell) cellBack).getCTCell().getV(),
                    "Element 'v' should be set now as the formula was calculated manually");
            }
        }
    }

    @Test
    public void testBug63339() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);

            Cell cell = row.createCell(0, CellType.FORMULA);
            cell.setCellFormula("SUM(B1:E1)");

            assertNotNull(((XSSFCell) cell).getCTCell().getF(),
                    "Element 'f' should contain the formula now");

            // this will actually set the "cached" value of the Formula
            // you will need to use setCellType() to change the cell to
            // a different type of value
            cell.setCellValue(34.5);

            assertNotNull(((XSSFCell) cell).getCTCell().getF(),
                    "Element 'f' should not be set now");
            assertEquals("34.5", ((XSSFCell) cell).getCTCell().getV(),
                    "Element 'v' should contain the string now");

            try (Workbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb)) {
                Cell cellBack = wbBack.getSheetAt(0).getRow(0).getCell(0);

                assertNotNull(((XSSFCell) cellBack).getCTCell().getF(),
                        "Element 'f' should not be set now");
                assertEquals("34.5", ((XSSFCell) cellBack).getCTCell().getV(),
                        "Element 'v' should contain the string now");
            }
        }
    }

    @Test
    void testBug64508() throws IOException {
        try (Workbook wb = openSampleWorkbook("64508.xlsx")) {
            int activeSheet = wb.getActiveSheetIndex();
            Sheet sheet1 = wb.getSheetAt(activeSheet);
            Row row = sheet1.getRow(1);
            CellReference aCellReference = new CellReference("E2");
            Cell aCell = row.getCell(aCellReference.getCol());
            assertEquals(CellType.STRING, aCell.getCellType());
            assertEquals("", aCell.getStringCellValue());
        }
    }

    @Test
    void testBug64667() throws IOException {
        //test that an NPE isn't thrown on opening
        try (Workbook wb = openSampleWorkbook("64667.xlsx")) {
            int activeSheet = wb.getActiveSheetIndex();
            assertEquals(0, activeSheet);
            assertNotNull(wb.getSheetAt(activeSheet));
        }
    }



    @Test
    @Tag("scratchpad.ignore")
    void testXLSXinPPT() throws Exception {
        assumeFalse(Boolean.getBoolean("scratchpad.ignore"));

        try (SlideShow<?,?> ppt = SlideShowFactory.create(
                POIDataSamples.getSlideShowInstance().openResourceAsStream("testPPT_oleWorkbook.ppt"))) {

            Slide<?, ?> slide = ppt.getSlides().get(1);
            ObjectShape<?,?> oleShape = (ObjectShape<?,?>)slide.getShapes().get(2);

            org.apache.poi.sl.usermodel.ObjectData data = oleShape.getObjectData();
            assertNull(data.getFileName());

            // Will be OOXML wrapped in OLE2, not directly SpreadSheet
            POIFSFileSystem fs = new POIFSFileSystem(data.getInputStream());
            assertTrue(fs.getRoot().hasEntry(OOXML_PACKAGE));
            assertFalse(fs.getRoot().hasEntry("Workbook"));


            // Can fetch Package to get OOXML
            DirectoryNode root = fs.getRoot();
            DocumentEntry docEntry = (DocumentEntry) root.getEntry(OOXML_PACKAGE);
            try (DocumentInputStream dis = new DocumentInputStream(docEntry);
                 OPCPackage pkg = OPCPackage.open(dis);
                 XSSFWorkbook wb = new XSSFWorkbook(pkg)) {
                assertEquals(1, wb.getNumberOfSheets());
            }

            // Via the XSSF Factory
            XSSFWorkbookFactory xssfFactory = new XSSFWorkbookFactory();
            try (XSSFWorkbook wb = xssfFactory.create(fs.getRoot(), null)) {
                assertEquals(1, wb.getNumberOfSheets());
            }


            // Or can open via the normal Factory, as stream or OLE2
            try (Workbook wb = WorkbookFactory.create(fs)) {
                assertEquals(1, wb.getNumberOfSheets());
            }
            try (Workbook wb = WorkbookFactory.create(data.getInputStream())) {
                assertEquals(1, wb.getNumberOfSheets());
            }
        }
    }

    @Test
    void test64986() throws IOException {
        try (XSSFWorkbook w = new XSSFWorkbook()) {
            XSSFSheet s = w.createSheet();
            XSSFRow r = s.createRow(0);
            XSSFCell c = r.createCell(0);
            c.setCellFormula("MATCH(\"VAL\",B1:B11,)");

            FormulaEvaluator evaluator = w.getCreationHelper().createFormulaEvaluator();
            CellValue value = evaluator.evaluate(c);
            assertEquals(CellType.ERROR, value.getCellType());
            assertEquals(ErrorEval.NA.getErrorCode(), value.getErrorValue());

            // put a value in place so the match should find something
            Cell val = r.createCell(1);
            val.setCellValue("VAL");

            // clear and check that now we find a match
            evaluator.clearAllCachedResultValues();
            value = evaluator.evaluate(c);
            assertEquals(CellType.NUMERIC, value.getCellType());
            assertEquals(1, value.getNumberValue(), 0.01);
        }
    }

    @Test
    void test64750() throws IOException {
        try (Workbook wb = openSampleWorkbook("64750.xlsx")) {
            Sheet sheet = wb.getSheet("Sheet1");
            assertEquals(1, sheet.getDataValidations().size());
        }
    }

    @Test
    void test64450() throws IOException {
        try (Workbook wb = openSampleWorkbook("64450.xlsx")) {
            assertNotNull(wb);
        }
    }

    @Test
    void test64494() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle styleRight = wb.createCellStyle();
            CellStyle styleLeft = wb.createCellStyle();
            styleRight.setAlignment(HorizontalAlignment.RIGHT);
            //styleRight.setBorderBottom(BorderStyle.DASH_DOT);
            styleLeft.setAlignment(HorizontalAlignment.LEFT);
            //styleLeft.setBorderRight(BorderStyle.MEDIUM);

            assertEquals(HorizontalAlignment.RIGHT, styleRight.getAlignment());
            assertEquals(HorizontalAlignment.LEFT, styleLeft.getAlignment());

            Sheet sheet = wb.createSheet("test");
            Row row = sheet.createRow(0);

            Cell cellRight = row.createCell(0);
            cellRight.setCellValue("R");
            cellRight.setCellStyle(styleRight);

            Cell cellLeft = row.createCell(1);
            cellLeft.setCellValue("L");
            cellLeft.setCellStyle(styleLeft);

            assertEquals(HorizontalAlignment.RIGHT, cellRight.getCellStyle().getAlignment());
            assertEquals(HorizontalAlignment.LEFT, cellLeft.getCellStyle().getAlignment());
        }
    }

    @Test
    void test65464() throws IOException {
        try (XSSFWorkbook wb = openSampleWorkbook("bug65464.xlsx")) {
            XSSFSheet sheet = wb.getSheet("SheetWithSharedFormula");
            XSSFCell v15 = sheet.getRow(14).getCell(21);
            XSSFCell v16 = sheet.getRow(15).getCell(21);
            XSSFCell v17 = sheet.getRow(16).getCell(21);
            assertEquals("U15/R15", v15.getCellFormula());
            assertSame(STCellFormulaType.SHARED, v15.getCTCell().getF().getT());
            assertEquals("U16/R16", v16.getCellFormula());
            assertSame(STCellFormulaType.NORMAL, v16.getCTCell().getF().getT()); //anomaly in original file
            assertEquals("U17/R17", v17.getCellFormula());
            assertSame(STCellFormulaType.SHARED, v17.getCTCell().getF().getT());
            int calcChainSize = wb.getCalculationChain().getCTCalcChain().sizeOfCArray();

            v15.removeFormula();
            assertEquals(CellType.NUMERIC, v15.getCellType(), "V15 is no longer a function");
            assertNull(v15.getCTCell().getF(), "V15 xmlbeans function removed");
            assertEquals("U16/R16", v16.getCellFormula());
            assertSame(STCellFormulaType.NORMAL, v16.getCTCell().getF().getT());
            assertEquals("U17/R17", v17.getCellFormula());
            assertSame(STCellFormulaType.SHARED, v17.getCTCell().getF().getT());
            assertEquals(calcChainSize - 1, wb.getCalculationChain().getCTCalcChain().sizeOfCArray());
        }
    }

    @Test
    void testSetBlankOnNestedSharedFormulas() throws IOException {
        try (XSSFWorkbook wb1 = XSSFTestDataSamples.openSampleWorkbook("testSharedFormulasSetBlank.xlsx")) {
            XSSFSheet s1 = wb1.getSheetAt(0);
            assertNotNull(s1);
            Iterator<Row> rowIterator = s1.rowIterator();
            int count = 0;
            StringBuilder sb = new StringBuilder();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();

                    // the toString is needed to exhibit the broken state
                    sb.append(cell.toString()).append(",");
                    count++;

                    // breaks the sheet state
                    cell.setBlank();
                }
            }
            assertEquals(10, count);
            assertEquals("2-1,2-1,1+2,2-1,2-1,3+3,3+3,3+3,2-1,2-1,", sb.toString());
        }
    }

    @Test
    void testBug65306() throws IOException {
        try (XSSFWorkbook wb1 = XSSFTestDataSamples.openSampleWorkbook("bug65306.xlsx")) {
            XSSFSheet sheet = wb1.getSheetAt(0);
            assertNotNull(sheet);
            XSSFCell a1 = sheet.getRow(0).getCell(0);
            XSSFCell b1 = sheet.getRow(0).getCell(1);
            XSSFCell a2 = sheet.getRow(1).getCell(0);
            XSSFCell b2 = sheet.getRow(1).getCell(1);
            assertEquals(1.0, a1.getNumericCellValue());
            assertEquals(2.0, a2.getNumericCellValue());
            assertEquals("$A$1+3*$A$2", b1.getCellFormula());
            assertEquals("$A$1+3*$A$2", b2.getCellFormula());
            sheet.shiftRows(1, 1, -1);
            assertNull(sheet.getRow(1), "row 2 was removed?");
            a1 = sheet.getRow(0).getCell(0);
            b1 = sheet.getRow(0).getCell(1);
            assertEquals(2.0, a1.getNumericCellValue());
            assertEquals("#REF!+3*$A$1", b1.getCellFormula());
        }
    }

    @Test
    void testBug65452() throws IOException {
        File file = XSSFTestDataSamples.getSampleFile("workbook.xml");
        try (FileInputStream fis = new FileInputStream(file)) {
            IOException ie = assertThrows(IOException.class, () -> WorkbookFactory.create(fis));
            assertEquals("Can't open workbook - unsupported file type: XML", ie.getMessage());
        }
        IOException ie = assertThrows(IOException.class, () -> WorkbookFactory.create(file));
        assertEquals("Can't open workbook - unsupported file type: XML", ie.getMessage());
    }

    @Test
    void testBug62021() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFFormulaEvaluator fe = new XSSFFormulaEvaluator(wb);
            XSSFSheet sheet = wb.createSheet("testSheet");
            LocalDateTime ldt = LocalDateTime.parse("2021-10-15T12:00:00");
            addRow(sheet, 0, ldt);
            XSSFCell cell = wb.getSheetAt(0).getRow(0).createCell(100);
            assertDouble(fe, cell, "A1+1", DateUtil.getExcelDate(ldt) + 1);
            LocalDateTime expected = ldt.plusMinutes(90);
            assertDouble(fe, cell, "A1+\"1:30\"", DateUtil.getExcelDate(expected));
        }
    }

    @Test
    void testBug51037() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFCellStyle blueStyle = wb.createCellStyle();
            blueStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
            blueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle pinkStyle = wb.createCellStyle();
            pinkStyle.setFillForegroundColor(IndexedColors.PINK.getIndex());
            pinkStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Sheet s1 = wb.createSheet("Pretty columns");

            s1.setDefaultColumnStyle(4, blueStyle);
            s1.setDefaultColumnStyle(6, pinkStyle);

            Row r3 = s1.createRow(3);
            r3.createCell(0).setCellValue("The");
            r3.createCell(1).setCellValue("quick");
            r3.createCell(2).setCellValue("brown");
            r3.createCell(3).setCellValue("fox");
            r3.createCell(4).setCellValue("jumps");
            r3.createCell(5).setCellValue("over");
            r3.createCell(6).setCellValue("the");
            r3.createCell(7).setCellValue("lazy");
            r3.createCell(8).setCellValue("dog");
            Row r7 = s1.createRow(7);
            r7.createCell(1).setCellStyle(pinkStyle);
            r7.createCell(8).setCellStyle(blueStyle);

            assertEquals(blueStyle.getIndex(), r3.getCell(4).getCellStyle().getIndex());
            assertEquals(pinkStyle.getIndex(), r3.getCell(6).getCellStyle().getIndex());

            try (UnsynchronizedByteArrayOutputStream bos = new UnsynchronizedByteArrayOutputStream()) {
                wb.write(bos);
                try (XSSFWorkbook wb2 = new XSSFWorkbook(bos.toInputStream())) {
                    XSSFSheet wb2Sheet = wb2.getSheetAt(0);
                    XSSFRow wb2R3 = wb2Sheet.getRow(3);
                    assertEquals(blueStyle.getIndex(), wb2R3.getCell(4).getCellStyle().getIndex());
                    assertEquals(pinkStyle.getIndex(), wb2R3.getCell(6).getCellStyle().getIndex());
                }
            }
        }
    }

    @Test
    void testBug66181() throws IOException {
        File file = XSSFTestDataSamples.getSampleFile("ValueFunctionOfBlank.xlsx");
        try (
                FileInputStream fis = new FileInputStream(file);
                Workbook workbook = WorkbookFactory.create(fis)
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(0);
            Cell a1 = row.getCell(0);
            assertEquals(CellType.FORMULA, a1.getCellType());
            assertEquals(CellType.ERROR, a1.getCachedFormulaResultType());
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            CellValue cv1 = evaluator.evaluate(a1);
            assertEquals(CellType.ERROR, cv1.getCellType());
            assertEquals(ErrorEval.VALUE_INVALID.getErrorCode(), cv1.getErrorValue());
        }
    }

    @Test
    void testBug66216() throws IOException {
        File file = XSSFTestDataSamples.getSampleFile("ExcelPivotTableSample.xlsx");
        try (
                FileInputStream fis = new FileInputStream(file);
                XSSFWorkbook workbook = new XSSFWorkbook(fis)
        ) {
            for (XSSFPivotTable pivotTable : workbook.getPivotTables()) {
                assertNotNull(pivotTable.getCTPivotTableDefinition());
                assertNotNull(pivotTable.getPivotCacheDefinition());
                assertEquals(1, pivotTable.getRelations().size());
                assertInstanceOf(XSSFPivotCacheDefinition.class, pivotTable.getRelations().get(0));
                assertEquals("rId1", pivotTable.getPivotCacheDefinition().getCTPivotCacheDefinition().getId());
                assertEquals(3,
                        pivotTable.getPivotCacheDefinition().getCTPivotCacheDefinition().getRecordCount());
            }
        }
    }

    @Test
    void testTika3163() throws Exception {
        File file = XSSFTestDataSamples.getSampleFile("CVLKRA-KYC_Download_File_Structure_V3.1.xlsx");
        try (
                FileInputStream fis = new FileInputStream(file);
                XSSFWorkbook workbook = new XSSFWorkbook(fis)
        ) {
            assertNotNull(workbook.getStylesSource());
            assertEquals(23, workbook.getStylesSource().getFonts().size());
        }
        try (OPCPackage pkg = OPCPackage.open(file, PackageAccess.READ)) {
            XSSFReader reader = new XSSFReader(pkg);
            StylesTable stylesTable = reader.getStylesTable();
            assertNotNull(stylesTable);
            assertEquals(23, stylesTable.getFonts().size());
        }
    }
}