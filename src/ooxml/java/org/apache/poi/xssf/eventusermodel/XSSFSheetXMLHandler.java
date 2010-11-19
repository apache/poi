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

import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class handles the processing of a sheet#.xml 
 *  sheet part of a XSSF .xlsx file, and generates
 *  row and cell events for it.
 */
public class XSSFSheetXMLHandler extends DefaultHandler {
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
   
   /**
    * Table with the styles used for formatting
    */
   private StylesTable stylesTable;

   private ReadOnlySharedStringsTable sharedStringsTable;

   /**
    * Where our text is going
    */
   private final SheetContentsHandler output;

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
   private String cellRef;
   private boolean formulasNotResults;

   // Gathers characters as they are seen.
   private StringBuffer value = new StringBuffer();
   private StringBuffer formula = new StringBuffer();

   /**
    * Accepts objects needed while parsing.
    *
    * @param styles  Table of styles
    * @param strings Table of shared strings
    * @param cols    Minimum number of columns to show
    * @param target  Sink for output
    */
   public XSSFSheetXMLHandler(
           StylesTable styles,
           ReadOnlySharedStringsTable strings,
           SheetContentsHandler sheetContentsHandler,
           boolean formulasNotResults) {
       this.stylesTable = styles;
       this.sharedStringsTable = strings;
       this.output = sheetContentsHandler;
       this.formulasNotResults = formulasNotResults;
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
           int rowNum = Integer.parseInt(attributes.getValue("r")) - 1;
           output.startRow(rowNum);
       }
       // c => cell
       else if ("c".equals(name)) {
           // Set up defaults.
           this.nextDataType = xssfDataType.NUMBER;
           this.formatIndex = -1;
           this.formatString = null;
           cellRef = attributes.getValue("r");
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
           output.cell(cellRef, thisStr);
       } else if ("f".equals(name)) {
          fIsOpen = false;
       } else if ("row".equals(name)) {
          output.endRow();
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

   /**
    * You need to implement this to handle the results
    *  of the sheet parsing.
    */
   public interface SheetContentsHandler {
      /** A row with the (zero based) row number has started */
      public void startRow(int rowNum);
      /** A row with the (zero based) row number has ended */
      public void endRow();
      /** A cell, with the given formatted value, was encountered */
      public void cell(String cellReference, String formattedValue);
   }
}
