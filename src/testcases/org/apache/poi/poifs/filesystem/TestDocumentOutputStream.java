
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

package org.apache.poi.poifs.filesystem;

import java.io.*;

import java.util.*;

import junit.framework.*;

import org.apache.poi.poifs.property.DirectoryProperty;
import org.apache.poi.poifs.property.DocumentProperty;
import org.apache.poi.poifs.storage.RawDataBlock;

/**
 * Class to test DocumentOutputStream functionality
 *
 * @author Marc Johnson
 */

public class TestDocumentOutputStream
    extends TestCase
{

    /**
     * Constructor TestDocumentOutputStream
     *
     * @param name
     *
     * @exception IOException
     */

    public TestDocumentOutputStream(String name)
        throws IOException
    {
        super(name);
    }

    /**
     * test write(int) behavior
     *
     * @exception IOException
     */

    public void testWrite1()
        throws IOException
    {
        ByteArrayOutputStream stream  = new ByteArrayOutputStream();
        DocumentOutputStream  dstream = new DocumentOutputStream(stream, 25);

        for (int j = 0; j < 25; j++)
        {
            dstream.write(j);
        }
        try
        {
            dstream.write(0);
            fail("Should have caught IOException");
        }
        catch (IOException ignored)
        {
        }
        byte[] output = stream.toByteArray();

        assertEquals(25, output.length);
        for (int j = 0; j < 25; j++)
        {
            assertEquals(( byte ) j, output[ j ]);
        }
        stream.close();
    }

    /**
     * test write(byte[]) behavior
     *
     * @exception IOException
     */

    public void testWrite2()
        throws IOException
    {
        ByteArrayOutputStream stream  = new ByteArrayOutputStream();
        DocumentOutputStream  dstream = new DocumentOutputStream(stream, 25);

        for (int j = 0; j < 6; j++)
        {
            byte[] array = new byte[ 4 ];

            Arrays.fill(array, ( byte ) j);
            dstream.write(array);
        }
        try
        {
            byte[] array = new byte[ 4 ];

            Arrays.fill(array, ( byte ) 7);
            dstream.write(array);
            fail("Should have caught IOException");
        }
        catch (IOException ignored)
        {
        }
        byte[] output = stream.toByteArray();

        assertEquals(24, output.length);
        for (int j = 0; j < 6; j++)
        {
            for (int k = 0; k < 4; k++)
            {
                assertEquals(String.valueOf((j * 4) + k), ( byte ) j,
                             output[ (j * 4) + k ]);
            }
        }
        stream.close();
    }

    /**
     * test write(byte[], int, int) behavior
     *
     * @exception IOException
     */

    public void testWrite3()
        throws IOException
    {
        ByteArrayOutputStream stream  = new ByteArrayOutputStream();
        DocumentOutputStream  dstream = new DocumentOutputStream(stream, 25);
        byte[]                array   = new byte[ 50 ];

        for (int j = 0; j < 50; j++)
        {
            array[ j ] = ( byte ) j;
        }
        dstream.write(array, 1, 25);
        try
        {
            dstream.write(array, 0, 1);
            fail("Should have caught IOException");
        }
        catch (IOException ignored)
        {
        }
        byte[] output = stream.toByteArray();

        assertEquals(25, output.length);
        for (int j = 0; j < 25; j++)
        {
            assertEquals(( byte ) (j + 1), output[ j ]);
        }
        stream.close();
    }

    /**
     * test writeFiller()
     *
     * @exception IOException
     */

    public void testWriteFiller()
        throws IOException
    {
        ByteArrayOutputStream stream  = new ByteArrayOutputStream();
        DocumentOutputStream  dstream = new DocumentOutputStream(stream, 25);

        for (int j = 0; j < 25; j++)
        {
            dstream.write(j);
        }
        try
        {
            dstream.write(0);
            fail("Should have caught IOException");
        }
        catch (IOException ignored)
        {
        }
        dstream.writeFiller(100, ( byte ) 0xff);
        byte[] output = stream.toByteArray();

        assertEquals(100, output.length);
        for (int j = 0; j < 25; j++)
        {
            assertEquals(( byte ) j, output[ j ]);
        }
        for (int j = 25; j < 100; j++)
        {
            assertEquals(String.valueOf(j), ( byte ) 0xff, output[ j ]);
        }
        stream.close();
    }

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out.println(
            "Testing org.apache.poi.poifs.filesystem.DocumentOutputStream");
        junit.textui.TestRunner.run(TestDocumentOutputStream.class);
    }
}
