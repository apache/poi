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

import java.util.Arrays;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.formula.Formula;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Conditional Formatting Rule Record (0x01B1). 
 * 
 * <p>This is for the older-style Excel conditional formattings,
 *  new-style (Excel 2007+) also make use of {@link CFRule12Record}
 *  for their rules.</p>
 */
public final class CFRuleRecord extends CFRuleBase implements Cloneable {
    public static final short sid = 0x01B1;

    /** Creates new CFRuleRecord */
    private CFRuleRecord(byte conditionType, byte comparisonOperation) {
        super(conditionType, comparisonOperation);
        setDefaults();
    }

    private CFRuleRecord(byte conditionType, byte comparisonOperation, Ptg[] formula1, Ptg[] formula2) {
        super(conditionType, comparisonOperation, formula1, formula2);
        setDefaults();
    }
    private void setDefaults() {
        // Set modification flags to 1: by default options are not modified
        formatting_options = modificationBits.setValue(formatting_options, -1);
        // Set formatting block flags to 0 (no formatting blocks)
        formatting_options = fmtBlockBits.setValue(formatting_options, 0);
        formatting_options = undocumented.clear(formatting_options);

        formatting_not_used = (short)0x8002; // Excel seems to write this value, but it doesn't seem to care what it reads
        _fontFormatting = null;
        _borderFormatting = null;
        _patternFormatting = null;
    }

    /**
     * Creates a new comparison operation rule
     * 
     * @param sheet the sheet
     * @param formulaText the formula text
     * 
     * @return a new comparison operation rule
     */
    public static CFRuleRecord create(HSSFSheet sheet, String formulaText) {
        Ptg[] formula1 = parseFormula(formulaText, sheet);
        return new CFRuleRecord(CONDITION_TYPE_FORMULA, ComparisonOperator.NO_COMPARISON,
                formula1, null);
    }
    /**
     * Creates a new comparison operation rule
     * 
     * @param sheet the sheet
     * @param comparisonOperation the comparison operation
     * @param formulaText1 the first formula text
     * @param formulaText2 the second formula text
     * 
     * @return a new comparison operation rule
     */
    public static CFRuleRecord create(HSSFSheet sheet, byte comparisonOperation,
            String formulaText1, String formulaText2) {
        Ptg[] formula1 = parseFormula(formulaText1, sheet);
        Ptg[] formula2 = parseFormula(formulaText2, sheet);
        return new CFRuleRecord(CONDITION_TYPE_CELL_VALUE_IS, comparisonOperation, formula1, formula2);
    }

    public CFRuleRecord(RecordInputStream in) {
        setConditionType(in.readByte());
        setComparisonOperation(in.readByte());
        int field_3_formula1_len = in.readUShort();
        int field_4_formula2_len = in.readUShort();
        readFormatOptions(in);

        // "You may not use unions, intersections or array constants in Conditional Formatting criteria"
        setFormula1(Formula.read(field_3_formula1_len, in));
        setFormula2(Formula.read(field_4_formula2_len, in));
    }

    @Override
    public short getSid() {
        return sid;
    }

    /**
     * called by the class that is responsible for writing this sucker.
     * Subclasses should implement this so that their data is passed back in a
     * byte array.
     *
     * @param out the stream to write to
     */
    @Override
    public void serialize(LittleEndianOutput out) {
        int formula1Len=getFormulaSize(getFormula1());
        int formula2Len=getFormulaSize(getFormula2());

        out.writeByte(getConditionType());
        out.writeByte(getComparisonOperation());
        out.writeShort(formula1Len);
        out.writeShort(formula2Len);
        
        serializeFormattingBlock(out);

        getFormula1().serializeTokens(out);
        getFormula2().serializeTokens(out);
    }

    @Override
    protected int getDataSize() {
        return 6 + getFormattingBlockSize() +
               getFormulaSize(getFormula1())+
               getFormulaSize(getFormula2());
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[CFRULE]\n");
        buffer.append("    .condition_type   =").append(getConditionType()).append("\n");
        buffer.append("    OPTION FLAGS=0x").append(Integer.toHexString(getOptions())).append("\n");
        if (containsFontFormattingBlock()) {
            buffer.append(_fontFormatting).append("\n");
        }
        if (containsBorderFormattingBlock()) {
            buffer.append(_borderFormatting).append("\n");
        }
        if (containsPatternFormattingBlock()) {
            buffer.append(_patternFormatting).append("\n");
        }
        buffer.append("    Formula 1 =").append(Arrays.toString(getFormula1().getTokens())).append("\n");
        buffer.append("    Formula 2 =").append(Arrays.toString(getFormula2().getTokens())).append("\n");
        buffer.append("[/CFRULE]\n");
        return buffer.toString();
    }

    @Override
    public CFRuleRecord clone() {
        CFRuleRecord rec = new CFRuleRecord(getConditionType(), getComparisonOperation());
        super.copyTo(rec);
        return rec;
    }
}
