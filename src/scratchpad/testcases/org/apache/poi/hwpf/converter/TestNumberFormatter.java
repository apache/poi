package org.apache.poi.hwpf.converter;

import junit.framework.TestCase;

public class TestNumberFormatter extends TestCase
{

    public void testRoman()
    {
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

    public void testEnglish()
    {
        assertEquals( "a", NumberFormatter.getNumber( 1, 4 ) );
        assertEquals( "z", NumberFormatter.getNumber( 26, 4 ) );

        assertEquals( "aa", NumberFormatter.getNumber( 1 * 26 + 1, 4 ) );
        assertEquals( "az", NumberFormatter.getNumber( 1 * 26 + 26, 4 ) );

        assertEquals( "za", NumberFormatter.getNumber( 26 * 26 + 1, 4 ) );
        assertEquals( "zz", NumberFormatter.getNumber( 26 * 26 + 26, 4 ) );

        assertEquals( "aaa",
                NumberFormatter.getNumber( 26 * 26 + 1 * 26 + 1, 4 ) );
        assertEquals( "aaz",
                NumberFormatter.getNumber( 26 * 26 + 1 * 26 + 26, 4 ) );

        assertEquals( "aba",
                NumberFormatter.getNumber( 1 * 26 * 26 + 2 * 26 + 1, 4 ) );
        assertEquals( "aza",
                NumberFormatter.getNumber( 1 * 26 * 26 + 26 * 26 + 1, 4 ) );

        assertEquals( "azz",
                NumberFormatter.getNumber( 26 * 26 + 26 * 26 + 26, 4 ) );
        assertEquals( "baa",
                NumberFormatter.getNumber( 2 * 26 * 26 + 1 * 26 + 1, 4 ) );
        assertEquals( "zaa",
                NumberFormatter.getNumber( 26 * 26 * 26 + 1 * 26 + 1, 4 ) );
        assertEquals( "zzz",
                NumberFormatter.getNumber( 26 * 26 * 26 + 26 * 26 + 26, 4 ) );

        assertEquals(
                "aaaa",
                NumberFormatter.getNumber( 1 * 26 * 26 * 26 + 1 * 26 * 26 + 1
                        * 26 + 1, 4 ) );
        assertEquals(
                "azzz",
                NumberFormatter.getNumber( 1 * 26 * 26 * 26 + 26 * 26 * 26 + 26
                        * 26 + 26, 4 ) );
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
