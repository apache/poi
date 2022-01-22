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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.ooxml.POIXMLProperties.CoreProperties;
import org.apache.poi.ooxml.POIXMLProperties.CustomProperties;
import org.apache.poi.ooxml.POIXMLProperties.ExtendedProperties;
import org.apache.poi.ooxml.extractor.POIXMLTextExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.extractor.ExcelExtractor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.HeaderFooter;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.Comments;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.Styles;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSimpleShape;
import org.apache.xmlbeans.XmlException;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Implementation of a text extractor from OOXML Excel
 * files that uses SAX event based parsing.
 */
public class XSSFEventBasedExcelExtractor
    implements POIXMLTextExtractor, ExcelExtractor {

    private static final Logger LOGGER = LogManager.getLogger(XSSFEventBasedExcelExtractor.class);

    protected final OPCPackage container;
    protected final POIXMLProperties properties;

    protected Locale locale;
    protected boolean includeTextBoxes = true;
    protected boolean includeSheetNames = true;
    protected boolean includeCellComments;
    protected boolean includeHeadersFooters = true;
    protected boolean formulasNotResults;
    protected boolean concatenatePhoneticRuns = true;

    private boolean doCloseFilesystem = true;

    public XSSFEventBasedExcelExtractor(String path) throws XmlException, OpenXML4JException, IOException {
        this(OPCPackage.open(path));
    }

    public XSSFEventBasedExcelExtractor(OPCPackage container) throws XmlException, OpenXML4JException, IOException {
        this.container = container;
        properties = new POIXMLProperties(container);
    }

    /**
     * Should sheet names be included? Default is true
     */
    public void setIncludeSheetNames(boolean includeSheetNames) {
        this.includeSheetNames = includeSheetNames;
    }


    /**
     * @return whether to include sheet names
     * @since 3.16-beta3
     */
    public boolean getIncludeSheetNames() {
        return includeSheetNames;
    }

    /**
     * Should we return the formula itself, and not
     * the result it produces? Default is false
     */
    public void setFormulasNotResults(boolean formulasNotResults) {
        this.formulasNotResults = formulasNotResults;
    }

    /**
     * @return whether to include formulas but not results
     * @since 3.16-beta3
     */
    public boolean getFormulasNotResults() {
        return formulasNotResults;
    }

    /**
     * Should headers and footers be included? Default is true
     */
    public void setIncludeHeadersFooters(boolean includeHeadersFooters) {
        this.includeHeadersFooters = includeHeadersFooters;
    }

    /**
     * @return whether or not to include headers and footers
     * @since 3.16-beta3
     */
    public boolean getIncludeHeadersFooters() {
        return includeHeadersFooters;
    }

    /**
     * Should text from textboxes be included? Default is true
     */
    public void setIncludeTextBoxes(boolean includeTextBoxes) {
        this.includeTextBoxes = includeTextBoxes;
    }

    /**
     * @return whether or not to extract textboxes
     * @since 3.16-beta3
     */
    public boolean getIncludeTextBoxes() {
        return includeTextBoxes;
    }

    /**
     * Should cell comments be included? Default is false
     */
    public void setIncludeCellComments(boolean includeCellComments) {
        this.includeCellComments = includeCellComments;
    }

    /**
     * @return whether cell comments should be included
     * @since 3.16-beta3
     */
    public boolean getIncludeCellComments() {
        return includeCellComments;
    }

    /**
     * Concatenate text from &lt;rPh&gt; text elements in SharedStringsTable
     * Default is true;
     *
     * @param concatenatePhoneticRuns true if runs should be concatenated, false otherwise
     */
    public void setConcatenatePhoneticRuns(boolean concatenatePhoneticRuns) {
        this.concatenatePhoneticRuns = concatenatePhoneticRuns;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * @return locale
     * @since 3.16-beta3
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the opened OPCPackage container.
     */
    @Override
    public OPCPackage getPackage() {
        return container;
    }

    /**
     * Returns the core document properties
     */
    @Override
    public CoreProperties getCoreProperties() {
        return properties.getCoreProperties();
    }

    /**
     * Returns the extended document properties
     */
    @Override
    public ExtendedProperties getExtendedProperties() {
        return properties.getExtendedProperties();
    }

    /**
     * Returns the custom document properties
     */
    @Override
    public CustomProperties getCustomProperties() {
        return properties.getCustomProperties();
    }


    /**
     * Processes the given sheet
     */
    public void processSheet(
            SheetContentsHandler sheetContentsExtractor,
            Styles styles,
            Comments comments,
            SharedStrings strings,
            InputStream sheetInputStream)
            throws IOException, SAXException {

        DataFormatter formatter;
        if (locale == null) {
            formatter = new DataFormatter();
        } else {
            formatter = new DataFormatter(locale);
        }

        InputSource sheetSource = new InputSource(sheetInputStream);
        try {
            XMLReader sheetParser = XMLHelper.newXMLReader();
            ContentHandler handler = new XSSFSheetXMLHandler(
                    styles, comments, strings, sheetContentsExtractor, formatter, formulasNotResults);
            sheetParser.setContentHandler(handler);
            sheetParser.parse(sheetSource);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
        }
    }

    protected SharedStrings createSharedStringsTable(XSSFReader xssfReader, OPCPackage container)
            throws IOException, SAXException {
        return new ReadOnlySharedStringsTable(container, concatenatePhoneticRuns);
    }

    /**
     * Processes the file and returns the text
     */
    public String getText() {
        try {
            XSSFReader xssfReader = new XSSFReader(container);
            SharedStrings strings = createSharedStringsTable(xssfReader, container);
            StylesTable styles = xssfReader.getStylesTable();
            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            StringBuilder text = new StringBuilder(64);
            SheetTextExtractor sheetExtractor = new SheetTextExtractor();

            while (iter.hasNext()) {
                try (InputStream stream = iter.next()) {
                    if (includeSheetNames) {
                        text.append(iter.getSheetName());
                        text.append('\n');
                    }
                    Comments comments = includeCellComments ? iter.getSheetComments() : null;
                    processSheet(sheetExtractor, styles, comments, strings, stream);
                    if (includeHeadersFooters) {
                        sheetExtractor.appendHeaderText(text);
                    }
                    sheetExtractor.appendCellText(text);
                    if (includeTextBoxes) {
                        processShapes(iter.getShapes(), text);
                    }
                    if (includeHeadersFooters) {
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

    void processShapes(List<XSSFShape> shapes, StringBuilder text) {
        if (shapes == null) {
            return;
        }
        for (XSSFShape shape : shapes) {
            if (shape instanceof XSSFSimpleShape) {
                String sText = ((XSSFSimpleShape) shape).getText();
                if (sText != null && sText.length() > 0) {
                    text.append(sText).append('\n');
                }
            }
        }
    }

    @Override
    public POIXMLDocument getDocument() {
        return null;
    }

    @Override
    public void setCloseFilesystem(boolean doCloseFilesystem) {
        this.doCloseFilesystem = doCloseFilesystem;
    }

    @Override
    public boolean isCloseFilesystem() {
        return doCloseFilesystem;
    }

    @Override
    public OPCPackage getFilesystem() {
        return container;
    }

    protected class SheetTextExtractor implements SheetContentsHandler {
        private final StringBuilder output = new StringBuilder(64);
        private boolean firstCellOfRow;
        private final Map<String, String> headerFooterMap;

        protected SheetTextExtractor() {
            this.firstCellOfRow = true;
            this.headerFooterMap = includeHeadersFooters ? new HashMap<>() : null;
        }

        @Override
        public void startRow(int rowNum) {
            firstCellOfRow = true;
        }

        @Override
        public void endRow(int rowNum) {
            output.append('\n');
        }

        @Override
        public void cell(String cellRef, String formattedValue, XSSFComment comment) {
            if (firstCellOfRow) {
                firstCellOfRow = false;
            } else {
                output.append('\t');
            }
            if (formattedValue != null) {
                checkMaxTextSize(output, formattedValue);
                output.append(formattedValue);
            }
            if (includeCellComments && comment != null) {
                String commentText = comment.getString().getString().replace('\n', ' ');
                output.append(formattedValue != null ? " Comment by " : "Comment by ");
                checkMaxTextSize(output, commentText);
                if (commentText.startsWith(comment.getAuthor() + ": ")) {
                    output.append(commentText);
                } else {
                    output.append(comment.getAuthor()).append(": ").append(commentText);
                }
            }
        }

        @Override
        public void headerFooter(String text, boolean isHeader, String tagName) {
            if (headerFooterMap != null) {
                headerFooterMap.put(tagName, text);
            }
        }

        /**
         * Append the text for the named header or footer if found.
         */
        private void appendHeaderFooterText(StringBuilder buffer, String name) {
            String text = headerFooterMap.get(name);
            if (text != null && text.length() > 0) {
                // this is a naive way of handling the left, center, and right
                // header and footer delimiters, but it seems to be as good as
                // the method used by XSSFExcelExtractor
                text = handleHeaderFooterDelimiter(text, "&L");
                text = handleHeaderFooterDelimiter(text, "&C");
                text = handleHeaderFooterDelimiter(text, "&R");
                buffer.append(text).append('\n');
            }
        }

        /**
         * Remove the delimiter if its found at the beginning of the text,
         * or replace it with a tab if its in the middle.
         */
        private String handleHeaderFooterDelimiter(String text, String delimiter) {
            int index = text.indexOf(delimiter);
            if (index == 0) {
                text = text.substring(2);
            } else if (index > 0) {
                text = text.substring(0, index) + "\t" + text.substring(index + 2);
            }
            return text;
        }


        /**
         * Append the text for each header type in the same order
         * they are appended in XSSFExcelExtractor.
         *
         * @see XSSFExcelExtractor#getText()
         * @see org.apache.poi.hssf.extractor.ExcelExtractor#_extractHeaderFooter(HeaderFooter)
         */
        void appendHeaderText(StringBuilder buffer) {
            appendHeaderFooterText(buffer, "firstHeader");
            appendHeaderFooterText(buffer, "oddHeader");
            appendHeaderFooterText(buffer, "evenHeader");
        }

        /**
         * Append the text for each footer type in the same order
         * they are appended in XSSFExcelExtractor.
         *
         * @see XSSFExcelExtractor#getText()
         * @see org.apache.poi.hssf.extractor.ExcelExtractor#_extractHeaderFooter(HeaderFooter)
         */
        void appendFooterText(StringBuilder buffer) {
            // append the text for each footer type in the same order
            // they are appended in XSSFExcelExtractor
            appendHeaderFooterText(buffer, "firstFooter");
            appendHeaderFooterText(buffer, "oddFooter");
            appendHeaderFooterText(buffer, "evenFooter");
        }

        /**
         * Append the cell contents we have collected.
         */
        void appendCellText(StringBuilder buffer) {
            checkMaxTextSize(buffer, output.toString());
            buffer.append(output);
        }

        /**
         * Reset this <code>SheetTextExtractor</code> for the next sheet.
         */
        void reset() {
            output.setLength(0);
            firstCellOfRow = true;
            if (headerFooterMap != null) {
                headerFooterMap.clear();
            }
        }
    }
}
