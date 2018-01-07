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
package org.apache.poi.xssf.streaming.examples;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.xml.sax.SAXException;

/**
 * This demonstrates how a hybrid approach to workbook read can be taken, using 
 * a mix of traditional XSSF and streaming one particular worksheet (perhaps one
 * which is too big for the ordinary DOM parse).
 */
public class HybridStreaming {
    
    private static final String SHEET_TO_STREAM = "large sheet";

    public static void main(String[] args) throws IOException, SAXException {
        try (InputStream sourceBytes = new FileInputStream("workbook.xlsx")) {
            XSSFWorkbook workbook = new XSSFWorkbook(sourceBytes) {
                /**
                 * Avoid DOM parse of large sheet
                 */
                @Override
                public void parseSheet(java.util.Map<String, XSSFSheet> shIdMap, CTSheet ctSheet) {
                    if (!SHEET_TO_STREAM.equals(ctSheet.getName())) {
                        super.parseSheet(shIdMap, ctSheet);
                    }
                }
            };

            // Having avoided a DOM-based parse of the sheet, we can stream it instead.
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(workbook.getPackage());
            new XSSFSheetXMLHandler(workbook.getStylesSource(), strings, createSheetContentsHandler(), false);
            workbook.close();
        }
    }

    private static SheetContentsHandler createSheetContentsHandler() {
        return new SheetContentsHandler() {
            
            @Override
            public void startRow(int rowNum) {
            }
            
            @Override
            public void endRow(int rowNum) {
            }
            
            @Override
            public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            }
        };
    }
}
