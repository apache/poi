
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.util;

import junit.framework.*;

import java.io.*;

/**
 * Test ShortField code
 *
 * @author  Marc Johnson (mjohnson at apache dot org)
 */

public class TestShortField
    extends TestCase
{

    /**
     * Constructor
     *
     * @param name
     */

    public TestShortField(String name)
    {
        super(name);
    }

    static private final short[] _test_array =
    {
        Short.MIN_VALUE, ( short ) -1, ( short ) 0, ( short ) 1,
        Short.MAX_VALUE
    };

    /**
     * Test constructors.
     */

    public void testConstructors()
    {
        try
        {
            new ShortField(-1);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        ShortField field = new ShortField(2);

        assertEquals(0, field.get());
        try
        {
            new ShortField(-1, ( short ) 1);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new ShortField(2, ( short ) 0x1234);
        assertEquals(0x1234, field.get());
        byte[] array = new byte[ 4 ];

        try
        {
            new ShortField(-1, ( short ) 1, array);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new ShortField(2, ( short ) 0x1234, array);
        assertEquals(( short ) 0x1234, field.get());
        assertEquals(( byte ) 0x34, array[ 2 ]);
        assertEquals(( byte ) 0x12, array[ 3 ]);
        array = new byte[ 3 ];
        try
        {
            new ShortField(2, ( short ) 5, array);
            fail("should have gotten ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        for (int j = 0; j < _test_array.length; j++)
        {
            array = new byte[ 2 ];
            new ShortField(0, _test_array[ j ], array);
            assertEquals(_test_array[ j ], new ShortField(0, array).get());
        }
    }

    /**
     * Test set() methods
     */

    public void testSet()
    {
        ShortField field = new ShortField(0);
        byte[]     array = new byte[ 2 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            field.set(_test_array[ j ]);
            assertEquals("testing _1 " + j, _test_array[ j ], field.get());
            field = new ShortField(0);
            field.set(_test_array[ j ], array);
            assertEquals("testing _2 ", _test_array[ j ], field.get());
            assertEquals("testing _3.0 " + _test_array[ j ],
                         ( byte ) (_test_array[ j ] % 256), array[ 0 ]);
            assertEquals("testing _3.1 " + _test_array[ j ],
                         ( byte ) ((_test_array[ j ] >> 8) % 256),
                         array[ 1 ]);
        }
    }

    /**
     * Test readFromBytes
     */

    public void testReadFromBytes()
    {
        ShortField field = new ShortField(1);
        byte[]     array = new byte[ 2 ];

        try
        {
            field.readFromBytes(array);
            fail("should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new ShortField(0);
        for (int j = 0; j < _test_array.length; j++)
        {
            array[ 0 ] = ( byte ) (_test_array[ j ] % 256);
            array[ 1 ] = ( byte ) ((_test_array[ j ] >> 8) % 256);
            field.readFromBytes(array);
            assertEquals("testing " + j, _test_array[ j ], field.get());
        }
    }

    /**
     * Test readFromStream
     *
     * @exception IOException
     */

    public void testReadFromStream()
        throws IOException
    {
        ShortField field  = new ShortField(0);
        byte[]     buffer = new byte[ _test_array.length * 2 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            buffer[ (j * 2) + 0 ] = ( byte ) (_test_array[ j ] % 256);
            buffer[ (j * 2) + 1 ] = ( byte ) ((_test_array[ j ] >> 8) % 256);
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);

        for (int j = 0; j < buffer.length / 2; j++)
        {
            field.readFromStream(stream);
            assertEquals("Testing " + j, _test_array[ j ], field.get());
        }
    }

    /**
     * test writeToBytes
     */

    public void testWriteToBytes()
    {
        ShortField field = new ShortField(0);
        byte[]     array = new byte[ 2 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            field.set(_test_array[ j ]);
            field.writeToBytes(array);
            short val = ( short ) (array[ 1 ] << 8);

            val &= ( short ) 0xFF00;
            val += ( short ) (array[ 0 ] & 0x00FF);
            assertEquals("testing ", _test_array[ j ], val);
        }
    }

    /**
     * Main
     *
     * @param args
     */

    public static void main(String [] args)
    {
        System.out.println("Testing util.ShortField functionality");
        junit.textui.TestRunner.run(TestShortField.class);
    }
}
