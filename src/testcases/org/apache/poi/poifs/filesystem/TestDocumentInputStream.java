/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.poifs.filesystem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.poi.poifs.property.DirectoryProperty;
import org.apache.poi.poifs.storage.RawDataBlock;

/**
 * Class to test DocumentInputStream functionality
 *
 * @author Marc Johnson
 */

public final class TestDocumentInputStream extends TestCase {

	protected void setUp() throws Exception {
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
     */
    public void testConstructor() throws IOException {
        DocumentInputStream stream = new DocumentInputStream(_workbook);

        assertEquals(_workbook_size, stream.available());
    }

    /**
     * test available() behavior
     */
    public void testAvailable() throws IOException {
        DocumentInputStream stream = new DocumentInputStream(_workbook);

        assertEquals(_workbook_size, stream.available());
        stream.close();
        try
        {
            stream.available();
            fail("Should have caught IOException");
        } catch (IllegalStateException ignored) {

            // as expected
        }
    }

    /**
     * test mark/reset/markSupported.
     */
    public void testMarkFunctions() throws IOException {
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
     */
    public void testReadSingleByte() throws IOException {
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
     */
    public void testBufferRead() throws IOException {
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
     */
    public void testComplexBufferRead() throws IOException {
        DocumentInputStream stream = new DocumentInputStream(_workbook);

        try {
            stream.read(null, 0, 1);
            fail("Should have caught NullPointerException");
        } catch (IllegalArgumentException ignored) {
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
     */
    public void testSkip() throws IOException {
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
}
