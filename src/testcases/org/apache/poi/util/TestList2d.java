package org.apache.poi.util;

import junit.framework.TestCase;

/**
 * @version $Id$
 */
public class TestList2d
        extends TestCase
{
    public void testAccess()
            throws Exception
    {
        Object objectA = new Object();
        Object objectB = new Object();

        List2d array = new List2d();
        assertNull( array.get( 0, 0 ) );
        assertNull( array.get( 1, 1 ) );
        assertNull( array.get( 100, 100 ) );
        array.set( 100, 100, objectA );
        assertSame( objectA, array.get( 100, 100 ) );
        assertNull( array.get( 0, 0 ) );
        array.set( 0, 0, objectB );
        assertSame( objectB, array.get( 0, 0 ) );

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
