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

package org.apache.poi.ss.formula.ptg;

import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * This ptg indicates a data table.
 * It only occurs in a FORMULA record, never in an
 *  ARRAY or NAME record.  When ptgTbl occurs in a
 *  formula, it is the only token in the formula.
 *
 * This indicates that the cell containing the
 *  formula is an interior cell in a data table;
 *  the table description is found in a TABLE
 *  record. Rows and columns which contain input
 *  values to be substituted in the table do
 *  not contain ptgTbl.
 * See page 811 of the june 08 binary docs.
 */
public final class TblPtg extends ControlPtg {
    private final static int  SIZE = 5;
    public final static short sid  = 0x02;
    /** The row number of the upper left corner */
    private final int field_1_first_row;
    /** The column number of the upper left corner */
    private final int field_2_first_col;

    public TblPtg(LittleEndianInput in)  {
      field_1_first_row = in.readUShort();
      field_2_first_col = in.readUShort();
    }

    public void write(LittleEndianOutput out) {
        out.writeByte(sid + getPtgClass());
        out.writeShort(field_1_first_row);
        out.writeShort(field_2_first_col);
    }

    public int getSize() {
        return SIZE;
    }

    public int getRow() {
      return field_1_first_row;
    }

    public int getColumn() {
      return field_2_first_col;
    }

    public String toFormulaString()
    {
        // table(....)[][]
        throw new RuntimeException("Table and Arrays are not yet supported");
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("[Data Table - Parent cell is an interior cell in a data table]\n");
        buffer.append("top left row = ").append(getRow()).append("\n");
        buffer.append("top left col = ").append(getColumn()).append("\n");
        return buffer.toString();
    }
}
