package com.attackt.logivisual.model.newfunctions;

/**
 * 结果类型
 */
public enum SourceValueType {
    BlankEval(1),
    BoolEval(2),
    CacheAreaEval(3),
    ErrorEval(4),
    ExternalNameEval(5),
    FunctionNameEval(6),
    LazyRefEval(7),
    MissingArgEval(8),
    NumberEval(9),
    RefEvalBase(10),
    RefListEval(11),
    StringEval(12),
    LazyAreaEval(13);
    // 成员变量
    private int index;
    // 构造方法
    SourceValueType(int index) {
        this.index = index;
    }
    //覆盖方法
    @Override
    public String toString() {
        return this.index+"";
    }
}
