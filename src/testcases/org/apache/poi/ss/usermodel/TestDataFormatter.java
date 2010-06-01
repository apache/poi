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

import java.util.Locale;

import org.apache.poi.hssf.usermodel.TestHSSFDataFormatter;

import junit.framework.TestCase;

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
}
