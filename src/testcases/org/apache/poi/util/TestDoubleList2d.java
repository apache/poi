package org.apache.poi.util;

import junit.framework.TestCase;

/**
 * @version $Id$
 */
public class TestDoubleList2d
        extends TestCase
{
    public void testAccess()
            throws Exception
    {
        DoubleList2d array = new DoubleList2d();
        assertEquals( 0, array.get( 0, 0 ), 0.00001 );
        assertEquals( 0, array.get( 1, 1 ), 0.00001 );
        assertEquals( 0, array.get( 100, 100 ), 0.00001 );
        array.set( 100, 100, 999 );
        assertEquals( 999, array.get( 100, 100 ), 0.00001 );
        assertEquals( 0, array.get( 0, 0 ), 0.00001 );
        array.set( 0, 0, 999 );
        assertEquals( 999, array.get( 0, 0 ), 0.00001 );

        try
        {
            array.get( -1, -1 );
            fail();
        }
        catch ( ArrayIndexOutOfBoundsException e )
        {
            // pass
        }
    }
}
