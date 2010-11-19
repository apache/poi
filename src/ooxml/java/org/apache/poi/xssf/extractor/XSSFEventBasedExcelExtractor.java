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
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.xmlbeans.XmlException;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Implementation of a text extractor from OOXML Excel
 *  files that uses SAX event based parsing.
 */
public class XSSFEventBasedExcelExtractor extends POIXMLTextExtractor {
   private OPCPackage container;
	private boolean includeSheetNames = true;
	private boolean formulasNotResults = false;

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
    * Processes the given sheet
    */
   public void processSheet(
           SheetTextExtractor sheetExtractor,
           StylesTable styles,
           ReadOnlySharedStringsTable strings,
           InputStream sheetInputStream)
           throws IOException, SAXException {

       InputSource sheetSource = new InputSource(sheetInputStream);
       SAXParserFactory saxFactory = SAXParserFactory.newInstance();
       try {
          SAXParser saxParser = saxFactory.newSAXParser();
          XMLReader sheetParser = saxParser.getXMLReader();
          ContentHandler handler = new XSSFSheetXMLHandler(styles, strings, sheetExtractor, formulasNotResults);
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
          SheetTextExtractor sheetExtractor = new SheetTextExtractor(text);
          
          while (iter.hasNext()) {
              InputStream stream = iter.next();
              if(includeSheetNames) {
                 text.append(iter.getSheetName());
                 text.append('\n');
              }
              processSheet(sheetExtractor, styles, strings, stream);
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
   
   protected class SheetTextExtractor implements SheetContentsHandler {
      private final StringBuffer output;
      private boolean firstCellOfRow = true;
      
      protected SheetTextExtractor(StringBuffer output) {
         this.output = output;
      }
      
      public void startRow(int rowNum) {
         firstCellOfRow = true;
      }
      
      public void endRow() {
         output.append('\n');
      }

      public void cell(String cellRef, String formattedValue) {
         if(firstCellOfRow) {
            firstCellOfRow = false;
         } else {
            output.append('\t');
         }
         output.append(formattedValue);
      }
   }
}
