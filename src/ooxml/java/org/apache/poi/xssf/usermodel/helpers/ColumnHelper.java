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

package org.apache.poi.xssf.usermodel.helpers;


import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;


public class ColumnHelper {
    
    private CTWorksheet worksheet;
    

    public ColumnHelper(CTWorksheet worksheet) {
        super();
        this.worksheet = worksheet;
        cleanColumns();
    }

    public void cleanColumns() {
        CTCols newCols = CTCols.Factory.newInstance();
        CTCols[] colsArray = worksheet.getColsArray();
        int i = 0;
        for (i = 0 ; i < colsArray.length ; i++) {
            CTCols cols = colsArray[i];
            CTCol[] colArray = cols.getColArray();
            for (int y = 0 ; y < colArray.length ; y++) {
                 CTCol col = colArray[y];
                 for (long k = col.getMin() ; k <= col.getMax() ; k++) {
                     if (!columnExists(newCols, k)) {
                         CTCol newCol = newCols.addNewCol();
                         newCol.setMin(k);
                         newCol.setMax(k);
                         setColumnAttributes(col, newCol);
                     }
                 }
            }
        }
        for (int y = i-1 ; y >= 0 ; y--) {
            worksheet.removeCols(y);
        }
        worksheet.addNewCols();
        worksheet.setColsArray(0, newCols);
    }
    
    public CTCol getColumn(long index) {
        for (int i = 0 ; i < worksheet.getColsArray(0).sizeOfColArray() ; i++) {
            if (worksheet.getColsArray(0).getColArray(i).getMin() == index) {
                return worksheet.getColsArray(0).getColArray(i);
            }
        }
        return  null;
    }
    
    public boolean columnExists(CTCols cols, long index) {
        for (int i = 0 ; i < cols.sizeOfColArray() ; i++) {
            if (cols.getColArray(i).getMin() == index) {
                return true;
            }
        }
        return false;
    }
    
    public void setColumnAttributes(CTCol col, CTCol newCol) {
        if (col.getWidth() != 0) {
            newCol.setWidth(col.getWidth());
        }
        if (col.getHidden()) {
            newCol.setHidden(true);
        }
    }
    
}
