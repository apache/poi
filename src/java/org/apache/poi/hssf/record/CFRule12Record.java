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

import org.apache.poi.hssf.record.common.FtrHeader;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.formula.Formula;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Conditional Formatting v12 Rule Record (0x087A). 
 * 
 * <p>This is for newer-style Excel conditional formattings,
 *  from Excel 2007 onwards.
 *  
 * <p>{@link CFRuleRecord} is used where the condition type is
 *  {@link #CONDITION_TYPE_CELL_VALUE_IS} or {@link #CONDITION_TYPE_FORMULA},
 *  this is only used for the other types
 */
public final class CFRule12Record extends CFRuleBase {
    public static final short sid = 0x087A;

    private FtrHeader futureHeader;
    private Formula formulaScale;

    /** Creates new CFRuleRecord */
    private CFRule12Record(byte conditionType, byte comparisonOperation) {
        super(conditionType, comparisonOperation);
        futureHeader = new FtrHeader();
        futureHeader.setRecordType(sid);
        // TODO Remaining fields
    }

    private CFRule12Record(byte conditionType, byte comparisonOperation, Ptg[] formula1, Ptg[] formula2, Ptg[] formulaScale) {
        super(conditionType, comparisonOperation, formula1, formula2);
        this.formulaScale = Formula.create(formulaScale);
        // TODO Remaining fields
    }

    /**
     * Creates a new comparison operation rule
     */
    public static CFRule12Record create(HSSFSheet sheet, String formulaText) {
        Ptg[] formula1 = parseFormula(formulaText, sheet);
        return new CFRule12Record(CONDITION_TYPE_FORMULA, ComparisonOperator.NO_COMPARISON,
                formula1, null, null);
    }
    /**
     * Creates a new comparison operation rule
     */
    public static CFRule12Record create(HSSFSheet sheet, byte comparisonOperation,
            String formulaText1, String formulaText2) {
        Ptg[] formula1 = parseFormula(formulaText1, sheet);
        Ptg[] formula2 = parseFormula(formulaText2, sheet);
        return new CFRule12Record(CONDITION_TYPE_CELL_VALUE_IS, comparisonOperation, 
                formula1, formula2, null);
    }
    /**
     * Creates a new comparison operation rule
     */
    public static CFRule12Record create(HSSFSheet sheet, byte comparisonOperation,
            String formulaText1, String formulaText2, String formulaTextScale) {
        Ptg[] formula1 = parseFormula(formulaText1, sheet);
        Ptg[] formula2 = parseFormula(formulaText2, sheet);
        Ptg[] formula3 = parseFormula(formulaTextScale, sheet);
        return new CFRule12Record(CONDITION_TYPE_CELL_VALUE_IS, comparisonOperation, 
                formula1, formula2, formula3);
    }

    public CFRule12Record(RecordInputStream in) {
        futureHeader = new FtrHeader(in);
        setConditionType(in.readByte());
        setComparisonOperation(in.readByte());
        int field_3_formula1_len = in.readUShort();
        int field_4_formula2_len = in.readUShort();
        
        // TODO Handle the remainder
    }

    /**
     * get the stack of the scale expression as a list
     *
     * @return list of tokens (casts stack to a list and returns it!)
     * this method can return null is we are unable to create Ptgs from
     *	 existing excel file
     * callers should check for null!
     */
    public Ptg[] getParsedExpressionScale() {
        return formulaScale.getTokens();
    }
    public void setParsedExpressionScale(Ptg[] ptgs) {
        formulaScale = Formula.create(ptgs);
    }

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
    public void serialize(LittleEndianOutput out) {
        futureHeader.serialize(out);
        
        int formula1Len=getFormulaSize(getFormula1());
        int formula2Len=getFormulaSize(getFormula2());

        out.writeByte(getConditionType());
        out.writeByte(getComparisonOperation());
        out.writeShort(formula1Len);
        out.writeShort(formula2Len);
        
        // TODO Output the rest
    }

    protected int getDataSize() {
        // TODO Calculate
        return 0;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[CFRULE12]\n");
        buffer.append("    .condition_type   =").append(getConditionType()).append("\n");
        buffer.append("    TODO The rest!\n");
        buffer.append("    Formula 1 =").append(Arrays.toString(getFormula1().getTokens())).append("\n");
        buffer.append("    Formula 2 =").append(Arrays.toString(getFormula2().getTokens())).append("\n");
        buffer.append("    Formula S =").append(Arrays.toString(formulaScale.getTokens())).append("\n");
        buffer.append("[/CFRULE12]\n");
        return buffer.toString();
    }

    public Object clone() {
        CFRule12Record rec = new CFRule12Record(getConditionType(), getComparisonOperation());
        
        // TODO The other fields
        
        rec.setFormula1(getFormula1().copy());
        rec.setFormula2(getFormula2().copy());
        rec.formulaScale = formulaScale.copy();

        return rec;
    }
}
