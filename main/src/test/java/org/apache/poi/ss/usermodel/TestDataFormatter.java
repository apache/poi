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

   2012 - Alfresco Software, Ltd.
   Alfresco Software has modified source of this file
   The details of changes as svn diff can be found in svn at location root/projects/3rd-party/src
==================================================================== */

package org.apache.poi.ss.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.TestHSSFDataFormatter;
import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.format.CellFormatResult;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.SuppressForbidden;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests of {@link DataFormatter}
 *
 * See {@link TestHSSFDataFormatter} too for
 *  more tests.
 */
class TestDataFormatter {
    private static final double _15_MINUTES = 0.041666667;

    @BeforeAll
    @SuppressForbidden
    public static void setUpClass() {
        // some pre-checks to hunt for a problem in the Maven build
        // these checks ensure that the correct locale is set, so a failure here
        // usually indicates an invalid locale during test-execution

        assertFalse(DateUtil.isADateFormat(-1, "_-* #,##0.00_-;-* #,##0.00_-;_-* \"-\"??_-;_-@_-"));
        Locale ul = LocaleUtil.getUserLocale();
        assertTrue(Locale.ROOT.equals(ul) || Locale.getDefault().equals(ul));
        final String textValue = NumberToTextConverter.toText(1234.56);
        assertEquals(-1, textValue.indexOf('E'));
        Object cellValueO = Double.valueOf(1234.56);

        /*CellFormat cellFormat = new CellFormat("_-* #,##0.00_-;-* #,##0.00_-;_-* \"-\"??_-;_-@_-");
        CellFormatResult result = cellFormat.apply(cellValueO);
        assertEquals("    1,234.56 ", result.text);*/

        CellFormat cfmt = CellFormat.getInstance("_-* #,##0.00_-;-* #,##0.00_-;_-* \"-\"??_-;_-@_-");
        CellFormatResult result = cfmt.apply(cellValueO);
        assertEquals("    1,234.56 ", result.text,
            "This failure can indicate that the wrong locale is used during test-execution, ensure you run with english/US via -Duser.language=en -Duser.country=US");
    }

    /**
     * Test that we use the specified locale when deciding
     *   how to format normal numbers
     */
    @Test
    void testLocale() {
        DataFormatter dfUS = new DataFormatter(Locale.US);
        DataFormatter dfFR = new DataFormatter(Locale.FRENCH);

        assertEquals("1234", dfUS.formatRawCellContents(1234, -1, "@"));
        assertEquals("1234", dfFR.formatRawCellContents(1234, -1, "@"));

        assertEquals("12.34", dfUS.formatRawCellContents(12.34, -1, "@"));
        assertEquals("12,34", dfFR.formatRawCellContents(12.34, -1, "@"));
    }

    /**
     * At the moment, we don't decode the locale strings into
     *  a specific locale, but we should format things as if
     *  the locale (eg '[$-1010409]') isn't there
     */
    @Test
    void testLocaleBasedFormats() {
        DataFormatter dfUS = new DataFormatter(Locale.US);

        // Standard formats
        assertEquals("63", dfUS.formatRawCellContents(63.0, -1, "[$-1010409]General"));
        assertEquals("63", dfUS.formatRawCellContents(63.0, -1, "[$-1010409]@"));

        // Regular numeric style formats
        assertEquals("63", dfUS.formatRawCellContents(63.0, -1, "[$-1010409]##"));
        assertEquals("63", dfUS.formatRawCellContents(63.0, -1, "[$-1010409]00"));

    }


    /**
     * Test that we use the specified locale when deciding
     *   how to format normal numbers
     */
    @Test
    void testGrouping() {
        DataFormatter dfUS = new DataFormatter(Locale.US);
        DataFormatter dfDE = new DataFormatter(Locale.GERMAN);

        assertEquals("1,234.57", dfUS.formatRawCellContents(1234.567, -1, "#,##0.00"));
        assertEquals("1'234.57", dfUS.formatRawCellContents(1234.567, -1, "#'##0.00"));
        assertEquals("1 234.57", dfUS.formatRawCellContents(1234.567, -1, "# ##0.00"));

        assertEquals("1.234,57", dfDE.formatRawCellContents(1234.567, -1, "#,##0.00"));
        assertEquals("1'234,57", dfDE.formatRawCellContents(1234.567, -1, "#'##0.00"));
        assertEquals("1 234,57", dfDE.formatRawCellContents(1234.567, -1, "# ##0.00"));
    }

    /**
     * Ensure that colours get correctly
     *  zapped from within the format strings
     */
    @Test
    void testColours() {
        DataFormatter dfUS = new DataFormatter(Locale.US);

        String[] formats = {
                "##.##",
                "[WHITE]##.##",
                "[BLACK]##.##;[RED]-##.##",
                "[COLOR11]##.##;[COLOR 43]-##.00",
        };
        for (String format : formats) {
            assertEquals(
                "12.34",
                dfUS.formatRawCellContents(12.343, -1, format),
                "Wrong format for: " + format
            );
            assertEquals(
                "-12.34",
                dfUS.formatRawCellContents(-12.343, -1, format),
                "Wrong format for: " + format
            );
        }

        // Ensure that random square brackets remain
        assertEquals("12.34[a]", dfUS.formatRawCellContents(12.343, -1, "##.##[a]"));
        assertEquals("[ab]12.34[x]", dfUS.formatRawCellContents(12.343, -1, "[ab]##.##[x]"));
    }

    @Test
    void testColoursAndBrackets() {
        DataFormatter dfUS = new DataFormatter(Locale.US);

        // Without currency symbols
        String[] formats = { "#,##0.00;[Blue](#,##0.00)" };
        for (String format : formats) {
            assertEquals(
                "12.34",
                dfUS.formatRawCellContents(12.343, -1, format),
                "Wrong format for: " + format
            );
            assertEquals(
                "(12.34)",
                dfUS.formatRawCellContents(-12.343, -1, format),
                "Wrong format for: " + format
            );
        }

        // With
        formats = new String[] { "$#,##0.00;[Red]($#,##0.00)" };
        for (String format : formats) {
            assertEquals(
                "$12.34",
                dfUS.formatRawCellContents(12.343, -1, format),
                "Wrong format for: " + format
            );
            assertEquals(
                "($12.34)",
                dfUS.formatRawCellContents(-12.343, -1, format),
                "Wrong format for: " + format
            );
        }
    }

    @Test
    void testConditionalRanges() {
        DataFormatter dfUS = new DataFormatter(Locale.US);

        String format = "[>=10]#,##0;[<10]0.0";
        assertEquals("17,876", dfUS.formatRawCellContents(17876.000, -1, format), "Wrong format for " + format);
        assertEquals("9.7", dfUS.formatRawCellContents(9.71, -1, format), "Wrong format for " + format);
    }

    /**
     * Test how we handle negative and zeros.
     * Note - some tests are disabled as DecimalFormat
     *  and Excel differ, and workarounds are not
     *  yet in place for all of these
     */
    @Test
    void testNegativeZero() {
        DataFormatter dfUS = new DataFormatter(Locale.US);

        String all2dp = "00.00";
        String alln1dp = "(00.0)";
        String p1dp_n1dp = "00.0;(00.0)";
        String p2dp_n1dp = "00.00;(00.0)";
        String p2dp_n1dp_z0 = "00.00;(00.0);0";
        String all2dpTSP = "00.00_x";
        String p2dp_n2dpTSP = "00.00_x;(00.00)_x";
        //String p2dp_n1dpTSP = "00.00_x;(00.0)_x";

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
//        assertEquals("(12.3)", dfUS.formatRawCellContents(-12.343, -1, p2dp_n1dp));
//        assertEquals("(12.3)", dfUS.formatRawCellContents(-12.343, -1, p2dp_n1dp_z0));

        assertEquals("00.00", dfUS.formatRawCellContents(0, -1, all2dp));
        assertEquals("00.00", dfUS.formatRawCellContents(0, -1, p2dp_n1dp));
        assertEquals("0", dfUS.formatRawCellContents(0, -1, p2dp_n1dp_z0));

        // Spaces are skipped
        assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, all2dpTSP));
        assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, p2dp_n2dpTSP));
        assertEquals("(12.34)", dfUS.formatRawCellContents(-12.343, -1, p2dp_n2dpTSP));
//        assertEquals("12.34", dfUS.formatRawCellContents(12.343, -1, p2dp_n1dpTSP));
//        assertEquals("(12.3)", dfUS.formatRawCellContents(-12.343, -1, p2dp_n1dpTSP));
    }

    /**
     * Test that we correctly handle fractions in the
     *  format string, eg # #/#
     */
    @Test
    void testFractions() {
        DataFormatter dfUS = new DataFormatter(Locale.US);

        // Excel often prefers "# #/#"
        assertEquals("321 1/3",   dfUS.formatRawCellContents(321.321, -1, "# #/#"));
        assertEquals("321 26/81", dfUS.formatRawCellContents(321.321, -1, "# #/##"));
        assertEquals("26027/81",  dfUS.formatRawCellContents(321.321, -1, "#/##"));

        // OOo seems to like the "# ?/?" form
        assertEquals("321 1/3",   dfUS.formatRawCellContents(321.321, -1, "# ?/?"));
        assertEquals("321 26/81", dfUS.formatRawCellContents(321.321, -1, "# ?/??"));
        assertEquals("26027/81",  dfUS.formatRawCellContents(321.321, -1, "?/??"));

        // p;n;z;s parts
        assertEquals("321 1/3",  dfUS.formatRawCellContents(321.321,  -1, "# #/#;# ##/#;0;xxx"));
        assertEquals("321 1/3",  dfUS.formatRawCellContents(-321.321, -1, "# #/#;# ##/#;0;xxx")); // Note the lack of - sign!
        assertEquals("0",        dfUS.formatRawCellContents(0,       -1, "# #/#;# ##/#;0;xxx"));
//        assertEquals(".",        dfUS.formatRawCellContents(0,       -1, "# #/#;# ##/#;#.#;xxx")); // Currently shows as 0. not .

        // Custom formats with text
        assertEquals("+ve",       dfUS.formatRawCellContents(1,        -1, "+ve;-ve;zero;xxx"));
        assertEquals("-ve",       dfUS.formatRawCellContents(-1,       -1, "-ve;-ve;zero;xxx"));
        assertEquals("zero",      dfUS.formatRawCellContents(0,        -1, "zero;-ve;zero;xxx"));

        // Custom formats - check text is stripped, including multiple spaces
        assertEquals("321 1/3",   dfUS.formatRawCellContents(321.321, -1, "#   #/#"));
        assertEquals("321 1/3",   dfUS.formatRawCellContents(321.321, -1, "#\"  \" #/#"));
        assertEquals("321 1/3",   dfUS.formatRawCellContents(321.321, -1, "#\"FRED\" #/#"));
        assertEquals("321 1/3",   dfUS.formatRawCellContents(321.321, -1, "#\\ #/#"));
        assertEquals("321 1/3",   dfUS.formatRawCellContents(321.321, -1, "# \\q#/#"));

        // Cases that were very slow
        assertEquals("321 1/3",   dfUS.formatRawCellContents(321.321, -1, "0\" \"?/?;?/?")); // 0" "?/?;?/?     - length of -ve part was used
        assertEquals("321 1/3",   dfUS.formatRawCellContents(321.321, -1, "0 \"#\"\\#\\#?/?")); // 0 "#"\#\#?/? - length of text was used

        assertEquals("321 295/919",  dfUS.formatRawCellContents(321.321, -1, "# #/###"));
        assertEquals("321 321/1000",  dfUS.formatRawCellContents(321.321, -1, "# #/####")); // Code limits to #### as that is as slow as we want to get
        assertEquals("321 321/1000",  dfUS.formatRawCellContents(321.321, -1, "# #/##########"));

        // Not a valid fraction formats (too many #/# or ?/?) - hence the strange expected results
/*
        assertEquals("321 / ?/?",   dfUS.formatRawCellContents(321.321, -1, "# #/# ?/?"));
        assertEquals("321 / /",     dfUS.formatRawCellContents(321.321, -1, "# #/# #/#"));
        assertEquals("321 ?/? ?/?",   dfUS.formatRawCellContents(321.321, -1, "# ?/? ?/?"));
        assertEquals("321 ?/? / /",   dfUS.formatRawCellContents(321.321, -1, "# ?/? #/# #/#"));
*/

        //Bug54686 patch sets default behavior of # #/## if there is a failure to parse
        assertEquals("321 1/3",   dfUS.formatRawCellContents(321.321, -1, "# #/# ?/?"));
        assertEquals("321 1/3",     dfUS.formatRawCellContents(321.321, -1, "# #/# #/#"));
        assertEquals("321 1/3",   dfUS.formatRawCellContents(321.321, -1, "# ?/? ?/?"));
        assertEquals("321 1/3",   dfUS.formatRawCellContents(321.321, -1, "# ?/? #/# #/#"));

        // Where +ve has a fraction, but -ve doesn't, we currently show both
        assertEquals("123 1/3", dfUS.formatRawCellContents( 123.321, -1, "0 ?/?;0"));
        //assertEquals("123",     dfUS.formatRawCellContents(-123.321, -1, "0 ?/?;0"));

        //Bug54868 patch has a hit on the first string before the ";"
        assertEquals("-123 1/3", dfUS.formatRawCellContents(-123.321, -1, "0 ?/?;0"));
        assertEquals("123 1/3", dfUS.formatRawCellContents(123.321, -1, "0 ?/?;0"));

        //Bug53150 formatting a whole number with fractions should just give the number
        assertEquals("1",   dfUS.formatRawCellContents(1.0, -1, "# #/#"));
        assertEquals("11",   dfUS.formatRawCellContents(11.0, -1, "# #/#"));
    }

    /**
     * Test that _x (blank with the space taken by "x")
     *  and *x (fill to the column width with "x"s) are
     *  correctly ignored by us.
     */
    @Test
    void testPaddingSpaces() {
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
    @Test
    void testPaddingSpacesCSV() {
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
    @Test
    void testMMMMM() {
        DataFormatter dfUS = new DataFormatter(Locale.US);

        Calendar c = LocaleUtil.getLocaleCalendar(2010, 5, 1, 2, 0, 0);

        assertEquals("2010-J-1 2:00:00", dfUS.formatRawCellContents(
                DateUtil.getExcelDate(c, false), -1, "YYYY-MMMMM-D h:mm:ss"
        ));
    }

    /**
     * Tests that we do AM/PM handling properly
     */
    @Test
    void testAMPM() {
        DataFormatter dfUS = new DataFormatter(Locale.US);

        assertEquals("06:00", dfUS.formatRawCellContents(0.25, -1, "hh:mm"));
        assertEquals("18:00", dfUS.formatRawCellContents(0.75, -1, "hh:mm"));

        assertEquals("06:00 AM", dfUS.formatRawCellContents(0.25, -1, "hh:mm AM/PM"));
        assertEquals("06:00 PM", dfUS.formatRawCellContents(0.75, -1, "hh:mm AM/PM"));

        assertEquals("1904-01-01 06:00:00 AM", dfUS.formatRawCellContents(0.25, -1, "yyyy-mm-dd hh:mm:ss AM/PM", true));
        assertEquals("1904-01-01 06:00:00 PM", dfUS.formatRawCellContents(0.75, -1, "yyyy-mm-dd hh:mm:ss AM/PM", true));
    }

    /**
     * Test that we can handle elapsed time,
     *  eg formatting 1 day 4 hours as 28 hours
     */
    @Test
    void testElapsedTime() {
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

    @Test
    void testDateWindowing() {
        DataFormatter dfUS = new DataFormatter(Locale.US);

        assertEquals("1899-12-31 00:00:00", dfUS.formatRawCellContents(0.0, -1, "yyyy-mm-dd hh:mm:ss"));
        assertEquals("1899-12-31 00:00:00", dfUS.formatRawCellContents(0.0, -1, "yyyy-mm-dd hh:mm:ss", false));
        assertEquals("1904-01-01 00:00:00", dfUS.formatRawCellContents(0.0, -1, "yyyy-mm-dd hh:mm:ss", true));
    }

    @Test
    void testScientificNotation() {
        DataFormatter dfUS = new DataFormatter(Locale.US);

        assertEquals("1.23E+01", dfUS.formatRawCellContents(12.343, -1, "0.00E+00"));
        assertEquals("-1.23E+01", dfUS.formatRawCellContents(-12.343, -1, "0.00E+00"));
        assertEquals("0.00E+00", dfUS.formatRawCellContents(0.0, -1, "0.00E+00"));
    }

    @Test
    void testInvalidDate() {
        DataFormatter df1 = new DataFormatter(Locale.US);
        assertEquals("-1.0", df1.formatRawCellContents(-1, -1, "mm/dd/yyyy"));

        DataFormatter df2 = new DataFormatter(Locale.US, true);
        assertEquals("###############################################################################################################################################################################################################################################################",
                df2.formatRawCellContents(-1, -1, "mm/dd/yyyy"));
    }

    @Test
    void testEscapes() {
        DataFormatter dfUS = new DataFormatter(Locale.US);

        assertEquals("1901-01-01", dfUS.formatRawCellContents(367.0, -1, "yyyy-mm-dd"));
        assertEquals("1901-01-01", dfUS.formatRawCellContents(367.0, -1, "yyyy\\-mm\\-dd"));

        assertEquals("1901.01.01", dfUS.formatRawCellContents(367.0, -1, "yyyy.mm.dd"));
        assertEquals("1901.01.01", dfUS.formatRawCellContents(367.0, -1, "yyyy\\.mm\\.dd"));

        assertEquals("1901/01/01", dfUS.formatRawCellContents(367.0, -1, "yyyy/mm/dd"));
        assertEquals("1901/01/01", dfUS.formatRawCellContents(367.0, -1, "yyyy\\/mm\\/dd"));
    }

    @Test
    void testFormatsWithPadding() {
        DataFormatter dfUS = new DataFormatter(Locale.US, true);

        // These request space-padding, based on the cell width
        // There should always be one space after, variable (non-zero) amount before
        // Because the Cell Width isn't available, this gets emulated with
        //  4 leading spaces, or a minus then 3 leading spaces
        // This isn't all that consistent, but it's the best we can really manage...
        assertEquals("    1,234.56 ", dfUS.formatRawCellContents( 1234.56, -1, "_-* #,##0.00_-;-* #,##0.00_-;_-* \"-\"??_-;_-@_-"));
        assertEquals("-   1,234.56 ", dfUS.formatRawCellContents(-1234.56, -1, "_-* #,##0.00_-;-* #,##0.00_-;_-* \"-\"??_-;_-@_-"));
        assertEquals("    12.34 ", dfUS.formatRawCellContents( 12.34, -1, "_-* #,##0.00_-;-* #,##0.00_-;_-* \"-\"??_-;_-@_-"));
        assertEquals("-   12.34 ", dfUS.formatRawCellContents(-12.34, -1, "_-* #,##0.00_-;-* #,##0.00_-;_-* \"-\"??_-;_-@_-"));

        assertEquals("    0.10 ", dfUS.formatRawCellContents( 0.1, -1, "_-* #,##0.00_-;-* #,##0.00_-;_-* \"-\"??_-;_-@_-"));
        assertEquals("-   0.10 ", dfUS.formatRawCellContents(-0.1, -1, "_-* #,##0.00_-;-* #,##0.00_-;_-* \"-\"??_-;_-@_-"));
        // TODO Fix this, we are randomly adding a 0 at the end that shouldn't be there
        //assertEquals("     -   ", dfUS.formatRawCellContents(0.0, -1, "_-* #,##0.00_-;-* #,##0.00_-;_-* \"-\"??_-;_-@_-"));

        assertEquals(" $   1.10 ", dfUS.formatRawCellContents( 1.1, -1, "_-$* #,##0.00_-;-$* #,##0.00_-;_-$* \"-\"??_-;_-@_-"));
        assertEquals("-$   1.10 ", dfUS.formatRawCellContents(-1.1, -1, "_-$* #,##0.00_-;-$* #,##0.00_-;_-$* \"-\"??_-;_-@_-"));
        // TODO Fix this, we are randomly adding a 0 at the end that shouldn't be there
        //assertEquals(" $    -   ", dfUS.formatRawCellContents( 0.0, -1, "_-$* #,##0.00_-;-$* #,##0.00_-;_-$* \"-\"??_-;_-@_-"));
    }

    @Test
    void testErrors() throws IOException {
        DataFormatter dfUS = new DataFormatter(Locale.US, true);

        // Create a spreadsheet with some formula errors in it
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet s = wb.createSheet();
            Row r = s.createRow(0);
            Cell c = r.createCell(0, CellType.ERROR);

            c.setCellErrorValue(FormulaError.DIV0.getCode());
            assertEquals(FormulaError.DIV0.getString(), dfUS.formatCellValue(c));

            c.setCellErrorValue(FormulaError.REF.getCode());
            assertEquals(FormulaError.REF.getString(), dfUS.formatCellValue(c));
        }
    }

    @Test
    void testBoolean() throws IOException {
        DataFormatter formatter = new DataFormatter();

        // Create a spreadsheet with some TRUE/FALSE boolean values in it
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet s = wb.createSheet();
            Row r = s.createRow(0);
            Cell c = r.createCell(0);

            c.setCellValue(true);
            assertEquals("TRUE", formatter.formatCellValue(c));

            c.setCellValue(false);
            assertEquals("FALSE", formatter.formatCellValue(c));
        }
    }

    /**
     * While we don't currently support using a locale code at
     *  the start of a format string to format it differently, we
     *  should at least handle it as it if wasn't there
     */
    @Test
    void testDatesWithLocales() {
        DataFormatter dfUS = new DataFormatter(Locale.US, true);

        String dateFormatEnglish = "[$-409]mmmm dd yyyy  h:mm AM/PM";
        String dateFormatChinese = "[$-804]mmmm dd yyyy  h:mm AM/PM";

        // Check we format the English one correctly
        double date = 26995.477777777778;
        assertEquals(
            "November 27 1973  11:28 AM",
            dfUS.formatRawCellContents(date, -1, dateFormatEnglish)
        );

        // Check that, in the absence of locale support, we handle
        //  the Chinese one the same as the English one
        assertEquals(
            "November 27 1973  11:28 AM",
            dfUS.formatRawCellContents(date, -1, dateFormatChinese)
        );
    }

    /**
     * TODO Fix these so that they work
     */
    @Test
    @Disabled
    void testCustomFormats() {
        DataFormatter dfUS = new DataFormatter(Locale.US, true);
        String fmt;

        fmt = "\"At\" H:MM AM/PM \"on\" DDDD MMMM D\",\" YYYY";
        assertEquals(
            "At 4:20 AM on Thursday May 17, 2007",
            dfUS.formatRawCellContents(39219.1805636921, -1, fmt)
        );

        fmt = "0 \"dollars and\" .00 \"cents\"";
        assertEquals("19 dollars and .99 cents", dfUS.formatRawCellContents(19.99, -1, fmt));
    }

    /**
     * ExcelStyleDateFormatter should work for Milliseconds too
     */
    @Test
    void testExcelStyleDateFormatterStringOnMillis() {
        // Test directly with the .000 style
        DateFormat formatter1 = new ExcelStyleDateFormatter("ss.000");

        assertEquals("00.001", formatter1.format(new Date(1L)));
        assertEquals("00.010", formatter1.format(new Date(10L)));
        assertEquals("00.100", formatter1.format(new Date(100L)));
        assertEquals("01.000", formatter1.format(new Date(1000L)));
        assertEquals("01.001", formatter1.format(new Date(1001L)));
        assertEquals("10.000", formatter1.format(new Date(10000L)));
        assertEquals("10.001", formatter1.format(new Date(10001L)));

        // Test directly with the .SSS style
        DateFormat formatter2 = new ExcelStyleDateFormatter("ss.SSS");

        assertEquals("00.001", formatter2.format(new Date(1L)));
        assertEquals("00.010", formatter2.format(new Date(10L)));
        assertEquals("00.100", formatter2.format(new Date(100L)));
        assertEquals("01.000", formatter2.format(new Date(1000L)));
        assertEquals("01.001", formatter2.format(new Date(1001L)));
        assertEquals("10.000", formatter2.format(new Date(10000L)));
        assertEquals("10.001", formatter2.format(new Date(10001L)));


        // Test via DataFormatter
        DataFormatter dfUS = new DataFormatter(Locale.US, true);
        assertEquals("01.010", dfUS.formatRawCellContents(0.0000116898, -1, "ss.000"));
    }

    @Test
    void testBug54786() {
        DataFormatter formatter = new DataFormatter();
        String format = "[h]\"\"h\"\" m\"\"m\"\"";
        assertTrue(DateUtil.isADateFormat(-1,format));
        assertTrue(DateUtil.isValidExcelDate(_15_MINUTES));

        assertEquals("1h 0m", formatter.formatRawCellContents(_15_MINUTES, -1, format, false));
        assertEquals("0.041666667", formatter.formatRawCellContents(_15_MINUTES, -1, "[h]'h'", false));
        assertEquals("1h 0m\"", formatter.formatRawCellContents(_15_MINUTES, -1, "[h]\"\"h\"\" m\"\"m\"\"\"", false));
        assertEquals("1h", formatter.formatRawCellContents(_15_MINUTES, -1, "[h]\"\"h\"\"", false));
        assertEquals("h1", formatter.formatRawCellContents(_15_MINUTES, -1, "\"\"h\"\"[h]", false));
        assertEquals("h1", formatter.formatRawCellContents(_15_MINUTES, -1, "\"\"h\"\"h", false));
        assertEquals(" 60", formatter.formatRawCellContents(_15_MINUTES, -1, " [m]", false));
        assertEquals("h60", formatter.formatRawCellContents(_15_MINUTES, -1, "\"\"h\"\"[m]", false));
        assertEquals("m1", formatter.formatRawCellContents(_15_MINUTES, -1, "\"\"m\"\"h", false));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
            formatter.formatRawCellContents(_15_MINUTES, -1, "[h]\"\"h\"\" m\"\"m\"\"\"\"", false),
            "Catches exception because of invalid format, i.e. trailing quoting");
        assertTrue(e.getMessage().contains("Cannot format given Object as a Number"));
    }

    @Test
    void testIsADateFormat() {
        // first check some cases that should not be a date, also call multiple times to ensure the cache is used
        assertFalse(DateUtil.isADateFormat(-1, null));
        assertFalse(DateUtil.isADateFormat(-1, null));
        assertFalse(DateUtil.isADateFormat(123, null));
        assertFalse(DateUtil.isADateFormat(123, ""));
        assertFalse(DateUtil.isADateFormat(124, ""));
        assertFalse(DateUtil.isADateFormat(-1, ""));
        assertFalse(DateUtil.isADateFormat(-1, ""));
        assertFalse(DateUtil.isADateFormat(-1, "nodateformat"));

        // then also do the same for some valid date formats
        assertTrue(DateUtil.isADateFormat(0x0e, null));
        assertTrue(DateUtil.isADateFormat(0x2f, null));
        assertTrue(DateUtil.isADateFormat(-1, "yyyy"));
        assertTrue(DateUtil.isADateFormat(-1, "yyyy"));
        assertTrue(DateUtil.isADateFormat(-1, "dd/mm/yy;[red]dd/mm/yy"));
        assertTrue(DateUtil.isADateFormat(-1, "dd/mm/yy;[red]dd/mm/yy"));
        assertTrue(DateUtil.isADateFormat(-1, "[h]"));
    }


    @Test
    void testLargeNumbersAndENotation() throws IOException{
        assertFormatsTo("1E+86", 99999999999999999999999999999999999999999999999999999999999999999999999999999999999999d);
        assertFormatsTo("1E-84", 0.000000000000000000000000000000000000000000000000000000000000000000000000000000000001d);
        // Smallest double
        assertFormatsTo("1E-323", 0.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001d);

        // "up to 11 numeric characters, with the decimal point counting as a numeric character"
        // https://support.microsoft.com/en-us/kb/65903
        assertFormatsTo( "12345678911",   12345678911d);
        assertFormatsTo( "1.23457E+11",   123456789112d);  // 12th digit of integer -> scientific
        assertFormatsTo( "-12345678911", -12345678911d);
        assertFormatsTo( "-1.23457E+11", -123456789112d);
        assertFormatsTo( "0.1",           0.1);
        assertFormatsTo( "0.000000001",   0.000000001);
        assertFormatsTo( "1E-10",         0.0000000001);  // 12th digit
        assertFormatsTo( "-0.000000001", -0.000000001);
        assertFormatsTo( "-1E-10",       -0.0000000001);
        assertFormatsTo( "123.4567892",   123.45678919);  // excess decimals are simply rounded away
        assertFormatsTo("-123.4567892",  -123.45678919);
        assertFormatsTo( "1.234567893",   1.2345678925);  // rounding mode is half-up
        assertFormatsTo("-1.234567893",  -1.2345678925);
        assertFormatsTo( "1.23457E+19",   12345650000000000000d);
        assertFormatsTo("-1.23457E+19",  -12345650000000000000d);
        assertFormatsTo( "1.23457E-19",   0.0000000000000000001234565d);
        assertFormatsTo("-1.23457E-19",  -0.0000000000000000001234565d);
        assertFormatsTo( "1.000000001",   1.000000001);
        assertFormatsTo( "1",             1.0000000001);
        assertFormatsTo( "1234.567891",   1234.567891123456789d);
        assertFormatsTo( "1234567.891",   1234567.891123456789d);
        assertFormatsTo( "12345678912",   12345678911.63456789d);  // integer portion uses all 11 digits
        assertFormatsTo( "12345678913",   12345678912.5d);  // half-up here too
        assertFormatsTo("-12345678913",  -12345678912.5d);
        assertFormatsTo( "1.23457E+11",   123456789112.3456789d);
    }

    private static void assertFormatsTo(String expected, double input) throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet s1 = wb.createSheet();
            Row row = s1.createRow(0);
            Cell rawValue = row.createCell(0);
            rawValue.setCellValue(input);
            CellStyle newStyle = wb.createCellStyle();
            DataFormat dataFormat = wb.createDataFormat();
            newStyle.setDataFormat(dataFormat.getFormat("General"));
            String actual = new DataFormatter().formatCellValue(rawValue);
            assertEquals(expected, actual);
        }
    }

    @Test
    void testFormulaEvaluation() throws IOException {
        Workbook wb = HSSFTestDataSamples.openSampleWorkbook("FormulaEvalTestData.xls");

        CellReference ref = new CellReference("D47");

        Cell cell = wb.getSheetAt(0).getRow(ref.getRow()).getCell(ref.getCol());
        assertEquals(CellType.FORMULA, cell.getCellType());
        assertEquals("G9:K9 I7:I12", cell.getCellFormula());

        DataFormatter formatter = new DataFormatter();
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        assertEquals("5.6789", formatter.formatCellValue(cell, evaluator));

        wb.close();
    }

    @Test
    void testFormatWithTrailingDotsUS() {
        DataFormatter dfUS = new DataFormatter(Locale.US);
        assertEquals("1,000,000", dfUS.formatRawCellContents(1000000, -1, "#,##0"));
        assertEquals("1,000", dfUS.formatRawCellContents(1000000, -1, "#,##0,"));
        assertEquals("1", dfUS.formatRawCellContents(1000000, -1, "#,##0,,"));
        assertEquals("1,000,000.0", dfUS.formatRawCellContents(1000000, -1, "#,##0.0"));
        assertEquals("1,000.0", dfUS.formatRawCellContents(1000000, -1, "#,##0.0,"));
        assertEquals("1.0", dfUS.formatRawCellContents(1000000, -1, "#,##0.0,,"));
        assertEquals("1,000,000.00", dfUS.formatRawCellContents(1000000, -1, "#,##0.00"));
        assertEquals("1,000.00", dfUS.formatRawCellContents(1000000, -1, "#,##0.00,"));
        assertEquals("1.00", dfUS.formatRawCellContents(1000000, -1, "#,##0.00,,"));
        assertEquals("1,000,000", dfUS.formatRawCellContents(1e24, -1, "#,##0,,,,,,"));
    }

    @Test
    void testFormatWithTrailingDotsOtherLocale() {
        DataFormatter dfIT = new DataFormatter(Locale.ITALY);
        assertEquals("1.000.000", dfIT.formatRawCellContents(1000000, -1, "#,##0"));
        assertEquals("1.000", dfIT.formatRawCellContents(1000000, -1, "#,##0,"));
        assertEquals("1", dfIT.formatRawCellContents(1000000, -1, "#,##0,,"));
        assertEquals("1.000.000,0", dfIT.formatRawCellContents(1000000, -1, "#,##0.0"));
        assertEquals("1.000,0", dfIT.formatRawCellContents(1000000, -1, "#,##0.0,"));
        assertEquals("1,0", dfIT.formatRawCellContents(1000000, -1, "#,##0.0,,"));
        assertEquals("1.000.000,00", dfIT.formatRawCellContents(1000000, -1, "#,##0.00"));
        assertEquals("1.000,00", dfIT.formatRawCellContents(1000000, -1, "#,##0.00,"));
        assertEquals("1,00", dfIT.formatRawCellContents(1000000, -1, "#,##0.00,,"));
        assertEquals("1.000.000", dfIT.formatRawCellContents(1e24, -1, "#,##0,,,,,,"));
    }

    /**
     * bug 60031: DataFormatter parses months incorrectly when put at the end of date segment
     */
    @Test
    void testBug60031() {
        // 23-08-2016 08:51:01 which is 42605.368761574071 as double was parsed
        // with format "yyyy-dd-MM HH:mm:ss" into "2016-23-51 08:51:01".
        DataFormatter dfUS = new DataFormatter(Locale.US);
        assertEquals("2016-23-08 08:51:01", dfUS.formatRawCellContents(42605.368761574071, -1, "yyyy-dd-MM HH:mm:ss"));
        assertEquals("2016-23 08:51:01 08", dfUS.formatRawCellContents(42605.368761574071, -1, "yyyy-dd HH:mm:ss MM"));
        assertEquals("2017-12-01 January 09:54:33", dfUS.formatRawCellContents(42747.412892397523, -1, "yyyy-dd-MM MMMM HH:mm:ss"));
        assertEquals("08", dfUS.formatRawCellContents(42605.368761574071, -1, "MM"));
        assertEquals("01", dfUS.formatRawCellContents(42605.368761574071, -1, "ss"));

        // From Excel help:
        /*
            The "m" or "mm" code must appear immediately after the "h" or"hh"
            code or immediately before the "ss" code; otherwise, Microsoft
            Excel displays the month instead of minutes."
          */
        assertEquals("08", dfUS.formatRawCellContents(42605.368761574071, -1, "mm"));
        assertEquals("08:51", dfUS.formatRawCellContents(42605.368761574071, -1, "hh:mm"));
        assertEquals("51:01", dfUS.formatRawCellContents(42605.368761574071, -1, "mm:ss"));
    }

    @Test
    void testDateFormattingWithLocales() {
        // 2017-12-01 09:54:33 which is 42747.412892397523 as double
        DataFormatter dfDE = new DataFormatter(Locale.GERMANY);
        DataFormatter dfZH = new DataFormatter(Locale.PRC);
        DataFormatter dfIE = new DataFormatter(new Locale("GA", "IE"));
        double date = 42747.412892397523;
        String format = "dd MMMM yyyy HH:mm:ss";
        assertEquals("12 Januar 2017 09:54:33", dfDE.formatRawCellContents(date, -1, format));
        assertEquals("12 \u4E00\u6708 2017 09:54:33", dfZH.formatRawCellContents(date, -1, format));
        assertEquals("12 Ean\u00E1ir 2017 09:54:33", dfIE.formatRawCellContents(date, -1, format));
    }

    /**
     * bug 60422 : simple number formats seem ok
     */
    @Test
    void testSimpleNumericFormatsInGermanyLocale() {
        Locale[] locales = new Locale[] {Locale.GERMANY, Locale.US, Locale.ROOT};
        for (Locale locale : locales) {
            //show that LocaleUtil has no effect on these tests
            LocaleUtil.setUserLocale(locale);
            try {
                char euro = '\u20AC';
                DataFormatter df = new DataFormatter(Locale.GERMANY);
                assertEquals("4,33", df.formatRawCellContents(4.33, -1, "#,##0.00"));
                assertEquals("1.234,33", df.formatRawCellContents(1234.333, -1, "#,##0.00"));
                assertEquals("-1.234,33", df.formatRawCellContents(-1234.333, -1, "#,##0.00"));
                assertEquals("1.234,33 " + euro, df.formatRawCellContents(1234.33, -1, "#,##0.00 " + euro));
                assertEquals("1.234,33 " + euro, df.formatRawCellContents(1234.33, -1, "#,##0.00 \"" + euro + "\""));
            } finally {
                LocaleUtil.resetUserLocale();
            }
        }
    }

    /**
     * bug 60422 : DataFormatter has issues with a specific NumberFormat in Germany default locale
≈    */
    @Test
    void testBug60422() {
        char euro = '\u20AC';
        DataFormatter df = new DataFormatter(Locale.GERMANY);
        String formatString = String.format(Locale.ROOT,
                "_-* #,##0.00\\ \"%s\"_-;\\-* #,##0.00\\ \"%s\"_-;_-* \"-\"??\\ \"%s\"_-;_-@_-",
                euro, euro, euro);
        assertEquals("4,33 " + euro, df.formatRawCellContents(4.33, 178, formatString));
        assertEquals("1.234,33 " + euro, df.formatRawCellContents(1234.33, 178, formatString));
    }

    @Test
    void testBug62839() {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellFormula("FLOOR(-123,10)");
        DataFormatter df = new DataFormatter(Locale.GERMANY);

        String value = df.formatCellValue(cell, wb.getCreationHelper().createFormulaEvaluator());
        assertEquals("-130", value);
    }

    /**
     * Bug #63292
     */
    @Test
    void test1904With4PartFormat() {
        Date date = new Date();
        int formatIndex = 105;
        String formatString1 = "[$-F400]m/d/yy h:mm:ss\\ AM/PM";
        String formatString4 = "[$-F400]m/d/yy h:mm:ss\\ AM/PM;[$-F400]m/d/yy h:mm:ss\\ AM/PM;_-* \"\"??_-;_-@_-";

        String s1900, s1904;

        // These two format calls return the same thing, as expected:
        // The assertEquals() passes with 1-part format
        s1900 = format(date, formatIndex, formatString1, false);
        s1904 = format(date, formatIndex, formatString1, true);
        assertEquals(s1900, s1904);  // WORKS

        // These two format calls should return the same thing but don't:
        // It fails with 4-part format because the call to CellFormat ignores 'use1904Windowing'
        s1900 = format(date, formatIndex, formatString4, false);
        s1904 = format(date, formatIndex, formatString4, true);
        assertEquals(s1900, s1904);  // FAILS before fix for #63292
    }

    private String format(Date date, int formatIndex, String formatString, boolean use1904Windowing) {
        DataFormatter formatter = new DataFormatter();
        double n = DateUtil.getExcelDate(date, use1904Windowing);
        return formatter.formatRawCellContents(n, formatIndex, formatString, use1904Windowing);
    }

    @Test
    void testConcurrentCellFormat() throws Exception {
        DataFormatter formatter1 = new DataFormatter();
        DataFormatter formatter2 = new DataFormatter();
        doFormatTestSequential(formatter1);
        doFormatTestConcurrent(formatter1, formatter2);
    }

    /**
     * Bug #64319
     *
     * A custom format string like TRUE shouldn't be E+
     * A numeric format string like 0E0 shouldn't be E+
     * A numeric format string like 0E+0 should be E+
     */
    @Test
    void testWithEinFormat() throws Exception {
        DataFormatter formatter = new DataFormatter();

        // Format string literals with an E in them shouldn't be
        //  treated as a Scientific format, so shouldn't become E+
        assertEquals("TRUE", formatter.formatRawCellContents(1.0, 170,
                       "\"TRUE\";\"FALSE\";\"ZERO\""));
        assertEquals("ZERO", formatter.formatRawCellContents(0.0, 170,
                       "\"TRUE\";\"FALSE\";\"ZERO\""));
        assertEquals("FALSE", formatter.formatRawCellContents(-1.0, 170,
                       "\"TRUE\";\"FALSE\";\"ZERO\""));

        // Explicit Scientific format does need E+
        assertEquals("1E+05", formatter.formatRawCellContents(1e05, 170,
                       "0E+00"));
        assertEquals("1E+10", formatter.formatRawCellContents(1e10, 170,
                       "0E+00"));
        assertEquals("1E-10", formatter.formatRawCellContents(1e-10, 170,
                      "0E+00"));

        // Large numbers with "General" need E+
        assertEquals("100000", formatter.formatRawCellContents(1e05, -1, "General"));
        assertEquals("1E+12", formatter.formatRawCellContents(1e12, -1, "General"));

        // Less common Scientific-like formats which don't ask for
        //  the + on >1 exponentials don't need it adding
        // (Java will put the -ve ones in for E-## automatically)
        assertEquals("1E5", formatter.formatRawCellContents(1e05, 170,
                       "0E0"));
        assertEquals("1E10", formatter.formatRawCellContents(1e10, 170,
                       "0E0"));
        assertEquals("1E-10", formatter.formatRawCellContents(1e-10, 170,
                       "0E0"));

        assertEquals("1E5", formatter.formatRawCellContents(1e05, 170,
                       "0E-0"));
        assertEquals("1E10", formatter.formatRawCellContents(1e10, 170,
                       "0E-0"));
        assertEquals("1E-10", formatter.formatRawCellContents(1e-10, 170,
                       "0E-0"));

    }

    private void doFormatTestSequential(DataFormatter formatter) {
        for (int i = 0; i < 1_000; i++) {
            assertTrue(doFormatTest(formatter, 43551.50990171296, "3/27/19 12:14:15 PM", i));
            assertTrue(doFormatTest(formatter, 36104.424780092595, "11/5/98 10:11:41 AM", i));
        }
    }

    private void doFormatTestConcurrent(DataFormatter formatter1, DataFormatter formatter2) throws Exception {
        ArrayList<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < 1_000; i++) {
            final int iteration = i;
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(
                    () -> {
                        boolean r1 = doFormatTest(formatter1, 43551.50990171296, "3/27/19 12:14:15 PM", iteration);
                        boolean r2 = doFormatTest(formatter1, 36104.424780092595, "11/5/98 10:11:41 AM", iteration);
                        return r1 && r2;
                    });
            futures.add(future);
            future = CompletableFuture.supplyAsync(
                    () -> {
                        boolean r1 = doFormatTest(formatter2, 43551.50990171296, "3/27/19 12:14:15 PM", iteration);
                        boolean r2 = doFormatTest(formatter2, 36104.424780092595, "11/5/98 10:11:41 AM", iteration);
                        return r1 && r2;
                    });
            futures.add(future);
        }
        for (CompletableFuture<Boolean> future : futures) {
            assertTrue(future.get(1, TimeUnit.MINUTES));
        }
    }

    private static boolean doFormatTest(DataFormatter formatter, double n, String expected, int iteration) {
        int formatIndex = 105;
        String formatString = "[$-F400]m/d/yy h:mm:ss\\ AM/PM;[$-F400]m/d/yy h:mm:ss\\ AM/PM;_-* \"\"??_-;_-@_-";
        String actual = formatter.formatRawCellContents(n, formatIndex, formatString);
        assertEquals(expected, actual, "Failed on iteration " + iteration);
        return true;
    }

}
