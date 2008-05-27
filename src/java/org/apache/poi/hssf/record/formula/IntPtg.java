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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.record.RecordInputStream;

/**
 * Integer (unsigned short integer)
 * Stores an unsigned short value (java int) in a formula
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class IntPtg extends ScalarConstantPtg {
    // 16 bit unsigned integer
    private static final int MIN_VALUE = 0x0000;
    private static final int MAX_VALUE = 0xFFFF;
    
    /**
     * Excel represents integers 0..65535 with the tInt token. 
     * @return <code>true</code> if the specified value is within the range of values 
     * <tt>IntPtg</tt> can represent. 
     */
    public static boolean isInRange(int i) {
        return i>=MIN_VALUE && i <=MAX_VALUE;
    }

    public final static int  SIZE = 3;
    public final static byte sid  = 0x1e;
    private int            field_1_value;
  
    public IntPtg(RecordInputStream in) {
        this(in.readUShort());
    }


    public IntPtg(int value) {
        if(!isInRange(value)) {
            throw new IllegalArgumentException("value is out of range: " + value);
        }
        field_1_value = value;
    }

    public int getValue() {
        return field_1_value;
    }


    public void writeBytes(byte [] array, int offset)
    {
        array[ offset + 0 ] = sid;
        LittleEndian.putUShort(array, offset + 1, getValue());
    }

    public int getSize() {
        return SIZE;
    }

    public String toFormulaString(HSSFWorkbook book) {
        return String.valueOf(getValue());
    }

    public Object clone() {
     return new IntPtg(field_1_value);
    }
    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(getClass().getName()).append(" [");
        sb.append(field_1_value);
        sb.append("]");
        return sb.toString();
    }
}
