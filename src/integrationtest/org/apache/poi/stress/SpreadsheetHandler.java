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
package org.apache.poi.stress;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.RecordFormatException;

public abstract class SpreadsheetHandler extends AbstractFileHandler {
	public void handleWorkbook(Workbook wb, String extension) throws IOException {
		// try to access some of the content
		readContent(wb);
		
		// write out the file
		ByteArrayOutputStream out = writeToArray(wb);
		
		// access some more content (we had cases where writing corrupts the data in memory)
		readContent(wb);

		// write once more
		out = writeToArray(wb);

		// read in the writen file
		Workbook read;
		try {
			read = WorkbookFactory.create(new ByteArrayInputStream(out.toByteArray()));
		} catch (InvalidFormatException e) {
			throw new IllegalStateException(e);
		}
		assertNotNull(read);
		
		readContent(read);
		
		modifyContent(read);

		read.close();
	}

	private ByteArrayOutputStream writeToArray(Workbook wb)
			throws FileNotFoundException, IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			wb.write(stream);
		} finally {
			stream.close();
		}
		
		return stream;
	}

	private void readContent(Workbook wb) {
	    for(int i = 0;i < wb.getNumberOfSheets();i++) {
			Sheet sheet = wb.getSheetAt(i);
			assertNotNull(wb.getSheet(sheet.getSheetName()));
			sheet.groupColumn((short) 4, (short) 5);
			sheet.setColumnGroupCollapsed(4, true);
			sheet.setColumnGroupCollapsed(4, false);

			// don't do this for very large sheets as it will take a long time
			if(sheet.getPhysicalNumberOfRows() > 1000) {
			    continue;
			}
			
			for(Row row : sheet) {
			    for(Cell cell : row) {
			        cell.toString();
			    }
			}
		}
	}
	
	private void modifyContent(Workbook wb) {
	    for (int i=wb.getNumberOfSheets()-1; i>=0; i--) {
	        try {
	            wb.cloneSheet(i);
	        } catch (RecordFormatException e) {
	            if (e.getCause() instanceof CloneNotSupportedException) {
	                // ignore me
	                continue;
	            }
	            throw e;
	        } catch (RuntimeException e) {
	            if ("Could not find 'internal references' EXTERNALBOOK".equals(e.getMessage()) ||
	                    "CountryRecord not found".equals(e.getMessage()) ||
	                    "Cannot add more than 65535 shapes".equals(e.getMessage()) ) {
	                // ignore these here for now
	                continue;
                }
	            throw e;
	        }
	    }
	}
}