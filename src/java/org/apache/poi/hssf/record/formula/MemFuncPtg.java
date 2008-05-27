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
 * Ptg.java
 *
 * Created on October 28, 2001, 6:30 PM
 */
package org.apache.poi.hssf.record.formula;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.record.RecordInputStream;

/**
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class MemFuncPtg extends OperandPtg {

    public final static byte sid = 0x29;
    private short field_1_len_ref_subexpression = 0;

    public MemFuncPtg()
    {
        //Required for clone methods
    }

    /**Creates new function pointer from a byte array
     * usually called while reading an excel file.
     */
    public MemFuncPtg( RecordInputStream in )
    {
        field_1_len_ref_subexpression = in.readShort();
    }

    public int getSize()
    {
        return 3;
    }

    public void writeBytes( byte[] array, int offset )
    {
        array[offset + 0] =  sid ;
        LittleEndian.putShort( array, offset + 1, (short)field_1_len_ref_subexpression );
    }

    public String toFormulaString(HSSFWorkbook book)
    {
        return "";
    }

    public byte getDefaultOperandClass()
    {
        return 0;
    }

    public int getNumberOfOperands()
    {
        return field_1_len_ref_subexpression;
    }

    public Object clone()
    {
        MemFuncPtg ptg = new MemFuncPtg();
        ptg.field_1_len_ref_subexpression = this.field_1_len_ref_subexpression;
        return ptg;
    }

    public int getLenRefSubexpression()
    {
        return field_1_len_ref_subexpression;
    }

    public void setLenRefSubexpression(int len)
    {
        field_1_len_ref_subexpression = (short)len;
    }

}