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

package org.apache.poi.ss.usermodel;

import java.util.Calendar;
import java.util.Locale;

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.TestHSSFDataFormatter;

/**
 * Tests of {@link DataFormatter}
 *
 * See {@link TestHSSFDataFormatter} too for
 *  more tests.
 */
public class TestDataFormatter extends TestCase {
    /**
     * Test that we use the specified locale when deciding
     *   how to format normal numbers
     */
    public void testLocale() {
       DataFormatter dfUS = new DataFormatter(Locale.US);
       DataFormatter dfFR = new DataFormatter(Locale.FRENCH);
       
       assertEquals("1234", dfUS.formatRawCellContents(1234, -1, "@"));
       assertEquals("1234", dfFR.formatRawCellContents(1234, -1, "@"));
       
       assertEquals("12.34", dfUS.formatRawCellContents(12.34, -1, "@"));
       assertEquals("12,34", dfFR.formatRawCellContents(12.34, -1, "@"));
    }
    
    /**
     * Ensure that colours get correctly
     *  zapped from within the format strings
     */
    public void testColours() {
       DataFormatter dfUS = new DataFormatter(Locale.US);
       
       String[] formats = new String[] {
             "##.##",
             "[WHITE]##.##",
             "[BLACK]##.##;[RED]-##.##",
             "[COLOR11]##.##;[COLOR 43]-##.00",
       };
       for(String format : formats) {
          assertEquals(
                "Wrong format for: " + format,
                "12.34",
                dfUS.formatRawCellContents(12.343, -1, format)
          );
          assertEquals(
                "Wrong format for: " + format,
                "-12.34",
                dfUS.formatRawCellContents(-12.343, -1, format)
          );
       }
       
       // Ensure that random square brackets remain
       assertEquals("12.34[a]", dfUS.formatRawCellContents(12.343, -1, "##.##[a]"));
       assertEquals("[ab]12.34[x]", dfUS.formatRawCellContents(12.343, -1, "[ab]##.##[x]"));
    }
    
    public void testColoursAndBrackets() {
       DataFormatter dfUS = new DataFormatter(Locale.US);
       
       // Without currency symbols
       String[] formats = new String[] {
             "#,##0.00;[Blue](#,##0.00)",
       };
       for(String format : formats) {
          assertEquals(
                "Wrong format for: " + format,
                "12.34",
                dfUS.formatRawCellContents(12.343, -1, format)
          );
          assertEquals(
                "Wrong format for: " + format,
                "(12.34)",
                dfUS.formatRawCellContents(-12.343, -1, format)
          );
       }
       
       // With
       formats = new String[] {
             "$#,##0.00;[Red]($#,##0.00)"
       };
       for(String format : formats) {
          assertEquals(
                "Wrong format for: " + format,
                "$12.34",
                dfUS.formatRawCellContents(12.343, -1, format)
          );
          assertEquals(
                "Wrong format for: " + format,
                "($12.34)",
                dfUS.formatRawCellContents(-12.343, -1, format)
          );
       }
    }
    
    /**
     * Test how we handle negative and zeros.
     * Note - some tests are disabled as DecimalFormat
     *  and Excel differ, and workarounds are not
     *  yet in place for all of these
     */
    public void testNegativeZero() {
       DataFormatter dfUS = new DataFormatter(Locale.US);
       
       String all2dp = "00.00";
       String alln1dp = "(00.0)";
       String p1dp_n1dp = "00.0;(00.0)";
       String p2dp_n1dp = "00.00;(00.0)";
       String p2dp_n1dp_z0 = "00.00;(00.0);0";
       String all2dpTSP = "00.00_x";
       String p2dp_n2dpTSP = "00.00_x;(00.00)_x";
       String p2dp_n1dpTSP = "00.00_x;(00.0)_x";
       
       assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, all2dp));
       assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, p2dp_n1dp));
       assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, p2dp_n1dp_z0));
       
       assertEquals("(12.3)", dfUS.formatRawCellContents(12.343, -1, alln1dp));
       assertEquals("-(12.3)", dfUS.formatRawCellContents(-12.343, -1, alln1dp));
       assertEquals("12.3", dfUS.formatRawCellContents(12.343, -1, p1dp_n1dp));
       assertEquals("(12.3)", dfUS.formatRawCellContents(-12.343, -1, p1dp_n1dp));
       
       assertEquals("-12.34", dfUS.formatRawCellContents(-12.343, -1, all2dp));
       // TODO - fix case of negative subpattern differing from the
       //  positive one by more than just the prefix+suffix, which
       //  is all DecimalFormat supports...
//       assertEquals("(12.3)", dfUS.formatRawCellContents(-12.343, -1, p2dp_n1dp));
//       assertEquals("(12.3)", dfUS.formatRawCellContents(-12.343, -1, p2dp_n1dp_z0));
       
       assertEquals("00.00", dfUS.formatRawCellContents(0, -1, all2dp));
       assertEquals("00.00", dfUS.formatRawCellContents(0, -1, p2dp_n1dp));
       assertEquals("0", dfUS.formatRawCellContents(0, -1, p2dp_n1dp_z0));
       
       // Spaces are skipped
       assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, all2dpTSP));
       assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, p2dp_n2dpTSP));
       assertEquals("(12.34)", dfUS.formatRawCellContents(-12.343, -1, p2dp_n2dpTSP));
//       assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, p2dp_n1dpTSP));
//       assertEquals("(12.3)", dfUS.formatRawCellContents(-12.343, -1, p2dp_n1dpTSP));
    }
    
    /**
     * Test that _x (blank with the space taken by "x")
     *  and *x (fill to the column width with "x"s) are
     *  correctly ignored by us.
     */
    public void testPaddingSpaces() {
       DataFormatter dfUS = new DataFormatter(Locale.US);
       assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, "##.##_ "));
       assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, "##.##_1"));
       assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, "##.##_)"));
       assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, "_-##.##"));
       
       assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, "##.##* "));
       assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, "##.##*1"));
       assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, "##.##*)"));
       assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, "*-##.##"));
    }
    
    /**
     * DataFormatter is the CSV mode preserves spaces
     */
    public void testPaddingSpacesCSV() {
       DataFormatter dfUS = new DataFormatter(Locale.US, true);
       assertEquals("12.34 ", dfUS.formatRawCellContents(12.343, -1, "##.##_ "));
       assertEquals("-12.34 ", dfUS.formatRawCellContents(-12.343, -1, "##.##_ "));
       assertEquals(". ", dfUS.formatRawCellContents(0.0, -1, "##.##_ "));
       assertEquals("12.34 ", dfUS.formatRawCellContents(12.343, -1, "##.##_1"));
       assertEquals("-12.34 ", dfUS.formatRawCellContents(-12.343, -1, "##.##_1"));
       assertEquals(". ", dfUS.formatRawCellContents(0.0, -1, "##.##_1"));
       assertEquals("12.34 ", dfUS.formatRawCellContents(12.343, -1, "##.##_)"));
       assertEquals("-12.34 ", dfUS.formatRawCellContents(-12.343, -1, "##.##_)"));
       assertEquals(". ", dfUS.formatRawCellContents(0.0, -1, "##.##_)"));
       assertEquals(" 12.34", dfUS.formatRawCellContents(12.343, -1, "_-##.##"));
       assertEquals("- 12.34", dfUS.formatRawCellContents(-12.343, -1, "_-##.##"));
       assertEquals(" .", dfUS.formatRawCellContents(0.0, -1, "_-##.##"));

       assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, "##.##* "));
       assertEquals("-12.34", dfUS.formatRawCellContents(-12.343, -1, "##.##* "));
       assertEquals(".", dfUS.formatRawCellContents(0.0, -1, "##.##* "));
       assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, "##.##*1"));
       assertEquals("-12.34", dfUS.formatRawCellContents(-12.343, -1, "##.##*1"));
       assertEquals(".", dfUS.formatRawCellContents(0.0, -1, "##.##*1"));
       assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, "##.##*)"));
       assertEquals("-12.34", dfUS.formatRawCellContents(-12.343, -1, "##.##*)"));
       assertEquals(".", dfUS.formatRawCellContents(0.0, -1, "##.##*)"));
       assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, "*-##.##"));
       assertEquals("-12.34", dfUS.formatRawCellContents(-12.343, -1, "*-##.##"));
       assertEquals(".", dfUS.formatRawCellContents(0.0, -1, "*-##.##"));
    }

    /**
     * Test that the special Excel month format MMMMM
     *  gets turned into the first letter of the month
     */
    public void testMMMMM() {
       DataFormatter dfUS = new DataFormatter(Locale.US);
       
       Calendar c = Calendar.getInstance();
       c.set(Calendar.MILLISECOND, 0);
       c.set(2010, 5, 1, 2, 0, 0);
       
       assertEquals("2010-J-1 2:00:00", dfUS.formatRawCellContents(
             DateUtil.getExcelDate(c, false), -1, "YYYY-MMMMM-D h:mm:ss"
       ));
    }
    
    /**
     * Test that we can handle elapsed time,
     *  eg formatting 1 day 4 hours as 28 hours
     */
    public void testElapsedTime() {
       DataFormatter dfUS = new DataFormatter(Locale.US);

       double hour = 1.0/24.0;

       assertEquals("01:00", dfUS.formatRawCellContents(1*hour, -1, "hh:mm"));
       assertEquals("05:00", dfUS.formatRawCellContents(5*hour, -1, "hh:mm"));
       assertEquals("20:00", dfUS.formatRawCellContents(20*hour, -1, "hh:mm"));
       assertEquals("23:00", dfUS.formatRawCellContents(23*hour, -1, "hh:mm"));
       assertEquals("00:00", dfUS.formatRawCellContents(24*hour, -1, "hh:mm"));
       assertEquals("02:00", dfUS.formatRawCellContents(26*hour, -1, "hh:mm"));
       assertEquals("20:00", dfUS.formatRawCellContents(44*hour, -1, "hh:mm"));
       assertEquals("02:00", dfUS.formatRawCellContents(50*hour, -1, "hh:mm"));

       assertEquals("01:00", dfUS.formatRawCellContents(1*hour, -1, "[hh]:mm"));
       assertEquals("05:00", dfUS.formatRawCellContents(5*hour, -1, "[hh]:mm"));
       assertEquals("20:00", dfUS.formatRawCellContents(20*hour, -1, "[hh]:mm"));
       assertEquals("23:00", dfUS.formatRawCellContents(23*hour, -1, "[hh]:mm"));
       assertEquals("24:00", dfUS.formatRawCellContents(24*hour, -1, "[hh]:mm"));
       assertEquals("26:00", dfUS.formatRawCellContents(26*hour, -1, "[hh]:mm"));
       assertEquals("44:00", dfUS.formatRawCellContents(44*hour, -1, "[hh]:mm"));
       assertEquals("50:00", dfUS.formatRawCellContents(50*hour, -1, "[hh]:mm"));

       assertEquals("01", dfUS.formatRawCellContents(1*hour, -1, "[hh]"));
       assertEquals("05", dfUS.formatRawCellContents(5*hour, -1, "[hh]"));
       assertEquals("20", dfUS.formatRawCellContents(20*hour, -1, "[hh]"));
       assertEquals("23", dfUS.formatRawCellContents(23*hour, -1, "[hh]"));
       assertEquals("24", dfUS.formatRawCellContents(24*hour, -1, "[hh]"));
       assertEquals("26", dfUS.formatRawCellContents(26*hour, -1, "[hh]"));
       assertEquals("44", dfUS.formatRawCellContents(44*hour, -1, "[hh]"));
       assertEquals("50", dfUS.formatRawCellContents(50*hour, -1, "[hh]"));

       double minute = 1.0/24.0/60.0;
       assertEquals("01:00", dfUS.formatRawCellContents(1*minute, -1, "[mm]:ss"));
       assertEquals("05:00", dfUS.formatRawCellContents(5*minute, -1, "[mm]:ss"));
       assertEquals("20:00", dfUS.formatRawCellContents(20*minute, -1, "[mm]:ss"));
       assertEquals("23:00", dfUS.formatRawCellContents(23*minute, -1, "[mm]:ss"));
       assertEquals("24:00", dfUS.formatRawCellContents(24*minute, -1, "[mm]:ss"));
       assertEquals("26:00", dfUS.formatRawCellContents(26*minute, -1, "[mm]:ss"));
       assertEquals("44:00", dfUS.formatRawCellContents(44*minute, -1, "[mm]:ss"));
       assertEquals("50:00", dfUS.formatRawCellContents(50*minute, -1, "[mm]:ss"));
       assertEquals("59:00", dfUS.formatRawCellContents(59*minute, -1, "[mm]:ss"));
       assertEquals("60:00", dfUS.formatRawCellContents(60*minute, -1, "[mm]:ss"));
       assertEquals("61:00", dfUS.formatRawCellContents(61*minute, -1, "[mm]:ss"));
       assertEquals("119:00", dfUS.formatRawCellContents(119*minute, -1, "[mm]:ss"));
       assertEquals("120:00", dfUS.formatRawCellContents(120*minute, -1, "[mm]:ss"));
       assertEquals("121:00", dfUS.formatRawCellContents(121*minute, -1, "[mm]:ss"));

       assertEquals("01", dfUS.formatRawCellContents(1*minute, -1, "[mm]"));
       assertEquals("05", dfUS.formatRawCellContents(5*minute, -1, "[mm]"));
       assertEquals("20", dfUS.formatRawCellContents(20*minute, -1, "[mm]"));
       assertEquals("23", dfUS.formatRawCellContents(23*minute, -1, "[mm]"));
       assertEquals("24", dfUS.formatRawCellContents(24*minute, -1, "[mm]"));
       assertEquals("26", dfUS.formatRawCellContents(26*minute, -1, "[mm]"));
       assertEquals("44", dfUS.formatRawCellContents(44*minute, -1, "[mm]"));
       assertEquals("50", dfUS.formatRawCellContents(50*minute, -1, "[mm]"));
       assertEquals("59", dfUS.formatRawCellContents(59*minute, -1, "[mm]"));
       assertEquals("60", dfUS.formatRawCellContents(60*minute, -1, "[mm]"));
       assertEquals("61", dfUS.formatRawCellContents(61*minute, -1, "[mm]"));
       assertEquals("119", dfUS.formatRawCellContents(119*minute, -1, "[mm]"));
       assertEquals("120", dfUS.formatRawCellContents(120*minute, -1, "[mm]"));
       assertEquals("121", dfUS.formatRawCellContents(121*minute, -1, "[mm]"));

       double second = 1.0/24.0/60.0/60.0;
       assertEquals("86400", dfUS.formatRawCellContents(86400*second, -1, "[ss]"));
       assertEquals("01", dfUS.formatRawCellContents(1*second, -1, "[ss]"));
       assertEquals("05", dfUS.formatRawCellContents(5*second, -1, "[ss]"));
       assertEquals("20", dfUS.formatRawCellContents(20*second, -1, "[ss]"));
       assertEquals("23", dfUS.formatRawCellContents(23*second, -1, "[ss]"));
       assertEquals("24", dfUS.formatRawCellContents(24*second, -1, "[ss]"));
       assertEquals("26", dfUS.formatRawCellContents(26*second, -1, "[ss]"));
       assertEquals("44", dfUS.formatRawCellContents(44*second, -1, "[ss]"));
       assertEquals("50", dfUS.formatRawCellContents(50*second, -1, "[ss]"));
       assertEquals("59", dfUS.formatRawCellContents(59*second, -1, "[ss]"));
       assertEquals("60", dfUS.formatRawCellContents(60*second, -1, "[ss]"));
       assertEquals("61", dfUS.formatRawCellContents(61*second, -1, "[ss]"));
       assertEquals("119", dfUS.formatRawCellContents(119*second, -1, "[ss]"));
       assertEquals("120", dfUS.formatRawCellContents(120*second, -1, "[ss]"));
       assertEquals("121", dfUS.formatRawCellContents(121*second, -1, "[ss]"));

       assertEquals("27:18:08", dfUS.formatRawCellContents(1.1376, -1, "[h]:mm:ss"));
       assertEquals("28:48:00", dfUS.formatRawCellContents(1.2, -1,  "[h]:mm:ss"));
       assertEquals("29:31:12", dfUS.formatRawCellContents(1.23, -1, "[h]:mm:ss"));
       assertEquals("31:26:24", dfUS.formatRawCellContents(1.31, -1, "[h]:mm:ss"));

       assertEquals("27:18:08", dfUS.formatRawCellContents(1.1376, -1, "[hh]:mm:ss"));
       assertEquals("28:48:00", dfUS.formatRawCellContents(1.2, -1,  "[hh]:mm:ss"));
       assertEquals("29:31:12", dfUS.formatRawCellContents(1.23, -1, "[hh]:mm:ss"));
       assertEquals("31:26:24", dfUS.formatRawCellContents(1.31, -1, "[hh]:mm:ss"));

       assertEquals("57:07.2", dfUS.formatRawCellContents(.123, -1, "mm:ss.0;@"));
       assertEquals("57:41.8", dfUS.formatRawCellContents(.1234, -1, "mm:ss.0;@"));
       assertEquals("57:41.76", dfUS.formatRawCellContents(.1234, -1, "mm:ss.00;@"));
       assertEquals("57:41.760", dfUS.formatRawCellContents(.1234, -1, "mm:ss.000;@"));
       assertEquals("24:00.0", dfUS.formatRawCellContents(123456.6, -1, "mm:ss.0"));
    }

    public void testDateWindowing() {
       DataFormatter dfUS = new DataFormatter(Locale.US);
       
       assertEquals("1899-12-31 00:00:00", dfUS.formatRawCellContents(0.0, -1, "yyyy-mm-dd hh:mm:ss"));
       assertEquals("1899-12-31 00:00:00", dfUS.formatRawCellContents(0.0, -1, "yyyy-mm-dd hh:mm:ss", false));
       assertEquals("1904-01-01 00:00:00", dfUS.formatRawCellContents(0.0, -1, "yyyy-mm-dd hh:mm:ss", true));
    }

    public void testScientificNotation() {
        DataFormatter dfUS = new DataFormatter(Locale.US);

        assertEquals("1.23E+01", dfUS.formatRawCellContents(12.343, -1, "0.00E+00"));
        assertEquals("-1.23E+01", dfUS.formatRawCellContents(-12.343, -1, "0.00E+00"));
        assertEquals("0.00E+00", dfUS.formatRawCellContents(0.0, -1, "0.00E+00"));
     }

    public void testInvalidDate() {
        DataFormatter df1 = new DataFormatter(Locale.US);
        assertEquals("-1.0", df1.formatRawCellContents(-1, -1, "mm/dd/yyyy"));

        DataFormatter df2 = new DataFormatter(Locale.US, true);
        assertEquals("###############################################################################################################################################################################################################################################################",
                df2.formatRawCellContents(-1, -1, "mm/dd/yyyy"));
    }

    public void testEscapes() {
       DataFormatter dfUS = new DataFormatter(Locale.US);

       assertEquals("1901-01-01", dfUS.formatRawCellContents(367.0, -1, "yyyy-mm-dd"));
       assertEquals("1901-01-01", dfUS.formatRawCellContents(367.0, -1, "yyyy\\-mm\\-dd"));
       
       assertEquals("1901.01.01", dfUS.formatRawCellContents(367.0, -1, "yyyy.mm.dd"));
       assertEquals("1901.01.01", dfUS.formatRawCellContents(367.0, -1, "yyyy\\.mm\\.dd"));
       
       assertEquals("1901/01/01", dfUS.formatRawCellContents(367.0, -1, "yyyy/mm/dd"));
       assertEquals("1901/01/01", dfUS.formatRawCellContents(367.0, -1, "yyyy\\/mm\\/dd"));
    }

    public void testOther() {
        DataFormatter dfUS = new DataFormatter(Locale.US, true);

        assertEquals(" 12.34 ", dfUS.formatRawCellContents(12.34, -1, "_-* #,##0.00_-;-* #,##0.00_-;_-* \"-\"??_-;_-@_-"));
        assertEquals("-12.34 ", dfUS.formatRawCellContents(-12.34, -1, "_-* #,##0.00_-;-* #,##0.00_-;_-* \"-\"??_-;_-@_-"));
        assertEquals(" -   ", dfUS.formatRawCellContents(0.0, -1, "_-* #,##0.00_-;-* #,##0.00_-;_-* \"-\"??_-;_-@_-"));
        assertEquals(" $-   ", dfUS.formatRawCellContents(0.0, -1, "_-$* #,##0.00_-;-$* #,##0.00_-;_-$* \"-\"??_-;_-@_-"));
    }
}
