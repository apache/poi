
/* ====================================================================
   Copyright 2003-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


/*
 * MemErrPtg.java
 *
 * Created on November 21, 2001, 8:46 AM
 */
package org.apache.poi.hssf.record.formula;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hssf.model.Workbook;

/**
 *
 * @author  andy
 * @author Jason Height (jheight at chariot dot net dot au)
 */

public class MemErrPtg
    extends Ptg
{
    public final static short sid  = 0x27;
    private final static int  SIZE = 7;
    private int               field_1_reserved;
    private short             field_2_subex_len;

    /** Creates new MemErrPtg */

    public MemErrPtg()
    {
    }

    public MemErrPtg(byte [] data, int offset)
    {
        field_1_reserved  = LittleEndian.getInt(data, 0);
        field_2_subex_len = LittleEndian.getShort(data, 4);
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
    }

    public int getSize()
    {
        return SIZE;
    }

    public String toFormulaString(Workbook book)
    {
        return "ERR#";
    }
    public byte getDefaultOperandClass() {return Ptg.CLASS_VALUE;}

    public Object clone() {
      MemErrPtg ptg = new MemErrPtg();
      ptg.field_1_reserved = field_1_reserved;
      ptg.field_2_subex_len = field_2_subex_len;
      return ptg;
    }
}
