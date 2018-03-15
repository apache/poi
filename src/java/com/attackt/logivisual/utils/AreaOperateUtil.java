package com.attackt.logivisual.utils;

import com.attackt.logivisual.model.newfunctions.CellIndex;
import org.apache.poi.ss.formula.ptg.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * area操作类
 */
public class AreaOperateUtil {
    Workbook workbook = null;

    public AreaOperateUtil(Workbook workbook) {
        this.workbook = workbook;
    }

    /**
     * 搜索ptg中的结构块
     *
     * @param arr_ptg
     * @return
     */
    public List<String> searchAreaBlock(Ptg[] arr_ptg) {
        List<String> result = new ArrayList<>();
        List<String> tempRefList = new ArrayList<>(2);
        int saveFlag = 1;
        for (int i = 0; i < arr_ptg.length; i++) {
            Ptg ptg = arr_ptg[i];
            if (ptg instanceof AreaPtg) {
                AreaPtg areaPtg = (AreaPtg) ptg;
                result.add(areaPtg.toFormulaString());
            } else if (ptg instanceof MemAreaPtg) {
                saveFlag = 2;
            } else if (ptg instanceof Ref3DPxg) {
                // 引用类型《跨表数组时只能得到参数，无法得到完整的数组，需要进入RangePtg处理》
                Ref3DPxg ref3DPxg = (Ref3DPxg) ptg;
                if (saveFlag == 1) {
                    // 不含数组
                    result.add(ref3DPxg.toFormulaString());
                } else {
                    // 含跨表数组
                    tempRefList.add(ref3DPxg.toFormulaString());
                }
            } else if (ptg instanceof RangePtg) {
                {
                    RangePtg rangePtg = (RangePtg) ptg;
                    if (tempRefList.size() > 0 && tempRefList.size() % 2 == 0) {
                        String strContact = "";
                        for (int j = 0; j < tempRefList.size(); j++) {
                            strContact += tempRefList.get(j);
                            if ((j + 1) % 2 == 0) {
                                result.add(strContact);
                                strContact = "";
                            } else {
                                if (!strContact.contains(":")) {
                                    strContact += ":";
                                }
                            }
                        }
                    }
                    tempRefList = new ArrayList<String>();
                }
            }
        }
        return result;
    }


    /**
     * 返回公式结构块中的数组坐标
     *
     * @param srcSheetName
     * @param searchList
     * @return <{"A1:A2",[{rowIndex,columnIndex}]}
     */
    public Map<String, List<CellIndex>> returnIndex(String srcSheetName, List<String> searchList) {
        Map<String, List<CellIndex>> map = new HashMap<>();

        for (String searchStr : searchList) {
            List<CellIndex> cellIndexList = new ArrayList<>();
            AreaReference areaReference = new AreaReference(searchStr, workbook.getSpreadsheetVersion());
            AreaPtg areaPtg = new AreaPtg(areaReference);
            int firstRow = areaPtg.getFirstRow();
            int firstColumn = areaPtg.getFirstColumn();
            int lastRow = areaPtg.getLastRow();
            int lastColumn = areaPtg.getLastColumn();
            String sheetName = areaReference.getFirstCell().getSheetName();
            if (sheetName == null) {
                sheetName = srcSheetName;
            }
            int sheetIndex = workbook.getSheetIndex(sheetName);
            for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
                for (int columnIndex = firstColumn; columnIndex <= lastColumn; columnIndex++) {
                    cellIndexList.add(new CellIndex(rowIndex, columnIndex, sheetIndex, sheetName));
                }
            }
            map.put(searchStr, cellIndexList);
        }
        return map;
    }

    /**
     * 搜索公式块中的结果
     *
     * @param srcSheetName
     * @param areaResult
     * @param areaBlock
     * @param areaBlockMap
     * @param srcCellRowIndex
     * @param srcCellColumnIndex
     * @return
     */
    public List<CellIndex> searchAreaOfSrcIndex(String srcSheetName, String areaResult, List<String> areaBlock, Map<String, List<CellIndex>> areaBlockMap, int srcCellRowIndex, int srcCellColumnIndex) {
        List<CellIndex> cellIndexList = new ArrayList<>();
        for (String area : areaBlock) {
            AreaReference areaReference = new AreaReference(area, workbook.getSpreadsheetVersion());
            AreaPtg areaPtg = new AreaPtg(areaReference);
            String sheetName = areaReference.getFirstCell().getSheetName();
            if (sheetName == null) {
                sheetName = srcSheetName;
            }
            int sheetIndex = workbook.getSheetIndex(sheetName);
            if (areaPtg.getFirstColumn() == areaPtg.getLastColumn()) {
                // 一列
                int row = srcCellRowIndex;
                int column = areaPtg.getFirstColumn();
                cellIndexList.add(new CellIndex(row, column, sheetIndex, sheetName));
            } else if (areaPtg.getFirstRow() == areaPtg.getLastRow()) {
                // 一行
                int row = areaPtg.getFirstRow();
                int column = srcCellColumnIndex;
                cellIndexList.add(new CellIndex(row, column, sheetIndex, sheetName));
            } else {
                AreaReference areaReferencea = new AreaReference(areaResult, workbook.getSpreadsheetVersion());
                AreaPtg areaPtg1 = new AreaPtg(areaReferencea);
                //F7:H11
                int firstRow = areaPtg1.getFirstRow();
                int firstColumn = areaPtg1.getFirstColumn();
                int lastRow = areaPtg1.getLastRow();
                int lastColumn = areaPtg1.getLastColumn();
                List<CellIndex> cellIndexList1 = new ArrayList<>();
                for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
                    for (int columnIndex = firstColumn; columnIndex <= lastColumn; columnIndex++) {
                        cellIndexList1.add(new CellIndex(rowIndex, columnIndex, sheetIndex, sheetName));
                    }
                }
                // 寻找srcCellRowIndex srcCellColumnIndex 在F7:H11中位置position
                int temp = -1;
                for (int i = 0; i < cellIndexList1.size(); i++) {
                    CellIndex cellIndex = cellIndexList1.get(i);
                    if (cellIndex.getRowIndex() == srcCellRowIndex && cellIndex.getColumnIndex() == srcCellColumnIndex) {
                        temp = i;
                        break;
                    }
                }
                //A1:B3 B2:C3 寻找temp在这两块block中的位置(一次循环只匹配一个block)
                if (temp != -1) {
                    for (String key : areaBlockMap.keySet()) {
                        if (key.equals(area)) {
                            List<CellIndex> cellIndexList2 = areaBlockMap.get(key);
                            if (temp < cellIndexList2.size()) {
                                CellIndex cellIndex = cellIndexList2.get(temp);
                                cellIndexList.add(cellIndex);
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (cellIndexList.size() != areaBlockMap.keySet().size()) {
            cellIndexList = new ArrayList<CellIndex>();
        }
        return cellIndexList;
    }
}
