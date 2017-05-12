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
