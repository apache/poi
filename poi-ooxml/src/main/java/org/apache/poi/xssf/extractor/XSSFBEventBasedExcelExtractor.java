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
package org.apache.poi.xssf.extractor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.poi.logging.PoiLogManager;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.binary.XSSFBCommentsTable;
import org.apache.poi.xssf.binary.XSSFBHyperlinksTable;
import org.apache.poi.xssf.binary.XSSFBSharedStringsTable;
import org.apache.poi.xssf.binary.XSSFBSheetHandler;
import org.apache.poi.xssf.binary.XSSFBStylesTable;
import org.apache.poi.xssf.binary.XSSFHyperlinkRecord;
import org.apache.poi.xssf.eventusermodel.XSSFBReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.xmlbeans.XmlException;
import org.xml.sax.SAXException;

/**
 * Implementation of a text extractor or xlsb Excel
 * files that uses SAX-like binary parsing.
 *
 * @since 3.16-beta3
 */
public class XSSFBEventBasedExcelExtractor extends XSSFEventBasedExcelExtractor {

    private static final Logger LOGGER = PoiLogManager.getLogger(XSSFBEventBasedExcelExtractor.class);

    public static final List<XSSFRelation> SUPPORTED_TYPES = Collections.singletonList(
            XSSFRelation.XLSB_BINARY_WORKBOOK
    );

    private boolean handleHyperlinksInCells;

    public XSSFBEventBasedExcelExtractor(String path) throws XmlException, OpenXML4JException, IOException {
        super(path);
    }

    public XSSFBEventBasedExcelExtractor(OPCPackage container) throws XmlException, OpenXML4JException, IOException {
        super(container);
    }

    /**
     * Should the targets of the hyperlinks be added to the text of the cells they cover?
     * The target is appended to the cell text as {@code " <target>"}, which is the url
     * for external links and the location (e.g. {@code Sheet2!A1}) for links within
     * the workbook. Default is false.
     *
     * @param handleHyperlinksInCells {@code true} to add the hyperlink targets
     */
    public void setHandleHyperlinksInCells(boolean handleHyperlinksInCells) {
        this.handleHyperlinksInCells = handleHyperlinksInCells;
    }

    /**
     * Should we return the formula itself, and not
     * the result it produces? Default is false
     * This is currently unsupported for xssfb
     */
    @Override
    public void setFormulasNotResults(boolean formulasNotResults) {
        throw new IllegalArgumentException("Not currently supported");
    }

    /**
     * Processes the given sheet
     */
    public void processSheet(
            SheetContentsHandler sheetContentsExtractor,
            XSSFBStylesTable styles,
            XSSFBCommentsTable comments,
            SharedStrings strings,
            InputStream sheetInputStream)
            throws IOException {

        DataFormatter formatter;
        if (getLocale() == null) {
            formatter = new DataFormatter();
        } else {
            formatter = new DataFormatter(getLocale());
        }

        XSSFBSheetHandler xssfbSheetHandler = new XSSFBSheetHandler(
                sheetInputStream,
                styles, comments, strings, sheetContentsExtractor, formatter, getFormulasNotResults()
        );
        xssfbSheetHandler.parse();
    }

    /**
     * Processes the file and returns the text
     */
    public String getText() {
        try {
            XSSFBSharedStringsTable strings = new XSSFBSharedStringsTable(getPackage());
            XSSFBReader xssfbReader = new XSSFBReader(getPackage());
            XSSFBStylesTable styles = xssfbReader.getXSSFBStylesTable();
            XSSFBReader.SheetIterator iter = (XSSFBReader.SheetIterator) xssfbReader.getSheetsData();

            StringBuilder text = new StringBuilder(64);
            SheetTextExtractor sheetExtractor = new SheetTextExtractor();
            while (iter.hasNext()) {
                try (InputStream stream = iter.next()) {
                    if (getIncludeSheetNames()) {
                        text.append(iter.getSheetName());
                        text.append('\n');
                    }
                    SheetContentsHandler handler = sheetExtractor;
                    if (handleHyperlinksInCells) {
                        XSSFBHyperlinksTable hyperlinksTable = new XSSFBHyperlinksTable(iter.getSheetPart());
                        if (!hyperlinksTable.getHyperLinks().isEmpty()) {
                            handler = new HyperlinkAppendingHandler(sheetExtractor, hyperlinksTable);
                        }
                    }
                    XSSFBCommentsTable comments = getIncludeCellComments() ? iter.getXSSFBSheetComments() : null;
                    processSheet(handler, styles, comments, strings, stream);
                    if (getIncludeHeadersFooters()) {
                        sheetExtractor.appendHeaderText(text);
                    }
                    sheetExtractor.appendCellText(text);
                    if (getIncludeTextBoxes()) {
                        processShapes(iter.getShapes(), text);
                    }
                    if (getIncludeHeadersFooters()) {
                        sheetExtractor.appendFooterText(text);
                    }
                    sheetExtractor.reset();
                }
            }

            return text.toString();
        } catch (IOException | OpenXML4JException | SAXException e) {
            LOGGER.atWarn().withThrowable(e).log("Failed to load text");
            return "";
        }
    }

    /**
     * Appends the targets of the hyperlinks covering a cell to the cell text,
     * like {@code XWPFWordExtractor.setFetchHyperlinks} does for linked text
     */
    private static final class HyperlinkAppendingHandler implements SheetContentsHandler {
        private final SheetContentsHandler delegate;
        private final XSSFBHyperlinksTable hyperlinksTable;

        HyperlinkAppendingHandler(SheetContentsHandler delegate, XSSFBHyperlinksTable hyperlinksTable) {
            this.delegate = delegate;
            this.hyperlinksTable = hyperlinksTable;
        }

        @Override
        public void startRow(int rowNum) {
            delegate.startRow(rowNum);
        }

        @Override
        public void endRow(int rowNum) {
            delegate.endRow(rowNum);
        }

        @Override
        public void cell(String cellReference, String formattedValue, Comment comment) {
            List<XSSFHyperlinkRecord> records = cellReference == null ? null
                    : hyperlinksTable.findHyperlinkRecord(new CellAddress(cellReference));
            if (records != null) {
                StringBuilder sb = new StringBuilder(formattedValue == null ? "" : formattedValue);
                for (XSSFHyperlinkRecord record : records) {
                    String target = record.getLocation();
                    if (target != null && !target.isEmpty()) {
                        sb.append(" <").append(target).append('>');
                    }
                }
                formattedValue = sb.toString();
            }
            delegate.cell(cellReference, formattedValue, comment);
        }

        @Override
        public void headerFooter(String text, boolean isHeader, String tagName) {
            delegate.headerFooter(text, isHeader, tagName);
        }

        @Override
        public void endSheet() {
            delegate.endSheet();
        }
    }
}
