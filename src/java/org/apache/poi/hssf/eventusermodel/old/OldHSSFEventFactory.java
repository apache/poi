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
package org.apache.poi.hssf.eventusermodel.old;

import org.apache.poi.hssf.record.*;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * Reads the old Excel files, which are too old for
 * HSSFWorkbook to handle. This includes Excel 95, and very old
 * (pre-OLE2) Excel files, such as Excel 4 files.
 * <p>
 * Returns much (but not all) records of the file
 * </p>
 * Content of this class was moved basically from {@link org.apache.poi.hssf.extractor.OldExcelExtractor}
 */
public class OldHSSFEventFactory {

    /**
     * Default instance to track formats and encodings
     */
    private final CodepageRecord defaultCodepage;

    public OldHSSFEventFactory() {
        this(null);
    }

    /**
     * @param defaultCodepage default instance to track formats and encodings
     *
     * @see org.apache.poi.util.CodePageUtil
     */
    public OldHSSFEventFactory(CodepageRecord defaultCodepage) {
        this.defaultCodepage = defaultCodepage;
    }

    public void process(InputStream inputStream, OldHSSFListener l) throws IOException {
        RecordInputStream ris = getRecordInputStream(inputStream);
        final int biffVersion;
        {
            BOFEntry bofEntry = processBOFRecord(ris);
            biffVersion = bofEntry.biffVersion;
            l.onBOFRecord(bofEntry.record, biffVersion);
        }
        process(ris, biffVersion, l);
    }

    public void process(RecordInputStream ris, int biffVersion, OldHSSFListener l) {
        CodepageRecord codepage = this.defaultCodepage;
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
                    l.onOldSheetRecord(shr);
                    break;

                case OldLabelRecord.biff2_sid:
                case OldLabelRecord.biff345_sid:
                    OldLabelRecord lr = new OldLabelRecord(ris);
                    lr.setCodePage(codepage);
                    l.onOldLabelRecord(lr);
                    break;
                case OldStringRecord.biff2_sid:
                case OldStringRecord.biff345_sid:
                    OldStringRecord sr = new OldStringRecord(ris);
                    sr.setCodePage(codepage);
                    l.onOldStringRecord(sr);
                    break;

                case NumberRecord.sid:
                    NumberRecord nr = new NumberRecord(ris);
                    l.onNumberRecord(nr);
                    break;
                case OldFormulaRecord.biff2_sid:
                case OldFormulaRecord.biff3_sid:
                case OldFormulaRecord.biff4_sid:
                    // Biff 2 and 5+ share the same SID, due to a bug...
                    if (biffVersion == 5) {
                        FormulaRecord fr = new FormulaRecord(ris);
                        if (fr.getCachedResultType() == Cell.CELL_TYPE_NUMERIC) {
                            l.onFormulaRecord(fr);
                        }
                    } else {
                        OldFormulaRecord fr = new OldFormulaRecord(ris);
                        if (fr.getCachedResultType() == Cell.CELL_TYPE_NUMERIC) {
                            l.onOldFormulaRecord(fr);
                        }
                    }
                    break;
                case RKRecord.sid:
                    RKRecord rr = new RKRecord(ris);
                    l.onRKRecord(rr);
                    break;
                case MulRKRecord.sid:
                    l.onMulRKRecord(new MulRKRecord(ris));
                    break;

                case CodepageRecord.sid:
                    codepage = new CodepageRecord(ris);
                    break;

                default:
                    ris.readFully(new byte[ris.remaining()]);
            }
        }
        l.onBookEnd();
    }

    public static BOFEntry processBOFRecord(RecordInputStream ris) {
        if (!ris.hasNextRecord()) {
            throw new IllegalArgumentException("File contains no records!");
        }
        ris.nextRecord();

        // Work out what version we're dealing with
        int biffVersion;
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
        return new BOFEntry(bof, biffVersion);
    }

    public static RecordInputStream getRecordInputStream(InputStream inputStream) throws IOException {
        if (!(inputStream instanceof PushbackInputStream) && !inputStream.markSupported()) {
            inputStream = new PushbackInputStream(inputStream, 8);
        }

        if (NPOIFSFileSystem.hasPOIFSHeader(inputStream)) {
            NPOIFSFileSystem fs = new NPOIFSFileSystem(inputStream);
            return getRecordInputStream(fs);
        } else {
            return new RecordInputStream(inputStream);
        }
    }

    public static RecordInputStream getRecordInputStream(NPOIFSFileSystem fs) throws IOException {
        return getRecordInputStream(fs.getRoot());
    }

    public static RecordInputStream getRecordInputStream(DirectoryNode rootNode) throws IOException {
        DocumentNode book = (DocumentNode) rootNode.getEntry("Book");
        return new RecordInputStream(rootNode.createDocumentInputStream(book));
    }


    public static final class BOFEntry {
        private final BOFRecord record;
        private final int biffVersion;

        private BOFEntry(BOFRecord record, int biffVersion) {
            this.record = record;
            this.biffVersion = biffVersion;
        }

        public BOFRecord getRecord() {
            return record;
        }

        public int getBiffVersion() {
            return biffVersion;
        }
    }
}
