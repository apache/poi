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

import org.apache.poi.ss.formula.Formula;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.util.GenericRecordUtil;

/**
 * Formula Record (0x0006 / 0x0206 / 0x0406) - holds a formula in
 *  encoded form, along with the value if a number
 */
public final class OldFormulaRecord extends OldCellRecord {
    public static final short biff2_sid = 0x0006;
    public static final short biff3_sid = 0x0206;
    public static final short biff4_sid = 0x0406;
    public static final short biff5_sid = 0x0006;

    private FormulaSpecialCachedValue specialCachedValue;
    private double  field_4_value;
    private short   field_5_options;
    private Formula field_6_parsed_expr;

    public OldFormulaRecord(RecordInputStream ris) {
        super(ris, ris.getSid() == biff2_sid);

        if (isBiff2()) {
            field_4_value = ris.readDouble();
        } else {
            long valueLongBits  = ris.readLong();
            specialCachedValue = FormulaSpecialCachedValue.create(valueLongBits);
            if (specialCachedValue == null) {
                field_4_value = Double.longBitsToDouble(valueLongBits);
            }
        }

        if (isBiff2()) {
            field_5_options = (short)ris.readUByte();
        } else {
            field_5_options = ris.readShort();
        }

        int expression_len = ris.readShort();
        int nBytesAvailable = ris.available();
        field_6_parsed_expr = Formula.read(expression_len, ris, nBytesAvailable);
    }

    /**
     * @deprecated POI 5.0.0, will be removed in 5.0, use getCachedResultTypeEnum until switch to enum is fully done
     */
    @Deprecated
    public int getCachedResultType() {
        if (specialCachedValue == null) {
            return CellType.NUMERIC.getCode();
        }
        return specialCachedValue.getValueType();
    }

    /**
     * Returns the type of the cached result
     * @return A CellType
     * @since POI 5.0.0
     */
    public CellType getCachedResultTypeEnum() {
        if (specialCachedValue == null) {
            return CellType.NUMERIC;
        }
        return specialCachedValue.getValueTypeEnum();
    }

    public boolean getCachedBooleanValue() {
        return specialCachedValue.getBooleanValue();
    }
    public int getCachedErrorValue() {
        return specialCachedValue.getErrorValue();
    }

    /**
     * get the calculated value of the formula
     *
     * @return calculated value
     */
    public double getValue() {
        return field_4_value;
    }

    /**
     * get the option flags
     *
     * @return bitmask
     */
    public short getOptions() {
        return field_5_options;
    }

    /**
     * @return the formula tokens. never <code>null</code>
     */
    public Ptg[] getParsedExpression() {
        return field_6_parsed_expr.getTokens();
    }

    public Formula getFormula() {
        return field_6_parsed_expr;
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.FORMULA;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "base", super::getGenericProperties,
            "options", this::getOptions,
            "formula", this::getFormula,
            "value", this::getValue
        );
    }
}
