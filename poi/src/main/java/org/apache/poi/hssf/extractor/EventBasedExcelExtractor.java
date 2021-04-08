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

package org.apache.poi.hssf.extractor;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.POIDocument;
import org.apache.poi.extractor.POIOLE2TextExtractor;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.eventusermodel.FormatTrackingHSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * A text extractor for Excel files, that is based
 *  on the HSSF EventUserModel API.
 * It will typically use less memory than
 *  {@link ExcelExtractor}, but may not provide
 *  the same richness of formatting.
 * Returns the textual content of the file, suitable for
 *  indexing by something like Lucene, but not really
 *  intended for display to the user.
 * <p>
 * To turn an excel file into a CSV or similar, then see
 *  the XLS2CSVmra example
 * </p>
 *
 * @see <a href="http://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/hssf/eventusermodel/examples/XLS2CSVmra.java">XLS2CSVmra</a>
 */
public class EventBasedExcelExtractor implements POIOLE2TextExtractor, org.apache.poi.ss.extractor.ExcelExtractor {
    private final POIFSFileSystem poifs;
    private final DirectoryNode _dir;
    private boolean doCloseFilesystem = true;
    boolean _includeSheetNames = true;
    boolean _formulasNotResults;

    public EventBasedExcelExtractor(DirectoryNode dir) {
        poifs = null;
        _dir = dir;
    }

   public EventBasedExcelExtractor(POIFSFileSystem fs) {
        poifs = fs;
        _dir = fs.getRoot();
   }

   /**
    * Would return the document information metadata for the document,
    *  if we supported it
    */
   @Override
   public DocumentSummaryInformation getDocSummaryInformation() {
       throw new IllegalStateException("Metadata extraction not supported in streaming mode, please use ExcelExtractor");
   }
   /**
    * Would return the summary information metadata for the document,
    *  if we supported it
    */
   @Override
   public SummaryInformation getSummaryInformation() {
       throw new IllegalStateException("Metadata extraction not supported in streaming mode, please use ExcelExtractor");
   }


   /**
    * Would control the inclusion of cell comments from the document,
    *  if we supported it
    */
   public void setIncludeCellComments(boolean includeComments) {
       throw new IllegalStateException("Comment extraction not supported in streaming mode, please use ExcelExtractor");
   }

   /**
    * Would control the inclusion of headers and footers from the document,
    *  if we supported it
    */
   public void setIncludeHeadersFooters(boolean includeHeadersFooters) {
       throw new IllegalStateException("Header/Footer extraction not supported in streaming mode, please use ExcelExtractor");
   }


   /**
    * Should sheet names be included? Default is true
    */
   public void setIncludeSheetNames(boolean includeSheetNames) {
       _includeSheetNames = includeSheetNames;
   }
   /**
    * Should we return the formula itself, and not
    *  the result it produces? Default is false
    */
   public void setFormulasNotResults(boolean formulasNotResults) {
       _formulasNotResults = formulasNotResults;
   }


   /**
    * Retreives the text contents of the file
    */
   public String getText() {
       String text;
       try {
           TextListener tl = triggerExtraction();

           text = tl._text.toString();
           if(! text.endsWith("\n")) {
               text = text + "\n";
           }
       } catch(IOException e) {
           throw new RuntimeException(e);
       }

       return text;
   }

   private TextListener triggerExtraction() throws IOException {
       TextListener tl = new TextListener();
       FormatTrackingHSSFListener ft = new FormatTrackingHSSFListener(tl);
       tl._ft = ft;

       // Register and process
       HSSFEventFactory factory = new HSSFEventFactory();
       HSSFRequest request = new HSSFRequest();
       request.addListenerForAllRecords(ft);

       factory.processWorkbookEvents(request, _dir);

       return tl;
   }

   private class TextListener implements HSSFListener {
       FormatTrackingHSSFListener _ft;
       private SSTRecord sstRecord;

       private final List<String> sheetNames;
       final StringBuilder _text = new StringBuilder();
       private int sheetNum = -1;
       private int rowNum;

       private boolean outputNextStringValue;
       private int nextRow = -1;

       public TextListener() {
           sheetNames = new ArrayList<>();
       }
       public void processRecord(org.apache.poi.hssf.record.Record record) {
           String thisText = null;
           int thisRow = -1;

           switch(record.getSid()) {
           case BoundSheetRecord.sid:
               BoundSheetRecord sr = (BoundSheetRecord)record;
               sheetNames.add(sr.getSheetname());
               break;
           case BOFRecord.sid:
               BOFRecord bof = (BOFRecord)record;
               if(bof.getType() == BOFRecord.TYPE_WORKSHEET) {
                   sheetNum++;
                   rowNum = -1;

                   if(_includeSheetNames) {
                       if(_text.length() > 0) _text.append("\n");
                       _text.append(sheetNames.get(sheetNum));
                   }
               }
               break;
           case SSTRecord.sid:
               sstRecord = (SSTRecord)record;
               break;

           case FormulaRecord.sid:
               FormulaRecord frec = (FormulaRecord) record;
               thisRow = frec.getRow();

               if(_formulasNotResults) {
                   thisText = HSSFFormulaParser.toFormulaString(null, frec.getParsedExpression());
               } else {
                   if(frec.hasCachedResultString()) {
                       // Formula result is a string
                       // This is stored in the next record
                       outputNextStringValue = true;
                       nextRow = frec.getRow();
                   } else {
                       thisText = _ft.formatNumberDateCell(frec);
                   }
               }
               break;
           case StringRecord.sid:
               if(outputNextStringValue) {
                   // String for formula
                   StringRecord srec = (StringRecord)record;
                   thisText = srec.getString();
                   thisRow = nextRow;
                   outputNextStringValue = false;
               }
               break;
           case LabelRecord.sid:
               LabelRecord lrec = (LabelRecord) record;
               thisRow = lrec.getRow();
               thisText = lrec.getValue();
               break;
           case LabelSSTRecord.sid:
               LabelSSTRecord lsrec = (LabelSSTRecord) record;
               thisRow = lsrec.getRow();
               if(sstRecord == null) {
                   throw new IllegalStateException("No SST record found");
               }
               thisText = sstRecord.getString(lsrec.getSSTIndex()).toString();
               break;
           case NoteRecord.sid:
               NoteRecord nrec = (NoteRecord) record;
               thisRow = nrec.getRow();
               // TODO: Find object to match nrec.getShapeId()
               break;
           case NumberRecord.sid:
               NumberRecord numrec = (NumberRecord) record;
               thisRow = numrec.getRow();
               thisText = _ft.formatNumberDateCell(numrec);
               break;
           default:
               break;
           }

           if(thisText != null) {
               if(thisRow != rowNum) {
                   rowNum = thisRow;
                   if(_text.length() > 0)
                       _text.append("\n");
               } else {
                   _text.append("\t");
               }
               _text.append(thisText);
           }
       }
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
    public Closeable getFilesystem() {
        return poifs;
    }

    @Override
    public POIDocument getDocument() {
        return null;
    }

    @Override
    public DirectoryEntry getRoot() {
        return _dir;
    }

    @Override
    public void close() throws IOException {
        // first perform the default close
        POIOLE2TextExtractor.super.close();

        // also ensure that an underlying DirectoryNode
        // is closed properly to avoid leaking file-handles
        DirectoryEntry root = getRoot();
        if (root instanceof DirectoryNode) {
            Closeable fs = ((DirectoryNode) root).getFileSystem();
            if (isCloseFilesystem() && fs != null) {
                fs.close();
            }
        }
    }
}
