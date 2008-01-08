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
package org.apache.poi.hssf.usermodel;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellType;

/**
 * User facing wrapper around an underlying cell object
 */
public class HSSFXMLCell {
    private CTCell cell;

    /** The workbook to which this cell belongs */
    private final HSSFXMLWorkbook workbook;

    public HSSFXMLCell(CTCell rawCell, HSSFXMLWorkbook workbook) {
        this.cell = rawCell;
        this.workbook = workbook;
    }

    /**
     * Formats the cell's contents, based on its type,
     *  and returns it as a string.
     */
    public String getStringValue() {

        switch (cell.getT().intValue()) {
        case STCellType.INT_S:
            return this.workbook.getSharedString(Integer.valueOf(cell.getV()));
        case STCellType.INT_N:
            return cell.getV();
        // TODO: support other types
        default:
            return "UNSUPPORTED CELL TYPE: '" + cell.getT() + "'";
        }
    }

    public String toString() {
        return cell.getR() + " - " + getStringValue(); 
    }
}
