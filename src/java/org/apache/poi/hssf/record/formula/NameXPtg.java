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

package org.apache.poi.hssf.record.formula;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hssf.model.Workbook;

/**
 *
 * @author  aviks
 */

public class NameXPtg extends Ptg
{
    public final static short sid  = 0x39;
    private final static int  SIZE = 7;
    private short             field_1_ixals;   // index to externsheet record
    private short             field_2_ilbl;    //index to name or externname table(1 based)
    private short            field_3_reserved;   // reserved must be 0


    private NameXPtg() {
      //Required for clone methods
    }

    /** Creates new NamePtg */

    public NameXPtg(String name)
    {
        //TODO
    }

    /** Creates new NamePtg */

    public NameXPtg(byte[] data, int offset)
    {
        offset++;
        field_1_ixals        = LittleEndian.getShort(data, offset);
        field_2_ilbl        = LittleEndian.getShort(data, offset + 2);
        field_3_reserved = LittleEndian.getShort(data, offset +4);
        
        //field_2_reserved = LittleEndian.getByteArray(data, offset + 12,12);
    }

    public void writeBytes(byte [] array, int offset)
    {
        array[ offset + 0 ] = (byte)(sid + ptgClass);
        LittleEndian.putShort(array, offset + 1, field_1_ixals);
        LittleEndian.putShort(array,offset+3, field_2_ilbl);
        LittleEndian.putShort(array, offset + 5, field_3_reserved);
    }

    public int getSize()
    {
        return SIZE;
    }

    public String toFormulaString(Workbook book)
    {
        return "NO IDEA - NAME";
    }
    
    public byte getDefaultOperandClass() {return Ptg.CLASS_VALUE;}

    public Object clone() {
      NameXPtg ptg = new NameXPtg();
      ptg.field_1_ixals = field_1_ixals;
      ptg.field_3_reserved = field_3_reserved;
      ptg.field_2_ilbl = field_2_ilbl;
      ptg.setClass(ptgClass);
      return ptg;
    }
}
