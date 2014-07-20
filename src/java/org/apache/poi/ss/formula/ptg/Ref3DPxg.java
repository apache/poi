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

import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.LittleEndianOutput;

/**
 * <p>Title:        XSSF 3D Reference</p>
 * <p>Description:  Defines a cell in an external or different sheet.</p>
 * <p>REFERENCE: </p>
 * 
 * <p>This is XSSF only, as it stores the sheet / book references
 *  in String form. The HSSF equivalent using indexes is {@link Ref3DPtg}</p>
 */
public final class Ref3DPxg extends RefPtgBase implements Pxg {
    private int externalWorkbookNumber = -1;
    private String sheetName;

    public Ref3DPxg(int externalWorkbookNumber, String sheetName, String cellref) {
        this(externalWorkbookNumber, sheetName, new CellReference(cellref));
    }
    public Ref3DPxg(int externalWorkbookNumber, String sheetName, CellReference c) {
        super(c);
        this.externalWorkbookNumber = externalWorkbookNumber;
        this.sheetName = sheetName;
    }
    
    public Ref3DPxg(String sheetName, String cellref) {
        this(sheetName, new CellReference(cellref));
    }
    public Ref3DPxg(String sheetName, CellReference c) {
        this(-1, sheetName, c);
    }

    public String toString() {
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
        sb.append(formatReferenceAsString());
        sb.append("]");
        return sb.toString();
    }
    
    public int getExternalWorkbookNumber() {
        return externalWorkbookNumber;
    }
    public String getSheetName() {
        return sheetName;
    }
    
    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }
    
    public String format2DRefAsString() {
        return formatReferenceAsString();
    }

    public String toFormulaString() {
        StringBuffer sb = new StringBuffer();
        if (externalWorkbookNumber >= 0) {
            sb.append('[');
            sb.append(externalWorkbookNumber);
            sb.append(']');
        }
        sb.append(sheetName);
        sb.append('!');
        sb.append(formatReferenceAsString());
        return sb.toString();
    }
    
    public int getSize() {
        return 1;
    }
    public void write(LittleEndianOutput out) {
        throw new IllegalStateException("XSSF-only Ptg, should not be serialised");
    }
}
