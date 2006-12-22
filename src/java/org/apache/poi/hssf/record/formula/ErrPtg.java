
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

import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.usermodel.HSSFErrorConstants;

/**
 * @author Daniel Noll (daniel at nuix dot com dot au)
 */
public class ErrPtg extends Ptg
{
    public static final short sid  = 0x1c;
    private static final int  SIZE = 7;
    private byte              field_1_error_code;

    /** Creates new ErrPtg */

    public ErrPtg()
    {
    }

    public ErrPtg(RecordInputStream in)
    {
        field_1_error_code = in.readByte();
    }

    public void writeBytes(byte [] array, int offset)
    {
        array[offset] = (byte) (sid + ptgClass);
        array[offset + 1] = field_1_error_code;
    }

    public String toFormulaString(Workbook book)
    {
        switch(field_1_error_code)
        {
            case HSSFErrorConstants.ERROR_NULL:
                return "#NULL!";
            case HSSFErrorConstants.ERROR_DIV_0:
                return "#DIV/0!";
            case HSSFErrorConstants.ERROR_VALUE:
                return "#VALUE!";
            case HSSFErrorConstants.ERROR_REF:
                return "#REF!";
            case HSSFErrorConstants.ERROR_NAME:
                return "#NAME?";
            case HSSFErrorConstants.ERROR_NUM:
                return "#NUM!";
            case HSSFErrorConstants.ERROR_NA:
                return "#N/A";
        }

        // Shouldn't happen anyway.  Excel docs say that this is returned for all other codes.
        return "#N/A";
    }

    public int getSize()
    {
        return SIZE;
    }

    public byte getDefaultOperandClass()
    {
        return Ptg.CLASS_VALUE;
    }

    public Object clone() {
        ErrPtg ptg = new ErrPtg();
        ptg.field_1_error_code = field_1_error_code;
        return ptg;
    }
}
