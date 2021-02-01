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
package org.apache.poi.ss.tests.format;

import static java.awt.Color.ORANGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Color;
import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.format.CellFormatPart;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Class for spreadsheet-based tests, such as are used for cell formatting.
 * This reads tests from the spreadsheet, as well as reading
 * flags that can be used to paramterize these tests.
 * <p>
 * Each test has four parts: The expected result (column A), the format string
 * (column B), the value to format (column C), and a comma-separated list of
 * categores that this test falls in. Normally all tests are run, but if the
 * flag "Categories" is not empty, only tests that have at least one category
 * listed in "Categories" are run.
 */
class TestCellFormatPart {
    private static final Pattern NUMBER_EXTRACT_FMT = Pattern.compile(
            "([-+]?[0-9]+)(\\.[0-9]+)?.*(?:(e).*?([+-]?[0-9]+))",
            Pattern.CASE_INSENSITIVE);
    private static final Color TEST_COLOR = ORANGE.darker();
    private static Locale userLocale;



    @BeforeAll
    public static void setLocale() {
        userLocale = LocaleUtil.getUserLocale();
        LocaleUtil.setUserLocale(Locale.UK);
    }

    @AfterAll
    public static void unsetLocale() {
        LocaleUtil.setUserLocale(userLocale);
    }

    private final ITestDataProvider _testDataProvider = XSSFITestDataProvider.instance;

    private interface CellValue {
        Object getValue(Cell cell);

        default void equivalent(String expected, String actual, CellFormatPart format) {
            assertEquals('"' + expected + '"', '"' + actual + '"', "format \"" + format + "\"");
        }
    }

    @Test
    void testGeneralFormat() throws IOException {
        runFormatTests("GeneralFormatTests.xlsx", cell -> {
            assertNotNull(cell);
            switch (CellFormat.ultimateType(cell)) {
                case BOOLEAN:
                    return cell.getBooleanCellValue();
                case NUMERIC:
                    return cell.getNumericCellValue();
                default:
                    return cell.getStringCellValue();
            }
        });
    }

    @Test
    void testNumberFormat() throws IOException {
        runFormatTests("NumberFormatTests.xlsx", Cell::getNumericCellValue);
    }

    @Test
    void testNumberApproxFormat() throws IOException {
        runFormatTests("NumberFormatApproxTests.xlsx", new CellValue() {
            @Override
            public Object getValue(Cell cell) {
                return cell.getNumericCellValue();
            }

            @Override
            public void equivalent(String expected, String actual,
                    CellFormatPart format) {
                double expectedVal = extractNumber(expected);
                double actualVal = extractNumber(actual);
                // equal within 1%
                double delta = expectedVal / 100;
                assertEquals(expectedVal, actualVal, delta, "format \"" + format + "\"," + expected + " ~= " + actual);
            }
        });
    }

    @Test
    void testDateFormat() throws IOException {
        TimeZone tz = LocaleUtil.getUserTimeZone();
        LocaleUtil.setUserTimeZone(TimeZone.getTimeZone("CET"));
        try {
            runFormatTests("DateFormatTests.xlsx", Cell::getDateCellValue);
        } finally {
            LocaleUtil.setUserTimeZone(tz);
        }
    }

    @Test
    void testElapsedFormat() throws IOException {
        runFormatTests("ElapsedFormatTests.xlsx", Cell::getNumericCellValue);
    }

    @Test
    void testTextFormat() throws IOException {
        runFormatTests("TextFormatTests.xlsx", cell ->
            (CellFormat.ultimateType(cell) == CellType.BOOLEAN) ? cell.getBooleanCellValue() : cell.getStringCellValue()
        );
    }

    @Test
    void testConditions() throws IOException {
        runFormatTests("FormatConditionTests.xlsx", Cell::getNumericCellValue);
    }

    private double extractNumber(String str) {
        Matcher m = NUMBER_EXTRACT_FMT.matcher(str);
        if (!m.find()) {
            throw new IllegalArgumentException("Cannot find number in \"" + str + "\"");
        }

        StringBuilder sb = new StringBuilder();
        // The groups in the pattern are the parts of the number
        for (int i = 1; i <= m.groupCount(); i++) {
            String part = m.group(i);
            if (part != null)
                sb.append(part);
        }
        return Double.parseDouble(sb.toString());
    }


    protected void runFormatTests(String workbookName, CellValue valueGetter) throws IOException {
        try (Workbook workbook = _testDataProvider.openSampleWorkbook(workbookName)) {
            workbook.setMissingCellPolicy(Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

            Sheet sheet = workbook.getSheet("Tests");
            boolean isHeader = true;
            for (Row row : sheet) {
                // Skip the header row
                if (isHeader || row == null) {
                    isHeader = false;
                    continue;
                }
                String expectedText = row.getCell(0).getStringCellValue();
                String format = row.getCell(1).getStringCellValue();
                Cell value = row.getCell(2);

                if (expectedText.isEmpty() && format.isEmpty()) {
                    continue;
                }

                Object objVal = valueGetter.getValue(value);
                JLabel label = new JLabel();
                label.setForeground(TEST_COLOR);
                label.setText("xyzzy");

                Color origColor = label.getForeground();
                CellFormatPart cellFormatPart = new CellFormatPart(format);
                // If this doesn't apply, no color change is expected
                Color expectedColor = cellFormatPart.apply(label, objVal).applies ? TEST_COLOR : origColor;

                String actualText = label.getText();
                Color actualColor = label.getForeground();
                valueGetter.equivalent(expectedText, actualText, cellFormatPart);
                assertEquals(expectedColor, actualColor, "no color");
            }
        }
    }
}
