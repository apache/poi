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

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.POIXMLProperties.CoreProperties;
import org.apache.poi.POIXMLProperties.CustomProperties;
import org.apache.poi.POIXMLProperties.ExtendedProperties;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.CommentsTable;
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
 *  files that uses SAX event based parsing.
 */
public class XSSFEventBasedExcelExtractor extends POIXMLTextExtractor 
       implements org.apache.poi.ss.extractor.ExcelExtractor {

    private static final POILogger LOGGER = POILogFactory.getLogger(XSSFEventBasedExcelExtractor.class);

    private OPCPackage container;
    private POIXMLProperties properties;

    private Locale locale;
    private boolean includeTextBoxes = true;
    private boolean includeSheetNames = true;
    private boolean includeCellComments = false;
    private boolean includeHeadersFooters = true;
    private boolean formulasNotResults = false;
    private boolean concatenatePhoneticRuns = true;

    public XSSFEventBasedExcelExtractor(String path) throws XmlException, OpenXML4JException, IOException {
        this(OPCPackage.open(path));
    }
    public XSSFEventBasedExcelExtractor(OPCPackage container) throws XmlException, OpenXML4JException, IOException {
        super(null);
        this.container = container;

        properties = new POIXMLProperties(container);
    }

    public static void main(String[] args) throws Exception {
        if(args.length < 1) {
            System.err.println("Use:");
            System.err.println("  XSSFEventBasedExcelExtractor <filename.xlsx>");
            System.exit(1);
        }
        POIXMLTextExtractor extractor =
            new XSSFEventBasedExcelExtractor(args[0]);
        System.out.println(extractor.getText());
        extractor.close();
    }

    /**
     * Should sheet names be included? Default is true
     */
    public void setIncludeSheetNames(boolean includeSheetNames) {
        this.includeSheetNames = includeSheetNames;
    }


    /**
     *
     * @return whether to include sheet names
     *
     * @since 3.16-beta3
     */
    public boolean getIncludeSheetNames() {
        return includeSheetNames;
    }

    /**
     * Should we return the formula itself, and not
     *  the result it produces? Default is false
     */
    public void setFormulasNotResults(boolean formulasNotResults) {
        this.formulasNotResults = formulasNotResults;
    }

    /**
     *
     * @return whether to include formulas but not results
     *
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
     *
     * @return whether or not to include headers and footers
     *
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
     *
     * @return whether or not to extract textboxes
     *
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
     *
     * @since 3.16-beta3
     */
    public boolean getIncludeCellComments() {
        return includeCellComments;
    }
    /**
     * Concatenate text from &lt;rPh&gt; text elements in SharedStringsTable
     * Default is true;
     * @param concatenatePhoneticRuns
     */
    public void setConcatenatePhoneticRuns(boolean concatenatePhoneticRuns) {
        this.concatenatePhoneticRuns = concatenatePhoneticRuns;
    }
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * @return locale
     *
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
            StylesTable styles,
            CommentsTable comments,
            ReadOnlySharedStringsTable strings,
            InputStream sheetInputStream)
            throws IOException, SAXException {

       DataFormatter formatter;
       if(locale == null) {
          formatter = new DataFormatter();
       } else  {
          formatter = new DataFormatter(locale);
       }
      
       InputSource sheetSource = new InputSource(sheetInputStream);
       try {
          XMLReader sheetParser = SAXHelper.newXMLReader();
          ContentHandler handler = new XSSFSheetXMLHandler(
                styles, comments, strings, sheetContentsExtractor, formatter, formulasNotResults);
          sheetParser.setContentHandler(handler);
          sheetParser.parse(sheetSource);
       } catch(ParserConfigurationException e) {
          throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
       }
   }

   /**
    * Processes the file and returns the text
    */
   public String getText() {
       try {
          ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(container, concatenatePhoneticRuns);
          XSSFReader xssfReader = new XSSFReader(container);
          StylesTable styles = xssfReader.getStylesTable();
          XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
   
          StringBuffer text = new StringBuffer();
          SheetTextExtractor sheetExtractor = new SheetTextExtractor();
          
          while (iter.hasNext()) {
              InputStream stream = iter.next();
              if(includeSheetNames) {
                 text.append(iter.getSheetName());
                 text.append('\n');
              }
              CommentsTable comments = includeCellComments ? iter.getSheetComments() : null;
              processSheet(sheetExtractor, styles, comments, strings, stream);
              if (includeHeadersFooters) {
                  sheetExtractor.appendHeaderText(text);
              }
              sheetExtractor.appendCellText(text);
              if (includeTextBoxes){
                  processShapes(iter.getShapes(), text);
              }
              if (includeHeadersFooters) {
                  sheetExtractor.appendFooterText(text);
              }
              sheetExtractor.reset();
              stream.close();
          }
          
          return text.toString();
       } catch(IOException e) {
          LOGGER.log(POILogger.WARN, e);
          return null;
       } catch(SAXException se) {
           LOGGER.log(POILogger.WARN, se);
          return null;
       } catch(OpenXML4JException o4je) {
           LOGGER.log(POILogger.WARN, o4je);
          return null;
       }
   }
   
    void processShapes(List<XSSFShape> shapes, StringBuffer text) {
        if (shapes == null){
            return;
        }
        for (XSSFShape shape : shapes){
            if (shape instanceof XSSFSimpleShape){
                String sText = ((XSSFSimpleShape)shape).getText();
                if (sText != null && sText.length() > 0){
                    text.append(sText).append('\n');
                }
            }
        }
    }
    @Override
	public void close() throws IOException {
		if (container != null) {
			container.close();
			container = null;
		}
		super.close();
	}

    protected class SheetTextExtractor implements SheetContentsHandler {
        private final StringBuffer output;
        private boolean firstCellOfRow;
        private final Map<String, String> headerFooterMap;

        protected SheetTextExtractor() {
            this.output = new StringBuffer();
            this.firstCellOfRow = true;
            this.headerFooterMap = includeHeadersFooters ? new HashMap<String, String>() : null;
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
            if(firstCellOfRow) {
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
        private void appendHeaderFooterText(StringBuffer buffer, String name) {
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
         * @see XSSFExcelExtractor#getText()
         * @see org.apache.poi.hssf.extractor.ExcelExtractor#_extractHeaderFooter(org.apache.poi.ss.usermodel.HeaderFooter)
         */
        void appendHeaderText(StringBuffer buffer) {
            appendHeaderFooterText(buffer, "firstHeader");
            appendHeaderFooterText(buffer, "oddHeader");
            appendHeaderFooterText(buffer, "evenHeader");
        }

        /**
         * Append the text for each footer type in the same order
         * they are appended in XSSFExcelExtractor.
         * @see XSSFExcelExtractor#getText()
         * @see org.apache.poi.hssf.extractor.ExcelExtractor#_extractHeaderFooter(org.apache.poi.ss.usermodel.HeaderFooter)
         */
        void appendFooterText(StringBuffer buffer) {
            // append the text for each footer type in the same order
            // they are appended in XSSFExcelExtractor
            appendHeaderFooterText(buffer, "firstFooter");
            appendHeaderFooterText(buffer, "oddFooter");
            appendHeaderFooterText(buffer, "evenFooter");
        }

        /**
         * Append the cell contents we have collected.
         */
        void appendCellText(StringBuffer buffer) {
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
