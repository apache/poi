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
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.formula.WorkbookDependentFormula;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.util.LittleEndian;

/**
 * Title:        Reference 3D Ptg <P>
 * Description:  Defined a cell in extern sheet. <P>
 * REFERENCE:  <P>
 * @author Libin Roman (Vista Portal LDT. Developer)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 1.0-pre
 */
public final class Ref3DPtg extends RefPtgBase implements WorkbookDependentFormula {
    public final static byte sid  = 0x3a;

    private final static int  SIZE = 7; // 6 + 1 for Ptg
    private int             field_1_index_extern_sheet;

    /** Creates new AreaPtg */
    public Ref3DPtg() {}

    public Ref3DPtg(RecordInputStream in) {
        field_1_index_extern_sheet = in.readShort();
        readCoordinates(in);
    }
    
    public Ref3DPtg(String cellref, int externIdx ) {
        CellReference c= new CellReference(cellref);
        setRow(c.getRow());
        setColumn(c.getCol());
        setColRelative(!c.isColAbsolute());
        setRowRelative(!c.isRowAbsolute());   
        setExternSheetIndex(externIdx);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append(" [");
        sb.append("sheetIx=").append(getExternSheetIndex());
        sb.append(" ! ");
        sb.append(formatReferenceAsString());
        sb.append("]");
        return sb.toString();
    }

    public void writeBytes(byte [] array, int offset) {
        LittleEndian.putByte(array, 0 + offset, sid + getPtgClass());
        LittleEndian.putUShort(array, 1 + offset, getExternSheetIndex());
        writeCoordinates(array, offset + 3);
    }

    public int getSize() {
        return SIZE;
    }

    public int getExternSheetIndex(){
        return field_1_index_extern_sheet;
    }

    public void setExternSheetIndex(int index){
        field_1_index_extern_sheet = index;
    }

    /**
     * @return text representation of this cell reference that can be used in text 
     * formulas. The sheet name will get properly delimited if required.
     */
    public String toFormulaString(FormulaRenderingWorkbook book) {
        return ExternSheetNameResolver.prependSheetName(book, field_1_index_extern_sheet, formatReferenceAsString());
    }
    public String toFormulaString() {
        throw new RuntimeException("3D references need a workbook to determine formula text");
    }
}
