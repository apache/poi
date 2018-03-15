package com.attackt.logivisual.model.service;

import com.attackt.logivisual.model.FormulaSplitInfo;

import java.util.List;

/**
 * excel 原生数组业务类
 */
public class SheetMultiplyData {
    private String sheetName;
    private String cellStr;
    private List<FormulaSplitInfo> formulaSplitInfos;

    public SheetMultiplyData(String sheetName, String cellStr, List<FormulaSplitInfo> formulaSplitInfos) {
        this.sheetName = sheetName;
        this.cellStr = cellStr;
        this.formulaSplitInfos=formulaSplitInfos;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getCellStr() {
        return cellStr;
    }

    public void setCellStr(String cellStr) {
        this.cellStr = cellStr;
    }

    public List<FormulaSplitInfo> getFormulaSplitInfos() {
        return formulaSplitInfos;
    }

    public void setFormulaSplitInfos(List<FormulaSplitInfo> formulaSplitInfos) {
        this.formulaSplitInfos = formulaSplitInfos;
    }
}
