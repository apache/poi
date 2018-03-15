package com.attackt.logivisual.model.newfunctions;

public class ParaInfo {
    private int numArgs;
    private String nodeAttr;
    private int nodeType;
    private int sheetIndex;

    public ParaInfo(int numArgs, String nodeAttr, int nodeType, int sheetIndex) {
        this.numArgs = numArgs;
        this.nodeAttr = nodeAttr;
        this.nodeType = nodeType;
        this.sheetIndex = sheetIndex;
    }

    public int getNumArgs() {
        return numArgs;
    }

    public void setNumArgs(int numArgs) {
        this.numArgs = numArgs;
    }

    public String getNodeAttr() {
        return nodeAttr;
    }

    public void setNodeAttr(String nodeAttr) {
        this.nodeAttr = nodeAttr;
    }

    public int getNodeType() {
        return nodeType;
    }

    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }

    public int getSheetIndex() {
        return sheetIndex;
    }

    public void setSheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
    }
}
