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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.RecordFormatException;
import org.apache.poi.util.StringUtil;

/**
 * Title: Write Access Record (0x005C)<p>
 *
 * Description: Stores the username of that who owns the spreadsheet generator (on unix the user's
 * login, on Windoze its the name you typed when you installed the thing)
 */
public final class WriteAccessRecord extends StandardRecord {
    public static final short sid = 0x005C;

    private static final BitField UTF16FLAG = BitFieldFactory.getInstance(1);

    private static final byte PAD_CHAR = (byte) ' ';
    private static final int DATA_SIZE = 112;
    private static final int STRING_SIZE = DATA_SIZE - 3;

    /** this record is always padded to a constant length */
    private static final byte[] PADDING = new byte[STRING_SIZE];
    static {
        Arrays.fill(PADDING, PAD_CHAR);
    }

    private String field_1_username;


    public WriteAccessRecord() {
        setUsername("");
    }

    public WriteAccessRecord(WriteAccessRecord other) {
        super(other);
        field_1_username = other.field_1_username;
    }

    public WriteAccessRecord(RecordInputStream in) {
        if (in.remaining() > DATA_SIZE) {
            throw new RecordFormatException("Expected data size (" + DATA_SIZE + ") but got (" + in.remaining() + ")");
        }

        // The string is always 109 characters (padded with spaces), therefore
        // this record can not be continued.

        int nChars = in.readUShort();
        int is16BitFlag = in.readUByte();
        final byte[] data;
        final Charset charset;
        final int byteCnt;
        if (nChars > STRING_SIZE || (is16BitFlag & 0xFE) != 0) {
            // something is wrong - reconstruct data
            if (in.isEncrypted()) {
                // WPS Office seems to generate files with this record unencrypted (#66115)
                // Libre Office/Excel can read those, but Excel will convert those back to encrypted
                data = IOUtils.safelyAllocate(in.remaining(), STRING_SIZE);
                in.readPlain(data, 0, data.length);
                int i = data.length;
                // PAD_CHAR is filled for every byte even for UTF16 strings
                while (i>0 && data[i-1] == PAD_CHAR) {
                    i--;
                }
                byteCnt = i;
                // poor mans utf16 detection ...
                charset = (data.length > 1 && data[1] == 0) ? StandardCharsets.UTF_16LE : StandardCharsets.ISO_8859_1;
            } else {
                // String header looks wrong (probably missing)
                // OOO doc says this is optional anyway.
                byteCnt = 3 + in.remaining();
                data = IOUtils.safelyAllocate(byteCnt, DATA_SIZE);
                LittleEndian.putUShort(data, 0, nChars);
                LittleEndian.putByte(data, 2, is16BitFlag);
                in.readFully(data, 3, byteCnt-3);
                charset = StandardCharsets.UTF_8;
            }
        } else {
            // the normal case ...
            data = IOUtils.safelyAllocate(in.remaining(), STRING_SIZE);
            in.readFully(data);
            if (UTF16FLAG.isSet(is16BitFlag)) {
                byteCnt = Math.min(nChars * 2, data.length);
                charset = StandardCharsets.UTF_16LE;
            } else {
                byteCnt = Math.min(nChars, data.length);
                charset = StandardCharsets.ISO_8859_1;
            }
        }

        String rawValue = new String(data, 0, byteCnt, charset);
        setUsername(rawValue.trim());
    }

    /**
     * set the username for the user that created the report. HSSF uses the
     * logged in user.
     *
     * @param username of the user who is logged in (probably "tomcat" or "apache")
     */
    public void setUsername(String username) {
        boolean is16bit = StringUtil.hasMultibyte(username);
        int encodedByteCount = username.length() * (is16bit ? 2 : 1);
        if (encodedByteCount > STRING_SIZE) {
            throw new IllegalArgumentException("Name is too long: " + username);
        }

        field_1_username = username;
    }

    /**
     * get the username for the user that created the report. HSSF uses the
     * logged in user. On natively created M$ Excel sheet this would be the name
     * you typed in when you installed it in most cases.
     *
     * @return username of the user who is logged in (probably "tomcat" or "apache")
     */
    public String getUsername() {
        return field_1_username;
    }

    @Override
    public void serialize(LittleEndianOutput out) {
        String username = getUsername();
        boolean is16bit = StringUtil.hasMultibyte(username);

        out.writeShort(username.length());
        out.writeByte(is16bit ? 0x01 : 0x00);

        byte[] buf = PADDING.clone();
        if (is16bit) {
            StringUtil.putUnicodeLE(username, buf, 0);
        } else {
            StringUtil.putCompressedUnicode(username, buf, 0);
        }
        out.write(buf);
    }

    @Override
    protected int getDataSize() {
        return DATA_SIZE;
    }

    @Override
    public short getSid() {
        return sid;
    }

    @Override
    public WriteAccessRecord copy() {
        return new WriteAccessRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.WRITE_ACCESS;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("username", this::getUsername);
    }
}
