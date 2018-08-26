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
package org.apache.poi.ss.format;

import static org.junit.Assert.assertEquals;

import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/** Test the individual CellFormatPart types. */
public class TestCellFormatPart extends CellFormatTestBase {
    
    private static Locale userLocale;
    
    @BeforeClass
    public static void setLocale() {
        userLocale = LocaleUtil.getUserLocale();
        LocaleUtil.setUserLocale(Locale.ROOT);
    }
    
    @AfterClass
    public static void unsetLocale() {
        LocaleUtil.setUserLocale(userLocale);
    }
    
    private static final Pattern NUMBER_EXTRACT_FMT = Pattern.compile(
            "([-+]?[0-9]+)(\\.[0-9]+)?.*(?:(e).*?([+-]?[0-9]+))",
            Pattern.CASE_INSENSITIVE);

    public TestCellFormatPart() {
        super(XSSFITestDataProvider.instance);
    }

    @Test
    public void testGeneralFormat() {
        runFormatTests("GeneralFormatTests.xlsx", new CellValue() {
            @Override
            public Object getValue(Cell cell) {
                switch (CellFormat.ultimateType(cell)) {
                    case BOOLEAN:
                        return cell.getBooleanCellValue();
                    case NUMERIC:
                        return cell.getNumericCellValue();
                    default:
                        return cell.getStringCellValue();
                }
            }
        });
    }

    @Test
    public void testNumberFormat() {
        runFormatTests("NumberFormatTests.xlsx", new CellValue() {
            @Override
            public Object getValue(Cell cell) {
                return cell.getNumericCellValue();
            }
        });
    }

    @Test
    public void testNumberApproxFormat() {
        runFormatTests("NumberFormatApproxTests.xlsx", new CellValue() {
            @Override
            public Object getValue(Cell cell) {
                return cell.getNumericCellValue();
            }

            @Override
            void equivalent(String expected, String actual,
                    CellFormatPart format) {
                double expectedVal = extractNumber(expected);
                double actualVal = extractNumber(actual);
                // equal within 1%
                double delta = expectedVal / 100;
                assertEquals("format \"" + format + "\"," + expected + " ~= " +
                        actual, expectedVal, actualVal, delta);
            }
        });
    }

    @Test
    public void testDateFormat() {
        TimeZone tz = LocaleUtil.getUserTimeZone();
        LocaleUtil.setUserTimeZone(TimeZone.getTimeZone("CET"));
        try {
            runFormatTests("DateFormatTests.xlsx", new CellValue() {
                @Override
                public Object getValue(Cell cell) {
                    return cell.getDateCellValue();
                }
            });
        } finally {
            LocaleUtil.setUserTimeZone(tz);
        }
    }

    @Test
    public void testElapsedFormat() {
        runFormatTests("ElapsedFormatTests.xlsx", new CellValue() {
            @Override
            public Object getValue(Cell cell) {
                return cell.getNumericCellValue();
            }
        });
    }

    @Test
    public void testTextFormat() {
        runFormatTests("TextFormatTests.xlsx", new CellValue() {
            @Override
            public Object getValue(Cell cell) {
                switch(CellFormat.ultimateType(cell)) {
                    case BOOLEAN:
                        return cell.getBooleanCellValue();
                    default:
                        return cell.getStringCellValue();
                }
            }
        });
    }

    @Test
    public void testConditions() {
        runFormatTests("FormatConditionTests.xlsx", new CellValue() {
            @Override
            Object getValue(Cell cell) {
                return cell.getNumericCellValue();
            }
        });
    }

    private double extractNumber(String str) {
        Matcher m = NUMBER_EXTRACT_FMT.matcher(str);
        if (!m.find())
            throw new IllegalArgumentException(
                    "Cannot find number in \"" + str + "\"");

        StringBuilder sb = new StringBuilder();
        // The groups in the pattern are the parts of the number
        for (int i = 1; i <= m.groupCount(); i++) {
            String part = m.group(i);
            if (part != null)
                sb.append(part);
        }
        return Double.valueOf(sb.toString());
    }
}
