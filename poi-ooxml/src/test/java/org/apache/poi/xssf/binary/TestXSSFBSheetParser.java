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

package org.apache.poi.xssf.binary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.util.Map;
import java.util.LinkedHashMap;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.XSSFBReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.junit.jupiter.api.Test;

class TestXSSFBSheetParser {
    private static POIDataSamples _ssTests = POIDataSamples.getSpreadSheetInstance();

    /*
     * Test reading from a binary spreadsheet that various data types are
     * read and that they have the same values as a literal as they
     * do as a formula.  Reference bug #66682
     */
    @Test
    void test66682() throws Exception {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("bug66682.xlsb"))) {
            XSSFBReader reader = new XSSFBReader(pkg);
            XSSFBReader.SheetIterator it = (XSSFBReader.SheetIterator) reader.getSheetsData();

            while (it.hasNext()) {
                try (InputStream is = it.next()) {
                    String name = it.getSheetName();
                    if (!"test2".equals(name))
                        continue;
                    ValueGrabber contentHandler = new ValueGrabber();
                    contentHandler.startSheet(name);
                    XSSFBSheetHandler sheetHandler =
                            new XSSFBSheetHandler(is,
                                    reader.getXSSFBStylesTable(),
                                    it.getXSSFBSheetComments(),
                                    new XSSFBSharedStringsTable(pkg),
                                    contentHandler,
                                    new DataFormatter(),
                                    false);
                    sheetHandler.parse();
                    contentHandler.endSheet();

                    /*
                     * Check each row grabbed from the spreadsheet and
                     * assert that the second and third columns are not null,
                     * not empty and have the same formatted value.
                     *
                     * The second column contains the literal value,
                     * the third column contains the formula
                     */
                    for (Map.Entry<Integer, String[]> me : contentHandler.getRowValues().entrySet()) {
                        String[] vals = me.getValue();
                        assertNotNull(vals[1]);
                        assertNotNull(vals[2]);
                        assertFalse(vals[1].trim().isEmpty());
                        assertEquals(vals[1], vals[2]);
                    }
                }
            }
        }
    }


    private static class ValueGrabber
            implements XSSFSheetXMLHandler.SheetContentsHandler {

        private int currentRow, currentCol;
        private boolean firstRow;
        private int maxColumn;
        private Map<Integer,String[]> rowValues;

        ValueGrabber() {
            maxColumn = -1;
            rowValues = new LinkedHashMap<>();
        }

        Map<Integer,String[]> getRowValues() {
            return rowValues;
        }

        public void startSheet(String sheetName) {
            currentRow = -1;
            firstRow = true;
        }

        @Override
        public void endSheet() {
        }

        @Override
        public void startRow(int rowNum) {
            currentRow = rowNum;
            currentCol = -1;
        }

        @Override
        public void endRow(int rowNum) {
            if (firstRow)
                firstRow = false;
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            if (cellReference == null)
                cellReference = new CellAddress(currentRow, currentCol+1).formatAsString();

            int thisCol = (new CellReference(cellReference)).getCol();
            if (firstRow)
                maxColumn = thisCol > maxColumn ? thisCol : maxColumn;
            else if (thisCol > maxColumn)
                return;


            if (!firstRow && formattedValue != null)
                rowValues.computeIfAbsent(currentRow, k -> new String[maxColumn+1])[thisCol] = formattedValue;

            currentCol = thisCol;
        }
    }
}
