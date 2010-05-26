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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFException;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.Section;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.wellknown.SectionIDMap;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.POIDataSamples;

/**
 * Tests various bugs have been fixed
 */
public final class TestHPSFBugs extends TestCase {
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
}
