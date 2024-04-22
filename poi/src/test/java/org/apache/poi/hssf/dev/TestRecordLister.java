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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.output.NullWriter;
import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordFactory;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.RecordFormatException;

/**
 * This is a low-level debugging class, which simply prints out what records come in what order.
 * Most people will want to use {@link BiffViewer} or {@link TestEFBiffViewer}, but this can be handy when
 * trying to make sense of {@link ContinueRecord} special cases.
 * <p>
 * Output is of the form:
 * SID - Length - Type (if known)
 * byte0 byte1 byte2 byte3 .... byte(n-4) byte(n-3) byte(n-2) byte(n-1)
 */

class TestRecordLister extends BaseTestIteratingXLS {
    @Override
    protected Map<String, Class<? extends Throwable>> getExcludes() {
        Map<String, Class<? extends Throwable>> excludes = super.getExcludes();
        excludes.put("clusterfuzz-testcase-minimized-POIHSSFFuzzer-5786329142919168.xls", RecordFormatException.class);
        excludes.put("clusterfuzz-testcase-minimized-POIHSSFFuzzer-5889658057523200.xls", IndexOutOfBoundsException.class);
        excludes.put("clusterfuzz-testcase-minimized-POIHSSFFuzzer-5175219985448960.xls", RecordFormatException.class);
        excludes.put("clusterfuzz-testcase-minimized-POIHSSFFuzzer-6137883240824832.xls", RecordFormatException.class);
        excludes.put("clusterfuzz-testcase-minimized-POIHSSFFuzzer-6483562584932352.xls", RecordFormatException.class);
        excludes.put("clusterfuzz-testcase-minimized-POIHSSFFuzzer-5816431116615680.xls", RecordFormatException.class);

        return excludes;
    }

    @Override
    void runOneFile(File fileIn) throws IOException {
        // replace it with System.out if you like it more verbatim
        PrintWriter out = new PrintWriter(NullWriter.INSTANCE);

        try (POIFSFileSystem fs = new POIFSFileSystem(fileIn, true);
             InputStream din = BiffViewer.getPOIFSInputStream(fs)) {
            RecordInputStream rinp = new RecordInputStream(din);

            while (rinp.hasNextRecord()) {
                int sid = rinp.getNextSid();
                rinp.nextRecord();

                int size = rinp.available();
                Class<? extends Record> clz = RecordFactory.getRecordClass(sid);

                out.printf(Locale.ROOT, "%1$#06x (%1$04d) - %2$#05x (%2$03d) bytes", sid, size);

                if (clz != null) {
                    out.print("  \t");
                    out.print(clz.getSimpleName());
                }
                out.println();

                byte[] data = rinp.readRemainder();
                if (data.length > 0) {
                    out.print("   ");
                    out.println(formatData(data));
                }
            }
        }
    }

    /*
    private static String formatSID(int sid) {
        String hex = Integer.toHexString(sid);
        String dec = Integer.toString(sid);

        StringBuilder s = new StringBuilder();
        s.append("0x");
        for (int i = hex.length(); i < 4; i++) {
            s.append('0');
        }
        s.append(hex);

        s.append(" (");
        for (int i = dec.length(); i < 4; i++) {
            s.append('0');
        }
        s.append(dec);
        s.append(")");

        return s.toString();
    }

    private static String formatSize(int size) {
        String hex = Integer.toHexString(size);
        String dec = Integer.toString(size);

        final String MAX_DIGITS = "000";

        StringBuilder s = new StringBuilder();
        s.append(MAX_DIGITS, 0, Math.max(MAX_DIGITS.length()-hex.length(),0));
        s.append(hex);

        s.append(" (");
        s.append(MAX_DIGITS, 0, Math.max(MAX_DIGITS.length()-dec.length(),0));
        s.append(dec);
        s.append(')');

        return s.toString();
    }*/

    private static String formatData(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }

        StringBuilder s = new StringBuilder();

        // If possible, do first 4 and last 4 bytes
        final int MAX_BYTES = 9;
        int bLen = Math.min(data.length, MAX_BYTES);
        for (int i=0; i<bLen; i++) {
            if (i>0) {
                s.append(' ');
            }
            int b;
            if (i<MAX_BYTES/2) {
                b = data[i];
            } else if (i == MAX_BYTES/2 && data.length > MAX_BYTES) {
                s.append("...");
                continue;
            } else {
                b = data[data.length-(bLen-i)];
            }

            // byte to hex
            if (b < 0) {
                b += 256;
            }
            if (b < 16) {
                s.append('0');
            }
            s.append(Integer.toHexString(b));
        }

        return s.toString();
    }
}
