
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
 * Test LongField code
 *
 * @author  Marc Johnson (mjohnson at apache dot org)
 */

public class TestLongField
    extends TestCase
{

    /**
     * Constructor
     *
     * @param name
     */

    public TestLongField(String name)
    {
        super(name);
    }

    static private final long[] _test_array =
    {
        Long.MIN_VALUE, -1L, 0L, 1L, Long.MAX_VALUE
    };

    /**
     * Test constructors.
     */

    public void testConstructors()
    {
        try
        {
            new LongField(-1);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        LongField field = new LongField(2);

        assertEquals(0L, field.get());
        try
        {
            new LongField(-1, 1L);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new LongField(2, 0x123456789ABCDEF0L);
        assertEquals(0x123456789ABCDEF0L, field.get());
        byte[] array = new byte[ 10 ];

        try
        {
            new LongField(-1, 1L, array);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new LongField(2, 0x123456789ABCDEF0L, array);
        assertEquals(0x123456789ABCDEF0L, field.get());
        assertEquals(( byte ) 0xF0, array[ 2 ]);
        assertEquals(( byte ) 0xDE, array[ 3 ]);
        assertEquals(( byte ) 0xBC, array[ 4 ]);
        assertEquals(( byte ) 0x9A, array[ 5 ]);
        assertEquals(( byte ) 0x78, array[ 6 ]);
        assertEquals(( byte ) 0x56, array[ 7 ]);
        assertEquals(( byte ) 0x34, array[ 8 ]);
        assertEquals(( byte ) 0x12, array[ 9 ]);
        array = new byte[ 9 ];
        try
        {
            new LongField(2, 5L, array);
            fail("should have gotten ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        for (int j = 0; j < _test_array.length; j++)
        {
            array = new byte[ 8 ];
            new LongField(0, _test_array[ j ], array);
            assertEquals(_test_array[ j ], new LongField(0, array).get());
        }
    }

    /**
     * Test set() methods
     */

    public void testSet()
    {
        LongField field = new LongField(0);
        byte[]    array = new byte[ 8 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            field.set(_test_array[ j ]);
            assertEquals("testing _1 " + j, _test_array[ j ], field.get());
            field = new LongField(0);
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
            assertEquals("testing _3.4 " + _test_array[ j ],
                         ( byte ) ((_test_array[ j ] >> 32) % 256),
                         array[ 4 ]);
            assertEquals("testing _3.5 " + _test_array[ j ],
                         ( byte ) ((_test_array[ j ] >> 40) % 256),
                         array[ 5 ]);
            assertEquals("testing _3.6 " + _test_array[ j ],
                         ( byte ) ((_test_array[ j ] >> 48) % 256),
                         array[ 6 ]);
            assertEquals("testing _3.7 " + _test_array[ j ],
                         ( byte ) ((_test_array[ j ] >> 56) % 256),
                         array[ 7 ]);
        }
    }

    /**
     * Test readFromBytes
     */

    public void testReadFromBytes()
    {
        LongField field = new LongField(1);
        byte[]    array = new byte[ 8 ];

        try
        {
            field.readFromBytes(array);
            fail("should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new LongField(0);
        for (int j = 0; j < _test_array.length; j++)
        {
            array[ 0 ] = ( byte ) (_test_array[ j ] % 256);
            array[ 1 ] = ( byte ) ((_test_array[ j ] >> 8) % 256);
            array[ 2 ] = ( byte ) ((_test_array[ j ] >> 16) % 256);
            array[ 3 ] = ( byte ) ((_test_array[ j ] >> 24) % 256);
            array[ 4 ] = ( byte ) ((_test_array[ j ] >> 32) % 256);
            array[ 5 ] = ( byte ) ((_test_array[ j ] >> 40) % 256);
            array[ 6 ] = ( byte ) ((_test_array[ j ] >> 48) % 256);
            array[ 7 ] = ( byte ) ((_test_array[ j ] >> 56) % 256);
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
        LongField field  = new LongField(0);
        byte[]    buffer = new byte[ _test_array.length * 8 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            buffer[ (j * 8) + 0 ] = ( byte ) (_test_array[ j ] % 256);
            buffer[ (j * 8) + 1 ] = ( byte ) ((_test_array[ j ] >> 8) % 256);
            buffer[ (j * 8) + 2 ] = ( byte ) ((_test_array[ j ] >> 16) % 256);
            buffer[ (j * 8) + 3 ] = ( byte ) ((_test_array[ j ] >> 24) % 256);
            buffer[ (j * 8) + 4 ] = ( byte ) ((_test_array[ j ] >> 32) % 256);
            buffer[ (j * 8) + 5 ] = ( byte ) ((_test_array[ j ] >> 40) % 256);
            buffer[ (j * 8) + 6 ] = ( byte ) ((_test_array[ j ] >> 48) % 256);
            buffer[ (j * 8) + 7 ] = ( byte ) ((_test_array[ j ] >> 56) % 256);
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);

        for (int j = 0; j < buffer.length / 8; j++)
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
        LongField field = new LongField(0);
        byte[]    array = new byte[ 8 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            field.set(_test_array[ j ]);
            field.writeToBytes(array);
            long val = (( long ) array[ 7 ]) << 56;

            val &= 0xFF00000000000000L;
            val += ((( long ) array[ 6 ]) << 48) & 0x00FF000000000000L;
            val += ((( long ) array[ 5 ]) << 40) & 0x0000FF0000000000L;
            val += ((( long ) array[ 4 ]) << 32) & 0x000000FF00000000L;
            val += ((( long ) array[ 3 ]) << 24) & 0x00000000FF000000L;
            val += ((( long ) array[ 2 ]) << 16) & 0x0000000000FF0000L;
            val += ((( long ) array[ 1 ]) << 8) & 0x000000000000FF00L;
            val += (array[ 0 ] & 0x00000000000000FFL);
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
        System.out.println("Testing util.LongField functionality");
        junit.textui.TestRunner.run(TestLongField.class);
    }
}
