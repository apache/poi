
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

package org.apache.poi.poifs.storage;

import java.io.*;

import java.util.*;

import junit.framework.*;

import org.apache.poi.poifs.property.Property;

/**
 * Class to test PropertyBlock functionality
 *
 * @author Marc Johnson
 */

public class TestPropertyBlock
    extends TestCase
{

    /**
     * Constructor TestPropertyBlock
     *
     * @param name
     */

    public TestPropertyBlock(String name)
    {
        super(name);
    }

    /**
     * Test constructing PropertyBlocks
     *
     * @exception IOException
     */

    public void testCreatePropertyBlocks()
        throws IOException
    {

        // test with 0 properties
        List            properties = new ArrayList();
        BlockWritable[] blocks     =
            PropertyBlock.createPropertyBlockArray(properties);

        assertEquals(0, blocks.length);

        // test with 1 property
        properties.add(new LocalProperty("Root Entry"));
        blocks = PropertyBlock.createPropertyBlockArray(properties);
        assertEquals(1, blocks.length);
        byte[] testblock = new byte[ 512 ];

        for (int j = 0; j < 4; j++)
        {
            setDefaultBlock(testblock, j);
        }
        testblock[ 0x0000 ] = ( byte ) 'R';
        testblock[ 0x0002 ] = ( byte ) 'o';
        testblock[ 0x0004 ] = ( byte ) 'o';
        testblock[ 0x0006 ] = ( byte ) 't';
        testblock[ 0x0008 ] = ( byte ) ' ';
        testblock[ 0x000A ] = ( byte ) 'E';
        testblock[ 0x000C ] = ( byte ) 'n';
        testblock[ 0x000E ] = ( byte ) 't';
        testblock[ 0x0010 ] = ( byte ) 'r';
        testblock[ 0x0012 ] = ( byte ) 'y';
        testblock[ 0x0040 ] = ( byte ) 22;
        verifyCorrect(blocks, testblock);

        // test with 3 properties
        properties.add(new LocalProperty("workbook"));
        properties.add(new LocalProperty("summary"));
        blocks = PropertyBlock.createPropertyBlockArray(properties);
        assertEquals(1, blocks.length);
        testblock[ 0x0080 ] = ( byte ) 'w';
        testblock[ 0x0082 ] = ( byte ) 'o';
        testblock[ 0x0084 ] = ( byte ) 'r';
        testblock[ 0x0086 ] = ( byte ) 'k';
        testblock[ 0x0088 ] = ( byte ) 'b';
        testblock[ 0x008A ] = ( byte ) 'o';
        testblock[ 0x008C ] = ( byte ) 'o';
        testblock[ 0x008E ] = ( byte ) 'k';
        testblock[ 0x00C0 ] = ( byte ) 18;
        testblock[ 0x0100 ] = ( byte ) 's';
        testblock[ 0x0102 ] = ( byte ) 'u';
        testblock[ 0x0104 ] = ( byte ) 'm';
        testblock[ 0x0106 ] = ( byte ) 'm';
        testblock[ 0x0108 ] = ( byte ) 'a';
        testblock[ 0x010A ] = ( byte ) 'r';
        testblock[ 0x010C ] = ( byte ) 'y';
        testblock[ 0x0140 ] = ( byte ) 16;
        verifyCorrect(blocks, testblock);

        // test with 4 properties
        properties.add(new LocalProperty("wintery"));
        blocks = PropertyBlock.createPropertyBlockArray(properties);
        assertEquals(1, blocks.length);
        testblock[ 0x0180 ] = ( byte ) 'w';
        testblock[ 0x0182 ] = ( byte ) 'i';
        testblock[ 0x0184 ] = ( byte ) 'n';
        testblock[ 0x0186 ] = ( byte ) 't';
        testblock[ 0x0188 ] = ( byte ) 'e';
        testblock[ 0x018A ] = ( byte ) 'r';
        testblock[ 0x018C ] = ( byte ) 'y';
        testblock[ 0x01C0 ] = ( byte ) 16;
        verifyCorrect(blocks, testblock);

        // test with 5 properties
        properties.add(new LocalProperty("foo"));
        blocks = PropertyBlock.createPropertyBlockArray(properties);
        assertEquals(2, blocks.length);
        testblock = new byte[ 1024 ];
        for (int j = 0; j < 8; j++)
        {
            setDefaultBlock(testblock, j);
        }
        testblock[ 0x0000 ] = ( byte ) 'R';
        testblock[ 0x0002 ] = ( byte ) 'o';
        testblock[ 0x0004 ] = ( byte ) 'o';
        testblock[ 0x0006 ] = ( byte ) 't';
        testblock[ 0x0008 ] = ( byte ) ' ';
        testblock[ 0x000A ] = ( byte ) 'E';
        testblock[ 0x000C ] = ( byte ) 'n';
        testblock[ 0x000E ] = ( byte ) 't';
        testblock[ 0x0010 ] = ( byte ) 'r';
        testblock[ 0x0012 ] = ( byte ) 'y';
        testblock[ 0x0040 ] = ( byte ) 22;
        testblock[ 0x0080 ] = ( byte ) 'w';
        testblock[ 0x0082 ] = ( byte ) 'o';
        testblock[ 0x0084 ] = ( byte ) 'r';
        testblock[ 0x0086 ] = ( byte ) 'k';
        testblock[ 0x0088 ] = ( byte ) 'b';
        testblock[ 0x008A ] = ( byte ) 'o';
        testblock[ 0x008C ] = ( byte ) 'o';
        testblock[ 0x008E ] = ( byte ) 'k';
        testblock[ 0x00C0 ] = ( byte ) 18;
        testblock[ 0x0100 ] = ( byte ) 's';
        testblock[ 0x0102 ] = ( byte ) 'u';
        testblock[ 0x0104 ] = ( byte ) 'm';
        testblock[ 0x0106 ] = ( byte ) 'm';
        testblock[ 0x0108 ] = ( byte ) 'a';
        testblock[ 0x010A ] = ( byte ) 'r';
        testblock[ 0x010C ] = ( byte ) 'y';
        testblock[ 0x0140 ] = ( byte ) 16;
        testblock[ 0x0180 ] = ( byte ) 'w';
        testblock[ 0x0182 ] = ( byte ) 'i';
        testblock[ 0x0184 ] = ( byte ) 'n';
        testblock[ 0x0186 ] = ( byte ) 't';
        testblock[ 0x0188 ] = ( byte ) 'e';
        testblock[ 0x018A ] = ( byte ) 'r';
        testblock[ 0x018C ] = ( byte ) 'y';
        testblock[ 0x01C0 ] = ( byte ) 16;
        testblock[ 0x0200 ] = ( byte ) 'f';
        testblock[ 0x0202 ] = ( byte ) 'o';
        testblock[ 0x0204 ] = ( byte ) 'o';
        testblock[ 0x0240 ] = ( byte ) 8;
        verifyCorrect(blocks, testblock);
    }

    private void setDefaultBlock(byte [] testblock, int j)
    {
        int base  = j * 128;
        int index = 0;

        for (; index < 0x40; index++)
        {
            testblock[ base++ ] = ( byte ) 0;
        }
        testblock[ base++ ] = ( byte ) 2;
        testblock[ base++ ] = ( byte ) 0;
        index               += 2;
        for (; index < 0x44; index++)
        {
            testblock[ base++ ] = ( byte ) 0;
        }
        for (; index < 0x50; index++)
        {
            testblock[ base++ ] = ( byte ) 0xff;
        }
        for (; index < 0x80; index++)
        {
            testblock[ base++ ] = ( byte ) 0;
        }
    }

    private void verifyCorrect(BlockWritable [] blocks, byte [] testblock)
        throws IOException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(512
                                           * blocks.length);

        for (int j = 0; j < blocks.length; j++)
        {
            blocks[ j ].writeBlocks(stream);
        }
        byte[] output = stream.toByteArray();

        assertEquals(testblock.length, output.length);
        for (int j = 0; j < testblock.length; j++)
        {
            assertEquals("mismatch at offset " + j, testblock[ j ],
                         output[ j ]);
        }
    }

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out
            .println("Testing org.apache.poi.poifs.storage.PropertyBlock");
        junit.textui.TestRunner.run(TestPropertyBlock.class);
    }
}
