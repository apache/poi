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

import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.binary.XSSFBCommentsTable;
import org.apache.poi.xssf.binary.XSSFBHyperlinksTable;
import org.apache.poi.xssf.binary.XSSFBSharedStringsTable;
import org.apache.poi.xssf.binary.XSSFBSheetHandler;
import org.apache.poi.xssf.binary.XSSFBStylesTable;
import org.apache.poi.xssf.eventusermodel.XSSFBReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.xmlbeans.XmlException;
import org.xml.sax.SAXException;

/**
 * Implementation of a text extractor or xlsb Excel
 * files that uses SAX-like binary parsing.
 */
public class XSSFBEventBasedExcelExtractor extends XSSFEventBasedExcelExtractor
        implements org.apache.poi.ss.extractor.ExcelExtractor {

    public static final XSSFRelation[] SUPPORTED_TYPES = new XSSFRelation[] {
            XSSFRelation.XLSB_BINARY_WORKBOOK
    };

    private boolean handleHyperlinksInCells = false;

    public XSSFBEventBasedExcelExtractor(String path) throws XmlException, OpenXML4JException, IOException {
        super(path);
    }

    public XSSFBEventBasedExcelExtractor(OPCPackage container) throws XmlException, OpenXML4JException, IOException {
        super(container);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Use:");
            System.err.println("  XSSFBEventBasedExcelExtractor <filename.xlsb>");
            System.exit(1);
        }
        POIXMLTextExtractor extractor =
                new XSSFBEventBasedExcelExtractor(args[0]);
        System.out.println(extractor.getText());
        extractor.close();
    }

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
            XSSFBSharedStringsTable strings,
            InputStream sheetInputStream)
            throws IOException, SAXException {

        DataFormatter formatter;
        if (locale == null) {
            formatter = new DataFormatter();
        } else {
            formatter = new DataFormatter(locale);
        }

        XSSFBSheetHandler xssfbSheetHandler = new XSSFBSheetHandler(
                sheetInputStream,
                styles, comments, strings, sheetContentsExtractor, formatter, formulasNotResults
        );
        xssfbSheetHandler.parse();
    }

    /**
     * Processes the file and returns the text
     */
    public String getText() {
        try {
            XSSFBSharedStringsTable strings = new XSSFBSharedStringsTable(container);
            XSSFBReader xssfbReader = new XSSFBReader(container);
            XSSFBStylesTable styles = xssfbReader.getXSSFBStylesTable();
            XSSFBReader.SheetIterator iter = (XSSFBReader.SheetIterator) xssfbReader.getSheetsData();

            StringBuffer text = new StringBuffer();
            SheetTextExtractor sheetExtractor = new SheetTextExtractor();
            XSSFBHyperlinksTable hyperlinksTable = null;
            while (iter.hasNext()) {
                InputStream stream = iter.next();
                if (includeSheetNames) {
                    text.append(iter.getSheetName());
                    text.append('\n');
                }
                if (handleHyperlinksInCells) {
                    hyperlinksTable = new XSSFBHyperlinksTable(iter.getSheetPart());
                }
                XSSFBCommentsTable comments = includeCellComments ? iter.getXSSFBSheetComments() : null;
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
                stream.close();
            }

            return text.toString();
        } catch (IOException e) {
            System.err.println(e);
            return null;
        } catch (SAXException se) {
            System.err.println(se);
            return null;
        } catch (OpenXML4JException o4je) {
            System.err.println(o4je);
            return null;
        }
    }

}
