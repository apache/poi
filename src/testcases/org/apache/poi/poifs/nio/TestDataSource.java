
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

import java.io.File;
import java.nio.ByteBuffer;

import org.apache.poi.POIDataSamples;

import junit.framework.TestCase;

/**
 * Tests for the datasource implementations
 */
public class TestDataSource extends TestCase
{
   private static POIDataSamples data = POIDataSamples.getPOIFSInstance();
   
   public void testFile() throws Exception {
      File f = data.getFile("Notes.ole2");
      
      FileBackedDataSource ds = new FileBackedDataSource(f);
      assertEquals(8192, ds.size());
      
      // Start of file
      ByteBuffer bs = ByteBuffer.allocate(4); 
      ds.read(bs, 0);
      assertEquals(4, bs.capacity());
      assertEquals(4, bs.position());
      assertEquals(0xd0-256, bs.get(0));
      assertEquals(0xcf-256, bs.get(1));
      assertEquals(0x11-000, bs.get(2));
      assertEquals(0xe0-256, bs.get(3));
      
      // Mid way through
      bs = ByteBuffer.allocate(8);
      ds.read(bs, 0x400);
      assertEquals(8, bs.capacity());
      assertEquals(8, bs.position());
      assertEquals((byte)'R', bs.get(0));
      assertEquals(0, bs.get(1));
      assertEquals((byte)'o', bs.get(2));
      assertEquals(0, bs.get(3));
      assertEquals((byte)'o', bs.get(4));
      assertEquals(0, bs.get(5));
      assertEquals((byte)'t', bs.get(6));
      assertEquals(0, bs.get(7));
      
      // Can go to the end, but not past it
      bs.clear();
      ds.read(bs, 8190);
      assertEquals(2, bs.position());
      
      // Can't go off the end
      try {
         bs.clear();
         ds.read(bs, 8192);
         fail("Shouldn't be able to read off the end of the file");
      } catch(IllegalArgumentException e) {}
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
      ByteBuffer bs = ByteBuffer.allocate(4); 
      ds.read(bs, 0);
      assertEquals(4, bs.capacity());
      assertEquals(4, bs.position());
      assertEquals(0x00, bs.get(0));
      assertEquals(0x01, bs.get(1));
      assertEquals(0x02, bs.get(2));
      assertEquals(0x03, bs.get(3));
      
      // Middle
      bs.clear(); 
      ds.read(bs, 100);
      assertEquals(4, bs.capacity());
      assertEquals(4, bs.position());
      assertEquals(100, bs.get(0));
      assertEquals(101, bs.get(1));
      assertEquals(102, bs.get(2));
      assertEquals(103, bs.get(3));
      
      // End
      bs.clear(); 
      ds.read(bs, 252);
      assertEquals(4, bs.capacity());
      assertEquals(4, bs.position());
      assertEquals(-4, bs.get(0));
      assertEquals(-3, bs.get(1));
      assertEquals(-2, bs.get(2));
      assertEquals(-1, bs.get(3));
      
      // Off the end
      bs.clear(); 
      ds.read(bs, 254);
      assertEquals(4, bs.capacity());
      assertEquals(2, bs.position());
      assertEquals(-2, bs.get(0));
      assertEquals(-1, bs.get(1));

      // Past the end
      bs.clear(); 
      try {
         ds.read(bs, 256);
         fail("Shouldn't be able to read off the end");
      } catch(IndexOutOfBoundsException e) {}
      
      
      // Overwrite
      bs.clear();
      bs.put(0, (byte)-55);
      bs.put(1, (byte)-54);
      bs.put(2, (byte)-53);
      bs.put(3, (byte)-52);
      
      ds.write(bs, 40);
      bs.clear();
      ds.read(bs, 40);
      
      assertEquals(4, bs.position());
      assertEquals(-55, bs.get(0));
      assertEquals(-54, bs.get(1));
      assertEquals(-53, bs.get(2));
      assertEquals(-52, bs.get(3));
      
      // Append
      bs.clear();
      bs.put(0, (byte)-55);
      bs.put(1, (byte)-54);
      bs.put(2, (byte)-53);
      bs.put(3, (byte)-52);
      
      assertEquals(256, ds.size());
      ds.write(bs, 256);
      assertEquals(260, ds.size());
      
      bs.clear();
      ds.read(bs, 256);
      assertEquals(4, bs.position());
      assertEquals(-55, bs.get(0));
      assertEquals(-54, bs.get(1));
      assertEquals(-53, bs.get(2));
      assertEquals(-52, bs.get(3));
   }
}
