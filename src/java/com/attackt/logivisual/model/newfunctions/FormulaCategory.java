package com.attackt.logivisual.model.newfunctions;

/**
 * 公式类型
 */
public enum FormulaCategory {
    FormulaTypeA(1),
    FormulaTypeB1(2),
    FormulaTypeB2(3),
    FormulaTypeSerach(4);
    // 成员变量
    private int index;
    // 构造方法
    FormulaCategory(int index) {
        this.index = index;
    }
    //覆盖方法
    @Override
    public String toString() {
        return this.index+"";
    }
}
