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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.BitField;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.record.RecordInputStream;

/**
 * RefError - handles deleted cell reference
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class RefErrorPtg extends OperandPtg {

    private final static int SIZE = 5;
    public final static byte sid  = 0x2a;
    private int              field_1_reserved;

    private RefErrorPtg() {
      //Required for clone methods
    }
    
    public RefErrorPtg(RecordInputStream in)
    {
        field_1_reserved = in.readInt();

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer("[RefError]\n");

        buffer.append("reserved = ").append(getReserved()).append("\n");
        return buffer.toString();
    }

    public void writeBytes(byte [] array, int offset)
    {
        array[offset] = (byte) (sid + ptgClass);
        LittleEndian.putInt(array,offset+1,field_1_reserved);
    }

    public void setReserved(int reserved)
    {
        field_1_reserved = reserved;
    }

    public int getReserved()
    {
        return field_1_reserved;
    }

    public int getSize()
    {
        return SIZE;
    }

    public String toFormulaString(HSSFWorkbook book)
    {
        //TODO -- should we store a cellreference instance in this ptg?? but .. memory is an issue, i believe!
        return "#REF!";
    }
    
    public byte getDefaultOperandClass() {
        return Ptg.CLASS_REF;
    }
    
    public Object clone() {
      RefErrorPtg ptg = new RefErrorPtg();
      ptg.field_1_reserved = field_1_reserved;
      ptg.setClass(ptgClass);
      return ptg;
    }
}
