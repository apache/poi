package com.attackt.logivisual.model.retry;

import org.apache.poi.ss.formula.ptg.Ptg;

/**
 * 数值对象表
 */
public class KeyValueFormula {
    private String formula;
    private String result;
    private Ptg ptg;
    // 1为普通 2为不变
    private int formulaType=1;


    public KeyValueFormula(String formula, String result, Ptg ptg) {
        this.formula = formula;
        this.result = result;
        this.ptg = ptg;
    }

    public KeyValueFormula(String formula, int formulaType) {
        this.formula = formula;
        this.formulaType = formulaType;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Ptg getPtg() {
        return ptg;
    }

    public void setPtg(Ptg ptg) {
        this.ptg = ptg;
    }

    public int getFormulaType() {
        return formulaType;
    }

    public void setFormulaType(int formulaType) {
        this.formulaType = formulaType;
    }
}
