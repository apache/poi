/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.poi.hssf.usermodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for how HSSFWorkbook behaves with XLS files
 *  with a WORKBOOK or BOOK directory entry (instead of 
 *  the more usual, Workbook)
 */
public final class TestNonStandardWorkbookStreamNames {
	private final String xlsA = "WORKBOOK_in_capitals.xls";
    private final String xlsB = "BOOK_in_capitals.xls";

	/**
	 * Test that we can open a file with WORKBOOK
	 */
    @Test
	public void testOpenWORKBOOK() throws IOException {
        // Try to open the workbook
		InputStream is = HSSFTestDataSamples.openSampleFileStream(xlsA);
		HSSFWorkbook wb = new HSSFWorkbook(is);
		is.close();
		DirectoryNode root = wb.getDirectory();

		// Ensure that we have a WORKBOOK entry and a summary
		assertTrue(root.hasEntry("WORKBOOK"));
		assertTrue(root.hasEntry(SummaryInformation.DEFAULT_STREAM_NAME));

		// But not a Workbook one
		assertFalse(root.hasEntry("Workbook"));
		
		wb.close();
	}

   /**
    * Test that we can open a file with BOOK
    */
   @Test
   public void testOpenBOOK() throws IOException {
       // Try to open the workbook
      InputStream is = HSSFTestDataSamples.openSampleFileStream(xlsB);
      HSSFWorkbook wb = new HSSFWorkbook(is);
      is.close();
      DirectoryNode root = wb.getDirectory();

      // Ensure that we have a BOOK entry
      assertTrue(root.hasEntry("BOOK"));

      // But not a Workbook one and not a Summary one
      assertFalse(root.hasEntry("Workbook"));
      assertFalse(root.hasEntry(SummaryInformation.DEFAULT_STREAM_NAME));
      
      wb.close();
   }

	/**
	 * Test that when we write out, we go back to the correct case
	 */
    @Test
	public void testWrite() throws IOException {
	   for (String file : new String[] {xlsA, xlsB}) {
           // Open the workbook, not preserving nodes
	       InputStream is = HSSFTestDataSamples.openSampleFileStream(file);
	       HSSFWorkbook wb = new HSSFWorkbook(is, false);
           is.close();

           // Check now it can be opened
           HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb);
           wb.close();
           
           DirectoryNode root = wb2.getDirectory();
       
           // Check that we have the new entries
           assertTrue(root.hasEntry("Workbook"));
           assertFalse(root.hasEntry("BOOK"));
           assertFalse(root.hasEntry("WORKBOOK"));

           wb2.close();
       }
	}

	/**
	 * Test that when we write out preserving nodes, we go back to the
	 *  correct case
	 */
    @Test
	public void testWritePreserve() throws IOException {
        // Open the workbook, not preserving nodes
		InputStream is = HSSFTestDataSamples.openSampleFileStream(xlsA);
        HSSFWorkbook wb = new HSSFWorkbook(is,true);
        is.close();
        
        // Check now it can be opened
        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb);
        wb.close();

        DirectoryNode root = wb2.getDirectory();

        // Check that we have the new entries
        assertTrue(root.hasEntry("Workbook"));
        assertFalse(root.hasEntry("BOOK"));
        assertFalse(root.hasEntry("WORKBOOK"));

        // As we preserved, should also have a few other streams
        assertTrue(root.hasEntry(SummaryInformation.DEFAULT_STREAM_NAME));
        wb2.close();
	}
}
