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

import org.junit.jupiter.api.Test;

public class TestNumberFormatter {

    @Test
    void testRoman() {
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
    }

    @Test
    void testEnglish() {
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

        for ( int i = 1; i < 1000000; i++ )
        {
            // make sure there is no exceptions
            NumberFormatter.getNumber( i, 4 );
        }
    }
}
