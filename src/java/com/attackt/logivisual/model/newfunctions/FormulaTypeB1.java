package com.attackt.logivisual.model.newfunctions;

/**
 * B1类公式
 */
public enum FormulaTypeB1 {
    AVERAGE(59),
    AVEDEV(60),
    AVERAGEA(61),
    AVERAGEIF(62),
    AVERAGEIFS(63),
    CORREL(64),
    COUNT(65),
    COUNTA(66),
    COUNTBLANK(67),
    COUNTIF(68),
    COUNTIFS(69),
    COVAR(70),
    GEOMEAN(71),
    HARMEAN(72),
    INTERCEPT(73),
    KURT(74),
    LARGE(75),
    LINEST(76),
    LOGEST(77),
    PEARSON(78),
    PERMUT(79),
    PHI(80),
    PROB(81),
    RSQ(82),
    SKEW(83),
    SLOPE(84),
    SMALL(85),
    STANDARDIZE(86),
    STDEVA(87),
    STDEVPA(88),
    STEYX(89),
    TREND(90),
    TRIMMEAN(91),
    VARA(92),
    VAPPA(93),
    PRODUCT(94),
    MIN(95),
    MINA(96),
    MAX(97),
    MAXA(98),
    MEDIAN(99),
    SUM(100),
    SUMIF(101),
    SUMIFS(102),
    SUMPRODUCT(103),
    SUMSQ(104),
    SUMX2MY2(105),
    SUMX2PY2(106),
    SUMXMY2(107);
    // 成员变量
    private int index;
    // 构造方法
    FormulaTypeB1(int index) {
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
        for (FormulaTypeB1 temp: FormulaTypeB1.values()) {
            if(temp.name().equals(str.toUpperCase()))
            {
                return true;
            }
        }
        return false;
    }
}
