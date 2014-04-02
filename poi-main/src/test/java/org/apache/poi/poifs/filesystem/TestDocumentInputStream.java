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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.property.DirectoryProperty;
import org.apache.poi.poifs.storage.RawDataBlock;

/**
 * Class to test DocumentInputStream functionality
 *
 * @author Marc Johnson
 */

public final class TestDocumentInputStream extends TestCase {
   private DocumentNode     _workbook_n;
   private DocumentNode     _workbook_o;
   private byte[]           _workbook_data;
   private static final int _workbook_size = 5000;

   // non-even division of _workbook_size, also non-even division of
   // any block size
   private static final int _buffer_size   = 6;

	protected void setUp() throws Exception {
        int blocks = (_workbook_size + 511) / 512;

        _workbook_data = new byte[ 512 * blocks ];
        Arrays.fill(_workbook_data, ( byte ) -1);
        for (int j = 0; j < _workbook_size; j++)
        {
            _workbook_data[ j ] = ( byte ) (j * j);
        }
        
        // Create the Old POIFS Version
        RawDataBlock[]       rawBlocks = new RawDataBlock[ blocks ];
        ByteArrayInputStream stream    =
            new ByteArrayInputStream(_workbook_data);

        for (int j = 0; j < blocks; j++)
        {
            rawBlocks[ j ] = new RawDataBlock(stream);
        }
        POIFSDocument document = new POIFSDocument("Workbook", rawBlocks,
                                                   _workbook_size);

        _workbook_o = new DocumentNode(
            document.getDocumentProperty(),
            new DirectoryNode(
                new DirectoryProperty("Root Entry"), (POIFSFileSystem)null, null));
        
        // Now create the NPOIFS Version
        byte[] _workbook_data_only = new byte[_workbook_size];
        System.arraycopy(_workbook_data, 0, _workbook_data_only, 0, _workbook_size);
        
        NPOIFSFileSystem npoifs = new NPOIFSFileSystem();
        // Make it easy when debugging to see what isn't the doc
        byte[] minus1 = new byte[512];
        Arrays.fill(minus1, (byte)-1);
        npoifs.getBlockAt(-1).put(minus1);
        npoifs.getBlockAt(0).put(minus1);
        npoifs.getBlockAt(1).put(minus1);
        
        // Create the NPOIFS document
        _workbook_n = (DocumentNode)npoifs.createDocument(
              new ByteArrayInputStream(_workbook_data_only),
              "Workbook"
        );
    }

	/**
     * test constructor
     */
    public void testConstructor() throws IOException {
        DocumentInputStream ostream = new DocumentInputStream(_workbook_o);
        DocumentInputStream nstream = new NDocumentInputStream(_workbook_n);
        
        assertEquals(_workbook_size, _workbook_o.getSize());
        assertEquals(_workbook_size, _workbook_n.getSize());

        assertEquals(_workbook_size, ostream.available());
        assertEquals(_workbook_size, nstream.available());
    }

    /**
     * test available() behavior
     */
    public void testAvailable() throws IOException {
        DocumentInputStream ostream = new DocumentInputStream(_workbook_o);
        DocumentInputStream nstream = new NDocumentInputStream(_workbook_n);

        assertEquals(_workbook_size, ostream.available());
        assertEquals(_workbook_size, nstream.available());
        ostream.close();
        nstream.close();
        
        try {
           ostream.available();
           fail("Should have caught IOException");
        } catch (IllegalStateException ignored) {
           // as expected
        }
        try {
           nstream.available();
           fail("Should have caught IOException");
       } catch (IllegalStateException ignored) {
           // as expected
       }
    }

    /**
     * test mark/reset/markSupported.
     */
    public void testMarkFunctions() throws IOException {
        byte[] buffer = new byte[ _workbook_size / 5 ];
        byte[] small_buffer = new byte[212];
       
        DocumentInputStream[] streams = new DocumentInputStream[] {
              new DocumentInputStream(_workbook_o),
              new NDocumentInputStream(_workbook_n)
        };
        for(DocumentInputStream stream : streams) {
           // Read a fifth of it, and check all's correct
           stream.read(buffer);
           for (int j = 0; j < buffer.length; j++) {
              assertEquals(
                    "checking byte " + j, 
                    _workbook_data[ j ], buffer[ j ]
              );
           }
           assertEquals(_workbook_size - buffer.length, stream.available());
           
           // Reset, and check the available goes back to being the
           //  whole of the stream
           stream.reset();
           assertEquals(_workbook_size, stream.available());
           
           
           // Read part of a block
           stream.read(small_buffer);
           for (int j = 0; j < small_buffer.length; j++) {
              assertEquals(
                    "checking byte " + j, 
                    _workbook_data[ j ], small_buffer[ j ]
              );
           }
           assertEquals(_workbook_size - small_buffer.length, stream.available());
           stream.mark(0);
           
           // Read the next part
           stream.read(small_buffer);
           for (int j = 0; j < small_buffer.length; j++) {
              assertEquals(
                    "checking byte " + j, 
                    _workbook_data[ j+small_buffer.length ], small_buffer[ j ]
              );
           }
           assertEquals(_workbook_size - 2*small_buffer.length, stream.available());
           
           // Reset, check it goes back to where it was
           stream.reset();
           assertEquals(_workbook_size - small_buffer.length, stream.available());
           
           // Read 
           stream.read(small_buffer);
           for (int j = 0; j < small_buffer.length; j++) {
              assertEquals(
                    "checking byte " + j, 
                    _workbook_data[ j+small_buffer.length ], small_buffer[ j ]
              );
           }
           assertEquals(_workbook_size - 2*small_buffer.length, stream.available());
           
           
           // Now read at various points
           Arrays.fill(small_buffer, ( byte ) 0);
           stream.read(small_buffer, 6, 8);
           stream.read(small_buffer, 100, 10);
           stream.read(small_buffer, 150, 12);
           int pos = small_buffer.length * 2;
           for (int j = 0; j < small_buffer.length; j++) {
              byte exp = 0;
              if(j>= 6 && j<6+8) {
                 exp = _workbook_data[pos];
                 pos++;
              }
              if(j>= 100 && j<100+10) {
                 exp = _workbook_data[pos];
                 pos++;
              }
              if(j>= 150 && j<150+12) {
                 exp = _workbook_data[pos];
                 pos++;
              }
              
              assertEquals("checking byte " + j, exp, small_buffer[j]);
           }
        }
           
        // Now repeat it with spanning multiple blocks
        streams = new DocumentInputStream[] {
              new DocumentInputStream(_workbook_o),
              new NDocumentInputStream(_workbook_n)
        };
        for(DocumentInputStream stream : streams) {
           // Read several blocks work
           buffer = new byte[ _workbook_size / 5 ];
           stream.read(buffer);
           for (int j = 0; j < buffer.length; j++) {
              assertEquals(
                    "checking byte " + j, 
                    _workbook_data[ j ], buffer[ j ]
              );
           }
           assertEquals(_workbook_size - buffer.length, stream.available());
           
           // Read all of it again, check it began at the start again
           stream.reset();
           assertEquals(_workbook_size, stream.available());
           
           stream.read(buffer);
           for (int j = 0; j < buffer.length; j++) {
              assertEquals(
                    "checking byte " + j, 
                    _workbook_data[ j ], buffer[ j ]
              );
           }
           
           // Mark our position, and read another whole buffer
           stream.mark(12);
           stream.read(buffer);
           assertEquals(_workbook_size - (2 * buffer.length),
                 stream.available());
           for (int j = buffer.length; j < (2 * buffer.length); j++)
           {
              assertEquals("checking byte " + j, _workbook_data[ j ],
                    buffer[ j - buffer.length ]);
           }
           
           // Reset, should go back to only one buffer full read
           stream.reset();
           assertEquals(_workbook_size - buffer.length, stream.available());
           
           // Read the buffer again
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
    }

    /**
     * test simple read method
     */
    public void testReadSingleByte() throws IOException {
       DocumentInputStream[] streams = new DocumentInputStream[] {
             new DocumentInputStream(_workbook_o),
             new NDocumentInputStream(_workbook_n)
       };
       for(DocumentInputStream stream : streams) {
          int remaining = _workbook_size;

          // Try and read each byte in turn
          for (int j = 0; j < _workbook_size; j++) {
             int b = stream.read();
             assertTrue("checking sign of " + j, b >= 0);
             assertEquals("validating byte " + j, _workbook_data[ j ],
                   ( byte ) b);
             remaining--;
             assertEquals("checking remaining after reading byte " + j,
                   remaining, stream.available());
          }
          
          // Ensure we fell off the end
          assertEquals(-1, stream.read());
          
          // Check that after close we can no longer read
          stream.close();
          try {
             stream.read();
             fail("Should have caught IOException");
          } catch (IOException ignored) {
             // as expected
          }
       }
    }

    /**
     * Test buffered read
     */
    public void testBufferRead() throws IOException {
       DocumentInputStream[] streams = new DocumentInputStream[] {
             new DocumentInputStream(_workbook_o),
             new NDocumentInputStream(_workbook_n)
       };
       for(DocumentInputStream stream : streams) {
          // Need to give a byte array to read
          try {
             stream.read(null);
             fail("Should have caught NullPointerException");
          } catch (NullPointerException ignored) {
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
          try {
             stream.read(buffer);
             fail("Should have caught IOException");
          } catch (IOException ignored) {
             // as expected
          }
       }
    }

    /**
     * Test complex buffered read
     */
    public void testComplexBufferRead() throws IOException {
       DocumentInputStream[] streams = new DocumentInputStream[] {
             new DocumentInputStream(_workbook_o),
             new NDocumentInputStream(_workbook_n)
       };
       for(DocumentInputStream stream : streams) {
          try {
             stream.read(null, 0, 1);
             fail("Should have caught NullPointerException");
          } catch (IllegalArgumentException ignored) {
             // as expected
          }

          // test illegal offsets and lengths
          try {
             stream.read(new byte[ 5 ], -4, 0);
             fail("Should have caught IndexOutOfBoundsException");
          } catch (IndexOutOfBoundsException ignored) {
             // as expected
          }
          try {
             stream.read(new byte[ 5 ], 0, -4);
             fail("Should have caught IndexOutOfBoundsException");
          } catch (IndexOutOfBoundsException ignored) {
             // as expected
          }
          try {
             stream.read(new byte[ 5 ], 0, 6);
             fail("Should have caught IndexOutOfBoundsException");
          } catch (IndexOutOfBoundsException ignored) {
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
          try {
             stream.read(buffer, 0, 1);
             fail("Should have caught IOException");
          } catch (IOException ignored) {
             // as expected
          }
       }
    }

    /**
     * Tests that we can skip within the stream
     */
    public void testSkip() throws IOException {
       DocumentInputStream[] streams = new DocumentInputStream[] {
             new DocumentInputStream(_workbook_o),
             new NDocumentInputStream(_workbook_n)
       };
       for(DocumentInputStream stream : streams) {
          assertEquals(_workbook_size, stream.available());
          int count = stream.available();

          while (stream.available() >= _buffer_size) {
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
    
    /**
     * Test that we can read files at multiple levels down the tree
     */
    public void testReadMultipleTreeLevels() throws Exception {
       final POIDataSamples _samples = POIDataSamples.getPublisherInstance();
       File sample = _samples.getFile("Sample.pub");
       
       DocumentInputStream stream;
       
       NPOIFSFileSystem npoifs = new NPOIFSFileSystem(sample);
       POIFSFileSystem  opoifs = new POIFSFileSystem(new FileInputStream(sample));
       
       // Ensure we have what we expect on the root
       assertEquals(npoifs, npoifs.getRoot().getNFileSystem());
       assertEquals(null,   npoifs.getRoot().getFileSystem());
       assertEquals(opoifs, opoifs.getRoot().getFileSystem());
       assertEquals(null,   opoifs.getRoot().getNFileSystem());
       
       // Check inside
       for(DirectoryNode root : new DirectoryNode[] { opoifs.getRoot(), npoifs.getRoot() }) {
          // Top Level
          Entry top = root.getEntry("Contents");
          assertEquals(true, top.isDocumentEntry());
          stream = root.createDocumentInputStream(top);
          stream.read();
          
          // One Level Down
          DirectoryNode escher = (DirectoryNode)root.getEntry("Escher");
          Entry one = escher.getEntry("EscherStm");
          assertEquals(true, one.isDocumentEntry());
          stream = escher.createDocumentInputStream(one);
          stream.read();
          
          // Two Levels Down
          DirectoryNode quill = (DirectoryNode)root.getEntry("Quill");
          DirectoryNode quillSub = (DirectoryNode)quill.getEntry("QuillSub");
          Entry two = quillSub.getEntry("CONTENTS");
          assertEquals(true, two.isDocumentEntry());
          stream = quillSub.createDocumentInputStream(two);
          stream.read();
       }
    }
}
