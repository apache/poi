package org.apache.poi.util;

import junit.framework.TestCase;

/**
 * @version $Id$
 */
public class TestIntList2d
        extends TestCase
{
    public void testAccess()
            throws Exception
    {
        IntList2d array = new IntList2d();
        assertEquals( 0, array.get( 0, 0 ) );
        assertEquals( 0, array.get( 1, 1 ) );
        assertEquals( 0, array.get( 100, 100 ) );
        array.set( 100, 100, 999 );
        assertEquals( 999, array.get( 100, 100 ) );
        assertEquals( 0, array.get( 0, 0 ) );
        array.set( 0, 0, 999 );
        assertEquals( 999, array.get( 0, 0 ) );

        assertTrue(array.isAllocated( 0, 0 ) );
        assertTrue(array.isAllocated( 100, 100 ) );
        assertFalse(array.isAllocated( 200, 200 ) );

        try
        {
            assertEquals( 0, array.get( -1, -1 ) );
            fail();
        }
        catch ( ArrayIndexOutOfBoundsException e )
        {
            // pass
        }
    }
}
