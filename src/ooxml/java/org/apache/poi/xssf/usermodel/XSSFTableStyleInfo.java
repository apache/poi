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

package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.TableStyle;
import org.apache.poi.ss.usermodel.TableStyleInfo;
import org.apache.poi.xssf.model.StylesTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;

/**
 * Wrapper for the CT class, to cache values and add style lookup
 */
public class XSSFTableStyleInfo implements TableStyleInfo {

    private final boolean columnStripes;
    private final boolean rowStripes;
    private final boolean firstColumn;
    private final boolean lastColumn;
    private final TableStyle style;
    
    /**
     * @param stylesTable 
     * @param tableStyleInfo 
     */
    public XSSFTableStyleInfo(StylesTable stylesTable, CTTableStyleInfo tableStyleInfo) {
        this.columnStripes = tableStyleInfo.getShowColumnStripes();
        this.rowStripes = tableStyleInfo.getShowRowStripes();
        this.firstColumn = tableStyleInfo.getShowFirstColumn();
        this.lastColumn = tableStyleInfo.getShowLastColumn();
        this.style = stylesTable.getTableStyle(tableStyleInfo.getName());
    }

    public boolean isShowColumnStripes() {
        return columnStripes;
    }

    public boolean isShowRowStripes() {
        return rowStripes;
    }

    public boolean isShowFirstColumn() {
        return firstColumn;
    }

    public boolean isShowLastColumn() {
        return lastColumn;
    }

    public String getName() {
        return style.getName();
    }

    public TableStyle getStyle() {
        return style;
    }

}
