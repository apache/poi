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

package org.apache.poi.xssf.binary;

import java.io.IOException;
import java.io.InputStream;

import com.zaxxer.sparsebits.SparseBitSet;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianInputStream;

/**
 * Experimental parser for Microsoft's ooxml xlsb format.
 * Not thread safe, obviously.  Need to create a new one
 * for each thread.
 *
 * @since 3.16-beta3
 */
@Internal
public abstract class XSSFBParser {

    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 1_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    private final LittleEndianInputStream is;
    private final SparseBitSet records;

    /**
     * @param length the max record length allowed for XSSFBParser
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for XSSFBParser
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    public XSSFBParser(InputStream is) {
        this.is = new LittleEndianInputStream(is);
        records = null;
    }

    /**
     *
     * @param is inputStream
     * @param bitSet call {@link #handleRecord(int, byte[])} only on those records in this bitSet
     */
    protected XSSFBParser(InputStream is, SparseBitSet bitSet) {
        this.is = new LittleEndianInputStream(is);
        records = bitSet;
    }

    public void parse() throws IOException {

        while (true) {
            int bInt = is.read();
            if (bInt == -1) {
                return;
            }
            readNext((byte) bInt);
        }
    }

    private void readNext(byte b1) throws IOException {
        int recordId = 0;

        //if highest bit == 1
        if ((b1 >> 7 & 1) == 1) {
            byte b2 = is.readByte();
            b1 &= ~(1<<7); //unset highest bit
            b2 &= ~(1<<7); //unset highest bit (if it exists?)
            recordId = ((int)b2 << 7)+(int)b1;
        } else {
            recordId = b1;
        }

        long recordLength = 0;
        int i = 0;
        boolean halt = false;
        while (i < 4 && ! halt) {
            byte b = is.readByte();
            halt = (b >> 7 & 1) == 0; //if highest bit !=1 then continue
            b &= ~(1<<7);
            recordLength += (long) (int)b << (i*7); //multiply by 128^i
            i++;

        }
        if (records == null || records.get(recordId)) {
            byte[] buff = IOUtils.safelyAllocate(recordLength, MAX_RECORD_LENGTH);
            is.readFully(buff);
            handleRecord(recordId, buff);
        } else {
            long length = IOUtils.skipFully(is, recordLength);
            if (length != recordLength) {
                throw new XSSFBParseException("End of file reached before expected.\t"+
                "Tried to skip "+recordLength + ", but only skipped "+length);
            }
        }
    }

    //It hurts, hurts, hurts to create a new byte array for every record.
    //However, on a large Excel spreadsheet, this parser was 1/3 faster than
    //the ooxml sax parser (5 seconds for xssfb and 7.5 seconds for xssf.
    //The code is far cleaner to have the parser read all
    //of the data rather than having every component promise that it will read
    //the correct amount.
    abstract public void handleRecord(int recordType, byte[] data) throws XSSFBParseException;

}
