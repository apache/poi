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

package org.apache.poi.ss.formula.ptg;

import org.apache.poi.ss.formula.SheetNameFormatter;
import org.apache.poi.util.LittleEndianOutput;

/**
 * A Name, be that a Named Range or a Function / User Defined
 *  Function, addressed in the HSSF External Sheet style.
 *  
 * <p>This is XSSF only, as it stores the sheet / book references
 *  in String form. The HSSF equivalent using indexes is {@link NameXPtg}</p>
 */
public final class NameXPxg extends OperandPtg implements Pxg {
    private int externalWorkbookNumber = -1;
    private String sheetName;
    private String nameName;

    public NameXPxg(int externalWorkbookNumber, String sheetName, String nameName) {
        this.externalWorkbookNumber = externalWorkbookNumber;
        this.sheetName = sheetName;
        this.nameName = nameName;
    }
    public NameXPxg(String sheetName, String nameName) {
        this(-1, sheetName, nameName);
    }
    public NameXPxg(String nameName) {
        this(-1, null, nameName);
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append(" [");
        if (externalWorkbookNumber >= 0) {
            sb.append(" [");
            sb.append("workbook=").append(getExternalWorkbookNumber());
            sb.append("] ");
        }
        sb.append("sheet=").append(getSheetName());
        sb.append(" ! ");
        sb.append("name=");
        sb.append(nameName);
        sb.append("]");
        return sb.toString();
    }

    public int getExternalWorkbookNumber() {
        return externalWorkbookNumber;
    }
    public String getSheetName() {
        return sheetName;
    }
    public String getNameName() {
        return nameName;
    }
    
    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String toFormulaString() {
        StringBuilder sb = new StringBuilder(64);
        boolean needsExclamation = false;
        if (externalWorkbookNumber >= 0) {
            sb.append('[');
            sb.append(externalWorkbookNumber);
            sb.append(']');
            needsExclamation = true;
        }
        if (sheetName != null) {
            SheetNameFormatter.appendFormat(sb, sheetName);
            needsExclamation = true;
        }
        if (needsExclamation) {
            sb.append('!');
        }
        sb.append(nameName);
        return sb.toString();
    }
    
    public byte getDefaultOperandClass() {
        return Ptg.CLASS_VALUE;
    }

    public int getSize() {
        return 1;
    }
    public void write(LittleEndianOutput out) {
        throw new IllegalStateException("XSSF-only Ptg, should not be serialised");
    }
}
