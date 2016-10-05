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

import java.io.*;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestCase;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.OPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
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
   
   /**
    * Writing to the file we opened from - note, uses a temp file to
    *  avoid changing our test files!
    */
   @SuppressWarnings("resource")
   public void testInPlaceWrite() throws Exception {
       // Setup as a copy of a known-good file
       final File file = TempFile.createTempFile("TestDocument", ".doc");
       InputStream inputStream = POIDataSamples.getDocumentInstance().openResourceAsStream("SampleDoc.doc");
       try {
           FileOutputStream outputStream = new FileOutputStream(file);
           try {
               IOUtils.copy(inputStream, outputStream);
           } finally {
               outputStream.close();
           }
       } finally {
           inputStream.close();
       }

       // Open from the temp file in read-write mode
       HWPFDocument doc = new HWPFDocument(new NPOIFSFileSystem(file, false).getRoot());
       Range r = doc.getRange();
       assertEquals("I am a test document\r", r.getParagraph(0).text());

       // Change
       r.replaceText("X XX a test document\r", false);

       // Save in-place, close, re-open and check
       doc.write();
       doc.close();

       doc = new HWPFDocument(new NPOIFSFileSystem(file).getRoot());
       r = doc.getRange();
       assertEquals("X XX a test document\r", r.getParagraph(0).text());
       doc.close();
   }

   @SuppressWarnings("resource")
   public void testInvalidInPlaceWrite() throws Exception {
       HWPFDocument doc;

       // Can't work for InputStream opened files
       doc = new HWPFDocument(
               POIDataSamples.getDocumentInstance().openResourceAsStream("SampleDoc.doc"));
       try {
           doc.write();
           fail("Shouldn't work for InputStream");
       } catch (IllegalStateException e) {
           // expected here
       }
       doc.close();

       // Can't work for OPOIFS
       OPOIFSFileSystem ofs = new OPOIFSFileSystem(
               POIDataSamples.getDocumentInstance().openResourceAsStream("SampleDoc.doc"));
       doc = new HWPFDocument(ofs.getRoot());
       try {
           doc.write();
           fail("Shouldn't work for OPOIFSFileSystem");
       } catch (IllegalStateException e) {
           // expected here
       }
       doc.close();

       // Can't work for Read-Only files
       NPOIFSFileSystem fs = new NPOIFSFileSystem(
               POIDataSamples.getDocumentInstance().getFile("SampleDoc.doc"), true);
       doc = new HWPFDocument(fs.getRoot());
       try {
           doc.write();
           fail("Shouldn't work for Read Only");
       } catch (IllegalStateException e) {
           // expected here
       }
       doc.close();
   }
}
