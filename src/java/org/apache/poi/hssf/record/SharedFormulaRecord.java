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

import org.apache.poi.hssf.record.formula.*;
import org.apache.poi.hssf.util.CellRangeAddress8Bit;
import org.apache.poi.ss.formula.Formula;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title:        SHAREDFMLA (0x04BC) SharedFormulaRecord
 * Description:  Primarily used as an excel optimization so that multiple similar formulas
 *               are not written out too many times.  We should recognize this record and
 *               serialize as is since this is used when reading templates.
 * <p>
 * Note: the documentation says that the SID is BC where biffviewer reports 4BC.  The hex dump shows
 * that the two byte sid representation to be 'BC 04' that is consistent with the other high byte
 * record types.
 * @author Danny Mui at apache dot org
 */
public final class SharedFormulaRecord extends SharedValueRecordBase {
    public final static short   sid = 0x04BC;

    private int field_5_reserved;
    private Formula field_7_parsed_expr;

    // for testing only
    public SharedFormulaRecord() {
        this(new CellRangeAddress8Bit(0,0,0,0));
    }
    private SharedFormulaRecord(CellRangeAddress8Bit range) {
        super(range);
        field_7_parsed_expr = Formula.create(Ptg.EMPTY_PTG_ARRAY);
    }

    /**
     * @param in the RecordInputstream to read the record from
     */
    public SharedFormulaRecord(RecordInputStream in) {
        super(in);
        field_5_reserved        = in.readShort();
        int field_6_expression_len = in.readShort();
        int nAvailableBytes = in.available();
        field_7_parsed_expr = Formula.read(field_6_expression_len, in, nAvailableBytes);
    }

    protected void serializeExtraData(LittleEndianOutput out) {
        out.writeShort(field_5_reserved);
        field_7_parsed_expr.serialize(out);
    }
    
    protected int getExtraDataSize() {
        return 2 + field_7_parsed_expr.getEncodedSize();
    }

    /**
     * print a sort of string representation ([SHARED FORMULA RECORD] id = x [/SHARED FORMULA RECORD])
     */

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[SHARED FORMULA (").append(HexDump.intToHex(sid)).append("]\n");
        buffer.append("    .range      = ").append(getRange().toString()).append("\n");
        buffer.append("    .reserved    = ").append(HexDump.shortToHex(field_5_reserved)).append("\n");

        Ptg[] ptgs = field_7_parsed_expr.getTokens();
        for (int k = 0; k < ptgs.length; k++ ) {
           buffer.append("Formula[").append(k).append("]");
           Ptg ptg = ptgs[k];
           buffer.append(ptg.toString()).append(ptg.getRVAType()).append("\n");
        }

        buffer.append("[/SHARED FORMULA]\n");
        return buffer.toString();
    }

    public short getSid() {
        return sid;
    }

    /**
     * Creates a non shared formula from the shared formula counterpart<br/>
     * 
     * Perhaps this functionality could be implemented in terms of the raw 
     * byte array inside {@link Formula}.
     */
    public static Ptg[] convertSharedFormulas(Ptg[] ptgs, int formulaRow, int formulaColumn) {

        Ptg[] newPtgStack = new Ptg[ptgs.length];

        for (int k = 0; k < ptgs.length; k++) {
            Ptg ptg = ptgs[k];
            byte originalOperandClass = -1;
            if (!ptg.isBaseToken()) {
                originalOperandClass = ptg.getPtgClass();
            }
            if (ptg instanceof RefPtgBase) {
              RefPtgBase refNPtg = (RefPtgBase)ptg;
              ptg = new RefPtg(fixupRelativeRow(formulaRow,refNPtg.getRow(),refNPtg.isRowRelative()),
                                     fixupRelativeColumn(formulaColumn,refNPtg.getColumn(),refNPtg.isColRelative()),
                                     refNPtg.isRowRelative(),
                                     refNPtg.isColRelative());
            } else if (ptg instanceof AreaPtgBase) {
              AreaPtgBase areaNPtg = (AreaPtgBase)ptg;
              ptg = new AreaPtg(fixupRelativeRow(formulaRow,areaNPtg.getFirstRow(),areaNPtg.isFirstRowRelative()),
                                fixupRelativeRow(formulaRow,areaNPtg.getLastRow(),areaNPtg.isLastRowRelative()),
                                fixupRelativeColumn(formulaColumn,areaNPtg.getFirstColumn(),areaNPtg.isFirstColRelative()),
                                fixupRelativeColumn(formulaColumn,areaNPtg.getLastColumn(),areaNPtg.isLastColRelative()),
                                areaNPtg.isFirstRowRelative(),
                                areaNPtg.isLastRowRelative(),
                                areaNPtg.isFirstColRelative(),
                                areaNPtg.isLastColRelative());
            } else {
                if (false) {// do we need a ptg clone here?
                    ptg = ptg.copy();
                }
            }
            if (!ptg.isBaseToken()) {
                ptg.setClass(originalOperandClass);
            }

            newPtgStack[k] = ptg;
        }
        return newPtgStack;
    }

    /**
     * @return the equivalent {@link Ptg} array that the formula would have, were it not shared.
     */
    public Ptg[] getFormulaTokens(FormulaRecord formula) {
        int formulaRow = formula.getRow();
        int formulaColumn = formula.getColumn();
        //Sanity checks
        if (!isInRange(formulaRow, formulaColumn)) {
            throw new RuntimeException("Shared Formula Conversion: Coding Error");
        }

        return convertSharedFormulas(field_7_parsed_expr.getTokens(), formulaRow, formulaColumn);
    }

    private static int fixupRelativeColumn(int currentcolumn, int column, boolean relative) {
        if(relative) {
            // mask out upper bits to produce 'wrapping' at column 256 ("IV")
            return (column + currentcolumn) & 0x00FF;
        }
        return column;
    }

    private static int fixupRelativeRow(int currentrow, int row, boolean relative) {
        if(relative) {
            // mask out upper bits to produce 'wrapping' at row 65536
            return (row+currentrow) & 0x00FFFF;
        }
        return row;
    }

    public Object clone() {
        SharedFormulaRecord result = new SharedFormulaRecord(getRange());
        result.field_5_reserved = field_5_reserved;
        result.field_7_parsed_expr = field_7_parsed_expr.copy();
        return result;
    }
	public boolean isFormulaSame(SharedFormulaRecord other) {
		return field_7_parsed_expr.isSame(other.field_7_parsed_expr);
	}
}
