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

package org.apache.poi.hssf.record;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.StringUtil;

import static org.apache.logging.log4j.util.Unbox.box;

/**
 * Describes a number format -- those goofy strings like $(#,###)
 */
public final class FormatRecord extends StandardRecord {

    private static final Logger LOG = LogManager.getLogger(FormatRecord.class);

    public static final short sid = 0x041E;

    private final int field_1_index_code;
    private final boolean field_3_hasMultibyte;
    private final String field_4_formatstring;

    private FormatRecord(FormatRecord other) {
        super(other);
        field_1_index_code = other.field_1_index_code;
        field_3_hasMultibyte = other.field_3_hasMultibyte;
        field_4_formatstring = other.field_4_formatstring;
    }

    public FormatRecord(int indexCode, String fs) {
        field_1_index_code = indexCode;
        field_4_formatstring = fs;
        field_3_hasMultibyte = StringUtil.hasMultibyte(fs);
    }

    public FormatRecord(RecordInputStream in) {
        field_1_index_code = in.readShort();
        int field_3_unicode_len = in.readUShort();
        field_3_hasMultibyte = (in.readByte() & 0x01) != 0;

        if (field_3_hasMultibyte) {
            field_4_formatstring = readStringCommon(in, field_3_unicode_len, false);
        } else {
            field_4_formatstring = readStringCommon(in, field_3_unicode_len, true);
        }
    }

    /**
     * get the format index code (for built in formats)
     *
     * @return the format index code
     * @see InternalWorkbook
     */
    public int getIndexCode() {
        return field_1_index_code;
    }

    /**
     * get the format string
     *
     * @return the format string
     */
    public String getFormatString() {
        return field_4_formatstring;
    }

    public void serialize(LittleEndianOutput out) {
        String formatString = getFormatString();
        out.writeShort(getIndexCode());
        out.writeShort(formatString.length());
        out.writeByte(field_3_hasMultibyte ? 0x01 : 0x00);

      if ( field_3_hasMultibyte ) {
          StringUtil.putUnicodeLE( formatString, out);
      }  else {
          StringUtil.putCompressedUnicode( formatString, out);
      }
    }
    protected int getDataSize() {
        return 5 // 2 shorts + 1 byte
            + getFormatString().length() * (field_3_hasMultibyte ? 2 : 1);
    }

    public short getSid() {
        return sid;
    }

    @Override
    public FormatRecord copy() {
        return new FormatRecord(this);
    }

    private static String readStringCommon(RecordInputStream ris, int requestedLength, boolean pIsCompressedEncoding) {
        //custom copy of ris.readUnicodeLEString to allow for extra bytes at the end

        // Sanity check to detect garbage string lengths
        if (requestedLength < 0 || requestedLength > 0x100000) { // 16 million chars?
            throw new IllegalArgumentException("Bad requested string length (" + requestedLength + ")");
        }
        char[] buf;
        int availableChars = pIsCompressedEncoding ? ris.remaining() : ris.remaining() / LittleEndianConsts.SHORT_SIZE;
        //everything worked out.  Great!
        if (requestedLength == availableChars) {
            buf = new char[requestedLength];
        } else {
            //sometimes in older Excel 97 .xls files,
            //the requested length is wrong.
            //Read all available characters.
            buf = new char[availableChars];
        }
        for (int i = 0; i < buf.length; i++) {
            char ch;
            if (pIsCompressedEncoding) {
                ch = (char) ris.readUByte();
            } else {
                ch = (char) ris.readShort();
            }
            buf[i] = ch;
        }

        //TIKA-2154's file shows that even in a unicode string
        //there can be a remaining byte (without proper final '00')
        //that should be read as a byte
        if (ris.available() == 1) {
            char[] tmp = Arrays.copyOf(buf, buf.length+1);
            tmp[buf.length] = (char)ris.readUByte();
            buf = tmp;
        }

        if (ris.available() > 0) {
            LOG.atInfo().log("FormatRecord has {} unexplained bytes. Silently skipping", box(ris.available()));
            //swallow what's left
            while (ris.available() > 0) {
                ris.readByte();
            }
        }
        return new String(buf);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.FORMAT;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "indexCode", this::getIndexCode,
            "unicode", () -> field_3_hasMultibyte,
            "formatString", this::getFormatString
        );
    }
}
