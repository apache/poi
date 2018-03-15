package com.attackt.logivisual.model.newfunctions;

/**
 * B2类公式
 */
public enum FormulaTypeB2 {
    MMULT(108),
    MINVERSE(109),
    TRANSPOSE(110);
    // 成员变量
    private int index;
    // 构造方法
    FormulaTypeB2(int index) {
        this.index = index;
    }
    //覆盖方法
    @Override
    public String toString() {
        return this.index+"";
    }
    /**
     * 搜索
     * @param str
     * @return 返回值
     */
    public static boolean isContainStr(String str)
    {
        for (FormulaTypeB2 temp:FormulaTypeB2.values()) {
            if(temp.name().equals(str.toUpperCase()))
            {
                return true;
            }
        }
        return false;
    }
}
