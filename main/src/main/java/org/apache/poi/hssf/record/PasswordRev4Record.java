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

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Protection Revision 4 password Record (0x01BC)<p>
 * Stores the (2 byte??!!) encrypted password for a shared workbook
 */
public final class PasswordRev4Record extends StandardRecord {
    public static final short sid = 0x01BC;
    private int field_1_password;

    public PasswordRev4Record(int pw) {
        field_1_password = pw;
    }

    public PasswordRev4Record(PasswordRev4Record other) {
        super(other);
        field_1_password = other.field_1_password;
    }

    public PasswordRev4Record(RecordInputStream in) {
        field_1_password = in.readShort();
    }

    /**
     * set the password
     *
     * @param pw  representing the password
     */
    public void setPassword(short pw) {
        field_1_password = pw;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_password);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid() {
        return sid;
    }

    @Override
    public PasswordRev4Record copy() {
        return new PasswordRev4Record(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.PASSWORD_REV_4;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("password", () -> field_1_password);
    }
}
