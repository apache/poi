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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.xmlbeans.XmlException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Implementation of a text extractor from OOXML Excel
 *  files that uses SAX event based parsing.
 */
public class XSSFEventBasedExcelExtractor extends POIXMLTextExtractor {
   private OPCPackage container;
	private boolean includeSheetNames = true;
	private boolean formulasNotResults = false;

   /**
    * These are the different kinds of cells we support.
    * We keep track of the current one between
    *  the start and end.
    */
   enum xssfDataType {
       BOOLEAN,
       ERROR,
       FORMULA,
       INLINE_STRING,
       SST_STRING,
       NUMBER,
   }
   
	public XSSFEventBasedExcelExtractor(String path) throws XmlException, OpenXML4JException, IOException {
		this(OPCPackage.open(path));
	}
	public XSSFEventBasedExcelExtractor(OPCPackage container) throws XmlException, OpenXML4JException, IOException {
		super(null);
		this.container = container;
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
	}

	/**
	 * Should sheet names be included? Default is true
	 */
	public void setIncludeSheetNames(boolean includeSheetNames) {
		this.includeSheetNames = includeSheetNames;
	}
	/**
	 * Should we return the formula itself, and not
	 *  the result it produces? Default is false
	 */
	public void setFormulasNotResults(boolean formulasNotResults) {
		this.formulasNotResults = formulasNotResults;
	}
	
	
   /**
    * Handler for sheets. Processes each row and cell,
    *  formatting Cells as best as it can.
    */
   class MyXSSFSheetHandler extends DefaultHandler {
       /**
        * Table with the styles used for formatting
        */
       private StylesTable stylesTable;

       private ReadOnlySharedStringsTable sharedStringsTable;

       /**
        * Where our text is going
        */
       private final StringBuffer output;

       // Set when V start element is seen
       private boolean vIsOpen;
       // Set when F start element is seen
       private boolean fIsOpen;

       // Set when cell start element is seen;
       // used when cell close element is seen.
       private xssfDataType nextDataType;

       // Used to format numeric cell values.
       private short formatIndex;
       private String formatString;
       private final DataFormatter formatter;

       // Gathers characters as they are seen.
       private StringBuffer value = new StringBuffer();
       private StringBuffer formula = new StringBuffer();
       private boolean firstCellOfRow = true;

       /**
        * Accepts objects needed while parsing.
        *
        * @param styles  Table of styles
        * @param strings Table of shared strings
        * @param cols    Minimum number of columns to show
        * @param target  Sink for output
        */
       public MyXSSFSheetHandler(
               StylesTable styles,
               ReadOnlySharedStringsTable strings,
               StringBuffer output) {
           this.stylesTable = styles;
           this.sharedStringsTable = strings;
           this.output = output;
           this.nextDataType = xssfDataType.NUMBER;
           this.formatter = new DataFormatter();
       }

       public void startElement(String uri, String localName, String name,
                                Attributes attributes) throws SAXException {

           if ("inlineStr".equals(name) || "v".equals(name)) {
               vIsOpen = true;
               // Clear contents cache
               value.setLength(0);
           } else if ("f".equals(name)) {
              // Clear contents cache
              formula.setLength(0);
              
              // Mark us as being a formula if not already
              if(nextDataType == xssfDataType.NUMBER) {
                 nextDataType = xssfDataType.FORMULA;
              }
              
              // Decide where to get the formula string from
              String type = attributes.getValue("t"); 
              if(type != null && type.equals("shared")) {
                 System.err.println("Warning - shared formulas not yet supported!");
              } else {
                 fIsOpen = true;
              }
           }
           else if("row".equals(name)) {
               firstCellOfRow = true;
           }
           // c => cell
           else if ("c".equals(name)) {
               // Set up defaults.
               this.nextDataType = xssfDataType.NUMBER;
               this.formatIndex = -1;
               this.formatString = null;
               String cellType = attributes.getValue("t");
               String cellStyleStr = attributes.getValue("s");
               if ("b".equals(cellType))
                   nextDataType = xssfDataType.BOOLEAN;
               else if ("e".equals(cellType))
                   nextDataType = xssfDataType.ERROR;
               else if ("inlineStr".equals(cellType))
                   nextDataType = xssfDataType.INLINE_STRING;
               else if ("s".equals(cellType))
                   nextDataType = xssfDataType.SST_STRING;
               else if ("str".equals(cellType))
                   nextDataType = xssfDataType.FORMULA;
               else if (cellStyleStr != null) {
                  // Number, but almost certainly with a special style or format
                   int styleIndex = Integer.parseInt(cellStyleStr);
                   XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
                   this.formatIndex = style.getDataFormat();
                   this.formatString = style.getDataFormatString();
                   if (this.formatString == null)
                       this.formatString = BuiltinFormats.getBuiltinFormat(this.formatIndex);
               }
           }
       }

       public void endElement(String uri, String localName, String name)
               throws SAXException {
           String thisStr = null;

           // v => contents of a cell
           if ("v".equals(name)) {
               vIsOpen = false;
               
               // Process the value contents as required, now we have it all
               switch (nextDataType) {
                   case BOOLEAN:
                       char first = value.charAt(0);
                       thisStr = first == '0' ? "FALSE" : "TRUE";
                       break;

                   case ERROR:
                       thisStr = "ERROR:" + value.toString();
                       break;

                   case FORMULA:
                       if(formulasNotResults) {
                          thisStr = formula.toString();
                       } else {
                          thisStr = value.toString();
                       }
                       break;

                   case INLINE_STRING:
                       // TODO: have seen an example of this, so it's untested.
                       XSSFRichTextString rtsi = new XSSFRichTextString(value.toString());
                       thisStr = rtsi.toString();
                       break;

                   case SST_STRING:
                       String sstIndex = value.toString();
                       try {
                           int idx = Integer.parseInt(sstIndex);
                           XSSFRichTextString rtss = new XSSFRichTextString(sharedStringsTable.getEntryAt(idx));
                           thisStr = rtss.toString();
                       }
                       catch (NumberFormatException ex) {
                           System.err.println("Failed to parse SST index '" + sstIndex + "': " + ex.toString());
                       }
                       break;

                   case NUMBER:
                       String n = value.toString();
                       if (this.formatString != null)
                           thisStr = formatter.formatRawCellContents(Double.parseDouble(n), this.formatIndex, this.formatString);
                       else
                           thisStr = n;
                       break;

                   default:
                       thisStr = "(TODO: Unexpected type: " + nextDataType + ")";
                       break;
               }
               
               // Output
               if(!firstCellOfRow) {
                  output.append('\t');
               }
               firstCellOfRow = false;
               
               output.append(thisStr);
           } else if ("f".equals(name)) {
              fIsOpen = false;
           } else if ("row".equals(name)) {
              // Finish the line
              output.append('\n');
           }
       }

       /**
        * Captures characters only if a suitable element is open.
        * Originally was just "v"; extended for inlineStr also.
        */
       public void characters(char[] ch, int start, int length)
               throws SAXException {
           if (vIsOpen) {
               value.append(ch, start, length);
           }
           if (fIsOpen) {
              formula.append(ch, start, length);
           }
       }
   }

   /**
    * Processes the given sheet
    */
   public void processSheet(
           StringBuffer output,
           StylesTable styles,
           ReadOnlySharedStringsTable strings,
           InputStream sheetInputStream)
           throws IOException, SAXException {

       InputSource sheetSource = new InputSource(sheetInputStream);
       SAXParserFactory saxFactory = SAXParserFactory.newInstance();
       try {
          SAXParser saxParser = saxFactory.newSAXParser();
          XMLReader sheetParser = saxParser.getXMLReader();
          ContentHandler handler = new MyXSSFSheetHandler(styles, strings, output);
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
          ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(container);
          XSSFReader xssfReader = new XSSFReader(container);
          StylesTable styles = xssfReader.getStylesTable();
          XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
   
          StringBuffer text = new StringBuffer();
          while (iter.hasNext()) {
              InputStream stream = iter.next();
              if(includeSheetNames) {
                 text.append(iter.getSheetName());
                 text.append('\n');
              }
              processSheet(text, styles, strings, stream);
              stream.close();
          }
          
          return text.toString();
       } catch(IOException e) {
          System.err.println(e);
          return null;
       } catch(SAXException se) {
          System.err.println(se);
          return null;
       } catch(OpenXML4JException o4je) {
          System.err.println(o4je);
          return null;
       }
   }
}
