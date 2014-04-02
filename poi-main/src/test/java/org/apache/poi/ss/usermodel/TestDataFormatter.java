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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.TestHSSFDataFormatter;

/**
 * Tests of {@link DataFormatter}
 *
 * See {@link TestHSSFDataFormatter} too for
 *  more tests.
 */
public class TestDataFormatter extends TestCase {
    private static final double _15_MINUTES = 0.041666667;

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
     * At the moment, we don't decode the locale strings into
     *  a specific locale, but we should format things as if
     *  the locale (eg '[$-1010409]') isn't there
     */
    public void testLocaleBasedFormats() {
       DataFormatter dfUS = new DataFormatter(Locale.US);

       // Standard formats
       assertEquals("63", dfUS.formatRawCellContents(63.0, -1, "[$-1010409]General"));
       assertEquals("63", dfUS.formatRawCellContents(63.0, -1, "[$-1010409]@"));

       // Regular numeric style formats
       assertEquals("63", dfUS.formatRawCellContents(63.0, -1, "[$-1010409]##"));
       assertEquals("63", dfUS.formatRawCellContents(63.0, -1, "[$-1010409]00"));        

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
     * Test that we correctly handle fractions in the
     *  format string, eg # #/#
     */
    public void testFractions() {
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
       assertEquals( "321 1/3",  dfUS.formatRawCellContents(321.321,  -1, "# #/#;# ##/#;0;xxx"));
       assertEquals("-321 1/3",  dfUS.formatRawCellContents(-321.321, -1, "# #/#;# ##/#;0;xxx"));
       assertEquals("0",         dfUS.formatRawCellContents(0,        -1, "# #/#;# ##/#;0;xxx"));
//     assertEquals("0.0",       dfUS.formatRawCellContents(0,        -1, "# #/#;# ##/#;#.#;xxx")); // currently hard coded to 0
       
       // Custom formats with text are not currently supported
//     assertEquals("+ve",       dfUS.formatRawCellContents(0,        -1, "+ve;-ve;zero;xxx"));
//     assertEquals("-ve",       dfUS.formatRawCellContents(0,        -1, "-ve;-ve;zero;xxx"));
//     assertEquals("zero",      dfUS.formatRawCellContents(0,        -1, "zero;-ve;zero;xxx"));
       
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
       
/*       assertEquals("321 / ?/?",   dfUS.formatRawCellContents(321.321, -1, "# #/# ?/?"));
       assertEquals("321 / /",     dfUS.formatRawCellContents(321.321, -1, "# #/# #/#"));
       assertEquals("321 ?/? ?/?",   dfUS.formatRawCellContents(321.321, -1, "# ?/? ?/?"));
       assertEquals("321 ?/? / /",   dfUS.formatRawCellContents(321.321, -1, "# ?/? #/# #/#"));
*/

       //Bug54686 patch sets default behavior of # #/## if there is a failure to parse
       assertEquals("321 1/3",   dfUS.formatRawCellContents(321.321, -1, "# #/# ?/?"));
       assertEquals("321 1/3",     dfUS.formatRawCellContents(321.321, -1, "# #/# #/#"));
       assertEquals("321 1/3",   dfUS.formatRawCellContents(321.321, -1, "# ?/? ?/?"));
       assertEquals("321 1/3",   dfUS.formatRawCellContents(321.321, -1, "# ?/? #/# #/#"));

       // Where both p and n don't include a fraction, so cannot always be formatted
      // assertEquals("123", dfUS.formatRawCellContents(-123.321, -1, "0 ?/?;0"));

       //Bug54868 patch has a hit on the first string before the ";"
       assertEquals("-123 1/3", dfUS.formatRawCellContents(-123.321, -1, "0 ?/?;0"));

       //Bug53150 formatting a whole number with fractions should just give the number
       assertEquals("1",   dfUS.formatRawCellContents(1.0, -1, "# #/#"));
       assertEquals("11",   dfUS.formatRawCellContents(11.0, -1, "# #/#"));
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
     * Tests that we do AM/PM handling properly
     */
    public void testAMPM() {
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

        boolean jdk_1_5 = System.getProperty("java.vm.version").startsWith("1.5");
        if(!jdk_1_5) {
           // YK: the tests below were written under JDK 1.6 and assume that
           // the rounding mode in the underlying decimal formatters is HALF_UP
           // It is not so JDK 1.5 where the default rounding mode is HALV_EVEN and cannot be changed.

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
    
    public void testErrors() {
        DataFormatter dfUS = new DataFormatter(Locale.US, true);

        // Create a spreadsheet with some formula errors in it
        Workbook wb = new HSSFWorkbook();
        Sheet s = wb.createSheet();
        Row r = s.createRow(0);
        Cell c = r.createCell(0, Cell.CELL_TYPE_ERROR);
        
        c.setCellErrorValue(FormulaError.DIV0.getCode());
        assertEquals(FormulaError.DIV0.getString(), dfUS.formatCellValue(c));
        
        c.setCellErrorValue(FormulaError.REF.getCode());
        assertEquals(FormulaError.REF.getString(), dfUS.formatCellValue(c));
    }

    /**
     * TODO Fix these so that they work
     */
    public void DISABLEDtestCustomFormats() {
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
    public void testExcelStyleDateFormatterStringOnMillis() {
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

	public void testBug54786() {
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
		
		try {
			assertEquals("1h 0m\"", formatter.formatRawCellContents(_15_MINUTES, -1, "[h]\"\"h\"\" m\"\"m\"\"\"\"", false));
			fail("Catches exception because of invalid format, i.e. trailing quoting");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("Cannot format given Object as a Number"));
		}
	}
}
