
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
        

package org.apache.poi.hssf.record;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import junit.framework.TestCase;

public class TestChartTitleFormatRecord extends TestCase
{
    private String _test_file_path;
    private static final String _test_file_path_property = "HSSF.testdata.path";

    public TestChartTitleFormatRecord()
    {
        super();
        _test_file_path = System.getProperty( _test_file_path_property ) +
        	File.separator + "WithFormattedGraphTitle.xls";
    }

    public void testRecord() throws Exception {
    	POIFSFileSystem fs = new POIFSFileSystem(
    			new FileInputStream(_test_file_path)
    	);
    	
    	// Check we can open the file via usermodel
    	HSSFWorkbook hssf = new HSSFWorkbook(fs);
    	
    	// Now process it through eventusermodel, and
    	//  look out for the title records
    	ChartTitleFormatRecordGrabber grabber =
    		new ChartTitleFormatRecordGrabber();
    	InputStream din = fs.createDocumentInputStream("Workbook");
        HSSFRequest req = new HSSFRequest();
        req.addListenerForAllRecords(grabber);
        HSSFEventFactory factory = new HSSFEventFactory();
        factory.processEvents(req, din);
        din.close();
        
        // Should've found one
        assertEquals(1, grabber.chartTitleFormatRecords.size());
        // And it should be of something interesting
        ChartTitleFormatRecord r =
        	(ChartTitleFormatRecord)grabber.chartTitleFormatRecords.get(0);
        assertEquals(3, r.getFormatCount());
    }
    
    public static class ChartTitleFormatRecordGrabber implements HSSFListener {
    	private ArrayList chartTitleFormatRecords = new ArrayList();

		public void processRecord(Record record) {
			if(record instanceof ChartTitleFormatRecord) {
				chartTitleFormatRecords.add(
						record
				);
			}
		}
    	
    }
}
