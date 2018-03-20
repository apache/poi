package com.attackt.logivisual.model.newfunctions;

import java.util.List;

/**
 * 外层cell对象供拼接json使用
 */
public class CellJsonObject {
    private String cellId;
    private String bolanStr;
    private String formula;
    private int status;
    private String formulaType;
    private List<SourceExcelInfo> data;
    private List<String> sheetNames;

    public String getCellId() {
        return cellId;
    }

    public void setCellId(String cellId) {
        this.cellId = cellId;
    }

    public String getBolanStr() {
        return bolanStr;
    }

    public void setBolanStr(String bolanStr) {
        this.bolanStr = bolanStr;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public List<SourceExcelInfo> getData() {
        return data;
    }

    public void setData(List<SourceExcelInfo> data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<String> getSheetNames() {
        return sheetNames;
    }

    public void setSheetNames(List<String> sheetNames) {
        this.sheetNames = sheetNames;
    }

    public String getFormulaType() {
        return formulaType;
    }

    public void setFormulaType(String formulaType) {
        this.formulaType = formulaType;
    }
}
