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

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.OldFormulaRecord;
import org.apache.poi.hssf.record.OldLabelRecord;
import org.apache.poi.hssf.record.OldStringRecord;
import org.apache.poi.hssf.record.RKRecord;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
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
public class OldExcelExtractor {
    private RecordInputStream ris;
    private Closeable input;

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
        } catch (IOException e) {
            if (e.getMessage().startsWith("Invalid header signature")) {
                open(new FileInputStream(f));
            } else {
                throw e;
            }
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
    }
    private void open(NPOIFSFileSystem fs) throws IOException {
        input = fs;
        open(fs.getRoot());
    }
    private void open(DirectoryNode directory) throws IOException {
        DocumentNode book = (DocumentNode)directory.getEntry("Book");
        if (book == null) {
            throw new IOException("No Excel 5/95 Book stream found");
        }
        
        ris = new RecordInputStream(directory.createDocumentInputStream(book));
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Use:");
            System.err.println("   OldExcelExtractor <filename>");
            System.exit(1);
        }
        OldExcelExtractor extractor = new OldExcelExtractor(new File(args[0]));
        System.out.println(extractor.getText());
    }

    /**
     * Retrieves the text contents of the file, as best we can
     *  for these old file formats
     */
    public String getText() {
        StringBuffer text = new StringBuffer();

        while (ris.hasNextRecord()) {
            int sid = ris.getNextSid();
            ris.nextRecord();

            switch (sid) {
                // label - 5.63 - TODO Needs codepages
                case OldLabelRecord.biff2_sid:
                case OldLabelRecord.biff345_sid:
                    OldLabelRecord lr = new OldLabelRecord(ris);
                    text.append(lr.getValue());
                    text.append('\n');
                    break;
                // string - 5.102 - TODO Needs codepages
                case OldStringRecord.biff2_sid:
                case OldStringRecord.biff345_sid:
                    OldStringRecord sr = new OldStringRecord(ris);
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
                    OldFormulaRecord fr = new OldFormulaRecord(ris);
                    if (fr.getCachedResultType() == Cell.CELL_TYPE_NUMERIC) {
                        handleNumericCell(text, fr.getValue());
                    }
                    break;
                case RKRecord.sid:
                    RKRecord rr = new RKRecord(ris);
                    handleNumericCell(text, rr.getRKNumber());
                    break;
                    
                default:
                    ris.readFully(new byte[ris.remaining()]);
            }
        }
        
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {}
            input = null;
        }
        ris = null;

        return text.toString();
    }
    
    protected void handleNumericCell(StringBuffer text, double value) {
        // TODO Need to fetch / use format strings
        text.append(value);
        text.append('\n');
    }
}
