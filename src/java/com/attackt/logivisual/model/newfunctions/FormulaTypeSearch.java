package com.attackt.logivisual.model.newfunctions;

/**
 * 搜索类函数
 */
public enum FormulaTypeSearch {
    ADDRESS(111),
    AREAS(112),
    CHOOSE(113),
    INDEX(114),
    INDIRECT(115),
    OFFSET(116),
    VLOOKUP(117),
    HLOOKUP(118),
    LOOKUP(119),
    MATCH(120);
    // 成员变量
    private int index;
    // 构造方法
    FormulaTypeSearch(int index) {
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
        for (FormulaTypeSearch temp: FormulaTypeSearch.values()) {
            if(temp.name().equals(str.toUpperCase()))
            {
                return true;
            }
        }
        return false;
    }
}
