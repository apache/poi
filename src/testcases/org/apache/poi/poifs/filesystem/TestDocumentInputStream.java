
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
 * Class to test DocumentInputStream functionality
 *
 * @author Marc Johnson
 */

public class TestDocumentInputStream
    extends TestCase
{

    /**
     * Constructor TestDocumentInputStream
     *
     * @param name
     *
     * @exception IOException
     */

    public TestDocumentInputStream(String name)
        throws IOException
    {
        super(name);
        int blocks = (_workbook_size + 511) / 512;

        _workbook_data = new byte[ 512 * blocks ];
        Arrays.fill(_workbook_data, ( byte ) -1);
        for (int j = 0; j < _workbook_size; j++)
        {
            _workbook_data[ j ] = ( byte ) (j * j);
        }
        RawDataBlock[]       rawBlocks = new RawDataBlock[ blocks ];
        ByteArrayInputStream stream    =
            new ByteArrayInputStream(_workbook_data);

        for (int j = 0; j < blocks; j++)
        {
            rawBlocks[ j ] = new RawDataBlock(stream);
        }
        POIFSDocument document = new POIFSDocument("Workbook", rawBlocks,
                                                   _workbook_size);

        _workbook = new DocumentNode(
            document.getDocumentProperty(),
            new DirectoryNode(
                new DirectoryProperty("Root Entry"), null, null));
    }

    private DocumentNode     _workbook;
    private byte[]           _workbook_data;
    private static final int _workbook_size = 5000;

    // non-even division of _workbook_size, also non-even division of
    // any block size
    private static final int _buffer_size   = 6;

    /**
     * test constructor
     *
     * @exception IOException
     */

    public void testConstructor()
        throws IOException
    {
        DocumentInputStream stream = new DocumentInputStream(_workbook);

        assertEquals(_workbook_size, stream.available());
    }

    /**
     * test available() behavior
     *
     * @exception IOException
     */

    public void testAvailable()
        throws IOException
    {
        DocumentInputStream stream = new DocumentInputStream(_workbook);

        assertEquals(_workbook_size, stream.available());
        stream.close();
        try
        {
            stream.available();
            fail("Should have caught IOException");
        }
        catch (IOException ignored)
        {

            // as expected
        }
    }

    /**
     * test mark/reset/markSupported.
     *
     * @exception IOException
     */

    public void testMarkFunctions()
        throws IOException
    {
        DocumentInputStream stream = new DocumentInputStream(_workbook);
        byte[]              buffer = new byte[ _workbook_size / 5 ];

        stream.read(buffer);
        for (int j = 0; j < buffer.length; j++)
        {
            assertEquals("checking byte " + j, _workbook_data[ j ],
                         buffer[ j ]);
        }
        assertEquals(_workbook_size - buffer.length, stream.available());
        stream.reset();
        assertEquals(_workbook_size, stream.available());
        stream.read(buffer);
        stream.mark(12);
        stream.read(buffer);
        assertEquals(_workbook_size - (2 * buffer.length),
                     stream.available());
        for (int j = buffer.length; j < (2 * buffer.length); j++)
        {
            assertEquals("checking byte " + j, _workbook_data[ j ],
                         buffer[ j - buffer.length ]);
        }
        stream.reset();
        assertEquals(_workbook_size - buffer.length, stream.available());
        stream.read(buffer);
        assertEquals(_workbook_size - (2 * buffer.length),
                     stream.available());
        for (int j = buffer.length; j < (2 * buffer.length); j++)
        {
            assertEquals("checking byte " + j, _workbook_data[ j ],
                         buffer[ j - buffer.length ]);
        }
        assertTrue(stream.markSupported());
    }

    /**
     * test simple read method
     *
     * @exception IOException
     */

    public void testReadSingleByte()
        throws IOException
    {
        DocumentInputStream stream    = new DocumentInputStream(_workbook);
        int                 remaining = _workbook_size;

        for (int j = 0; j < _workbook_size; j++)
        {
	    int b = stream.read();
	    assertTrue("checking sign of " + j, b >= 0);
            assertEquals("validating byte " + j, _workbook_data[ j ],
                         ( byte ) b);
            remaining--;
            assertEquals("checking remaining after reading byte " + j,
                         remaining, stream.available());
        }
        assertEquals(-1, stream.read());
        stream.close();
        try
        {
            stream.read();
            fail("Should have caught IOException");
        }
        catch (IOException ignored)
        {

            // as expected
        }
    }

    /**
     * Test buffered read
     *
     * @exception IOException
     */

    public void testBufferRead()
        throws IOException
    {
        DocumentInputStream stream = new DocumentInputStream(_workbook);

        try
        {
            stream.read(null);
            fail("Should have caught NullPointerException");
        }
        catch (NullPointerException ignored)
        {

            // as expected
        }

        // test reading zero length buffer
        assertEquals(0, stream.read(new byte[ 0 ]));
        assertEquals(_workbook_size, stream.available());
        byte[] buffer = new byte[ _buffer_size ];
        int    offset = 0;

        while (stream.available() >= buffer.length)
        {
            assertEquals(_buffer_size, stream.read(buffer));
            for (int j = 0; j < buffer.length; j++)
            {
                assertEquals("in main loop, byte " + offset,
                             _workbook_data[ offset ], buffer[ j ]);
                offset++;
            }
            assertEquals("offset " + offset, _workbook_size - offset,
                         stream.available());
        }
        assertEquals(_workbook_size % _buffer_size, stream.available());
        Arrays.fill(buffer, ( byte ) 0);
        int count = stream.read(buffer);

        assertEquals(_workbook_size % _buffer_size, count);
        for (int j = 0; j < count; j++)
        {
            assertEquals("past main loop, byte " + offset,
                         _workbook_data[ offset ], buffer[ j ]);
            offset++;
        }
        assertEquals(_workbook_size, offset);
        for (int j = count; j < buffer.length; j++)
        {
            assertEquals("checking remainder, byte " + j, 0, buffer[ j ]);
        }
        assertEquals(-1, stream.read(buffer));
        stream.close();
        try
        {
            stream.read(buffer);
            fail("Should have caught IOException");
        }
        catch (IOException ignored)
        {

            // as expected
        }
    }

    /**
     * Test complex buffered read
     *
     * @exception IOException
     */

    public void testComplexBufferRead()
        throws IOException
    {
        DocumentInputStream stream = new DocumentInputStream(_workbook);

        try
        {
            stream.read(null, 0, 1);
            fail("Should have caught NullPointerException");
        }
        catch (NullPointerException ignored)
        {

            // as expected
        }

        // test illegal offsets and lengths
        try
        {
            stream.read(new byte[ 5 ], -4, 0);
            fail("Should have caught IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ignored)
        {

            // as expected
        }
        try
        {
            stream.read(new byte[ 5 ], 0, -4);
            fail("Should have caught IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ignored)
        {

            // as expected
        }
        try
        {
            stream.read(new byte[ 5 ], 0, 6);
            fail("Should have caught IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ignored)
        {

            // as expected
        }

        // test reading zero
        assertEquals(0, stream.read(new byte[ 5 ], 0, 0));
        assertEquals(_workbook_size, stream.available());
        byte[] buffer = new byte[ _workbook_size ];
        int    offset = 0;

        while (stream.available() >= _buffer_size)
        {
            Arrays.fill(buffer, ( byte ) 0);
            assertEquals(_buffer_size,
                         stream.read(buffer, offset, _buffer_size));
            for (int j = 0; j < offset; j++)
            {
                assertEquals("checking byte " + j, 0, buffer[ j ]);
            }
            for (int j = offset; j < (offset + _buffer_size); j++)
            {
                assertEquals("checking byte " + j, _workbook_data[ j ],
                             buffer[ j ]);
            }
            for (int j = offset + _buffer_size; j < buffer.length; j++)
            {
                assertEquals("checking byte " + j, 0, buffer[ j ]);
            }
            offset += _buffer_size;
            assertEquals("offset " + offset, _workbook_size - offset,
                         stream.available());
        }
        assertEquals(_workbook_size % _buffer_size, stream.available());
        Arrays.fill(buffer, ( byte ) 0);
        int count = stream.read(buffer, offset,
                                _workbook_size % _buffer_size);

        assertEquals(_workbook_size % _buffer_size, count);
        for (int j = 0; j < offset; j++)
        {
            assertEquals("checking byte " + j, 0, buffer[ j ]);
        }
        for (int j = offset; j < buffer.length; j++)
        {
            assertEquals("checking byte " + j, _workbook_data[ j ],
                         buffer[ j ]);
        }
        assertEquals(_workbook_size, offset + count);
        for (int j = count; j < offset; j++)
        {
            assertEquals("byte " + j, 0, buffer[ j ]);
        }
        assertEquals(-1, stream.read(buffer, 0, 1));
        stream.close();
        try
        {
            stream.read(buffer, 0, 1);
            fail("Should have caught IOException");
        }
        catch (IOException ignored)
        {

            // as expected
        }
    }

    /**
     * test skip
     *
     * @exception IOException
     */

    public void testSkip()
        throws IOException
    {
        DocumentInputStream stream = new DocumentInputStream(_workbook);

        assertEquals(_workbook_size, stream.available());
        int count = stream.available();

        while (stream.available() >= _buffer_size)
        {
            assertEquals(_buffer_size, stream.skip(_buffer_size));
            count -= _buffer_size;
            assertEquals(count, stream.available());
        }
        assertEquals(_workbook_size % _buffer_size,
                     stream.skip(_buffer_size));
        assertEquals(0, stream.available());
        stream.reset();
        assertEquals(_workbook_size, stream.available());
        assertEquals(_workbook_size, stream.skip(_workbook_size * 2));
        assertEquals(0, stream.available());
        stream.reset();
        assertEquals(_workbook_size, stream.available());
        assertEquals(_workbook_size,
                     stream.skip(2 + ( long ) Integer.MAX_VALUE));
        assertEquals(0, stream.available());
    }

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out.println(
            "Testing org.apache.poi.poifs.filesystem.DocumentInputStream");
        junit.textui.TestRunner.run(TestDocumentInputStream.class);
    }
}
