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

import org.apache.poi.EncryptedDocumentException;
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
import org.apache.poi.poifs.filesystem.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.util.IOUtils;

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

    private final static int FILE_PASS_RECORD_SID = 0x2f;
    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;


    private RecordInputStream ris;

    // sometimes we hold the stream here and thus need to ensure it is closed at some point
    private Closeable toClose;

    private int biffVersion;
    private int fileType;

    public OldExcelExtractor(InputStream input) throws IOException {
        open(input);
    }

    public OldExcelExtractor(File f) throws IOException {
        POIFSFileSystem poifs = null;
        try {
            poifs = new POIFSFileSystem(f);
            open(poifs);
            toClose = poifs;
            return;
        } catch (OldExcelFormatException | NotOLE2FileException e) {
            // will be handled by workaround below
        } finally {
            if (toClose == null) {
                IOUtils.closeQuietly(poifs);
            }
        }

        @SuppressWarnings("resource")
        FileInputStream biffStream = new FileInputStream(f); // NOSONAR
        try {
            open(biffStream);
        } catch (IOException | RuntimeException e)  {
            // ensure that the stream is properly closed here if an Exception
            // is thrown while opening
            biffStream.close();
            throw e;
        }
    }

    public OldExcelExtractor(POIFSFileSystem fs) throws IOException {
        open(fs);
    }

    public OldExcelExtractor(DirectoryNode directory) throws IOException {
        open(directory);
    }

    private void open(InputStream biffStream) throws IOException {
        BufferedInputStream bis = (biffStream instanceof BufferedInputStream) 
            ? (BufferedInputStream)biffStream
            : new BufferedInputStream(biffStream, 8);

        if (FileMagic.valueOf(bis) == FileMagic.OLE2) {
            POIFSFileSystem poifs = new POIFSFileSystem(bis);
            try {
                open(poifs);
                toClose = poifs; // Fixed by GR, we should not close it here
            } finally {
                if (toClose == null) {
                    poifs.close();
                }
            }
        } else {
            ris = new RecordInputStream(bis);
            toClose = bis;
            prepare();
        }
    }

    private void open(POIFSFileSystem fs) throws IOException {
        open(fs.getRoot());
    }

    private void open(DirectoryNode directory) throws IOException {
        DocumentNode book;
        try {
            book = (DocumentNode)directory.getEntry(OLD_WORKBOOK_DIR_ENTRY_NAME);
        } catch (FileNotFoundException | IllegalArgumentException e) {
            // some files have "Workbook" instead
            book = (DocumentNode)directory.getEntry(WORKBOOK_DIR_ENTRY_NAMES[0]);
        }

        if (book == null) {
            throw new IOException("No Excel 5/95 Book stream found");
        }
        
        ris = new RecordInputStream(directory.createDocumentInputStream(book));
        prepare();
    }

    public static void main(String[] args) throws IOException {
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
        if (! ris.hasNextRecord()) {
            throw new IllegalArgumentException("File contains no records!");
        } 
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
     * 
     * @return the Biff version
     */
    public int getBiffVersion() {
        return biffVersion;
    }
    
    /**
     * The kind of the file, one of {@link BOFRecord#TYPE_WORKSHEET},
     *  {@link BOFRecord#TYPE_CHART}, {@link BOFRecord#TYPE_EXCEL_4_MACRO}
     *  or {@link BOFRecord#TYPE_WORKSPACE_FILE}
     * 
     * @return the file type
     */
    public int getFileType() {
        return fileType;
    }

    /**
     * Retrieves the text contents of the file, as best we can
     *  for these old file formats
     * 
     * @return the text contents of the file
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
                case  FILE_PASS_RECORD_SID:
                    throw new EncryptedDocumentException("Encryption not supported for Old Excel files");

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
                        if (fr.getCachedResultType() == CellType.NUMERIC.getCode()) {
                            handleNumericCell(text, fr.getValue());
                        }
                    } else {
                        OldFormulaRecord fr = new OldFormulaRecord(ris);
                        if (fr.getCachedResultType() == CellType.NUMERIC.getCode()) {
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
                    ris.readFully(IOUtils.safelyAllocate(ris.remaining(), MAX_RECORD_LENGTH));
            }
        }

        close();
        ris = null;

        return text.toString();
    }

    @Override
    public void close() {
        // some cases require this close here
        if(toClose != null) {
            IOUtils.closeQuietly(toClose);
            toClose = null;
        }
    }

    protected void handleNumericCell(StringBuffer text, double value) {
        // TODO Need to fetch / use format strings
        text.append(value);
        text.append('\n');
    }
}
