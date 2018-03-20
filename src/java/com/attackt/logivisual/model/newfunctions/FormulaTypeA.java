package com.attackt.logivisual.model.newfunctions;

/**
 * A类公式
 */
public enum FormulaTypeA {
    ADD(1),
    SUBTRACT(2),
    MULTIPLY(3),
    DIVIDE(4),
    ABS(5),
    ACOS(6),
    ACOSH(7),
    ACOT(8),
    ACOTH(9),
    ARABIC(10),
    ASIN(11),
    ASINH(12),
    ATAN(13),
    ATAN2(14),
    ATANH(15),
    BASE(16),
    CEILING(17),
    COS(18),
    COSH(19),
    COT(20),
    COTH(21),
    CSC(22),
    CSCH(23),
    DECIMAL(24),
    DEGREES(25),
    EVEN(26),
    EXP(27),
    FACT(28),
    FACTDOUBLE(29),
    FLOOR(30),
    GCD(31),
    INT(32),
    LCM(33),
    LN(34),
    LOG(35),
    LOG10(36),
    MDETERM(37),
    MOD(38),
    MROUND(39),
    MULTINOMIAL(40),
    MUNIT(41),
    ODD(42),
    POWER(43),
    PRODUCT(44),
    QUOTIENT(45),
    RADIANS(46),
    ROMAN(47),
    ROUND(48),
    ROUNDDOWN(49),
    ROUNDUP(50),
    SEC(51),
    SECH(52),
    SERIESSUM(53),
    SIGN(54),
    SIN(55),
    SINH(56),
    SQRT(57),
    SQRTPI(58),
    SUBTOTAL(59),
    TAN(60),
    TANH(61),
    TRUNC(62);
    // 成员变量
    private int index;
    // 构造方法
    FormulaTypeA(int index) {
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
        for (FormulaTypeA temp: FormulaTypeA.values()) {
            if(temp.name().equals(str.toUpperCase()))
            {
                return true;
            }
        }
        return false;
    }
}
