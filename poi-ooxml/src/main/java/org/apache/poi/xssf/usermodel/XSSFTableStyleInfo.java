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
    private final CTTableStyleInfo styleInfo;
    private final StylesTable stylesTable;
    private TableStyle style;
    private boolean columnStripes;
    private boolean rowStripes;
    private boolean firstColumn;
    private boolean lastColumn;

    public XSSFTableStyleInfo(StylesTable stylesTable, CTTableStyleInfo tableStyleInfo) {
        this.columnStripes = tableStyleInfo.getShowColumnStripes();
        this.rowStripes = tableStyleInfo.getShowRowStripes();
        this.firstColumn = tableStyleInfo.getShowFirstColumn();
        this.lastColumn = tableStyleInfo.getShowLastColumn();
        this.style = stylesTable.getTableStyle(tableStyleInfo.getName());
        this.stylesTable = stylesTable;
        this.styleInfo = tableStyleInfo;
    }

    @Override
    public boolean isShowColumnStripes() {
        return columnStripes;
    }
    public void setShowColumnStripes(boolean show) {
        this.columnStripes = show;
        styleInfo.setShowColumnStripes(show);
    }

    @Override
    public boolean isShowRowStripes() {
        return rowStripes;
    }
    public void setShowRowStripes(boolean show) {
        this.rowStripes = show;
        styleInfo.setShowRowStripes(show);
    }

    @Override
    public boolean isShowFirstColumn() {
        return firstColumn;
    }
    public void setFirstColumn(boolean showFirstColumn) {
        this.firstColumn = showFirstColumn;
        styleInfo.setShowFirstColumn(showFirstColumn);
    }

    @Override
    public boolean isShowLastColumn() {
        return lastColumn;
    }
    public void setLastColumn(boolean showLastColumn) {
        this.lastColumn = showLastColumn;
        styleInfo.setShowLastColumn(showLastColumn);
    }

    @Override
    public String getName() {
        return style.getName();
    }
    public void setName(String name) {
        styleInfo.setName(name);
        style = stylesTable.getTableStyle(name);
    }

    @Override
    public TableStyle getStyle() {
        return style;
    }
}
