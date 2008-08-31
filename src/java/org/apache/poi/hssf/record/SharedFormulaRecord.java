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

import org.apache.poi.hssf.record.formula.AreaNPtg;
import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.RefNPtg;
import org.apache.poi.hssf.record.formula.RefPtg;
import org.apache.poi.util.HexDump;

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
    private Ptg[] field_7_parsed_expr;

    public SharedFormulaRecord() {
        field_7_parsed_expr = Ptg.EMPTY_PTG_ARRAY;
    }

    /**
     * @param in the RecordInputstream to read the record from
     */
    public SharedFormulaRecord(RecordInputStream in) {
        super(in);
        field_5_reserved        = in.readShort();
        int field_6_expression_len = in.readShort();
        field_7_parsed_expr = Ptg.readTokens(field_6_expression_len, in);
    }
    protected void serializeExtraData(int offset, byte[] data) {
        //Because this record is converted to individual Formula records, this method is not required.
        throw new UnsupportedOperationException("Cannot serialize a SharedFormulaRecord");
    }
    
    protected int getExtraDataSize() {
        //Because this record is converted to individual Formula records, this method is not required.
        throw new UnsupportedOperationException("Cannot get the size for a SharedFormulaRecord");

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

        for (int k = 0; k < field_7_parsed_expr.length; k++ ) {
           buffer.append("Formula[").append(k).append("]");
           Ptg ptg = field_7_parsed_expr[k];
           buffer.append(ptg.toString()).append(ptg.getRVAType()).append("\n");
        }

        buffer.append("[/SHARED FORMULA]\n");
        return buffer.toString();
    }

    public short getSid() {
        return sid;
    }

    /**
     * Creates a non shared formula from the shared formula
     * counter part
     */
    protected static Ptg[] convertSharedFormulas(Ptg[] ptgs, int formulaRow, int formulaColumn) {
        if(false) {
            /*
             * TODO - (May-2008) Stop converting relative ref Ptgs in shared formula records.
             * If/when POI writes out the workbook, this conversion makes an unnecessary diff in the BIFF records.
             * Disabling this code breaks one existing junit.
             * Some fix-up will be required to make Ptg.toFormulaString(HSSFWorkbook) work properly.
             * That method will need 2 extra params: rowIx and colIx.
             */
            return ptgs;
        }
        Ptg[] newPtgStack = new Ptg[ptgs.length];

        for (int k = 0; k < ptgs.length; k++) {
            Ptg ptg = ptgs[k];
            byte originalOperandClass = -1;
            if (!ptg.isBaseToken()) {
                originalOperandClass = ptg.getPtgClass();
            }
            if (ptg instanceof RefNPtg) {
              RefNPtg refNPtg = (RefNPtg)ptg;
              ptg = new RefPtg(fixupRelativeRow(formulaRow,refNPtg.getRow(),refNPtg.isRowRelative()),
                                     fixupRelativeColumn(formulaColumn,refNPtg.getColumn(),refNPtg.isColRelative()),
                                     refNPtg.isRowRelative(),
                                     refNPtg.isColRelative());
            } else if (ptg instanceof AreaNPtg) {
              AreaNPtg areaNPtg = (AreaNPtg)ptg;
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
     * Creates a non shared formula from the shared formula
     * counter part
     */
    public void convertSharedFormulaRecord(FormulaRecord formula) {
        int formulaRow = formula.getRow();
        int formulaColumn = formula.getColumn();
        //Sanity checks
        if (!isInRange(formulaRow, formulaColumn)) {
            throw new RuntimeException("Shared Formula Conversion: Coding Error");
        }

        Ptg[] ptgs = convertSharedFormulas(field_7_parsed_expr, formulaRow, formulaColumn);
        formula.setParsedExpression(ptgs);
        //Now its not shared!
        formula.setSharedFormula(false);
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
        //Because this record is converted to individual Formula records, this method is not required.
        throw new UnsupportedOperationException("Cannot clone a SharedFormulaRecord");
    }
}
