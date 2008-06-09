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
 *
 * @author  aviks
 */
public final class NameXPtg extends OperandPtg {
    public final static short sid  = 0x39;
    private final static int  SIZE = 7;
    private short             field_1_ixals;   // index to REF entry in externsheet record
    private short             field_2_ilbl;    //index to defined name or externname table(1 based)
    private short            field_3_reserved;   // reserved must be 0


    public NameXPtg(RecordInputStream in) {
        field_1_ixals        = in.readShort();
        field_2_ilbl        = in.readShort();
        field_3_reserved = in.readShort();
    }

    public void writeBytes(byte [] array, int offset) {
        array[ offset + 0 ] = (byte)(sid + getPtgClass());
        LittleEndian.putShort(array, offset + 1, field_1_ixals);
        LittleEndian.putShort(array,offset+3, field_2_ilbl);
        LittleEndian.putShort(array, offset + 5, field_3_reserved);
    }

    public int getSize() {
        return SIZE;
    }

    public String toFormulaString(Workbook book)
    {
        // -1 to convert definedNameIndex from 1-based to zero-based
        return book.resolveNameXText(field_1_ixals, field_2_ilbl-1); 
    }
    
    public byte getDefaultOperandClass() {
		return Ptg.CLASS_VALUE;
	}
}
