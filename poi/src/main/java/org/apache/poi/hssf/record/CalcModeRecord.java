
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
 * Tells the gui whether to calculate formulas automatically, manually or automatically except for tables.
 *
 * @version 2.0-pre
 * @see CalcCountRecord
 */

public final class CalcModeRecord extends StandardRecord {
    public static final short sid                     = 0xD;

    /** manually calculate formulas (0) */
    public static final short MANUAL                  = 0;

    /** automatically calculate formulas (1) */
    public static final short AUTOMATIC               = 1;

    /** automatically calculate formulas except for tables (-1) */
    public static final short AUTOMATIC_EXCEPT_TABLES = -1;

    private short field_1_calcmode;

    public CalcModeRecord() {}

    public CalcModeRecord(CalcModeRecord other) {
        super(other);
        field_1_calcmode = other.field_1_calcmode;
    }

    public CalcModeRecord(RecordInputStream in) {
        field_1_calcmode = in.readShort();
    }

    /**
     * set the calc mode flag for formulas
     *
     * @see #MANUAL
     * @see #AUTOMATIC
     * @see #AUTOMATIC_EXCEPT_TABLES
     *
     * @param calcmode one of the three flags above
     */

    public void setCalcMode(short calcmode)
    {
        field_1_calcmode = calcmode;
    }

    /**
     * get the calc mode flag for formulas
     *
     * @see #MANUAL
     * @see #AUTOMATIC
     * @see #AUTOMATIC_EXCEPT_TABLES
     *
     * @return calcmode one of the three flags above
     */

    public short getCalcMode()
    {
        return field_1_calcmode;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getCalcMode());
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public CalcModeRecord copy() {
        return new CalcModeRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.CALC_MODE;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "calcMode", this::getCalcMode
        );
    }
}
