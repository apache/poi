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

package org.apache.poi.hssf.dev;

import static org.apache.logging.log4j.util.Unbox.box;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.logging.log4j.Logger;
import org.apache.poi.logging.PoiLogManager;
import org.apache.poi.hssf.dev.BiffDumpingStream.IBiffRecordListener;
import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.hssf.record.HSSFRecordTypes;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.RecordInputStream.LeftoverDataException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.RecordFormatException;
import org.apache.poi.util.StringUtil;

/**
 *  Utility for reading in BIFF8 records and displaying data from them.
 */
public final class BiffViewer {
    private static final char[] NEW_LINE_CHARS = System.getProperty("line.separator").toCharArray();
    private static final Logger LOG = PoiLogManager.getLogger(BiffViewer.class);
    private static final String ESCHER_SERIALIZE = "poi.deserialize.escher";
    private static final int DUMP_LINE_LEN = 16;
    private static final char[] COLUMN_SEPARATOR = " | ".toCharArray();

    private boolean biffHex;
    private boolean interpretRecords = true;
    private boolean rawHexOnly;
    private boolean noHeader = true;
    private boolean zeroAlignRecord = true;
    private final List<String> _headers = new ArrayList<>();


    /**
     * show hex dump of each BIFF record
     */
    public void setDumpBiffHex(boolean biffhex) {
        this.biffHex = biffhex;
    }

    /**
     * output interpretation of BIFF records
     */
    public void setInterpretRecords(boolean interpretRecords) {
        this.interpretRecords = interpretRecords;
    }

    /**
     * output raw hex dump of whole workbook stream
     */
    public void setOutputRawHexOnly(boolean rawhex) {
        this.rawHexOnly = rawhex;
    }

    /**
     * do not print record header - default is on
     */
    public void setSuppressHeader(boolean noHeader) {
        this.noHeader = noHeader;
    }

    /**
     * turn on deserialization of escher records (default is off)
     */
    public void setSerializeEscher(boolean serialize) {
        if (serialize) {
            System.setProperty(ESCHER_SERIALIZE, "true");
        } else {
            System.clearProperty(ESCHER_SERIALIZE);
        }
    }

    public void setZeroAlignRecord(boolean zeroAlignRecord) {
        this.zeroAlignRecord = zeroAlignRecord;
    }

    public void parse(File file) throws IOException {
        parse(file, System.out);
    }

    public void parse(File file, OutputStream os) throws IOException {
        try (POIFSFileSystem fs = new POIFSFileSystem(file, true);
             InputStream is = getPOIFSInputStream(fs);
             PrintWriter pw = wrap(os)
        ) {
            if (rawHexOnly) {
                byte[] data = IOUtils.toByteArray(is);
                HexDump.dump(data, 0, System.out, 0);
            } else {
                IBiffRecordListener recListener = (globalOffset, recordCounter, sid, dataSize, data) -> {
                    String header = formatRecordDetails(globalOffset, sid, dataSize, recordCounter);
                    if (!noHeader) {
                        _headers.add(header);
                    }
                    if (biffHex) {
                        pw.write(header);
                        pw.write(NEW_LINE_CHARS);
                        hexDumpAligned(pw, data, dataSize+4, globalOffset);
                        pw.flush();
                    }
                };

                try (InputStream is2 = new BiffDumpingStream(is, recListener)) {
                    createRecords(is2, pw);
                }
            }
        }
    }

    private static String formatRecordDetails(int globalOffset, int sid, int size, int recordCounter) {
        return "Offset=" + HexDump.intToHex(globalOffset) + "(" + globalOffset + ")" +
            " recno=" + recordCounter +
            " sid=" + HexDump.shortToHex(sid) +
            " size=" + HexDump.shortToHex(size) + "(" + size + ")";
    }

    /**
     *  Create an array of records from an input stream
     *
     * @param is the InputStream from which the records will be obtained
     * @param ps the PrintWriter to output the record data
     *
     * @throws  RecordFormatException  on error processing the InputStream
     */
    private void createRecords(InputStream is, PrintWriter ps) throws RecordFormatException {
        RecordInputStream recStream = new RecordInputStream(is);
        while (true) {
            _headers.clear();
            boolean hasNext;
            try {
                hasNext = recStream.hasNextRecord();
            } catch (LeftoverDataException e) {
                LOG.atError().withThrowable(e).log("Discarding {} bytes and continuing", box(recStream.remaining()));
                recStream.readRemainder();
                hasNext = recStream.hasNextRecord();
            }
            if (!hasNext) {
                break;
            }
            recStream.nextRecord();
            if (recStream.getSid() == 0) {
                continue;
            }
            Record record;
            if (interpretRecords) {
                record = HSSFRecordTypes.forSID(recStream.getSid()).getRecordConstructor().apply(recStream);
                if (record.getSid() == ContinueRecord.sid) {
                    continue;
                }

                _headers.forEach(ps::println);
                ps.print(record);
            } else {
                recStream.readRemainder();
            }
            ps.println();
        }
    }

    private static PrintWriter wrap(OutputStream os) {
        final OutputStream osOut;
        final Charset cs;

        if (os == null) {
            cs = Charset.defaultCharset();
            osOut = NullOutputStream.INSTANCE;
        } else if (os == System.out) {
            // Use the system default encoding when sending to System Out
            cs = Charset.defaultCharset();
            osOut = CloseShieldOutputStream.wrap(System.out);
        } else {
            cs = StringUtil.UTF8;
            osOut = os;
        }
        return new PrintWriter(new OutputStreamWriter(osOut, cs));
    }


    static InputStream getPOIFSInputStream(POIFSFileSystem fs) throws IOException {
        String workbookName = HSSFWorkbook.getWorkbookDirEntryName(fs.getRoot());
        return fs.createDocumentInputStream(workbookName);
    }


    /**
     * Hex-dumps a portion of a byte array in typical format, also preserving dump-line alignment
     * @param globalOffset (somewhat arbitrary) used to calculate the addresses printed at the
     * start of each line
     */
    private void hexDumpAligned(Writer w, byte[] data, int dumpLen, int globalOffset) {
        int baseDataOffset = 0;

        // perhaps this code should be moved to HexDump
        int globalStart = globalOffset + baseDataOffset;
        int globalEnd = globalOffset + baseDataOffset + dumpLen;
        int startDelta = globalStart % DUMP_LINE_LEN;
        int endDelta = globalEnd % DUMP_LINE_LEN;
        if (zeroAlignRecord) {
            endDelta -= startDelta;
            if (endDelta < 0) {
                endDelta += DUMP_LINE_LEN;
            }
            startDelta = 0;
        }
        int startLineAddr;
        int endLineAddr;
        if (zeroAlignRecord) {
            endLineAddr = globalEnd - endDelta - (globalStart - startDelta);
            startLineAddr = 0;
        } else {
            startLineAddr = globalStart - startDelta;
            endLineAddr = globalEnd - endDelta;
        }

        int lineDataOffset = baseDataOffset - startDelta;
        int lineAddr = startLineAddr;

        // output (possibly incomplete) first line
        if (startLineAddr == endLineAddr) {
            hexDumpLine(w, data, lineAddr, lineDataOffset, startDelta, endDelta);
            return;
        }
        hexDumpLine(w, data, lineAddr, lineDataOffset, startDelta, DUMP_LINE_LEN);

        // output all full lines in the middle
        while (true) {
            lineAddr += DUMP_LINE_LEN;
            lineDataOffset += DUMP_LINE_LEN;
            if (lineAddr >= endLineAddr) {
                break;
            }
            hexDumpLine(w, data, lineAddr, lineDataOffset, 0, DUMP_LINE_LEN);
        }


        // output (possibly incomplete) last line
        if (endDelta != 0) {
            hexDumpLine(w, data, lineAddr, lineDataOffset, 0, endDelta);
        }
    }

    private static void hexDumpLine(Writer w, byte[] data, int lineStartAddress, int lineDataOffset, int startDelta, int endDelta) {
        final char[] buf = new char[8+2*COLUMN_SEPARATOR.length+DUMP_LINE_LEN*3-1+DUMP_LINE_LEN+NEW_LINE_CHARS.length];

        if (startDelta >= endDelta) {
            throw new IllegalArgumentException("Bad start/end delta");
        }
        int idx=0;
        try {
            writeHex(buf, idx, lineStartAddress, 8);
            idx = arraycopy(COLUMN_SEPARATOR, buf, idx+8);
            // raw hex data
            for (int i=0; i< DUMP_LINE_LEN; i++) {
                if (i>0) {
                    buf[idx++] = ' ';
                }
                if (i >= startDelta && i < endDelta) {
                    writeHex(buf, idx, data[lineDataOffset+i], 2);
                } else {
                    buf[idx] = ' ';
                    buf[idx+1] = ' ';
                }
                idx += 2;
            }
            idx = arraycopy(COLUMN_SEPARATOR, buf, idx);

            // interpreted ascii
            for (int i=0; i< DUMP_LINE_LEN; i++) {
                char ch = ' ';
                if (i >= startDelta && i < endDelta) {
                    ch = getPrintableChar(data[lineDataOffset+i]);
                }
                buf[idx++] = ch;
            }

            idx = arraycopy(NEW_LINE_CHARS, buf, idx);

            w.write(buf, 0, idx);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static int arraycopy(char[] in, char[] out, int pos) {
        int idx = pos;
        for (char c : in) {
            out[idx++] = c;
        }
        return idx;
    }

    private static char getPrintableChar(byte b) {
        char ib = (char) (b & 0x00FF);
        if (ib < 32 || ib > 126) {
            return '.';
        }
        return ib;
    }

    private static void writeHex(char[] buf, int startInBuf, int value, int nDigits) {
        int acc = value;
        for(int i=nDigits-1; i>=0; i--) {
            int digit = acc & 0x0F;
            buf[startInBuf+i] = (char) (digit < 10 ? ('0' + digit) : ('A' + digit - 10));
            acc >>>= 4;
        }
    }
}
