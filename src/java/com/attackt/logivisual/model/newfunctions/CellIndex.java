package com.attackt.logivisual.model.newfunctions;

/**
 * 配合area
 */
public class CellIndex {
    private String sheetName;
    private int sheetIndex;
    private int rowIndex;
    private int columnIndex;
    public CellIndex() {
    }

    public CellIndex(int rowIndex, int columnIndex,int sheetIndex, String sheetName) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.sheetName = sheetName;
        this.sheetIndex = sheetIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public String getSheetName() {
        return sheetName;
    }

    public int getSheetIndex() {
        return sheetIndex;
    }
}
