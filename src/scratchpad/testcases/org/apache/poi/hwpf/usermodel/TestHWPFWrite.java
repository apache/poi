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

package org.apache.poi.hwpf.usermodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestCase;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.TempFile;

/**
 * Test various write situations
 */
public final class TestHWPFWrite extends HWPFTestCase {
   /**
    * Write to a stream
    */
   public void testWriteStream() throws Exception {
      HWPFDocument doc = HWPFTestDataSamples.openSampleFile("SampleDoc.doc");

      Range r = doc.getRange();
      assertEquals("I am a test document\r", r.getParagraph(0).text());
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      doc.write(baos);
      doc.close();
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      
      doc = new HWPFDocument(bais);
      r = doc.getRange();
      assertEquals("I am a test document\r", r.getParagraph(0).text());
      doc.close();
   }
   
   /**
    * Write to a new file
    */
   public void testWriteNewFile() throws Exception {
       HWPFDocument doc = HWPFTestDataSamples.openSampleFile("SampleDoc.doc");

       Range r = doc.getRange();
       assertEquals("I am a test document\r", r.getParagraph(0).text());
       
       File file = TempFile.createTempFile("TestDocument", ".doc");
       doc.write(file);
       doc.close();

       // Check reading from File and Stream
       doc = new HWPFDocument(new FileInputStream(file));
       r = doc.getRange();
       assertEquals("I am a test document\r", r.getParagraph(0).text());
       doc.close();
       
       doc = new HWPFDocument(new POIFSFileSystem(file));
       r = doc.getRange();
       assertEquals("I am a test document\r", r.getParagraph(0).text());
       doc.close();
    }
   
   // TODO In-place write positive and negative checks
}
