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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.poi.POIDataSamples;
import org.apache.poi.POIDocument;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFPropertiesOnlyDocument;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Test;

/**
 * Tests various bugs have been fixed
 */
public final class TestHPSFBugs {
   private static final POIDataSamples _samples = POIDataSamples.getHPSFInstance();

   /**
    * Ensure that we can create a new HSSF Workbook,
    *  then add some properties to it, save +
    *  reload, and still access & change them.
    */
   @Test
   public void test48832() throws IOException {
      HSSFWorkbook wb1 = new HSSFWorkbook();

      // Starts empty
      assertNull(wb1.getDocumentSummaryInformation());
      assertNull(wb1.getSummaryInformation());

      // Add new properties
      wb1.createInformationProperties();

      assertNotNull(wb1.getDocumentSummaryInformation());
      assertNotNull(wb1.getSummaryInformation());

      // Set initial values
      wb1.getSummaryInformation().setAuthor("Apache POI");
      wb1.getSummaryInformation().setKeywords("Testing POI");
      wb1.getSummaryInformation().setCreateDateTime(new Date(12345));

      wb1.getDocumentSummaryInformation().setCompany("Apache");


      // Save and reload
      HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);
      wb1.close();


      // Ensure changes were taken
      assertNotNull(wb2.getDocumentSummaryInformation());
      assertNotNull(wb2.getSummaryInformation());

      assertEquals("Apache POI", wb2.getSummaryInformation().getAuthor());
      assertEquals("Testing POI", wb2.getSummaryInformation().getKeywords());
      assertEquals(12345, wb2.getSummaryInformation().getCreateDateTime().getTime());
      assertEquals("Apache", wb2.getDocumentSummaryInformation().getCompany());


      // Set some more, save + reload
      wb2.getSummaryInformation().setComments("Resaved");

      HSSFWorkbook wb3 = HSSFTestDataSamples.writeOutAndReadBack(wb2);
      wb2.close();

      // Check again
      assertNotNull(wb3.getDocumentSummaryInformation());
      assertNotNull(wb3.getSummaryInformation());

      assertEquals("Apache POI", wb3.getSummaryInformation().getAuthor());
      assertEquals("Testing POI", wb3.getSummaryInformation().getKeywords());
      assertEquals("Resaved", wb3.getSummaryInformation().getComments());
      assertEquals(12345, wb3.getSummaryInformation().getCreateDateTime().getTime());
      assertEquals("Apache", wb3.getDocumentSummaryInformation().getCompany());
      wb3.close();
   }

   /**
    * Some files seem to want the length and data to be on a 4-byte boundary,
    * and without that you'll hit an ArrayIndexOutOfBoundsException after
    * reading junk
    */
   @Test
   public void test54233() throws IOException, NoPropertySetStreamException, MarkUnsupportedException {
       InputStream is = _samples.openResourceAsStream("TestNon4ByteBoundary.doc");
       POIFSFileSystem fs = new POIFSFileSystem(is);
       is.close();

       SummaryInformation si = (SummaryInformation)
           PropertySetFactory.create(fs.getRoot(), SummaryInformation.DEFAULT_STREAM_NAME);
       DocumentSummaryInformation dsi = (DocumentSummaryInformation)
           PropertySetFactory.create(fs.getRoot(), DocumentSummaryInformation.DEFAULT_STREAM_NAME);

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

       doc.close();
       fs.close();
   }

   /**
    * CodePage Strings can be zero length
    */
   @Test
   public void test56138() throws IOException, NoPropertySetStreamException {
       InputStream is = _samples.openResourceAsStream("TestZeroLengthCodePage.mpp");
       POIFSFileSystem fs = new POIFSFileSystem(is);
       is.close();

       SummaryInformation si = (SummaryInformation)
           PropertySetFactory.create(fs.getRoot(), SummaryInformation.DEFAULT_STREAM_NAME);
       DocumentSummaryInformation dsi = (DocumentSummaryInformation)
           PropertySetFactory.create(fs.getRoot(), DocumentSummaryInformation.DEFAULT_STREAM_NAME);

       // Test
       assertEquals("MSProject", si.getApplicationName());
       assertEquals("project1", si.getTitle());
       assertEquals("Jon Iles", si.getAuthor());

       assertEquals("", dsi.getCompany());
       assertEquals(2, dsi.getSectionCount());

       fs.close();
   }

    @Test
    public void bug62451() throws IOException {
        final long millis = 920355314183864L;
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            wb.createSheet().createRow(0).createCell(0).setCellValue("foo");
            wb.createInformationProperties();
            SummaryInformation si = wb.getSummaryInformation();
            si.setLastPrinted(new Date(millis));
            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb)) {
                SummaryInformation si2 = wb2.getSummaryInformation();
                Date d = si2.getLastPrinted();
                assertNotNull(d);
                assertEquals(millis, d.getTime());
            }
        }
    }
}
