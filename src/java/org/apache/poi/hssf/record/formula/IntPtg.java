
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


/*
 * IntPtg.java
 *
 * Created on October 29, 2001, 7:37 PM
 */
package org.apache.poi.hssf.record.formula;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.RecordInputStream;

/**
 * Integer (unsigned short intger)
 * Stores an unsigned short value (java int) in a formula
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */

public class IntPtg
    extends Ptg
{
    public final static int  SIZE = 3;
    public final static byte sid  = 0x1e;
    private int            field_1_value;
  
    private IntPtg() {
      //Required for clone methods
    }

    public IntPtg(RecordInputStream in)
    {
        setValue(in.readUShort());
    }
    
    
    // IntPtg should be able to create itself, shouldnt have to call setValue
    public IntPtg(String formulaToken) {
        setValue(Integer.parseInt(formulaToken));
    }

    /**
     * Sets the wrapped value.
     * Normally you should call with a positive int.
     */
    public void setValue(int value)
    {
        if(value < 0 || value > (Short.MAX_VALUE + 1)*2 )
            throw new IllegalArgumentException("Unsigned short is out of range: " + value);
        field_1_value = value;
    }

    /**
     * Returns the value as a short, which may have
     *  been wrapped into negative numbers
     */
    public int getValue()
    {
        return field_1_value;
    }

    /**
     * Returns the value as an unsigned positive int.
     */
    public int getValueAsInt()
    {
    	if(field_1_value < 0) {
    		return (Short.MAX_VALUE + 1)*2 + field_1_value;
    	}
        return field_1_value;
    }

    public void writeBytes(byte [] array, int offset)
    {
        array[ offset + 0 ] = sid;
        LittleEndian.putUShort(array, offset + 1, getValue());
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
