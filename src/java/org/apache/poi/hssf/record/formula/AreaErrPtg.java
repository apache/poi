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

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.LittleEndian;

/**
 * AreaErr - handles deleted cell area references.
 *
 * @author Daniel Noll (daniel at nuix dot com dot au)
 */
public final class AreaErrPtg extends OperandPtg {
    public final static byte sid  = 0x2b;

    public AreaErrPtg(RecordInputStream in) {
    	// 8 bytes unused:
        in.readInt();
        in.readInt();
    }

    public void writeBytes(byte [] array, int offset) {
        array[offset] = (byte) (sid + getPtgClass());
        LittleEndian.putInt(array, offset+1, 0);
        LittleEndian.putInt(array, offset+5, 0);
    }

    public String toFormulaString(HSSFWorkbook book) {
        return "#REF!";
    }

	public byte getDefaultOperandClass() {
		return Ptg.CLASS_REF;
	}

	public int getSize() {
		return 9;
	}
}

