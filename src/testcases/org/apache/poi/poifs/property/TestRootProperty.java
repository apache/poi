
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

package org.apache.poi.poifs.property;

import java.io.*;

import java.util.*;

import junit.framework.*;

import org.apache.poi.poifs.common.POIFSConstants;

/**
 * Class to test RootProperty functionality
 *
 * @author Marc Johnson
 */

public class TestRootProperty
    extends TestCase
{
    private RootProperty _property;
    private byte[]       _testblock;

    /**
     * Constructor TestRootProperty
     *
     * @param name
     */

    public TestRootProperty(String name)
    {
        super(name);
    }

    /**
     * Test constructing RootProperty
     *
     * @exception IOException
     */

    public void testConstructor()
        throws IOException
    {
        createBasicRootProperty();
        verifyProperty();
    }

    private void createBasicRootProperty()
    {
        _property  = new RootProperty();
        _testblock = new byte[ 128 ];
        int index = 0;

        for (; index < 0x40; index++)
        {
            _testblock[ index ] = ( byte ) 0;
        }
        String name  = "Root Entry";
        int    limit = Math.min(31, name.length());

        _testblock[ index++ ] = ( byte ) (2 * (limit + 1));
        _testblock[ index++ ] = ( byte ) 0;
        _testblock[ index++ ] = ( byte ) 5;
        _testblock[ index++ ] = ( byte ) 1;
        for (; index < 0x50; index++)
        {
            _testblock[ index ] = ( byte ) 0xff;
        }
        for (; index < 0x74; index++)
        {
            _testblock[ index ] = ( byte ) 0;
        }
        _testblock[ index++ ] = ( byte ) POIFSConstants.END_OF_CHAIN;
        for (; index < 0x78; index++)
        {
            _testblock[ index ] = ( byte ) 0xff;
        }
        for (; index < 0x80; index++)
        {
            _testblock[ index ] = ( byte ) 0;
        }
        byte[] name_bytes = name.getBytes();

        for (index = 0; index < limit; index++)
        {
            _testblock[ index * 2 ] = name_bytes[ index ];
        }
    }

    private void verifyProperty()
        throws IOException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(512);

        _property.writeData(stream);
        byte[] output = stream.toByteArray();

        assertEquals(_testblock.length, output.length);
        for (int j = 0; j < _testblock.length; j++)
        {
            assertEquals("mismatch at offset " + j, _testblock[ j ],
                         output[ j ]);
        }
    }

    /**
     * test setSize
     */

    public void testSetSize()
    {
        for (int j = 0; j < 10; j++)
        {
            createBasicRootProperty();
            _property.setSize(j);
            assertEquals("trying block count of " + j, j * 64,
                         _property.getSize());
        }
    }

    /**
     * Test reading constructor
     *
     * @exception IOException
     */

    public void testReadingConstructor()
        throws IOException
    {
        byte[] input =
        {
            ( byte ) 0x52, ( byte ) 0x00, ( byte ) 0x6F, ( byte ) 0x00,
            ( byte ) 0x6F, ( byte ) 0x00, ( byte ) 0x74, ( byte ) 0x00,
            ( byte ) 0x20, ( byte ) 0x00, ( byte ) 0x45, ( byte ) 0x00,
            ( byte ) 0x6E, ( byte ) 0x00, ( byte ) 0x74, ( byte ) 0x00,
            ( byte ) 0x72, ( byte ) 0x00, ( byte ) 0x79, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x16, ( byte ) 0x00, ( byte ) 0x05, ( byte ) 0x01,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x02, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x20, ( byte ) 0x08, ( byte ) 0x02, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xC0, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x46,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0xC0, ( byte ) 0x5C, ( byte ) 0xE8, ( byte ) 0x23,
            ( byte ) 0x9E, ( byte ) 0x6B, ( byte ) 0xC1, ( byte ) 0x01,
            ( byte ) 0xFE, ( byte ) 0xFF, ( byte ) 0xFF, ( byte ) 0xFF,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00,
            ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00, ( byte ) 0x00
        };

        verifyReadingProperty(0, input, 0, "Root Entry");
    }

    private void verifyReadingProperty(int index, byte [] input, int offset,
                                       String name)
        throws IOException
    {
        RootProperty          property = new RootProperty(index, input,
                                             offset);
        ByteArrayOutputStream stream   = new ByteArrayOutputStream(128);
        byte[]                expected = new byte[ 128 ];

        System.arraycopy(input, offset, expected, 0, 128);
        property.writeData(stream);
        byte[] output = stream.toByteArray();

        assertEquals(128, output.length);
        for (int j = 0; j < 128; j++)
        {
            assertEquals("mismatch at offset " + j, expected[ j ],
                         output[ j ]);
        }
        assertEquals(index, property.getIndex());
        assertEquals(name, property.getName());
        assertTrue(!property.getChildren().hasNext());
    }

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out
            .println("Testing org.apache.poi.poifs.property.RootProperty");
        junit.textui.TestRunner.run(TestRootProperty.class);
    }
}
