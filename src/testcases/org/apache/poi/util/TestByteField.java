
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
 * Title:        Unit test for ByteField class
 * Description:  Unit test for ByteField class
 * @author       Marc Johnson (mjohnson at apache dot org)
 */

public class TestByteField
    extends TestCase
{

    /**
     * Constructor
     *
     * @param name
     */

    public TestByteField(String name)
    {
        super(name);
    }

    static private final byte[] _test_array =
    {
        Byte.MIN_VALUE, ( byte ) -1, ( byte ) 0, ( byte ) 1, Byte.MAX_VALUE
    };

    /**
     * Test constructors.
     */

    public void testConstructors()
    {
        try
        {
            new ByteField(-1);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        ByteField field = new ByteField(2);

        assertEquals(( byte ) 0, field.get());
        try
        {
            new ByteField(-1, ( byte ) 1);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new ByteField(2, ( byte ) 3);
        assertEquals(( byte ) 3, field.get());
        byte[] array = new byte[ 3 ];

        try
        {
            new ByteField(-1, ( byte ) 1, array);
            fail("Should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new ByteField(2, ( byte ) 4, array);
        assertEquals(( byte ) 4, field.get());
        assertEquals(( byte ) 4, array[ 2 ]);
        array = new byte[ 2 ];
        try
        {
            new ByteField(2, ( byte ) 5, array);
            fail("should have gotten ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        for (int j = 0; j < _test_array.length; j++)
        {
            array = new byte[ 1 ];
            new ByteField(0, _test_array[ j ], array);
            assertEquals(_test_array[ j ], new ByteField(0, array).get());
        }
    }

    /**
     * Test set() methods
     */

    public void testSet()
    {
        ByteField field = new ByteField(0);
        byte[]    array = new byte[ 1 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            field.set(_test_array[ j ]);
            assertEquals("testing _1 " + j, _test_array[ j ], field.get());
            field = new ByteField(0);
            field.set(_test_array[ j ], array);
            assertEquals("testing _2 ", _test_array[ j ], field.get());
            assertEquals("testing _3 ", _test_array[ j ], array[ 0 ]);
        }
    }

    /**
     * Test readFromBytes
     */

    public void testReadFromBytes()
    {
        ByteField field = new ByteField(1);
        byte[]    array = new byte[ 1 ];

        try
        {
            field.readFromBytes(array);
            fail("should have caught ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException ignored_e)
        {

            // as expected
        }
        field = new ByteField(0);
        for (int j = 0; j < _test_array.length; j++)
        {
            array[ 0 ] = _test_array[ j ];
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
        ByteField field  = new ByteField(0);
        byte[]    buffer = new byte[ _test_array.length ];

        System.arraycopy(_test_array, 0, buffer, 0, buffer.length);
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);

        for (int j = 0; j < buffer.length; j++)
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
        ByteField field = new ByteField(0);
        byte[]    array = new byte[ 1 ];

        for (int j = 0; j < _test_array.length; j++)
        {
            field.set(_test_array[ j ]);
            field.writeToBytes(array);
            assertEquals("testing ", _test_array[ j ], array[ 0 ]);
        }
    }

    /**
     * Main
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out.println("Testing util.ByteField functionality");
        junit.textui.TestRunner.run(TestByteField.class);
    }
}
