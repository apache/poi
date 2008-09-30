
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

/**
 * Title:        Password Record<P>
 * Description:  stores the encrypted password for a sheet or workbook (HSSF doesn't support encryption)
 * REFERENCE:  PG 371 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class PasswordRecord extends Record {
    public final static short sid = 0x13;
    private short             field_1_password;   // not sure why this is only 2 bytes, but it is... go figure

    public PasswordRecord() {
    }

    public PasswordRecord(RecordInputStream in) {
        field_1_password = in.readShort();
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
     * set the password
     *
     * @param password  representing the password
     */

    public void setPassword(short password) {
        field_1_password = password;
    }

    /**
     * get the password
     *
     * @return short  representing the password
     */
    public short getPassword() {
        return field_1_password;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[PASSWORD]\n");
        buffer.append("    .password       = ")
            .append(Integer.toHexString(getPassword())).append("\n");
        buffer.append("[/PASSWORD]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data) {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              (( short ) 0x02));   // 2 bytes (6 total)
        LittleEndian.putShort(data, 4 + offset, getPassword());
        return getRecordSize();
    }

    public int getRecordSize() {
        return 6;
    }

    public short getSid() {
        return sid;
    }

    /**
     * Clone this record.
     */
    public Object clone() {
      PasswordRecord clone = new PasswordRecord();
      clone.setPassword(field_1_password);
      return clone;
    }

}
