
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
 * Test IntegerField code
 *
 * @author  Marc Johnson (mjohnson at apache dot org)
 */

public class TestIntegerField
    extends TestCase
{

    /**
     * Constructor
     *
     * @param name
     */

    public TestIntegerField(String name)
    {
        super(name);
    }

    static private final int[] _test_array =
    {
        Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE
    };

    /**
     * Test constructors.
     */

    public void testConstructors()
    {
        try
        {
            new IntegerField(-1);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        IntegerField field = new IntegerField(2);

        assertEquals(0, field.get());
        try
        {
            new IntegerField(-1, 1);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new IntegerField(2, 0x12345678);
        assertEquals(0x12345678, field.get());
        byte[] array = new byte[ 6 ];

        try
        {
            new IntegerField(-1, 1, array);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new IntegerField(2, 0x12345678, array);
        assertEquals(0x12345678, field.get());
        assertEquals(( byte ) 0x78, array[ 2 ]);
        assertEquals(( byte ) 0x56, array[ 3 ]);
        assertEquals(( byte ) 0x34, array[ 4 ]);
        assertEquals(( byte ) 0x12, array[ 5 ]);
        array = new byte[ 5 ];
        try
        {
            new IntegerField(2, 5, array);
            fail("should have gotten ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        for (int j = 0; j < _test_array.length; j++)
        {
            array = new byte[ 4 ];
            new IntegerField(0, _test_array[ j ], array);
            assertEquals(_test_array[ j ], new IntegerField(0, array).get());
        }
    }

    /**
     * Test set() methods
     */

    public void testSet()
    {
        IntegerField field = new IntegerField(0);
        byte[]       array = new byte[ 4 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            field.set(_test_array[ j ]);
            assertEquals("testing _1 " + j, _test_array[ j ], field.get());
            field = new IntegerField(0);
            field.set(_test_array[ j ], array);
            assertEquals("testing _2 ", _test_array[ j ], field.get());
            assertEquals("testing _3.0 " + _test_array[ j ],
                         ( byte ) (_test_array[ j ] % 256), array[ 0 ]);
            assertEquals("testing _3.1 " + _test_array[ j ],
                         ( byte ) ((_test_array[ j ] >> 8) % 256),
                         array[ 1 ]);
            assertEquals("testing _3.2 " + _test_array[ j ],
                         ( byte ) ((_test_array[ j ] >> 16) % 256),
                         array[ 2 ]);
            assertEquals("testing _3.3 " + _test_array[ j ],
                         ( byte ) ((_test_array[ j ] >> 24) % 256),
                         array[ 3 ]);
        }
    }

    /**
     * Test readFromBytes
     */

    public void testReadFromBytes()
    {
        IntegerField field = new IntegerField(1);
        byte[]       array = new byte[ 4 ];

        try
        {
            field.readFromBytes(array);
            fail("should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new IntegerField(0);
        for (int j = 0; j < _test_array.length; j++)
        {
            array[ 0 ] = ( byte ) (_test_array[ j ] % 256);
            array[ 1 ] = ( byte ) ((_test_array[ j ] >> 8) % 256);
            array[ 2 ] = ( byte ) ((_test_array[ j ] >> 16) % 256);
            array[ 3 ] = ( byte ) ((_test_array[ j ] >> 24) % 256);
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
        IntegerField field  = new IntegerField(0);
        byte[]       buffer = new byte[ _test_array.length * 4 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            buffer[ (j * 4) + 0 ] = ( byte ) (_test_array[ j ] % 256);
            buffer[ (j * 4) + 1 ] = ( byte ) ((_test_array[ j ] >> 8) % 256);
            buffer[ (j * 4) + 2 ] = ( byte ) ((_test_array[ j ] >> 16) % 256);
            buffer[ (j * 4) + 3 ] = ( byte ) ((_test_array[ j ] >> 24) % 256);
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);

        for (int j = 0; j < buffer.length / 4; j++)
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
        IntegerField field = new IntegerField(0);
        byte[]       array = new byte[ 4 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            field.set(_test_array[ j ]);
            field.writeToBytes(array);
            int val = array[ 3 ] << 24;

            val &= 0xFF000000;
            val += (array[ 2 ] << 16) & 0x00FF0000;
            val += (array[ 1 ] << 8) & 0x0000FF00;
            val += (array[ 0 ] & 0x000000FF);
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
        System.out.println("Testing util.IntegerField functionality");
        junit.textui.TestRunner.run(TestIntegerField.class);
    }
}
