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

/**
 * Specifies a header for a sheet
 */
public final class HeaderRecord extends HeaderFooterBase {
    public static final short sid = 0x0014;

    public HeaderRecord(String text) {
        super(text);
    }

    public HeaderRecord(HeaderRecord other) {
        super(other);
    }

    public HeaderRecord(RecordInputStream in) {
        super(in);
    }

    public short getSid() {
        return sid;
    }

    @Override
    public HeaderRecord copy() {
        return new HeaderRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.HEADER;
    }
}
