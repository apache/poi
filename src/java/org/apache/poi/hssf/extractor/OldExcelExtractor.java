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

import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.eventusermodel.old.OldHSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.old.OldHSSFListener;
import org.apache.poi.hssf.record.*;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.NotOLE2FileException;

import java.io.*;

/**
 * A text extractor for old Excel files, which are too old for
 *  HSSFWorkbook to handle. This includes Excel 95, and very old 
 *  (pre-OLE2) Excel files, such as Excel 4 files.
 * <p>
 * Returns much (but not all) of the textual content of the file, 
 *  suitable for indexing by something like Apache Lucene, or used
 *  by Apache Tika, but not really intended for display to the user.
 * </p>
 * @see OldHSSFEventFactory
 */
public class OldExcelExtractor {
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
        ris = OldHSSFEventFactory.getRecordInputStream(directory);
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
    }
    
    private void prepare() {
        OldHSSFEventFactory.BOFEntry bofEntry = OldHSSFEventFactory.processBOFRecord(ris);
        this.biffVersion = bofEntry.getBiffVersion();
        // Get the type
        BOFRecord bof = bofEntry.getRecord();
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
        final StringBuilder text = new StringBuilder();
        OldHSSFEventFactory hssfEventFactory = new OldHSSFEventFactory();
        hssfEventFactory.process(ris, biffVersion, new TextCollectingListener(text));
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {}
            input = null;
        }
        ris = null;

        return text.toString();
    }

    private static final class TextCollectingListener implements OldHSSFListener {
        private final StringBuilder text;

        private TextCollectingListener(StringBuilder text) {
            this.text = text;
        }

        @Override
        public void onBOFRecord(BOFRecord record, int biffVersion) {

        }

        @Override
        public void onOldSheetRecord(OldSheetRecord shr) {
            text.append("Sheet: ");
            text.append(shr.getSheetname());
            text.append('\n');
        }

        @Override
        public void onOldLabelRecord(OldLabelRecord lr) {
            text.append(lr.getValue());
            text.append('\n');
        }

        @Override
        public void onOldStringRecord(OldStringRecord sr) {
            text.append(sr.getString());
            text.append('\n');
        }

        @Override
        public void onNumberRecord(NumberRecord nr) {
            handleNumericCell(nr.getValue());
        }

        @Override
        public void onFormulaRecord(FormulaRecord fr) {
            handleNumericCell(fr.getValue());
        }

        @Override
        public void onOldFormulaRecord(OldFormulaRecord fr) {
            handleNumericCell(fr.getValue());
        }

        @Override
        public void onRKRecord(RKRecord rr) {
            handleNumericCell(rr.getRKNumber());
        }

        @Override
        public void onMulRKRecord(MulRKRecord mrr) {
            final int columns = mrr.getNumColumns();
            for (int i = 0; i < columns; i++) {
                handleNumericCell(mrr.getRKNumberAt(i));

            }
        }

        @Override
        public void onBookEnd() {

        }

        private void handleNumericCell(double value) {
            // TODO Need to fetch / use format strings
            text.append(value);
            text.append('\n');
        }
    }
}
