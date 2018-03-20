package com.attackt.logivisual.utils;

import com.attackt.logivisual.model.newfunctions.SourceExcelInfo;
import com.attackt.logivisual.model.newfunctions.CellIndex;
import com.attackt.logivisual.model.newfunctions.CellJsonObject;
import com.attackt.logivisual.mysql.OperationUtils;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Stack;

/**
 * TRANSPOSE 公式
 */
public class TransposeFormula {
    Workbook workbook;
    public TransposeFormula(Workbook workbook){
        this.workbook = workbook;
    }
    /**
     * Transpose公式处理
     *
     * @param sourceExcelInfoList
     * @param sourceExcelInfo
     * @param status
     * @param arr_ptg
     * @param srcCellSheetIndex
     * @param srcCellSheetName
     * @param util
     * @param operatorStack
     * @param elementStack
     * @param uid
     * @param srcCellRowIndex
     * @param srcCellColumnIndex
     * @param cellFormulaStr
     * @param operationUtils
     * @param cell
     * @param sheetNames
     * @param isRetry
     */
    public void save(FormulaUtil formulaUtil, List<SourceExcelInfo> sourceExcelInfoList, SourceExcelInfo sourceExcelInfo, int status, Ptg[] arr_ptg, int srcCellSheetIndex, String srcCellSheetName, Util util, Stack<String> operatorStack, Stack<String> elementStack, String uid, int srcCellRowIndex, int srcCellColumnIndex, String cellFormulaStr, OperationUtils operationUtils, Cell cell, List<String> sheetNames, String areaResultPositon, boolean isRetry, String formulaType) {
        // 添加进excelInfo集合
        sourceExcelInfoList.add(sourceExcelInfo);
        // 完成状态
        status = 2;
        // 不是搜索类函数
        OtherFormulaUtil otherFomulaUtil = new OtherFormulaUtil(formulaUtil.workbook);
        List<CellIndex> cellIndexList = otherFomulaUtil.searchQuoteTranspose(arr_ptg, srcCellSheetName,areaResultPositon,cell);
        // 绑定cellIndexList 到 sourceExcelInfo
        formulaUtil.bindData(cellIndexList, sourceExcelInfo);
        // 获得逆波兰表达式
        String bolanStr = util.getBolanStr(operatorStack, elementStack);
        // 套壳
        CellJsonObject cellJsonObject = formulaUtil.dataHousing(String.valueOf(uid), srcCellSheetIndex, srcCellRowIndex, srcCellColumnIndex, bolanStr, sourceExcelInfoList, cellFormulaStr, status, sheetNames,formulaType);
        // 保存数据
        formulaUtil.saveData(operationUtils, cellJsonObject, uid, isRetry, cell, status);
    }
}
