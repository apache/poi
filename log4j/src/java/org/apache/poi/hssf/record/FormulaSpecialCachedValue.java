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

import static org.apache.poi.util.GenericRecordUtil.getEnumBitsAsString;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Manages the cached formula result values of other types besides numeric.
 * Excel encodes the same 8 bytes that would be field_4_value with various NaN
 * values that are decoded/encoded by this class.
 */
@Internal
public final class FormulaSpecialCachedValue implements GenericRecord {
    /** deliberately chosen by Excel in order to encode other values within Double NaNs */
    private static final long BIT_MARKER = 0xFFFF000000000000L;
    private static final int VARIABLE_DATA_LENGTH = 6;
    private static final int DATA_INDEX = 2;

    // FIXME: can these be merged with {@link CellType}?
    // are the numbers specific to the HSSF formula record format or just a poor-man's enum?
    public static final int STRING = 0;
    public static final int BOOLEAN = 1;
    public static final int ERROR_CODE = 2;
    public static final int EMPTY = 3;

    private final byte[] _variableData;

    FormulaSpecialCachedValue(FormulaSpecialCachedValue other) {
        _variableData = (other._variableData == null) ? null : other._variableData.clone();
    }

    private FormulaSpecialCachedValue(byte[] data) {
        _variableData = data;
    }

    public int getTypeCode() {
        return _variableData[0];
    }

    /**
     * @return <code>null</code> if the double value encoded by <tt>valueLongBits</tt>
     * is a normal (non NaN) double value.
     */
    public static FormulaSpecialCachedValue create(long valueLongBits) {
        if ((BIT_MARKER & valueLongBits) != BIT_MARKER) {
            return null;
        }

        byte[] result = new byte[VARIABLE_DATA_LENGTH];
        long x = valueLongBits;
        for (int i=0; i<VARIABLE_DATA_LENGTH; i++) {
            result[i] = (byte) x;
            x >>= 8;
        }
        switch (result[0]) {
            case STRING:
            case BOOLEAN:
            case ERROR_CODE:
            case EMPTY:
                break;
            default:
                throw new org.apache.poi.util.RecordFormatException("Bad special value code (" + result[0] + ")");
        }
        return new FormulaSpecialCachedValue(result);
    }

    public void serialize(LittleEndianOutput out) {
        out.write(_variableData);
        out.writeShort(0xFFFF);
    }

    public String formatDebugString() {
        return formatValue() + ' ' + HexDump.toHex(_variableData);
    }

    private String formatValue() {
        int typeCode = getTypeCode();
        switch (typeCode) {
            case STRING:
                return "<string>";
            case BOOLEAN:
                return getDataValue() == 0 ? "FALSE" : "TRUE";
            case ERROR_CODE:
                return ErrorEval.getText(getDataValue());
            case EMPTY:
                return "<empty>";
        }
        return "#error(type=" + typeCode + ")#";
    }

    private int getDataValue() {
        return _variableData[DATA_INDEX];
    }

    public static FormulaSpecialCachedValue createCachedEmptyValue() {
        return create(EMPTY, 0);
    }

    public static FormulaSpecialCachedValue createForString() {
        return create(STRING, 0);
    }

    public static FormulaSpecialCachedValue createCachedBoolean(boolean b) {
        return create(BOOLEAN, b ? 1 : 0);
    }

    public static FormulaSpecialCachedValue createCachedErrorCode(int errorCode) {
        return create(ERROR_CODE, errorCode);
    }

    private static FormulaSpecialCachedValue create(int code, int data) {
        byte[] vd = { (byte) code, 0, (byte) data, 0, 0, 0, };
        return new FormulaSpecialCachedValue(vd);
    }

    @Override
    public String toString() {
        return getClass().getName() + '[' + formatValue() + ']';
    }

    /**
     * @deprecated POI 5.0.0, will be removed in 5.0, use getValueTypeEnum until switch to enum is fully done
     */
    @Deprecated
    public int getValueType() {
        int typeCode = getTypeCode();
        switch (typeCode) {
            case EMPTY: // is this correct?
            case STRING:
                return CellType.STRING.getCode();
            case BOOLEAN:
                return CellType.BOOLEAN.getCode();
            case ERROR_CODE:
                return CellType.ERROR.getCode();
        }
        throw new IllegalStateException("Unexpected type id (" + typeCode + ")");
    }

    /**
     * Returns the type of the cached value
     * @return A CellType
     * @since POI 5.0.0
     */
    public CellType getValueTypeEnum() {
        int typeCode = getTypeCode();
        switch (typeCode) {
            case EMPTY: // is this correct?
            case STRING:
                return CellType.STRING;
            case BOOLEAN:
                return CellType.BOOLEAN;
            case ERROR_CODE:
                return CellType.ERROR;
        }
        throw new IllegalStateException("Unexpected type id (" + typeCode + ")");
    }

    public boolean getBooleanValue() {
        if (getTypeCode() != BOOLEAN) {
            throw new IllegalStateException("Not a boolean cached value - " + formatValue());
        }
        return getDataValue() != 0;
    }

    public int getErrorValue() {
        if (getTypeCode() != ERROR_CODE) {
            throw new IllegalStateException("Not an error cached value - " + formatValue());
        }
        return getDataValue();
    }

    private Object getGenericValue() {
        int typeCode = getTypeCode();
        switch (typeCode) {
            case EMPTY: // is this correct?
                return null;
            case STRING:
                return "string";
            case BOOLEAN:
                return getBooleanValue();
            case ERROR_CODE:
                return getErrorValue();
        }
        throw new IllegalStateException("Unexpected type id (" + typeCode + ")");
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "value", this::getGenericValue,
            "typeCode", getEnumBitsAsString(this::getTypeCode,
                new int[]{STRING,BOOLEAN,ERROR_CODE,EMPTY},
                new String[]{"STRING","BOOLEAN","ERROR_CODE","EMPTY"})
        );
    }
}
