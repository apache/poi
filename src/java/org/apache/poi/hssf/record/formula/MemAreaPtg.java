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
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.hssf.record.RecordInputStream;

/**
 * @author Daniel Noll (daniel at nuix dot com dot au)
 */
public class MemAreaPtg extends OperandPtg {
    public final static short sid  = 0x26;
    private final static int  SIZE = 7;
    private int               field_1_reserved;
    private short             field_2_subex_len;

    /** Creates new MemAreaPtg */

    public MemAreaPtg()
    {
    }

    public MemAreaPtg(RecordInputStream in)
    {
        field_1_reserved  = in.readInt();
        field_2_subex_len = in.readShort();
    }

    public void setReserved(int res)
    {
        field_1_reserved = res;
    }

    public int getReserved()
    {
        return field_1_reserved;
    }

    public void setSubexpressionLength(short subexlen)
    {
        field_2_subex_len = subexlen;
    }

    public short getSubexpressionLength()
    {
        return field_2_subex_len;
    }

    public void writeBytes(byte [] array, int offset)
    {
        array[offset] = (byte) (sid + ptgClass);
        LittleEndian.putInt(array, offset + 1, field_1_reserved);
        LittleEndian.putShort(array, offset + 5, field_2_subex_len);
    }

    public int getSize()
    {
        return SIZE;
    }

    public String toFormulaString(Workbook book)
    {
        return ""; // TODO: Not sure how to format this. -- DN
    }

    public byte getDefaultOperandClass() {return Ptg.CLASS_VALUE;}

    public Object clone() {
      MemAreaPtg ptg = new MemAreaPtg();
      ptg.field_1_reserved = field_1_reserved;
      ptg.field_2_subex_len = field_2_subex_len;
      return ptg;
    }
}
