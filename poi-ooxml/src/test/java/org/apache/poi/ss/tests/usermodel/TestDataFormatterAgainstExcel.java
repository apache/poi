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

package org.apache.poi.ss.tests.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Checks {@link DataFormatter} against text produced by Excel itself: every {@code TEXT(value, format)}
 * formula in these sample workbooks has its result cached as Excel calculated it, so the cached text
 * is what Excel displays for that value and format.
 * <p>
 * All the workbooks were last saved by Microsoft Excel. NumberFormatApproxTests.xlsx is left out,
 * as its exotic exponent formats are only checked approximately, even for CellFormat.
 */
class TestDataFormatterAgainstExcel {

    private static final String[] WORKBOOKS = {
        "NumberFormatTests.xlsx",
        "GeneralFormatTests.xlsx",
        "FormatConditionTests.xlsx",
        "FormatChoiceTests.xlsx",
        "ElapsedFormatTests.xlsx",
        "DateFormatTests.xlsx",
        "DateFormatNumberTests.xlsx",
        "DateTimeToNumberTestCases.xls",
        "59106.xlsx"
    };

    /**
     * Formats where DataFormatter does not match Excel for any of the sample values.
     */
    private static final Set<String> UNSUPPORTED_FORMATS = Set.of(
        // a decimal point with no digits after it is still shown
        "#,##.#",
        // two percent signs multiply by 100 twice
        "0,0.00%%",
        // '?' digit placeholders outside fractions
        "?,?", "?,??", "?,???", "?,????", "?,?????", "?,??????", "?,???????", "?,????????", "?,?????????",
        "|?.?|", "|??.?|", "|?.??|", "|??.??|",
        // fractions with text or spacing between the digits
        "|#_#/#|", "|#_#=/=#|", "|#_#=/=?|", "|#_#=/=0|", "|#_?=/=#|", "|#_?=/=?|",
        "|#\\:#/#|", "|#-#-#\\:#/#|", "|#\\:? ?#/#|", "|#\\:? ?0#/000",
        // a single-section format with a condition
        "[>=10]#\" Big\"", "[>-10]#\" Big\"", "[>=-10]#\" Big\"", "[<>-10]#\" Wow\""
    );

    /**
     * Format and value combinations where DataFormatter does not match Excel,
     * for formats that match Excel for other values.
     */
    private static final Set<String> KNOWN_FAILURES = Set.of(
        // the text is dropped when the condition of a single-section format applies
        failure("[<10]#\" Wow\"", 1.0), failure("[<10]#\" Wow\"", 1.5),
        failure("[< 10]#\" Wow\"", 1.0), failure("[<      10]#\" Wow\"", 1.0),
        failure("[<=10]#\" Wow\"", 1.0), failure("[<=10]#\" Wow\"", 10.0),
        failure("[=10]#\" Wow\"", 10.0),
        failure("[<>10]#\" Wow\"", 1.0), failure("[<>10]#\" Wow\"", 11.0),
        failure("[>10]#\" Big\"", 11.0), failure("[>10]#\" Big\"", 12.0),
        // General shows very small numbers in scientific notation
        failure("General", 1.1E-10), failure("General", 1.12E-10), failure("General", 1.123E-10),
        failure("General", 1.12345E-10), failure("General", 1.123451E-10), failure("General", 1.123459E-10),
        failure("General", -1.1E-10), failure("General", -1.12E-10), failure("General", -1.123E-10),
        failure("General", -1.12345E-10), failure("General", -1.123451E-10), failure("General", -1.123459E-10),
        // Excel rounds 37.638 seconds up to 38 when no fractional seconds are shown
        failure("yyyy-mm-dd hh:mm:ss", 1.0032134017094)
    );

    private static String failure(String format, double value) {
        return format + "|" + value;
    }

    private record ExcelText(String source, String format, double value, String expected, boolean date1904) {}

    @TestFactory
    Stream<DynamicTest> formatsMatchExcel() throws IOException {
        DataFormatter formatter = new DataFormatter(Locale.US);
        List<ExcelText> cases = new ArrayList<>();
        for (String name : WORKBOOKS) {
            readTextFormulas(name, cases);
        }
        assertFalse(cases.isEmpty());
        return cases.stream().map(c -> DynamicTest.dynamicTest(
            c.source() + " " + c.format() + " " + c.value(), () -> {
                String actual = formatter.formatRawCellContents(c.value(), -1, c.format(), c.date1904());
                if (UNSUPPORTED_FORMATS.contains(c.format()) || KNOWN_FAILURES.contains(failure(c.format(), c.value()))) {
                    assertNotEquals(c.expected(), actual, "now matches Excel, remove it from the known failures");
                } else {
                    assertEquals(c.expected(), actual);
                }
            }));
    }

    private static void readTextFormulas(String name, List<ExcelText> cases) throws IOException {
        try (Workbook wb = WorkbookFactory.create(POIDataSamples.getSpreadSheetInstance().getFile(name), null, true)) {
            boolean date1904 = wb instanceof XSSFWorkbook xwb ? xwb.isDate1904()
                : ((HSSFWorkbook) wb).getInternalWorkbook().isUsing1904DateWindowing();
            for (Sheet sheet : wb) {
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        ExcelText excelText = toExcelText(name, sheet, cell, date1904);
                        if (excelText != null) {
                            cases.add(excelText);
                        }
                    }
                }
            }
        }
    }

    private static ExcelText toExcelText(String name, Sheet sheet, Cell cell, boolean date1904) {
        if (cell.getCellType() != CellType.FORMULA || cell.getCachedFormulaResultType() != CellType.STRING) {
            return null;
        }
        String formula = cell.getCellFormula();
        if (!formula.regionMatches(true, 0, "TEXT(", 0, 5) || !formula.endsWith(")")) {
            return null;
        }
        String args = formula.substring(5, formula.length() - 1);
        int comma = topLevelComma(args);
        if (comma < 0) {
            return null;
        }
        Cell valueCell = referencedCell(sheet, args.substring(0, comma).trim());
        String format = formatArgument(sheet, args.substring(comma + 1).trim());
        if (valueCell == null || format == null || resultType(valueCell) != CellType.NUMERIC) {
            return null;
        }
        String source = name + "!" + sheet.getSheetName() + "!" + cell.getAddress();
        return new ExcelText(source, format, valueCell.getNumericCellValue(), cell.getStringCellValue(), date1904);
    }

    private static int topLevelComma(String args) {
        boolean quoted = false;
        for (int i = 0; i < args.length(); i++) {
            char c = args.charAt(i);
            if (c == '"') {
                quoted = !quoted;
            } else if (c == ',' && !quoted) {
                return i;
            }
        }
        return -1;
    }

    private static String formatArgument(Sheet sheet, String arg) {
        if (arg.length() >= 2 && arg.startsWith("\"") && arg.endsWith("\"")) {
            return arg.substring(1, arg.length() - 1).replace("\"\"", "\"");
        }
        Cell cell = referencedCell(sheet, arg);
        return cell != null && resultType(cell) == CellType.STRING ? cell.getStringCellValue() : null;
    }

    private static Cell referencedCell(Sheet sheet, String ref) {
        if (!ref.matches("\\$?[A-Z]+\\$?[0-9]+")) {
            return null;
        }
        CellReference cellReference = new CellReference(ref);
        Row row = sheet.getRow(cellReference.getRow());
        return row == null ? null : row.getCell(cellReference.getCol());
    }

    private static CellType resultType(Cell cell) {
        return cell.getCellType() == CellType.FORMULA ? cell.getCachedFormulaResultType() : cell.getCellType();
    }
}
