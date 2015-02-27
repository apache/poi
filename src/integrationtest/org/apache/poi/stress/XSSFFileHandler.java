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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.poi.xssf.extractor.XSSFExportToXml;
import org.apache.poi.xssf.usermodel.XSSFMap;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.xml.sax.SAXException;

public class XSSFFileHandler extends SpreadsheetHandler {
	@Override
    public void handleFile(InputStream stream) throws Exception {
	    // ignore password protected files
	    if (POIXMLDocumentHandler.isEncrypted(stream)) return;
	    
		XSSFWorkbook wb = new XSSFWorkbook(stream);
		
		// use the combined handler for HSSF/XSSF
		handleWorkbook(wb, ".xlsx");
		
        // TODO: some documents fail currently...
        //XSSFFormulaEvaluator evaluator = new XSSFFormulaEvaluator(wb);
        //evaluator.evaluateAll();

		// also verify general POIFS-stuff
		new POIXMLDocumentHandler().handlePOIXMLDocument(wb);
		
		// and finally ensure that exporting to XML works
		exportToXML(wb);
	}

    private void exportToXML(XSSFWorkbook wb) throws SAXException,
            ParserConfigurationException, TransformerException {
        for (XSSFMap map : wb.getCustomXMLMappings()) {
            XSSFExportToXml exporter = new XSSFExportToXml(map);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            exporter.exportToXML(os, true);
        }
    }
    
	// a test-case to test this locally without executing the full TestAllFiles
	@Test
	public void test() throws Exception {
		InputStream stream = new FileInputStream("test-data/spreadsheet/WithConditionalFormatting.xlsx");
		try {
			handleFile(stream);
		} finally {
			stream.close();
		}
	}

    // a test-case to test this locally without executing the full TestAllFiles
    @Test
    public void testExtractor() throws Exception {
        handleExtracting(new File("test-data/spreadsheet/56278.xlsx"));
    }
}