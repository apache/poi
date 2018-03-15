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

import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.StringUtil;

/**
 * Title:        FILESHARING (0x005B) <p>
 * Description:  stores the encrypted readonly for a workbook (write protect) 
 * This functionality is accessed from the options dialog box available when performing 'Save As'.<p>
 * REFERENCE:  PG 314 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 */
public final class FileSharingRecord extends StandardRecord implements Cloneable {

    public final static short sid = 0x005B;
    private short             field_1_readonly;
    private short             field_2_password;
    private byte              field_3_username_unicode_options;
    private String            field_3_username_value;

    public FileSharingRecord() {}

    public FileSharingRecord(RecordInputStream in) {
        field_1_readonly = in.readShort();
        field_2_password = in.readShort();
        
        int nameLen = in.readShort();
        
        if(nameLen > 0) {
            // TODO - Current examples(3) from junits only have zero length username. 
            field_3_username_unicode_options = in.readByte();
            field_3_username_value = in.readCompressedUnicode(nameLen);
        } else {
            field_3_username_value = "";
        }
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
     * @param password hashed password
     */
    public void setPassword(short password) {
        field_2_password = password;
    }

    /**
     * @return password hashed with hashPassword() (very lame)
     */
    public short getPassword() {
        return field_2_password;
    }


    /**
     * @return username of the user that created the file
     */
    public String getUsername() {
        return field_3_username_value;
    }

    /**
     * @param username of the user that created the file
     */
    public void setUsername(String username) {
        field_3_username_value = username;
    }


    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[FILESHARING]\n");
        buffer.append("    .readonly       = ")
            .append(getReadOnly() == 1 ? "true" : "false").append("\n");
        buffer.append("    .password       = ")
            .append(Integer.toHexString(getPassword())).append("\n");
        buffer.append("    .username       = ")
            .append(getUsername()).append("\n");
        buffer.append("[/FILESHARING]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        // TODO - junit
        out.writeShort(getReadOnly());
        out.writeShort(getPassword());
        out.writeShort(field_3_username_value.length());
        if(field_3_username_value.length() > 0) {
        	out.writeByte(field_3_username_unicode_options);
            StringUtil.putCompressedUnicode(getUsername(), out);
        }
    }

    protected int getDataSize() {
        int nameLen = field_3_username_value.length();
        if (nameLen < 1) {
            return 6;
        }
        return 7+nameLen;
    }

    public short getSid() {
        return sid;
    }

    @Override
    public FileSharingRecord clone() {
      FileSharingRecord clone = new FileSharingRecord();
      clone.setReadOnly(field_1_readonly);
      clone.setPassword(field_2_password);
      clone.setUsername(field_3_username_value);
      return clone;
    }
}
