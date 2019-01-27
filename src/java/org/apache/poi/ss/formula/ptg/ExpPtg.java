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
 *
 * @author  andy
 * @author Jason Height (jheight at chariot dot net dot au)
 * @author dmui (save existing implementation)
 */
public final class ExpPtg extends ControlPtg {
    private final static int  SIZE = 5;
    public final static short sid  = 0x1;
    private final int field_1_first_row;
    private final int field_2_first_col;

    public ExpPtg(LittleEndianInput in) {
      field_1_first_row = in.readShort();
      field_2_first_col = in.readShort();
    }

    public ExpPtg(int firstRow, int firstCol) {
      this.field_1_first_row = firstRow;
      this.field_2_first_col = firstCol;
    }

    @Override
    public void write(LittleEndianOutput out) {
        out.writeByte(sid + getPtgClass());
        out.writeShort(field_1_first_row);
        out.writeShort(field_2_first_col);
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    public int getRow() {
      return field_1_first_row;
    }

    public int getColumn() {
      return field_2_first_col;
    }

    @Override
    public String toFormulaString() {
        throw new RuntimeException("Coding Error: Expected ExpPtg to be converted from Shared to Non-Shared Formula by ValueRecordsAggregate, but it wasn't");
    }

    @Override
    public String toString() {
        return "[Array Formula or Shared Formula]\n" + "row = " + getRow() + "\n" + "col = " + getColumn() + "\n";
    }
}
