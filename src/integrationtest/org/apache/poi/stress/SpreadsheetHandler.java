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
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.extractor.EmbeddedData;
import org.apache.poi.ss.extractor.EmbeddedExtractor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.RecordFormatException;
import org.apache.poi.xssf.usermodel.XSSFChartSheet;

public abstract class SpreadsheetHandler extends AbstractFileHandler {
	public void handleWorkbook(Workbook wb) throws IOException {
		// try to access some of the content
		readContent(wb);
		
		// write out the file
		writeToArray(wb);
		
		// access some more content (we had cases where writing corrupts the data in memory)
		readContent(wb);

		// write once more
		ByteArrayOutputStream out = writeToArray(wb);

		// read in the written file
		Workbook read = WorkbookFactory.create(new ByteArrayInputStream(out.toByteArray()));

		assertNotNull(read);
		
		readContent(read);
		
		extractEmbedded(read);
		
		modifyContent(read);

		read.close();
	}

	private ByteArrayOutputStream writeToArray(Workbook wb) throws IOException {
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
			        assertNotNull(cell.toString());
			    }
			}
		}
	}

	private void extractEmbedded(Workbook wb) throws IOException {
        EmbeddedExtractor ee = new EmbeddedExtractor();

        for (Sheet s : wb) {
            for (EmbeddedData ed : ee.extractAll(s)) {
                assertNotNull(ed.getFilename());
                assertNotNull(ed.getEmbeddedData());
                assertNotNull(ed.getShape());
            }
        }
	}
	
	private void modifyContent(Workbook wb) {
		/* a number of file fail because of various things: udf, unimplemented functions, ...
		we would need quite a list of excludes and the large regression tests would probably
		take a lot longer to run...
		try {
			// try to re-compute all formulas to find cases where parsing fails
			wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
		} catch (RuntimeException e) {
			// only allow a specific exception which indicates that an external
			// reference was not found
			if(!e.getMessage().contains("Could not resolve external workbook name")) {
				throw e;
			}

		}*/

	    for (int i=wb.getNumberOfSheets()-1; i>=0; i--) {
	    	if(wb.getSheetAt(i) instanceof XSSFChartSheet) {
	    		// clone for chart-sheets is not supported
	    		continue;
			}

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