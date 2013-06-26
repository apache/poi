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

package org.apache.poi.hpsf.basic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.POIDocument;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFPropertiesOnlyDocument;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Tests various bugs have been fixed
 */
public final class TestHPSFBugs extends TestCase {
   private static final POIDataSamples _samples = POIDataSamples.getHPSFInstance();
    
   /**
    * Ensure that we can create a new HSSF Workbook,
    *  then add some properties to it, save +
    *  reload, and still access & change them.
    */
   public void test48832() throws Exception {
      HSSFWorkbook wb = new HSSFWorkbook();
      
      // Starts empty
      assertNull(wb.getDocumentSummaryInformation());
      assertNull(wb.getSummaryInformation());
      
      // Add new properties
      wb.createInformationProperties();
      
      assertNotNull(wb.getDocumentSummaryInformation());
      assertNotNull(wb.getSummaryInformation());
      
      // Set initial values
      wb.getSummaryInformation().setAuthor("Apache POI");
      wb.getSummaryInformation().setKeywords("Testing POI");
      wb.getSummaryInformation().setCreateDateTime(new Date(12345));
      
      wb.getDocumentSummaryInformation().setCompany("Apache");
      
      
      // Save and reload
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      wb.write(baos);
      ByteArrayInputStream bais = 
         new ByteArrayInputStream(baos.toByteArray());
      wb = new HSSFWorkbook(bais);
      
      
      // Ensure changes were taken
      assertNotNull(wb.getDocumentSummaryInformation());
      assertNotNull(wb.getSummaryInformation());
 
      assertEquals("Apache POI", wb.getSummaryInformation().getAuthor());
      assertEquals("Testing POI", wb.getSummaryInformation().getKeywords());
      assertEquals(12345, wb.getSummaryInformation().getCreateDateTime().getTime());
      assertEquals("Apache", wb.getDocumentSummaryInformation().getCompany());
      
      
      // Set some more, save + reload
      wb.getSummaryInformation().setComments("Resaved");
      
      baos = new ByteArrayOutputStream();
      wb.write(baos);
      bais = new ByteArrayInputStream(baos.toByteArray());
      wb = new HSSFWorkbook(bais);
      
      // Check again
      assertNotNull(wb.getDocumentSummaryInformation());
      assertNotNull(wb.getSummaryInformation());
 
      assertEquals("Apache POI", wb.getSummaryInformation().getAuthor());
      assertEquals("Testing POI", wb.getSummaryInformation().getKeywords());
      assertEquals("Resaved", wb.getSummaryInformation().getComments());
      assertEquals(12345, wb.getSummaryInformation().getCreateDateTime().getTime());
      assertEquals("Apache", wb.getDocumentSummaryInformation().getCompany());
   }
   
   /**
    * Some files seem to want the length and data to be on a 4-byte boundary,
    * and without that you'll hit an ArrayIndexOutOfBoundsException after
    * reading junk
    */
   public void test54233() throws Exception {
       DocumentInputStream dis;
       POIFSFileSystem fs = 
               new POIFSFileSystem(_samples.openResourceAsStream("TestNon4ByteBoundary.doc"));
       
       dis = fs.createDocumentInputStream(SummaryInformation.DEFAULT_STREAM_NAME);
       SummaryInformation si = (SummaryInformation)PropertySetFactory.create(dis);
       
       dis = fs.createDocumentInputStream(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
       DocumentSummaryInformation dsi = (DocumentSummaryInformation)PropertySetFactory.create(dis);
      
       // Test
       assertEquals("Microsoft Word 10.0", si.getApplicationName());
       assertEquals("", si.getTitle());
       assertEquals("", si.getAuthor());
       assertEquals("Cour de Justice", dsi.getCompany());
       
       
       // Write out and read back, should still be valid
       POIDocument doc = new HPSFPropertiesOnlyDocument(fs);
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       doc.write(baos);
       ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
       doc = new HPSFPropertiesOnlyDocument(new POIFSFileSystem(bais));
       
       // Check properties are still there
       assertEquals("Microsoft Word 10.0", si.getApplicationName());
       assertEquals("", si.getTitle());
       assertEquals("", si.getAuthor());
       assertEquals("Cour de Justice", dsi.getCompany());
   }
}
