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

package org.apache.poi.hssf.record.cf;

import java.util.Arrays;

import org.apache.poi.ss.formula.Formula;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.ConditionalFormattingThreshold.RangeType;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Threshold / value (CFVO) for changes in Conditional Formatting
 */
public abstract class Threshold {
    private byte type;
    private Formula formula;
    private Double value;

    protected Threshold() {
        type = (byte)RangeType.NUMBER.id;
        formula = Formula.create(null);
        value = 0d;
    }

    /** Creates new Threshold */
    protected Threshold(LittleEndianInput in) {
        type = in.readByte();
        short formulaLen = in.readShort();
        if (formulaLen > 0) {
            formula = Formula.read(formulaLen, in);
        } else {
            formula = Formula.create(null);
        }
        // Value is only there for non-formula, non min/max thresholds
        if (formulaLen == 0 && type != RangeType.MIN.id &&
                type != RangeType.MAX.id) {
            value = in.readDouble();
        }
    }

    public byte getType() {
        return type;
    }
    public void setType(byte type) {
        this.type = type;

        // Ensure the value presence / absence is consistent for the new type
        if (type == RangeType.MIN.id || type == RangeType.MAX.id ||
               type == RangeType.FORMULA.id) {
            this.value = null;
        } else if (value == null) {
            this.value = 0d;
        }
    }
    public void setType(int type) {
        this.type = (byte)type;
    }

    protected Formula getFormula() {
        return formula;
    }
    public Ptg[] getParsedExpression() {
        return formula.getTokens();
    }
    public void setParsedExpression(Ptg[] ptgs) {
        formula = Formula.create(ptgs);
        if (ptgs.length > 0) {
            this.value = null;
        }
    }

    public Double getValue() {
        return value;
    }
    public void setValue(Double value) {
        this.value = value;
    }
    
    public int getDataLength() {
        int len = 1 + formula.getEncodedSize();
        if (value != null) {
            len += 8;
        }
        return len;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("    [CF Threshold]\n");
        buffer.append("          .type    = ").append(Integer.toHexString(type)).append("\n");
        buffer.append("          .formula = ").append(Arrays.toString(formula.getTokens())).append("\n");
        buffer.append("          .value   = ").append(value).append("\n");
        buffer.append("    [/CF Threshold]\n");
        return buffer.toString();
    }

    public void copyTo(Threshold rec) {
      rec.type = type;
      rec.formula = formula;
      rec.value = value;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeByte(type);
        if (formula.getTokens().length == 0) {
            out.writeShort(0);
        } else {
            formula.serialize(out);
        }
        if (value != null) {
            out.writeDouble(value);
        }
    }
}
