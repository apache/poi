package com.attackt.logivisual.model.newfunctions;

import java.util.List;

/**
 * 外层excel信息
 */
public class SourceExcelInfo implements Cloneable{
    // 解析的excel唯一的id
    private String sourceExcelId;
    // 解析单元格的sheetIndex
    private Integer sourceSheetIndex;
    // 解析单元格的rowIndex
    private Integer sourceRowIndex;
    // 解析单元格的columnIndex
    private Integer sourceColumnIndex;
    // 公式类型
    private int nodeType;
    // 公式分类(A类,B1,B2,Search)
    private int nodeCategory;
    // PTG节点的属性
    private String nodeAttr;
    // 解析单元格的参数个数
    private Integer numArgs = 0;
    // 公式计算出来结果的类型
    private Integer funcValueType;
    // 公式计算出来结果的值
    private String funcValue;
    // 结果集合
    private List<ParaInfo> paraInfos;

    public SourceExcelInfo() {
    }

    public SourceExcelInfo(String sourceExcelId, Integer sourceSheetIndex, Integer sourceRowIndex, Integer sourceColumnIndex) {
        this.sourceExcelId = sourceExcelId;
        this.sourceSheetIndex = sourceSheetIndex;
        this.sourceRowIndex = sourceRowIndex;
        this.sourceColumnIndex = sourceColumnIndex;
    }

    public String getSourceExcelId() {
        return sourceExcelId;
    }

    public void setSourceExcelId(String sourceExcelId) {
        this.sourceExcelId = sourceExcelId;
    }

    public Integer getSourceSheetIndex() {
        return sourceSheetIndex;
    }

    public void setSourceSheetIndex(Integer sourceSheetIndex) {
        this.sourceSheetIndex = sourceSheetIndex;
    }

    public Integer getSourceRowIndex() {
        return sourceRowIndex;
    }

    public void setSourceRowIndex(Integer sourceRowIndex) {
        this.sourceRowIndex = sourceRowIndex;
    }

    public Integer getSourceColumnIndex() {
        return sourceColumnIndex;
    }

    public void setSourceColumnIndex(Integer sourceColumnIndex) {
        this.sourceColumnIndex = sourceColumnIndex;
    }

    public String getNodeAttr() {
        return nodeAttr;
    }

    public void setNodeAttr(String nodeAttr) {
        this.nodeAttr = nodeAttr;
    }

    public Integer getNumArgs() {
        return numArgs;
    }

    public void setNumArgs(Integer numArgs) {
        this.numArgs = numArgs;
    }

    public Integer getFuncValueType() {
        return funcValueType;
    }

    public void setFuncValueType(Integer funcValueType) {
        this.funcValueType = funcValueType;
    }

    public String getFuncValue() {
        return funcValue;
    }

    public void setFuncValue(String funcValue) {
        this.funcValue = funcValue;
    }

    public int getNodeType() {
        return nodeType;
    }

    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }

    public int getNodeCategory() {
        return nodeCategory;
    }

    public void setNodeCategory(int nodeCategory) {
        this.nodeCategory = nodeCategory;
    }

    public List<ParaInfo> getParaInfos() {
        return paraInfos;
    }

    public void setParaInfos(List<ParaInfo> paraInfos) {
        this.paraInfos = paraInfos;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        SourceExcelInfo sourceExcelInfo = null;
        sourceExcelInfo = (SourceExcelInfo)super.clone();
        return sourceExcelInfo;
    }
}
