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

import java.util.List;
import java.util.Stack;

import org.apache.poi.hssf.record.formula.AreaNPtg;
import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.RefNPtg;
import org.apache.poi.hssf.record.formula.RefPtg;

/**
 * Title:        SharedFormulaRecord
 * Description:  Primarily used as an excel optimization so that multiple similar formulas
 *               are not written out too many times.  We should recognize this record and
 *               serialize as is since this is used when reading templates.
 * <p>
 * Note: the documentation says that the SID is BC where biffviewer reports 4BC.  The hex dump shows
 * that the two byte sid representation to be 'BC 04' that is consistent with the other high byte
 * record types.
 * @author Danny Mui at apache dot org
 */
public final class SharedFormulaRecord extends Record {
    public final static short   sid = 0x04BC;

    private int               field_1_first_row;
    private int               field_2_last_row;
    private short             field_3_first_column;
    private short             field_4_last_column;
    private int               field_5_reserved;
    private short             field_6_expression_len;
    private Stack             field_7_parsed_expr;

    public SharedFormulaRecord()
    {
    }

    /**
     * @param in the RecordInputstream to read the record from
     */

    public SharedFormulaRecord(RecordInputStream in)
    {
          super(in);
    }

    protected void validateSid(short id)
    {
        if (id != this.sid)
        {
            throw new RecordFormatException("Not a valid SharedFormula");
        }
    }

    public int getFirstRow() {
      return field_1_first_row;
    }

    public int getLastRow() {
      return field_2_last_row;
    }

    public short getFirstColumn() {
      return field_3_first_column;
    }

    public short getLastColumn() {
      return field_4_last_column;
    }

    public short getExpressionLength()
    {
        return field_6_expression_len;
    }

    /**
     * spit the record out AS IS.  no interperatation or identification
     */

    public int serialize(int offset, byte [] data)
    {
        //Because this record is converted to individual Formula records, this method is not required.
        throw new UnsupportedOperationException("Cannot serialize a SharedFormulaRecord");
    }

    public int getRecordSize()
    {
        //Because this record is converted to individual Formula records, this method is not required.
        throw new UnsupportedOperationException("Cannot get the size for a SharedFormulaRecord");

    }

    /**
     * print a sort of string representation ([SHARED FORMULA RECORD] id = x [/SHARED FORMULA RECORD])
     */

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[SHARED FORMULA RECORD:" + Integer.toHexString(sid) + "]\n");
        buffer.append("    .id        = ").append(Integer.toHexString(sid))
            .append("\n");
        buffer.append("    .first_row       = ")
            .append(Integer.toHexString(getFirstRow())).append("\n");
        buffer.append("    .last_row    = ")
            .append(Integer.toHexString(getLastRow()))
            .append("\n");
        buffer.append("    .first_column       = ")
            .append(Integer.toHexString(getFirstColumn())).append("\n");
        buffer.append("    .last_column    = ")
            .append(Integer.toHexString(getLastColumn()))
            .append("\n");
        buffer.append("    .reserved    = ")
            .append(Integer.toHexString(field_5_reserved))
            .append("\n");
        buffer.append("    .expressionlength= ").append(getExpressionLength())
            .append("\n");

        buffer.append("    .numptgsinarray  = ").append(field_7_parsed_expr.size())
              .append("\n");

        for (int k = 0; k < field_7_parsed_expr.size(); k++ ) {
           buffer.append("Formula ")
                .append(k)
                .append("\n")
                .append(field_7_parsed_expr.get(k).toString())
                .append("\n");
        }

        buffer.append("[/SHARED FORMULA RECORD]\n");
        return buffer.toString();
    }

    public short getSid()
    {
        return sid;
    }

    protected void fillFields(RecordInputStream in)
    {
      field_1_first_row       = in.readUShort();
      field_2_last_row        = in.readUShort();
      field_3_first_column    = in.readUByte();
      field_4_last_column     = in.readUByte();
      field_5_reserved        = in.readShort();
      field_6_expression_len = in.readShort();
      field_7_parsed_expr    = getParsedExpressionTokens(in);
    }

    private Stack getParsedExpressionTokens(RecordInputStream in)
    {
        Stack stack = new Stack();

        while (in.remaining() != 0) {
            Ptg ptg = Ptg.createPtg(in);
            stack.push(ptg);
        }
        return stack;
    }

    /**
     * Are we shared by the supplied formula record?
     */
    public boolean isFormulaInShared(FormulaRecord formula) {
      final int formulaRow = formula.getRow();
      final int formulaColumn = formula.getColumn();
      return ((getFirstRow() <= formulaRow) && (getLastRow() >= formulaRow) &&
          (getFirstColumn() <= formulaColumn) && (getLastColumn() >= formulaColumn));
    }

    /**
     * Creates a non shared formula from the shared formula
     * counter part
     */
    protected static Stack convertSharedFormulas(Stack ptgs, int formulaRow, int formulaColumn) {
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
        Stack newPtgStack = new Stack();

        if (ptgs != null)
          for (int k = 0; k < ptgs.size(); k++) {
            Ptg ptg = (Ptg) ptgs.get(k);
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
            }
            if (!ptg.isBaseToken()) {
                ptg.setClass(originalOperandClass);
            }

            newPtgStack.add(ptg);
        }
        return newPtgStack;
    }

    /**
     * Creates a non shared formula from the shared formula
     * counter part
     */
    public void convertSharedFormulaRecord(FormulaRecord formula) {
      //Sanity checks
        if (!isFormulaInShared(formula)) {
            throw new RuntimeException("Shared Formula Conversion: Coding Error");
        }
        final int formulaRow = formula.getRow();
        final int formulaColumn = formula.getColumn();

        List ptgList =  convertSharedFormulas(field_7_parsed_expr, formulaRow, formulaColumn);
        Ptg[] ptgs = new Ptg[ptgList.size()];
        ptgList.toArray(ptgs);
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

    /**
     * Mirroring formula records so it is registered in the ValueRecordsAggregate
     */
    public boolean isInValueSection()
    {
         return true;
    }


     /**
      * Register it in the ValueRecordsAggregate so it can go into the FormulaRecordAggregate
      */
     public boolean isValue() {
         return true;
     }

    public Object clone() {
        //Because this record is converted to individual Formula records, this method is not required.
        throw new UnsupportedOperationException("Cannot clone a SharedFormulaRecord");
    }
}
