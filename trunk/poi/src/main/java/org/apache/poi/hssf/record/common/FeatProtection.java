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

package org.apache.poi.hssf.record.common;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.hssf.record.FeatRecord;
import org.apache.poi.hssf.record.PasswordRecord;
import org.apache.poi.hssf.record.PasswordRev4Record;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.StringUtil;

/**
 * Title: FeatProtection (Protection Shared Feature) common record part
 * <P>
 * This record part specifies Protection data for a sheet, stored
 *  as part of a Shared Feature. It can be found in records such
 *  as {@link FeatRecord}
 */
public final class FeatProtection implements SharedFeature {
    @SuppressWarnings("RedundantFieldInitialization")
    public static final long NO_SELF_RELATIVE_SECURITY_FEATURE = 0;
    public static final long HAS_SELF_RELATIVE_SECURITY_FEATURE = 1;

    private int fSD;

    /**
     * 0 means no password. Otherwise indicates the
     *  password verifier algorithm (same kind as
     *   {@link PasswordRecord} and
     *   {@link PasswordRev4Record})
     */
    private int passwordVerifier;

    private String title;
    private byte[] securityDescriptor;

    public FeatProtection() {
        securityDescriptor = new byte[0];
    }

    public FeatProtection(FeatProtection other) {
        fSD = other.fSD;
        passwordVerifier = other.passwordVerifier;
        title = other.title;
        securityDescriptor = (other.securityDescriptor == null) ? null : other.securityDescriptor.clone();
    }

    public FeatProtection(RecordInputStream in) {
        fSD = in.readInt();
        passwordVerifier = in.readInt();

        title = StringUtil.readUnicodeString(in);

        securityDescriptor = in.readRemainder();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeInt(fSD);
        out.writeInt(passwordVerifier);
        StringUtil.writeUnicodeString(out, title);
        out.write(securityDescriptor);
    }

    public int getDataSize() {
        return 4 + 4 + StringUtil.getEncodedSize(title) + securityDescriptor.length;
    }

    public int getPasswordVerifier() {
        return passwordVerifier;
    }
    public void setPasswordVerifier(int passwordVerifier) {
        this.passwordVerifier = passwordVerifier;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return Self Relative
     */
    public int getFSD() {
        return fSD;
    }

    @Override
    public FeatProtection copy() {
        return new FeatProtection(this);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "FSD", this::getFSD,
            "passwordVerifier", this::getPasswordVerifier,
            "title", this::getTitle,
            "securityDescriptor", () -> securityDescriptor
        );
    }
}
