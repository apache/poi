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

import static org.apache.poi.hssf.model.InternalWorkbook.OLD_WORKBOOK_DIR_ENTRY_NAME;
import static org.apache.poi.hssf.model.InternalWorkbook.WORKBOOK_DIR_ENTRY_NAMES;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.CodepageRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.OldFormulaRecord;
import org.apache.poi.hssf.record.OldLabelRecord;
import org.apache.poi.hssf.record.OldSheetRecord;
import org.apache.poi.hssf.record.OldStringRecord;
import org.apache.poi.hssf.record.RKRecord;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.NotOLE2FileException;
import org.apache.poi.ss.usermodel.Cell;

/**
 * A text extractor for old Excel files, which are too old for
 *  HSSFWorkbook to handle. This includes Excel 95, and very old 
 *  (pre-OLE2) Excel files, such as Excel 4 files.
 * <p>
 * Returns much (but not all) of the textual content of the file, 
 *  suitable for indexing by something like Apache Lucene, or used
 *  by Apache Tika, but not really intended for display to the user.
 * </p>
 */
public class OldExcelExtractor implements Closeable {
    private RecordInputStream ris;
    private Closeable input;
    private int biffVersion;
    private int fileType;

    public OldExcelExtractor(InputStream input) throws IOException {
        BufferedInputStream bstream = new BufferedInputStream(input, 8);
        if (NPOIFSFileSystem.hasPOIFSHeader(bstream)) {
            open(new NPOIFSFileSystem(bstream));
        } else {
            open(bstream);
        }
    }

    public OldExcelExtractor(File f) throws IOException {
        try {
            open(new NPOIFSFileSystem(f));
        } catch (OldExcelFormatException oe) {
            open(new FileInputStream(f));
        } catch (NotOLE2FileException e) {
            open(new FileInputStream(f));
        }
    }

    public OldExcelExtractor(NPOIFSFileSystem fs) throws IOException {
        open(fs);
    }

    public OldExcelExtractor(DirectoryNode directory) throws IOException {
        open(directory);
    }

    private void open(InputStream biffStream) {
        input = biffStream;
        ris = new RecordInputStream(biffStream);
        prepare();
    }

    private void open(NPOIFSFileSystem fs) throws IOException {
        input = fs;
        open(fs.getRoot());
    }

    private void open(DirectoryNode directory) throws IOException {
        DocumentNode book;
        try {
            book = (DocumentNode)directory.getEntry(OLD_WORKBOOK_DIR_ENTRY_NAME);
        } catch (FileNotFoundException e) {
            // some files have "Workbook" instead
            book = (DocumentNode)directory.getEntry(WORKBOOK_DIR_ENTRY_NAMES[0]);
        }

        if (book == null) {
            throw new IOException("No Excel 5/95 Book stream found");
        }
        
        ris = new RecordInputStream(directory.createDocumentInputStream(book));
        prepare();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Use:");
            System.err.println("   OldExcelExtractor <filename>");
            System.exit(1);
        }
        OldExcelExtractor extractor = new OldExcelExtractor(new File(args[0]));
        System.out.println(extractor.getText());
        extractor.close();
    }
    
    private void prepare() {
        if (! ris.hasNextRecord())
            throw new IllegalArgumentException("File contains no records!"); 
        ris.nextRecord();
        
        // Work out what version we're dealing with
        int bofSid = ris.getSid();
        switch (bofSid) {
            case BOFRecord.biff2_sid:
                biffVersion = 2;
                break;
            case BOFRecord.biff3_sid:
                biffVersion = 3;
                break;
            case BOFRecord.biff4_sid:
                biffVersion = 4;
                break;
            case BOFRecord.biff5_sid:
                biffVersion = 5;
                break;
            default:
                throw new IllegalArgumentException("File does not begin with a BOF, found sid of " + bofSid); 
        }
        
        // Get the type
        BOFRecord bof = new BOFRecord(ris);
        fileType = bof.getType();
    }

    /**
     * The Biff version, largely corresponding to the Excel version
     */
    public int getBiffVersion() {
        return biffVersion;
    }
    /**
     * The kind of the file, one of {@link BOFRecord#TYPE_WORKSHEET},
     *  {@link BOFRecord#TYPE_CHART}, {@link BOFRecord#TYPE_EXCEL_4_MACRO}
     *  or {@link BOFRecord#TYPE_WORKSPACE_FILE}
     */
    public int getFileType() {
        return fileType;
    }

    /**
     * Retrieves the text contents of the file, as best we can
     *  for these old file formats
     */
    public String getText() {
        StringBuffer text = new StringBuffer();
        
        // To track formats and encodings
        CodepageRecord codepage = null;
        // TODO track the XFs and Format Strings

        // Process each record in turn, looking for interesting ones
        while (ris.hasNextRecord()) {
            int sid = ris.getNextSid();
            ris.nextRecord();

            switch (sid) {
                // Biff 5+ only, no sheet names in older formats
                case OldSheetRecord.sid:
                    OldSheetRecord shr = new OldSheetRecord(ris);
                    shr.setCodePage(codepage);
                    text.append("Sheet: ");
                    text.append(shr.getSheetname());
                    text.append('\n');
                    break;
            
                case OldLabelRecord.biff2_sid:
                case OldLabelRecord.biff345_sid:
                    OldLabelRecord lr = new OldLabelRecord(ris);
                    lr.setCodePage(codepage);
                    text.append(lr.getValue());
                    text.append('\n');
                    break;
                case OldStringRecord.biff2_sid:
                case OldStringRecord.biff345_sid:
                    OldStringRecord sr = new OldStringRecord(ris);
                    sr.setCodePage(codepage);
                    text.append(sr.getString());
                    text.append('\n');
                    break;
                    
                case NumberRecord.sid:
                    NumberRecord nr = new NumberRecord(ris);
                    handleNumericCell(text, nr.getValue());
                    break;
                case OldFormulaRecord.biff2_sid:
                case OldFormulaRecord.biff3_sid:
                case OldFormulaRecord.biff4_sid:
                    // Biff 2 and 5+ share the same SID, due to a bug...
                    if (biffVersion == 5) {
                        FormulaRecord fr = new FormulaRecord(ris);
                        if (fr.getCachedResultType() == Cell.CELL_TYPE_NUMERIC) {
                            handleNumericCell(text, fr.getValue());
                        }
                    } else {
                        OldFormulaRecord fr = new OldFormulaRecord(ris);
                        if (fr.getCachedResultType() == Cell.CELL_TYPE_NUMERIC) {
                            handleNumericCell(text, fr.getValue());
                        }
                    }
                    break;
                case RKRecord.sid:
                    RKRecord rr = new RKRecord(ris);
                    handleNumericCell(text, rr.getRKNumber());
                    break;
                    
                case CodepageRecord.sid:
                    codepage = new CodepageRecord(ris);
                    break;
                    
                default:
                    ris.readFully(new byte[ris.remaining()]);
            }
        }

        close();
        ris = null;

        return text.toString();
    }

    @Override
    public void close() {
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {}
            input = null;
        }
    }
    
    protected void handleNumericCell(StringBuffer text, double value) {
        // TODO Need to fetch / use format strings
        text.append(value);
        text.append('\n');
    }
}
