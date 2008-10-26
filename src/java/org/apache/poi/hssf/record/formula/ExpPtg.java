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

package org.apache.poi.hssf.record.formula;

import org.apache.poi.hssf.record.RecordFormatException;
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
    private final short            field_1_first_row;
    private final short            field_2_first_col;

    public ExpPtg(LittleEndianInput in) 
    {
      field_1_first_row = in.readShort();
      field_2_first_col = in.readShort();
    }

    public void write(LittleEndianOutput out) {
        out.writeByte(sid + getPtgClass());
        out.writeShort(field_1_first_row);
        out.writeShort(field_2_first_col);
    }

    public int getSize()
    {
        return SIZE;
    }

    public short getRow() {
      return field_1_first_row;
    }

    public short getColumn() {
      return field_2_first_col;
    }

    public String toFormulaString()
    {
        throw new RecordFormatException("Coding Error: Expected ExpPtg to be converted from Shared to Non-Shared Formula by ValueRecordsAggregate, but it wasn't");
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer("[Array Formula or Shared Formula]\n");
        buffer.append("row = ").append(getRow()).append("\n");
        buffer.append("col = ").append(getColumn()).append("\n");
        return buffer.toString();
    }
}
