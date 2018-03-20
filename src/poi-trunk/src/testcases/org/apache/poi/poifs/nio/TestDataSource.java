
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
        

package org.apache.poi.poifs.nio;

import junit.framework.TestCase;
import org.apache.poi.POIDataSamples;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;

import java.io.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Tests for the datasource implementations
 */
public class TestDataSource extends TestCase
{
   private static POIDataSamples data = POIDataSamples.getPOIFSInstance();
   
   public void testFile() throws Exception {
      File f = data.getFile("Notes.ole2");
      
      FileBackedDataSource ds = new FileBackedDataSource(f);
      try {
          checkDataSource(ds, false);
      } finally {
          ds.close();
      }
      
      // try a second time
      ds = new FileBackedDataSource(f);
      try {
          checkDataSource(ds, false);
      } finally {
          ds.close();
      }
   }

   public void testFileWritable() throws Exception {
       File temp = TempFile.createTempFile("TestDataSource", ".test");
       try {
           writeDataToFile(temp);
       
           FileBackedDataSource ds = new FileBackedDataSource(temp, false);
           try {
               checkDataSource(ds, true);
           } finally {
               ds.close();
           }
           
           // try a second time
           ds = new FileBackedDataSource(temp, false);
           try {
               checkDataSource(ds, true);
           } finally {
               ds.close();
           }

           writeDataToFile(temp);
       } finally {
           assertTrue(temp.exists());
           assertTrue("Could not delete file " + temp, temp.delete());
       }
    }


   public void testRewritableFile() throws Exception {
       File temp = TempFile.createTempFile("TestDataSource", ".test");
       try {
           writeDataToFile(temp);
           
           FileBackedDataSource ds = new FileBackedDataSource(temp, true);
           try {
               ByteBuffer buf = ds.read(0, 10);
               assertNotNull(buf);
               buf = ds.read(8, 0x400);
               assertNotNull(buf);
           } finally {
               ds.close();
           }
           
           // try a second time
           ds = new FileBackedDataSource(temp, true);
           try {
               ByteBuffer buf = ds.read(0, 10);
               assertNotNull(buf);
               buf = ds.read(8, 0x400);
               assertNotNull(buf);
           } finally {
               ds.close();
           }
           
           writeDataToFile(temp);
       } finally {
           assertTrue(temp.exists());
           assertTrue(temp.delete());
       }
    }

    private void writeDataToFile(File temp) throws IOException {
        OutputStream str = new FileOutputStream(temp);
           try {
               InputStream in = data.openResourceAsStream("Notes.ole2");
               try {
                   IOUtils.copy(in, str);
               } finally {
                   in.close();
               }
           } finally {
               str.close();
           }
    }
    
    private void checkDataSource(FileBackedDataSource ds, boolean writeable) throws IOException {
        assertEquals(writeable, ds.isWriteable());
        assertNotNull(ds.getChannel());
        
        // rewriting changes the size
        if(writeable) {
            assertTrue("Had: " + ds.size(), ds.size() == 8192 || ds.size() == 8198); 
        } else {
            assertEquals(8192, ds.size());
        }

        // Start of file
        ByteBuffer bs;
        bs = ds.read(4, 0);
        assertEquals(4, bs.capacity());
        assertEquals(0, bs.position());
        assertEquals(0xd0 - 256, bs.get(0));
        assertEquals(0xcf - 256, bs.get(1));
        assertEquals(0x11, bs.get(2));
        assertEquals(0xe0 - 256, bs.get(3));
        assertEquals(0xd0 - 256, bs.get());
        assertEquals(0xcf - 256, bs.get());
        assertEquals(0x11, bs.get());
        assertEquals(0xe0 - 256, bs.get());

        // Mid way through
        bs = ds.read(8, 0x400);
        assertEquals(8, bs.capacity());
        assertEquals(0, bs.position());
        assertEquals((byte) 'R', bs.get(0));
        assertEquals(0, bs.get(1));
        assertEquals((byte) 'o', bs.get(2));
        assertEquals(0, bs.get(3));
        assertEquals((byte) 'o', bs.get(4));
        assertEquals(0, bs.get(5));
        assertEquals((byte) 't', bs.get(6));
        assertEquals(0, bs.get(7));

        // Can go to the end, but not past it
        bs = ds.read(8, 8190);
        assertEquals(0, bs.position()); // TODO How best to warn of a short read?

        // Can't go off the end
        try {
            ds.read(4, 8192);
            if(!writeable) {
                fail("Shouldn't be able to read off the end of the file");
            }
        } catch (IndexOutOfBoundsException e) {
            // expected here
        }
    }

   public void testByteArray() throws Exception {
      byte[] data = new byte[256];
      byte b;
      for(int i=0; i<data.length; i++) {
         b = (byte)i;
         data[i] = b;
      }
      
      ByteArrayBackedDataSource ds = new ByteArrayBackedDataSource(data);
      
      // Start
      ByteBuffer bs; 
      bs = ds.read(4, 0);
      assertEquals(0, bs.position());
      assertEquals(0x00, bs.get());
      assertEquals(0x01, bs.get());
      assertEquals(0x02, bs.get());
      assertEquals(0x03, bs.get());
      
      // Middle
      bs = ds.read(4, 100);
      assertEquals(100, bs.position());
      assertEquals(100, bs.get());
      assertEquals(101, bs.get());
      assertEquals(102, bs.get());
      assertEquals(103, bs.get());
      
      // End
      bs = ds.read(4, 252);
      assertEquals(-4, bs.get());
      assertEquals(-3, bs.get());
      assertEquals(-2, bs.get());
      assertEquals(-1, bs.get());
      
      // Off the end
      bs = ds.read(4, 254);
      assertEquals(-2, bs.get());
      assertEquals(-1, bs.get());
      try {
         bs.get();
         fail("Shouldn't be able to read off the end");
      } catch(BufferUnderflowException e) {
          // expected here
      }

      // Past the end
      try {
         ds.read(4, 256);
         fail("Shouldn't be able to read off the end");
      } catch(IndexOutOfBoundsException e) {
          // expected here
      }
      
      
      // Overwrite
      bs = ByteBuffer.allocate(4);
      bs.put(0, (byte)-55);
      bs.put(1, (byte)-54);
      bs.put(2, (byte)-53);
      bs.put(3, (byte)-52);
      
      assertEquals(256, ds.size());
      ds.write(bs, 40);
      assertEquals(256, ds.size());
      bs = ds.read(4, 40);
      
      assertEquals(-55, bs.get());
      assertEquals(-54, bs.get());
      assertEquals(-53, bs.get());
      assertEquals(-52, bs.get());
      
      // Append
      bs = ByteBuffer.allocate(4);
      bs.put(0, (byte)-55);
      bs.put(1, (byte)-54);
      bs.put(2, (byte)-53);
      bs.put(3, (byte)-52);
      
      assertEquals(256, ds.size());
      ds.write(bs, 256);
      assertEquals(260, ds.size());
      
      bs = ds.read(4, 256);
      assertEquals(256, bs.position());
      assertEquals(-55, bs.get());
      assertEquals(-54, bs.get());
      assertEquals(-53, bs.get());
      assertEquals(-52, bs.get());
   }
}
