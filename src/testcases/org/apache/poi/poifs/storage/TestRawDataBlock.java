
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

import junit.framework.*;

/**
 * Class to test RawDataBlock functionality
 *
 * @author Marc Johnson
 */

public class TestRawDataBlock
    extends TestCase
{

    /**
     * Constructor TestRawDataBlock
     *
     * @param name
     */

    public TestRawDataBlock(String name)
    {
        super(name);
    }

    /**
     * Test creating a normal RawDataBlock
     *
     * @exception IOException
     */

    public void testNormalConstructor()
        throws IOException
    {
        byte[] data = new byte[ 512 ];

        for (int j = 0; j < 512; j++)
        {
            data[ j ] = ( byte ) j;
        }
        RawDataBlock block = new RawDataBlock(new ByteArrayInputStream(data));

        assertTrue("Should not be at EOF", !block.eof());
        byte[] out_data = block.getData();

        assertEquals("Should be same length", data.length, out_data.length);
        for (int j = 0; j < 512; j++)
        {
            assertEquals("Should be same value at offset " + j, data[ j ],
                         out_data[ j ]);
        }
    }

    /**
     * Test creating an empty RawDataBlock
     *
     * @exception IOException
     */

    public void testEmptyConstructor()
        throws IOException
    {
        byte[]       data  = new byte[ 0 ];
        RawDataBlock block = new RawDataBlock(new ByteArrayInputStream(data));

        assertTrue("Should be at EOF", block.eof());
        try
        {
            block.getData();
        }
        catch (IOException ignored)
        {

            // as expected
        }
    }

    /**
     * Test creating a short RawDataBlock
     */

    public void testShortConstructor()
    {
        for (int k = 1; k < 512; k++)
        {
            byte[] data = new byte[ k ];

            for (int j = 0; j < k; j++)
            {
                data[ j ] = ( byte ) j;
            }
            RawDataBlock block = null;

            try
            {
                block = new RawDataBlock(new ByteArrayInputStream(data));
                fail("Should have thrown IOException creating short block");
            }
            catch (IOException ignored)
            {

                // as expected
            }
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
            .println("Testing org.apache.poi.poifs.storage.RawDataBlock");
        junit.textui.TestRunner.run(TestRawDataBlock.class);
    }
}
