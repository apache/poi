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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * Title:        FileSharing<P>
 * Description:  stores the encrypted readonly for a workbook (write protect) 
 * REFERENCE:  PG 314 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */

public class FileSharingRecord extends Record {
    public final static short sid = 0x5b;
    private short             field_1_readonly;
    private short             field_2_password;
    private byte              field_3_username_length;
    private short             field_4_unknown; // not documented
    private String            field_5_username;

    public FileSharingRecord() {}
    

    /**
     * Constructs a FileSharing record and sets its fields appropriately.
     * @param in the RecordInputstream to read the record from
     */

    public FileSharingRecord(RecordInputStream in) {
        super(in);
    }

    protected void validateSid(short id) {
        if (id != sid) {
            throw new RecordFormatException("NOT A FILESHARING RECORD");
        }
    }

    protected void fillFields(RecordInputStream in) {
        field_1_readonly = in.readShort();
        field_2_password = in.readShort();
        field_3_username_length = in.readByte();
        field_4_unknown = in.readShort();
        field_5_username = in.readCompressedUnicode(field_3_username_length);
    }

    //this is the world's lamest "security".  thanks to Wouter van Vugt for making me
    //not have to try real hard.  -ACO
    public static short hashPassword(String password) {
        byte[] passwordCharacters = password.getBytes();
        int hash = 0;
        if (passwordCharacters.length > 0) {
            int charIndex = passwordCharacters.length;
            while (charIndex-- > 0) {
                hash = ((hash >> 14) & 0x01) | ((hash << 1) & 0x7fff);
                hash ^= passwordCharacters[charIndex];
            }
            // also hash with charcount
            hash = ((hash >> 14) & 0x01) | ((hash << 1) & 0x7fff);
            hash ^= passwordCharacters.length;
            hash ^= (0x8000 | ('N' << 8) | 'K');
        }
        return (short)hash;
    } 

    /**
     * set the readonly flag
     *
     * @param readonly 1 for true, not 1 for false
     */
    public void setReadOnly(short readonly) {
        field_1_readonly = readonly;
    }

    /**
     * get the readonly
     *
     * @return short  representing if this is read only (1 = true)
     */
    public short getReadOnly() {
        return field_1_readonly;
    }

    /**
     * @param hashed password
     */
    public void setPassword(short password) {
        field_2_password = password;
    }

    /**
     * @returns password hashed with hashPassword() (very lame)
     */
    public short getPassword() {
        return field_2_password;
    }

    /**
     * @returns byte representing the length of the username field
     */
    public byte getUsernameLength() {
        return field_3_username_length ;
    }

    /**
     * @param byte representing the length of the username field
     */
    public void setUsernameLength(byte length) {
        this.field_3_username_length = length;
    }

    /**
     * @returns username of the user that created the file
     */
    public String getUsername() {
        return this.field_5_username;
    }

    /**
     * @param username of the user that created the file
     */
    public void setUsername(String username) {
        this.field_5_username = username;
        this.field_3_username_length = (byte)username.length();
    }

    /**
     * @return short value of a "bonus field" in Excel that was not doc'd
     */
    public short getUnknown() {
        return field_4_unknown;
    }

    /**
     * @param unknown field value to set (bonus field that is not doc'd)
     */
    public void setUnknown(short unk) {
        field_4_unknown = unk;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[FILESHARING]\n");
        buffer.append("    .readonly       = ")
            .append(getReadOnly() == 1 ? "true" : "false").append("\n");
        buffer.append("    .password       = ")
            .append(Integer.toHexString(getPassword())).append("\n");
        buffer.append("    .userlen        = ")
            .append(Integer.toHexString(getUsernameLength())).append("\n");
        buffer.append("    .unknown        = ")
            .append(Integer.toHexString(getUnknown())).append("\n");
        buffer.append("    .username       = ")
            .append(getUsername()).append("\n");
        buffer.append("[/FILESHARING]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data) {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize()-4));
        LittleEndian.putShort(data, 4 + offset, getReadOnly());
        LittleEndian.putShort(data, 6 + offset, getPassword());
        data[ 8 + offset ] =  getUsernameLength();
        LittleEndian.putShort(data, 9 + offset, getUnknown());
        StringUtil.putCompressedUnicode( getUsername(), data, 11 + offset );
        return getRecordSize();
    }

    public int getRecordSize() {
        return 11+getUsernameLength();
    }

    public short getSid() {
        return sid;
    }

    /**
     * Clone this record.
     */
    public Object clone() {
      FileSharingRecord clone = new FileSharingRecord();
      clone.setReadOnly(field_1_readonly);
      clone.setPassword(field_2_password);
      clone.setUsernameLength(field_3_username_length);
      clone.setUnknown(field_4_unknown);
      clone.setUsername(field_5_username);
      return clone;
    }

}
