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

package org.apache.poi.hssf.usermodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.eventmodel.ERFListener;
import org.apache.poi.hssf.eventmodel.EventRecordFactory;
import org.apache.poi.hssf.record.DVRecord;
import org.apache.poi.hssf.record.RecordFormatException;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;

/**
 * Class for testing Excel's data validation mechanism
 *
 * @author Dragos Buleandra ( dragos.buleandra@trade2b.ro )
 */
public final class TestDataValidation extends BaseTestDataValidation {

    public TestDataValidation(){
        super(HSSFITestDataProvider.instance);
    }


	public void assertDataValidation(Workbook wb) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream(22000);
		try {
			wb.write(baos);
			baos.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		byte[] generatedContent = baos.toByteArray();
		boolean isSame;
		if (false) {
			// TODO - add proof spreadsheet and compare
			InputStream proofStream = HSSFTestDataSamples.openSampleFileStream("TestDataValidation.xls");
			isSame = compareStreams(proofStream, generatedContent);
		}
		isSame = true;
		
		if (isSame) {
			return;
		}
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File generatedFile = new File(tempDir, "GeneratedTestDataValidation.xls");
		try {
			FileOutputStream fileOut = new FileOutputStream(generatedFile);
			fileOut.write(generatedContent);
			fileOut.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	
		PrintStream ps = System.out;
	
		ps.println("This test case has failed because the generated file differs from proof copy '" 
				); // TODO+ proofFile.getAbsolutePath() + "'.");
		ps.println("The cause is usually a change to this test, or some common spreadsheet generation code.  "
				+ "The developer has to decide whether the changes were wanted or unwanted.");
		ps.println("If the changes to the generated version were unwanted, "
				+ "make the fix elsewhere (do not modify this test or the proof spreadsheet to get the test working).");
		ps.println("If the changes were wanted, make sure to open the newly generated file in Excel "
				+ "and verify it manually.  The new proof file should be submitted after it is verified to be correct.");
		ps.println("");
		ps.println("One other possible (but less likely) cause of a failed test is a problem in the "
				+ "comparison logic used here. Perhaps some extra file regions need to be ignored.");
		ps.println("The generated file has been saved to '" + generatedFile.getAbsolutePath() + "' for manual inspection.");
	
		fail("Generated file differs from proof copy.  See sysout comments for details on how to fix.");
		
	}
	
	private static boolean compareStreams(InputStream isA, byte[] generatedContent) {

		InputStream isB = new ByteArrayInputStream(generatedContent);

		// The allowable regions where the generated file can differ from the 
		// proof should be small (i.e. much less than 1K)
		int[] allowableDifferenceRegions = { 
				0x0228, 16,  // a region of the file containing the OS username
				0x506C, 8,   // See RootProperty (super fields _seconds_2 and _days_2)
		};
		int[] diffs = StreamUtility.diffStreams(isA, isB, allowableDifferenceRegions);
		if (diffs == null) {
			return true;
		}
		System.err.println("Diff from proof: ");
		for (int i = 0; i < diffs.length; i++) {
			System.err.println("diff at offset: 0x" + Integer.toHexString(diffs[i]));
		}
		return false;
	}
  




  /* package */ static void setCellValue(HSSFCell cell, String text) {
	  cell.setCellValue(new HSSFRichTextString(text));
	  
  }
  
	public void testAddToExistingSheet() {

		// dvEmpty.xls is a simple one sheet workbook.  With a DataValidations header record but no 
		// DataValidations.  It's important that the example has one SHEETPROTECTION record.
		// Such a workbook can be created in Excel (2007) by adding datavalidation for one cell
		// and then deleting the row that contains the cell.
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("dvEmpty.xls");  
		int dvRow = 0;
		Sheet sheet = wb.getSheetAt(0);
		DataValidationHelper dataValidationHelper = sheet.getDataValidationHelper();
		DataValidationConstraint dc = dataValidationHelper.createIntegerConstraint(OP.EQUAL, "42", null);
		DataValidation dv = dataValidationHelper.createValidation(dc,new CellRangeAddressList(dvRow, dvRow, 0, 0));
		
		dv.setEmptyCellAllowed(false);
		dv.setErrorStyle(ES.STOP);
		dv.setShowPromptBox(true);
		dv.createErrorBox("Xxx", "Yyy");
		dv.setSuppressDropDownArrow(true);

		sheet.addValidationData(dv);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			wb.write(baos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		byte[] wbData = baos.toByteArray();
		
		if (false) { // TODO (Jul 2008) fix EventRecordFactory to process unknown records, (and DV records for that matter)

			ERFListener erfListener = null; // new MyERFListener();
			EventRecordFactory erf = new EventRecordFactory(erfListener, null);
			try {
				POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(baos.toByteArray()));
				erf.processRecords(fs.createDocumentInputStream("Workbook"));
			} catch (RecordFormatException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		// else verify record ordering by navigating the raw bytes
		
		byte[] dvHeaderRecStart= { (byte)0xB2, 0x01, 0x12, 0x00, };
		int dvHeaderOffset = findIndex(wbData, dvHeaderRecStart);
		assertTrue(dvHeaderOffset > 0);
		int nextRecIndex = dvHeaderOffset + 22;
		int nextSid 
			= ((wbData[nextRecIndex + 0] << 0) & 0x00FF) 
			+ ((wbData[nextRecIndex + 1] << 8) & 0xFF00)
			;
		// nextSid should be for a DVRecord.  If anything comes between the DV header record 
		// and the DV records, Excel will not be able to open the workbook without error.
		
		if (nextSid == 0x0867) {
			throw new AssertionFailedError("Identified bug 45519");
		}
		assertEquals(DVRecord.sid, nextSid);
	}
	private int findIndex(byte[] largeData, byte[] searchPattern) {
		byte firstByte = searchPattern[0];
		for (int i = 0; i < largeData.length; i++) {
			if(largeData[i] != firstByte) {
				continue;
			}
			boolean match = true;
			for (int j = 1; j < searchPattern.length; j++) {
				if(searchPattern[j] != largeData[i+j]) {
					match = false;
					break;
				}
			}
			if (match) {
				return i;
			}
		}
		return -1;
	}
}
