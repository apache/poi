package com.attackt.logivisual.model.newfunctions;

/**
 * 节点类型
 */
public enum SourceNodeType {
    AddPtg(1),
    Area3DPtg(2),
    Area3DPxg(3),
    AreaErrPtg(4),
    AreaNPtg(5),
    AreaPtg(6),
    ArrayPtg(7),
    AttrPtg(8),
    BoolPtg(9),
    ConcatPtg(10),
    ControlPtg(11),
    Deleted3DPxg(12),
    DeletedArea3DPtg(13),
    DeletedRef3DPtg(14),
    DividePtg(15),
    EqualPtg(16),
    ErrPtg(17),
    ExpPtg(18),
    FuncPtg(19),
    FuncVarPtg(20),
    GreaterEqualPtg(21),
    GreaterThanPtg(22),
    IntersectionPtg(23),
    IntPtg(24),
    LessEqualPtg(25),
    LessThanPtg(26),
    MemAreaPtg(27),
    MemErrPtg(28),
    MemFuncPtg(29),
    MissingArgPtg(30),
    MultiplyPtg(31),
    NamePtg(32),
    NameXPtg(33),
    NameXPxg(34),
    NotEqualPtg(35),
    NumberPtg(36),
    OperandPtg(37),
    OperationPtg(38),
    ParenthesisPtg(39),
    PercentPtg(40),
    PowerPtg(41),
    RangePtg(42),
    Ref3DPtg(43),
    Ref3DPxg(44),
    RefErrorPtg(45),
    RefNPtg(46),
    RefPtg(47),
    ScalarConstantPtg(48),
    StringPtg(49),
    SubtractPtg(50),
    TblPtg(51),
    UnaryMinusPtg(52),
    UnaryPlusPtg(53),
    UnionPtg(54),
    UnknownPtg(55),
    ValueOperatorPtg(56);
    // 成员变量
    private int index;
    // 构造方法
    SourceNodeType(int index) {
        this.index = index;
    }
    //覆盖方法
    @Override
    public String toString() {
        return this.index+"";
    }
}
