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
 * Whether to print the gridlines when you enjoy the spreadsheet on paper.
 *
 * @version 2.0-pre
 */
public final class PrintGridlinesRecord extends StandardRecord {
    public static final short sid = 0x2b;
    private short field_1_print_gridlines;

    public PrintGridlinesRecord() {}

    public PrintGridlinesRecord(PrintGridlinesRecord other) {
        super(other);
        field_1_print_gridlines = other.field_1_print_gridlines;
    }

    public PrintGridlinesRecord(RecordInputStream in) {
        field_1_print_gridlines = in.readShort();
    }

    /**
     * set whether or not to print the gridlines (and make your spreadsheet ugly)
     *
     * @param pg  make spreadsheet ugly - Y/N
     */
    public void setPrintGridlines(boolean pg) {
        field_1_print_gridlines = (short) (pg ? 1 : 0);
    }

    /**
     * get whether or not to print the gridlines (and make your spreadsheet ugly)
     *
     * @return make spreadsheet ugly - Y/N
     */
    public boolean getPrintGridlines()
    {
        return (field_1_print_gridlines == 1);
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_print_gridlines);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    public PrintGridlinesRecord copy() {
        return new PrintGridlinesRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.PRINT_GRIDLINES;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("printGridlines", this::getPrintGridlines);
    }
}
