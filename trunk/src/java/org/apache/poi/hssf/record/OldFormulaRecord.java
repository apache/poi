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

import org.apache.poi.hssf.record.FormulaRecord.SpecialCachedValue;
import org.apache.poi.ss.formula.Formula;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.CellType;

/**
 * Formula Record (0x0006 / 0x0206 / 0x0406) - holds a formula in
 *  encoded form, along with the value if a number
 */
public final class OldFormulaRecord extends OldCellRecord {
    public final static short biff2_sid = 0x0006;
    public final static short biff3_sid = 0x0206;
    public final static short biff4_sid = 0x0406;
    public final static short biff5_sid = 0x0006;

    private SpecialCachedValue specialCachedValue;
    private double  field_4_value;
    private short   field_5_options;
    private Formula field_6_parsed_expr;

    public OldFormulaRecord(RecordInputStream ris) {
        super(ris, ris.getSid() == biff2_sid);

        if (isBiff2()) {
            field_4_value = ris.readDouble();
        } else {
            long valueLongBits  = ris.readLong();
            specialCachedValue = SpecialCachedValue.create(valueLongBits);
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

    public int getCachedResultType() {
        if (specialCachedValue == null) {
            return CellType.NUMERIC.getCode();
        }
        return specialCachedValue.getValueType();
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

    protected void appendValueText(StringBuilder sb) {
        sb.append("    .value       = ").append(getValue()).append("\n");
    }
    protected String getRecordName() {
        return "Old Formula";
    }
}
