package com.attackt.logivisual.model.service;

/**
 * INDIRECT 业务类
 */
public class SheetIndirectData {
    private String sheetName;
    private String infoStr;

    public SheetIndirectData(String sheetName, String infoStr) {
        this.sheetName = sheetName;
        this.infoStr = infoStr;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getInfoStr() {
        return infoStr;
    }

    public void setInfoStr(String infoStr) {
        this.infoStr = infoStr;
    }
}
