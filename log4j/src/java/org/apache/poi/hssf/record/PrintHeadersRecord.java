
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
 * Whether or not to print the row/column headers when you enjoy your spreadsheet in the physical form.
 *
 * @version 2.0-pre
 */
public final class PrintHeadersRecord extends StandardRecord {
    public static final short sid = 0x2a;
    private short field_1_print_headers;

    public PrintHeadersRecord() {}

    public PrintHeadersRecord(PrintHeadersRecord other) {
        super(other);
        field_1_print_headers = other.field_1_print_headers;
    }


    public PrintHeadersRecord(RecordInputStream in) {
        field_1_print_headers = in.readShort();
    }

    /**
     * set to print the headers - y/n
     * @param p printheaders or not
     */
    public void setPrintHeaders(boolean p) {
        if (p) {
            field_1_print_headers = 1;
        } else {
            field_1_print_headers = 0;
        }
    }

    /**
     * get whether to print the headers - y/n
     * @return printheaders or not
     */
    public boolean getPrintHeaders()
    {
        return (field_1_print_headers == 1);
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_print_headers);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    public PrintHeadersRecord copy() {
      return new PrintHeadersRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.PRINT_HEADERS;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("printHeaders", this::getPrintHeaders);
    }
}
