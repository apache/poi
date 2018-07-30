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

import static org.apache.poi.xssf.usermodel.XSSFRelation.NS_SPREADSHEETML;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.model.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFComment;
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
    private static final POILogger logger = POILogFactory.getLogger(XSSFSheetXMLHandler.class);

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
   private Styles stylesTable;

   /**
    * Table with cell comments
    */
   private Comments comments;

   /**
    * Read only access to the shared strings table, for looking
    *  up (most) string cell's contents
    */
   private SharedStrings sharedStringsTable;

   /**
    * Where our text is going
    */
   private final SheetContentsHandler output;

   // Set when V start element is seen
   private boolean vIsOpen;
   // Set when F start element is seen
   private boolean fIsOpen;
   // Set when an Inline String "is" is seen
   private boolean isIsOpen;
   // Set when a header/footer element is seen
   private boolean hfIsOpen;

   // Set when cell start element is seen;
   // used when cell close element is seen.
   private xssfDataType nextDataType;

   // Used to format numeric cell values.
   private short formatIndex;
   private String formatString;
   private final DataFormatter formatter;
   private int rowNum;
   private int nextRowNum;      // some sheets do not have rowNums, Excel can read them so we should try to handle them correctly as well
   private String cellRef;
   private boolean formulasNotResults;

   // Gathers characters as they are seen.
   private StringBuilder value = new StringBuilder(64);
   private StringBuilder formula = new StringBuilder(64);
   private StringBuilder headerFooter = new StringBuilder(64);

   private Queue<CellAddress> commentCellRefs;

   /**
    * Accepts objects needed while parsing.
    *
    * @param styles  Table of styles
    * @param strings Table of shared strings
    */
   public XSSFSheetXMLHandler(
           Styles styles,
           Comments comments,
           SharedStrings strings,
           SheetContentsHandler sheetContentsHandler,
           DataFormatter dataFormatter,
           boolean formulasNotResults) {
       this.stylesTable = styles;
       this.comments = comments;
       this.sharedStringsTable = strings;
       this.output = sheetContentsHandler;
       this.formulasNotResults = formulasNotResults;
       this.nextDataType = xssfDataType.NUMBER;
       this.formatter = dataFormatter;
       init(comments);
   }
   
   /**
    * Accepts objects needed while parsing.
    *
    * @param styles  Table of styles
    * @param strings Table of shared strings
    */
   public XSSFSheetXMLHandler(
           Styles styles,
           SharedStrings strings,
           SheetContentsHandler sheetContentsHandler,
           DataFormatter dataFormatter,
           boolean formulasNotResults) {
       this(styles, null, strings, sheetContentsHandler, dataFormatter, formulasNotResults);
   }
   
   /**
    * Accepts objects needed while parsing.
    *
    * @param styles  Table of styles
    * @param strings Table of shared strings
    */
   public XSSFSheetXMLHandler(
           Styles styles,
           SharedStrings strings,
           SheetContentsHandler sheetContentsHandler,
           boolean formulasNotResults) {
       this(styles, strings, sheetContentsHandler, new DataFormatter(), formulasNotResults);
   }
   
   private void init(Comments commentsTable) {
       if (commentsTable != null) {
           commentCellRefs = new LinkedList<>();
           for (Iterator<CellAddress> iter = commentsTable.getCellAddresses(); iter.hasNext(); ) {
               commentCellRefs.add(iter.next());
           }
       }
   }

   private boolean isTextTag(String name) {
      if("v".equals(name)) {
         // Easy, normal v text tag
         return true;
      }
      if("inlineStr".equals(name)) {
         // Easy inline string
         return true;
      }
      if("t".equals(name) && isIsOpen) {
         // Inline string <is><t>...</t></is> pair
         return true;
      }
      // It isn't a text tag
      return false;
   }
   
   @Override
   @SuppressWarnings("unused")
   public void startElement(String uri, String localName, String qName,
                            Attributes attributes) throws SAXException {

       if (uri != null && ! uri.equals(NS_SPREADSHEETML)) {
           return;
       }

       if (isTextTag(localName)) {
           vIsOpen = true;
           // Clear contents cache
           value.setLength(0);
       } else if ("is".equals(localName)) {
          // Inline string outer tag
          isIsOpen = true;
       } else if ("f".equals(localName)) {
          // Clear contents cache
          formula.setLength(0);
          
          // Mark us as being a formula if not already
          if(nextDataType == xssfDataType.NUMBER) {
             nextDataType = xssfDataType.FORMULA;
          }
          
          // Decide where to get the formula string from
          String type = attributes.getValue("t");
          if(type != null && type.equals("shared")) {
             // Is it the one that defines the shared, or uses it?
             String ref = attributes.getValue("ref");
             String si = attributes.getValue("si");
             
             if(ref != null) {
                // This one defines it
                // TODO Save it somewhere
                fIsOpen = true;
             } else {
                // This one uses a shared formula
                // TODO Retrieve the shared formula and tweak it to 
                //  match the current cell
                if(formulasNotResults) {
                    logger.log(POILogger.WARN, "shared formulas not yet supported!");
                } /*else {
                   // It's a shared formula, so we can't get at the formula string yet
                   // However, they don't care about the formula string, so that's ok!
                }*/
             }
          } else {
             fIsOpen = true;
          }
       }
       else if("oddHeader".equals(localName) || "evenHeader".equals(localName) ||
             "firstHeader".equals(localName) || "firstFooter".equals(localName) ||
             "oddFooter".equals(localName) || "evenFooter".equals(localName)) {
          hfIsOpen = true;
          // Clear contents cache
          headerFooter.setLength(0);
       }
       else if("row".equals(localName)) {
           String rowNumStr = attributes.getValue("r");
           if(rowNumStr != null) {
               rowNum = Integer.parseInt(rowNumStr) - 1;
           } else {
               rowNum = nextRowNum;
           }
           output.startRow(rowNum);
       }
       // c => cell
       else if ("c".equals(localName)) {
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
           else {
               // Number, but almost certainly with a special style or format
               XSSFCellStyle style = null;
               if (stylesTable != null) {
                   if (cellStyleStr != null) {
                       int styleIndex = Integer.parseInt(cellStyleStr);
                       style = stylesTable.getStyleAt(styleIndex);
                   } else if (stylesTable.getNumCellStyles() > 0) {
                       style = stylesTable.getStyleAt(0);
                   }
               }
               if (style != null) {
                   this.formatIndex = style.getDataFormat();
                   this.formatString = style.getDataFormatString();
                   if (this.formatString == null)
                       this.formatString = BuiltinFormats.getBuiltinFormat(this.formatIndex);
               }
           }
       }
   }

   @Override
   public void endElement(String uri, String localName, String qName)
           throws SAXException {

       if (uri != null && ! uri.equals(NS_SPREADSHEETML)) {
           return;
       }

       String thisStr = null;

       // v => contents of a cell
       if (isTextTag(localName)) {
           vIsOpen = false;
           
           // Process the value contents as required, now we have it all
           switch (nextDataType) {
               case BOOLEAN:
                   char first = value.charAt(0);
                   thisStr = first == '0' ? "FALSE" : "TRUE";
                   break;

               case ERROR:
                   thisStr = "ERROR:" + value;
                   break;

               case FORMULA:
                   if(formulasNotResults) {
                      thisStr = formula.toString();
                   } else {
                      String fv = value.toString();
                      
                      if (this.formatString != null) {
                         try {
                            // Try to use the value as a formattable number
                            double d = Double.parseDouble(fv);
                            thisStr = formatter.formatRawCellContents(d, this.formatIndex, this.formatString);
                         } catch(NumberFormatException e) {
                            // Formula is a String result not a Numeric one
                            thisStr = fv;
                         }
                      } else {
                         // No formatting applied, just do raw value in all cases
                         thisStr = fv;
                      }
                   }
                   break;

               case INLINE_STRING:
                   // TODO: Can these ever have formatting on them?
                   XSSFRichTextString rtsi = new XSSFRichTextString(value.toString());
                   thisStr = rtsi.toString();
                   break;

               case SST_STRING:
                   String sstIndex = value.toString();
                   try {
                       int idx = Integer.parseInt(sstIndex);
                       RichTextString rtss = sharedStringsTable.getItemAt(idx);
                       thisStr = rtss.toString();
                   }
                   catch (NumberFormatException ex) {
                       logger.log(POILogger.ERROR, "Failed to parse SST index '" + sstIndex, ex);
                   }
                   break;

               case NUMBER:
                   String n = value.toString();
                   if (this.formatString != null && n.length() > 0)
                       thisStr = formatter.formatRawCellContents(Double.parseDouble(n), this.formatIndex, this.formatString);
                   else
                       thisStr = n;
                   break;

               default:
                   thisStr = "(TODO: Unexpected type: " + nextDataType + ")";
                   break;
           }
           
           // Do we have a comment for this cell?
           checkForEmptyCellComments(EmptyCellCommentsCheckType.CELL);
           XSSFComment comment = comments != null ? comments.findCellComment(new CellAddress(cellRef)) : null;
           
           // Output
           output.cell(cellRef, thisStr, comment);
       } else if ("f".equals(localName)) {
          fIsOpen = false;
       } else if ("is".equals(localName)) {
          isIsOpen = false;
       } else if ("row".equals(localName)) {
          // Handle any "missing" cells which had comments attached
          checkForEmptyCellComments(EmptyCellCommentsCheckType.END_OF_ROW);
          
          // Finish up the row
          output.endRow(rowNum);
          
          // some sheets do not have rowNum set in the XML, Excel can read them so we should try to read them as well
          nextRowNum = rowNum + 1;
       } else if ("sheetData".equals(localName)) {
           // Handle any "missing" cells which had comments attached
           checkForEmptyCellComments(EmptyCellCommentsCheckType.END_OF_SHEET_DATA);

           // indicate that this sheet is now done
           output.endSheet();
       }
       else if("oddHeader".equals(localName) || "evenHeader".equals(localName) ||
             "firstHeader".equals(localName)) {
          hfIsOpen = false;
          output.headerFooter(headerFooter.toString(), true, localName);
       }
       else if("oddFooter".equals(localName) || "evenFooter".equals(localName) ||
             "firstFooter".equals(localName)) {
          hfIsOpen = false;
          output.headerFooter(headerFooter.toString(), false, localName);
       }
   }

   /**
    * Captures characters only if a suitable element is open.
    * Originally was just "v"; extended for inlineStr also.
    */
   @Override
   public void characters(char[] ch, int start, int length)
           throws SAXException {
       if (vIsOpen) {
           value.append(ch, start, length);
       }
       if (fIsOpen) {
          formula.append(ch, start, length);
       }
       if (hfIsOpen) {
          headerFooter.append(ch, start, length);
       }
   }
   
   /**
    * Do a check for, and output, comments in otherwise empty cells.
    */
   private void checkForEmptyCellComments(EmptyCellCommentsCheckType type) {
       if (commentCellRefs != null && !commentCellRefs.isEmpty()) {
           // If we've reached the end of the sheet data, output any
           //  comments we haven't yet already handled
           if (type == EmptyCellCommentsCheckType.END_OF_SHEET_DATA) {
               while (!commentCellRefs.isEmpty()) {
                   outputEmptyCellComment(commentCellRefs.remove());
               }
               return;
           }

           // At the end of a row, handle any comments for "missing" rows before us
           if (this.cellRef == null) {
               if (type == EmptyCellCommentsCheckType.END_OF_ROW) {
                   while (!commentCellRefs.isEmpty()) {
                       if (commentCellRefs.peek().getRow() == rowNum) {
                           outputEmptyCellComment(commentCellRefs.remove());
                       } else {
                           return;
                       }
                   }
                   return;
               } else {
                   throw new IllegalStateException("Cell ref should be null only if there are only empty cells in the row; rowNum: " + rowNum);
               }
           }

           CellAddress nextCommentCellRef;
           do {
               CellAddress cellRef = new CellAddress(this.cellRef);
               CellAddress peekCellRef = commentCellRefs.peek();
               if (type == EmptyCellCommentsCheckType.CELL && cellRef.equals(peekCellRef)) {
                   // remove the comment cell ref from the list if we're about to handle it alongside the cell content
                   commentCellRefs.remove();
                   return;
               } else {
                   // fill in any gaps if there are empty cells with comment mixed in with non-empty cells
                   int comparison = peekCellRef.compareTo(cellRef);
                   if (comparison > 0 && type == EmptyCellCommentsCheckType.END_OF_ROW && peekCellRef.getRow() <= rowNum) {
                       nextCommentCellRef = commentCellRefs.remove();
                       outputEmptyCellComment(nextCommentCellRef);
                   } else if (comparison < 0 && type == EmptyCellCommentsCheckType.CELL && peekCellRef.getRow() <= rowNum) {
                       nextCommentCellRef = commentCellRefs.remove();
                       outputEmptyCellComment(nextCommentCellRef);
                   } else {
                       nextCommentCellRef = null;
                   }
               }
           } while (nextCommentCellRef != null && !commentCellRefs.isEmpty());
       }
   }


   /**
    * Output an empty-cell comment.
    */
   private void outputEmptyCellComment(CellAddress cellRef) {
       XSSFComment comment = comments.findCellComment(cellRef);
       output.cell(cellRef.formatAsString(), null, comment);
   }
   
   private enum EmptyCellCommentsCheckType {
       CELL,
       END_OF_ROW,
       END_OF_SHEET_DATA
   }

   /**
    * You need to implement this to handle the results
    *  of the sheet parsing.
    */
   public interface SheetContentsHandler {
      /** A row with the (zero based) row number has started */
      void startRow(int rowNum);

      /** A row with the (zero based) row number has ended */
      void endRow(int rowNum);

      /**
       * A cell, with the given formatted value (may be null), 
       * and possibly a comment (may be null), was encountered.
       *
       * Sheets that have missing or empty cells may result in
       * sparse calls to <code>cell</code>. See the code in
       * <code>src/examples/src/org/apache/poi/xssf/eventusermodel/XLSX2CSV.java</code>
       * for an example of how to handle this scenario.
       */
      void cell(String cellReference, String formattedValue, XSSFComment comment);

      /** A header or footer has been encountered */
      default void headerFooter(String text, boolean isHeader, String tagName) {}

      /** Signal that the end of a sheet was been reached */
      default void endSheet() {}
   }
}
