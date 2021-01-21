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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestXSSFSheetXMLHandler {
    private static final POIDataSamples _ssTests = POIDataSamples.getSpreadSheetInstance();

    @Test
    public void testInlineString() throws Exception {
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
                        assertEquals(cellCount++, 0);
                    }
                }, false));

                sheetParser.parse(new InputSource(stream));
            }
        }
    }
}
