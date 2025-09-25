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
package org.apache.poi.hwpf.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class TestNumberFormatter {

    @Test
    void testArabicOrOrdinal() {
        assertEquals( "1", NumberFormatter.getNumber( 1, 5 ) );
        assertEquals( "2", NumberFormatter.getNumber( 2, 5 ) );
        assertEquals( "3", NumberFormatter.getNumber( 3, 5 ) );
        assertEquals( "4", NumberFormatter.getNumber( 4, 5 ) );
        assertEquals( "5", NumberFormatter.getNumber( 5, 5 ) );
        assertEquals( "6", NumberFormatter.getNumber( 6, 5 ) );
        assertEquals( "7", NumberFormatter.getNumber( 7, 5 ) );
        assertEquals( "8", NumberFormatter.getNumber( 8, 5 ) );
        assertEquals( "9", NumberFormatter.getNumber( 9, 5 ) );
        assertEquals( "10", NumberFormatter.getNumber( 10, 5 ) );

        assertEquals( "1606", NumberFormatter.getNumber( 1606, 0 ) );
        assertEquals( "1910", NumberFormatter.getNumber( 1910, 0 ) );
        assertEquals( "1954", NumberFormatter.getNumber( 1954, 0 ) );

        for ( int i = 1; i < 1000000; i++ ) {
            // make sure there is no exceptions
            assertEquals(Integer.toString(i), NumberFormatter.getNumber( i, 0 ));
        }

        assertEquals( "0", NumberFormatter.getNumber( 0, 0 ) );
        assertEquals( "-1", NumberFormatter.getNumber( -1, 0 ) );

        assertEquals( "1", NumberFormatter.getNumber( 1, 34 ) );
    }

    @Test
    void testRomanLower() {
        assertEquals( "i", NumberFormatter.getNumber( 1, 2 ) );
        assertEquals( "ii", NumberFormatter.getNumber( 2, 2 ) );
        assertEquals( "iii", NumberFormatter.getNumber( 3, 2 ) );
        assertEquals( "iv", NumberFormatter.getNumber( 4, 2 ) );
        assertEquals( "v", NumberFormatter.getNumber( 5, 2 ) );
        assertEquals( "vi", NumberFormatter.getNumber( 6, 2 ) );
        assertEquals( "vii", NumberFormatter.getNumber( 7, 2 ) );
        assertEquals( "viii", NumberFormatter.getNumber( 8, 2 ) );
        assertEquals( "ix", NumberFormatter.getNumber( 9, 2 ) );
        assertEquals( "x", NumberFormatter.getNumber( 10, 2 ) );

        assertEquals( "mdcvi", NumberFormatter.getNumber( 1606, 2 ) );
        assertEquals( "mcmx", NumberFormatter.getNumber( 1910, 2 ) );
        assertEquals( "mcmliv", NumberFormatter.getNumber( 1954, 2 ) );

        assertThrows(IllegalArgumentException.class,
                () -> NumberFormatter.getNumber( 0, 2));

        assertThrows(IllegalArgumentException.class,
                () -> NumberFormatter.getNumber( -1, 2));
    }

    @Test
    void testRomanUpper() {
        assertEquals( "I", NumberFormatter.getNumber( 1, 1 ) );
        assertEquals( "II", NumberFormatter.getNumber( 2, 1 ) );
        assertEquals( "III", NumberFormatter.getNumber( 3, 1 ) );
        assertEquals( "IV", NumberFormatter.getNumber( 4, 1 ) );
        assertEquals( "V", NumberFormatter.getNumber( 5, 1 ) );
        assertEquals( "VI", NumberFormatter.getNumber( 6, 1 ) );
        assertEquals( "VII", NumberFormatter.getNumber( 7, 1 ) );
        assertEquals( "VIII", NumberFormatter.getNumber( 8, 1 ) );
        assertEquals( "IX", NumberFormatter.getNumber( 9, 1 ) );
        assertEquals( "X", NumberFormatter.getNumber( 10, 1 ) );

        assertEquals( "MDCVI", NumberFormatter.getNumber( 1606, 1 ) );
        assertEquals( "MCMX", NumberFormatter.getNumber( 1910, 1 ) );
        assertEquals( "MCMLIV", NumberFormatter.getNumber( 1954, 1 ) );

        assertThrows(IllegalArgumentException.class,
                () -> NumberFormatter.getNumber( 0, 1 ));

        assertThrows(IllegalArgumentException.class,
                () -> NumberFormatter.getNumber( -1, 1 ));
    }

    @Test
    void testEnglishLower() {
        assertEquals( "a", NumberFormatter.getNumber( 1, 4 ) );
        assertEquals( "z", NumberFormatter.getNumber( 26, 4 ) );

        assertEquals( "aa", NumberFormatter.getNumber(  26 + 1, 4 ) );
        assertEquals( "az", NumberFormatter.getNumber(  26 + 26, 4 ) );

        assertEquals( "za", NumberFormatter.getNumber( 26 * 26 + 1, 4 ) );
        assertEquals( "zz", NumberFormatter.getNumber( 26 * 26 + 26, 4 ) );

        assertEquals( "aaa",
                NumberFormatter.getNumber( 26 * 26 + 26 + 1, 4 ) );
        assertEquals( "aaz",
                NumberFormatter.getNumber( 26 * 26 + 26 + 26, 4 ) );

        assertEquals( "aba",
                NumberFormatter.getNumber( 26 * 26 + 2 * 26 + 1, 4 ) );
        assertEquals( "aza",
                NumberFormatter.getNumber( 26 * 26 + 26 * 26 + 1, 4 ) );

        assertEquals( "azz",
                NumberFormatter.getNumber( 26 * 26 + 26 * 26 + 26, 4 ) );
        assertEquals( "baa",
                NumberFormatter.getNumber( 2 * 26 * 26 + 26 + 1, 4 ) );
        assertEquals( "zaa",
                NumberFormatter.getNumber( 26 * 26 * 26 + 26 + 1, 4 ) );
        assertEquals( "zzz",
                NumberFormatter.getNumber( 26 * 26 * 26 + 26 * 26 + 26, 4 ) );

        assertEquals(
                "aaaa",
                NumberFormatter.getNumber( 26 * 26 * 26 + 26 * 26 + 26 + 1, 4 ) );
        assertEquals(
                "azzz",
                NumberFormatter.getNumber( 26 * 26 * 26 + 26 * 26 * 26 + 26 * 26 + 26, 4 ) );
        assertEquals(
                "zzzz",
                NumberFormatter.getNumber( 26 * 26 * 26 * 26 + 26 * 26 * 26
                        + 26 * 26 + 26, 4 ) );

        for ( int i = 1; i < 1000000; i++ ) {
            // make sure there is no exceptions
            NumberFormatter.getNumber( i, 4 );
        }

        assertThrows(IllegalArgumentException.class,
                () -> NumberFormatter.getNumber( 0, 4 ));

        assertThrows(IllegalArgumentException.class,
                () -> NumberFormatter.getNumber( -1, 4 ));
    }

    @Test
    void testEnglishUpper() {
        assertEquals( "A", NumberFormatter.getNumber( 1, 3 ) );
        assertEquals( "Z", NumberFormatter.getNumber( 26, 3 ) );

        assertEquals( "AA", NumberFormatter.getNumber(  26 + 1, 3 ) );
        assertEquals( "AZ", NumberFormatter.getNumber(  26 + 26, 3 ) );

        assertEquals( "ZA", NumberFormatter.getNumber( 26 * 26 + 1, 3 ) );
        assertEquals( "ZZ", NumberFormatter.getNumber( 26 * 26 + 26, 3 ) );

        assertEquals( "AAA",
                NumberFormatter.getNumber( 26 * 26 + 26 + 1, 3 ) );
        assertEquals( "AAZ",
                NumberFormatter.getNumber( 26 * 26 + 26 + 26, 3 ) );

        assertEquals( "ABA",
                NumberFormatter.getNumber( 26 * 26 + 2 * 26 + 1, 3 ) );
        assertEquals( "AZA",
                NumberFormatter.getNumber( 26 * 26 + 26 * 26 + 1, 3 ) );

        assertEquals( "AZZ",
                NumberFormatter.getNumber( 26 * 26 + 26 * 26 + 26, 3 ) );
        assertEquals( "BAA",
                NumberFormatter.getNumber( 2 * 26 * 26 + 26 + 1, 3 ) );
        assertEquals( "ZAA",
                NumberFormatter.getNumber( 26 * 26 * 26 + 26 + 1, 3 ) );
        assertEquals( "ZZZ",
                NumberFormatter.getNumber( 26 * 26 * 26 + 26 * 26 + 26, 3 ) );

        assertEquals(
                "AAAA",
                NumberFormatter.getNumber( 26 * 26 * 26 + 26 * 26 + 26 + 1, 3 ) );
        assertEquals(
                "AZZZ",
                NumberFormatter.getNumber( 26 * 26 * 26 + 26 * 26 * 26 + 26 * 26 + 26, 3 ) );
        assertEquals(
                "ZZZZ",
                NumberFormatter.getNumber( 26 * 26 * 26 * 26 + 26 * 26 * 26
                        + 26 * 26 + 26, 3 ) );

        for ( int i = 1; i < 1000000; i++ ) {
            // make sure there is no exceptions
            NumberFormatter.getNumber( i, 3 );
        }
    }
}
