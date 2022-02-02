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
package org.apache.poi.xssf.eventusermodel;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestXSSFSheetXMLHandler {
    private static final POIDataSamples _ssTests = POIDataSamples.getSpreadSheetInstance();

    @Test
    void testInlineString() throws Exception {
        try (OPCPackage xlsxPackage = OPCPackage.open(_ssTests.openResourceAsStream("InlineString.xlsx"))) {
            final XSSFReader reader = new XSSFReader(xlsxPackage);

            final Iterator<InputStream> iter = reader.getSheetsData();

            try (InputStream stream = iter.next()) {
                final XMLReader sheetParser = XMLHelper.getSaxParserFactory().newSAXParser().getXMLReader();

                sheetParser.setContentHandler(new XSSFSheetXMLHandler(reader.getStylesTable(),
                        new ReadOnlySharedStringsTable(xlsxPackage), new SheetContentsHandler() {

                    int cellCount = 0;

                    @Override
                    public void startRow(final int rowNum) {
                    }

                    @Override
                    public void endRow(final int rowNum) {
                    }

                    @Override
                    public void cell(final String cellReference, final String formattedValue,
                                     final XSSFComment comment) {
                        assertEquals("\uD83D\uDE1Cmore text", formattedValue);
                        assertEquals(0, cellCount++);
                    }
                }, false));

                assertDoesNotThrow(() -> sheetParser.parse(new InputSource(stream)));
            }
        }
    }

    @Test
    void testNumber() throws Exception {
        try (OPCPackage xlsxPackage = OPCPackage.open(_ssTests.openResourceAsStream("sample.xlsx"))) {
            final XSSFReader reader = new XSSFReader(xlsxPackage);

            final Iterator<InputStream> iter = reader.getSheetsData();

            try (InputStream stream = iter.next()) {
                final XMLReader sheetParser = XMLHelper.getSaxParserFactory().newSAXParser().getXMLReader();

                sheetParser.setContentHandler(new XSSFSheetXMLHandler(reader.getStylesTable(),
                        new ReadOnlySharedStringsTable(xlsxPackage), new SheetContentsHandler() {
                    @Override
                    public void startRow(final int rowNum) {
                    }

                    @Override
                    public void endRow(final int rowNum) {
                    }

                    @Override
                    public void cell(final String cellReference, final String formattedValue,
                                     final XSSFComment comment) {
                    }
                }, false));

                assertDoesNotThrow(() -> sheetParser.parse(new InputSource(stream)));
            }
        }
    }
}
