
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
 * IntPtg.java
 *
 * Created on October 29, 2001, 7:37 PM
 */
package org.apache.poi.hssf.record.formula;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hssf.model.Workbook;

/**
 * Integer (short intger)
 * Stores a (java) short value in a formula
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */

public class IntPtg
    extends Ptg
{
    public final static int  SIZE = 3;
    public final static byte sid  = 0x1e;
    private short            field_1_value;

    private String val;
    private int strlen = 0;
  
    private IntPtg() {
      //Required for clone methods
    }

    public IntPtg(byte [] data, int offset)
    {
        setValue(LittleEndian.getShort(data, offset + 1));
    }
    
    
    // IntPtg should be able to create itself, shouldnt have to call setValue
    public IntPtg(String formulaToken) {
        setValue(Short.parseShort(formulaToken));
    }

    public void setValue(short value)
    {
        field_1_value = value;
    }

    public short getValue()
    {
        return field_1_value;
    }

    public void writeBytes(byte [] array, int offset)
    {
        array[ offset + 0 ] = sid;
        LittleEndian.putShort(array, offset + 1, getValue());
    }

    public int getSize()
    {
        return SIZE;
    }

    public String toFormulaString(Workbook book)
    {
        return "" + getValue();
    }
 public byte getDefaultOperandClass() {return Ptg.CLASS_VALUE;}   

   public Object clone() {
     IntPtg ptg = new IntPtg();
     ptg.field_1_value = field_1_value;
     return ptg;
   }
}
