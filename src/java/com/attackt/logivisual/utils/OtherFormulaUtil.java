package com.attackt.logivisual.utils;

import com.attackt.logivisual.model.newfunctions.CellIndex;
import com.attackt.logivisual.model.newfunctions.KeyValueEntity;
import com.attackt.logivisual.model.retry.KeyValueFormula;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.poi.ss.formula.LazyAreaEval;
import org.apache.poi.ss.formula.ptg.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;

import java.util.*;

/**
 * 该类是为公式解析文档中第5步服务
 */
public class OtherFormulaUtil {
    private static Config config = ConfigFactory.load();
    private static boolean arrFlag = config.getBoolean("arr.flag");
    Workbook workbook = null;

    public OtherFormulaUtil(Workbook workbook) {
        this.workbook = workbook;
    }

    /**
     * 搜索ptg中引用
     *
     * @param arr_ptg
     * @return
     */
    public List<CellIndex> searchQuote(Ptg[] arr_ptg, int srcSheetIndex, String srcSheetName) {
        List<CellIndex> cellIndexList = new ArrayList<>();
        List<String> tempRefList = new ArrayList<>(2);
        int saveFlag = 1;
        for (int i = 0; i < arr_ptg.length; i++) {
            Ptg ptg = arr_ptg[i];
            if (ptg instanceof RefPtg) {
                RefPtg refPtg = (RefPtg) ptg;
                CellIndex cellIndex = new CellIndex(refPtg.getRow(), refPtg.getColumn(), srcSheetIndex, srcSheetName);
                cellIndexList.add(cellIndex);
            } else if (ptg instanceof AreaPtg) {
                if (arrFlag) {
                    AreaPtg areaPtg = (AreaPtg) ptg;
                    int firstRow = areaPtg.getFirstRow();
                    int firstColumn = areaPtg.getFirstColumn();
                    int lastRow = areaPtg.getLastRow();
                    int lastColumn = areaPtg.getLastColumn();
                    for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
                        for (int columnIndex = firstColumn; columnIndex <= lastColumn; columnIndex++) {
                            cellIndexList.add(new CellIndex(rowIndex, columnIndex, srcSheetIndex, srcSheetName));
                        }
                    }
                }
            } else if (ptg instanceof MemAreaPtg) {
                saveFlag = 2;
            } else if (ptg instanceof Ref3DPxg) {
                // 引用类型《跨表数组时只能得到参数，无法得到完整的数组，需要进入RangePtg处理》
                Ref3DPxg ref3DPxg = (Ref3DPxg) ptg;
                if (saveFlag == 1) {
                    // 不含数组
                    CellReference cellReference = new CellReference(ref3DPxg.format2DRefAsString());
                    int sheetIndex = workbook.getSheetIndex(ref3DPxg.getSheetName());
                    cellIndexList.add(new CellIndex(cellReference.getRow(), cellReference.getCol(), sheetIndex, ref3DPxg.getSheetName()));
                } else {
                    if (arrFlag) {
                        // 含跨表数组
                        tempRefList.add(ref3DPxg.toFormulaString());
                    }
                }
            } else if (ptg instanceof RangePtg) {
                RangePtg rangePtg = (RangePtg) ptg;
                if (tempRefList.size() > 0 && tempRefList.size() % 2 == 0) {
                    String strContact = "";
                    for (int j = 0; j < tempRefList.size(); j++) {
                        strContact += tempRefList.get(j);
                        if ((j + 1) % 2 == 0) {
                            //-----跨表数组公式解析开始
                            AreaReference areaReference = new AreaReference(strContact, workbook.getSpreadsheetVersion());
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
                            //-----跨表数组公式解析结束
                            strContact = "";
                        } else {
                            if (!strContact.contains(":")) {
                                strContact += ":";
                            }
                        }
                    }
                }
                tempRefList = new ArrayList<String>();
            } else if (ptg instanceof Area3DPxg) {
                Area3DPxg area3DPxg = (Area3DPxg) ptg;
                int firstRow = area3DPxg.getFirstRow();
                int firstColumn = area3DPxg.getFirstColumn();
                int lastRow = area3DPxg.getLastRow();
                int lastColumn = area3DPxg.getLastColumn();
                String sheetName = area3DPxg.getSheetName();
                if (sheetName == null) {
                    sheetName = srcSheetName;
                }
                int sheetIndex = workbook.getSheetIndex(sheetName);
                for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
                    for (int columnIndex = firstColumn; columnIndex <= lastColumn; columnIndex++) {
                        cellIndexList.add(new CellIndex(rowIndex, columnIndex, sheetIndex, sheetName));
                    }
                }
            }
        }
        return cellIndexList;
    }

    /**
     * 搜索ptg中引用
     *
     * @param arr_ptg
     * @return
     */
    public List<CellIndex> searchQuoteTranspose(Ptg[] arr_ptg, String srcSheetName, String areaResultPositon, Cell cell) {
        List<CellIndex> cellIndexList = new ArrayList<>();
        String areaStr = null;
        for (int i = 0; i < arr_ptg.length; i++) {
            Ptg ptg = arr_ptg[i];
            if (ptg instanceof AreaPtg) {
                AreaPtg areaPtg = (AreaPtg) ptg;
                areaStr = areaPtg.toFormulaString();
                break;
            } else if (ptg instanceof Area3DPxg) {
                Area3DPxg area3DPxg = (Area3DPxg) ptg;
                areaStr = area3DPxg.toFormulaString();
                break;
            }
        }
        // 变换
        AreaReference areaReference = new AreaReference(areaStr, workbook.getSpreadsheetVersion());
        CellReference[] cells = areaReference.getAllReferencedCells();
        Map<Integer, List<KeyValueEntity>> map = new TreeMap<>();
        for (int i = 0; i < cells.length; i++) {
            CellReference cellReference = cells[i];
            int row = cellReference.getRow();
            int column = cellReference.getCol();
            CellReference cellReference1 = new CellReference(column, row);
            String formulaAddress = cellReference1.formatAsString();
            List<KeyValueEntity> tempList = null;
            if (map.containsKey(column)) {
                tempList = map.get(column);
            } else {
                tempList = new ArrayList<>();
            }
            String sheetName = null;
            if (cellReference.getSheetName() == null) {
                sheetName = srcSheetName;
            } else {
                sheetName = cellReference.getSheetName();
            }
            CellIndex cellIndex = new CellIndex(cellReference.getRow(), cellReference.getCol(), workbook.getSheetIndex(sheetName), sheetName);
            tempList.add(new KeyValueEntity(formulaAddress, cellIndex));
            map.put(column, tempList);
        }
        // 创建结果数组
        AreaReference areaReferencea = new AreaReference(areaResultPositon, workbook.getSpreadsheetVersion());
        AreaPtg areaPtg1 = new AreaPtg(areaReferencea);
        int firstRow = areaPtg1.getFirstRow();
        int firstColumn = areaPtg1.getFirstColumn();
        int lastRow = areaPtg1.getLastRow();
        int lastColumn = areaPtg1.getLastColumn();
        int width = lastColumn - firstColumn + 1;
        int height = lastRow - firstRow + 1;
        Object[] objects = map.keySet().toArray();
        List<KeyValueEntity> entityList = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            if (i < objects.length) {
                Integer key = (Integer) objects[i];
                for (int j = 0; j < width; j++) {
                    if (map.containsKey(key) && map.get(key).size() > j) {
                        KeyValueEntity keyValueEntity = map.get(key).get(j);
                        entityList.add(keyValueEntity);
                    }
                }
            }
        }
        // 查找当前单元格在结果集中的位置
        CellReference[] cellReferences = areaReferencea.getAllReferencedCells();
        for (int i = 0; i < cellReferences.length; i++) {
            CellReference cellReference = cellReferences[i];
            if (cellReference.formatAsString().equals(cell.getAddress().formatAsString())) {
                if (i < entityList.size()) {
                    KeyValueEntity keyValueEntity = entityList.get(i);
                    cellIndexList.add(keyValueEntity.getValue());
                }
                break;
            }
        }
        return cellIndexList;
    }

    /**
     * 搜索公式
     *
     * @param arr_ptg
     * @return
     */
    public List<String> searchQuote(Ptg[] arr_ptg) {
        List<String> resultList = new ArrayList<>();
        List<String> tempRefList = new ArrayList<>(2);
        int saveFlag = 1;
        for (int i = 0; i < arr_ptg.length; i++) {
            Ptg ptg = arr_ptg[i];
            if (ptg instanceof RefPtg) {
                RefPtg refPtg = (RefPtg) ptg;
                resultList.add(refPtg.toFormulaString());
            } else if (ptg instanceof AreaPtg) {
                if (arrFlag) {
                    AreaPtg areaPtg = (AreaPtg) ptg;
                    resultList.add(areaPtg.toFormulaString());
                }
            } else if (ptg instanceof MemAreaPtg) {
                saveFlag = 2;
            } else if (ptg instanceof Ref3DPxg) {
                // 引用类型《跨表数组时只能得到参数，无法得到完整的数组，需要进入RangePtg处理》
                Ref3DPxg ref3DPxg = (Ref3DPxg) ptg;
                if (saveFlag == 1) {
                    resultList.add(ref3DPxg.toFormulaString());
                } else {
                    if (arrFlag) {
                        // 含跨表数组(此处无错，需求只需要字符串)
                        resultList.add(ref3DPxg.toFormulaString());
                    }
                }
            } else if (ptg instanceof RangePtg) {
                RangePtg rangePtg = (RangePtg) ptg;
                if (tempRefList.size() > 0 && tempRefList.size() % 2 == 0) {
                    String strContact = "";
                    for (int j = 0; j < tempRefList.size(); j++) {
                        strContact += tempRefList.get(j);
                        if ((j + 1) % 2 == 0) {
                            //-----跨表数组公式解析开始
                            AreaReference areaReference = new AreaReference(strContact, workbook.getSpreadsheetVersion());
                            AreaPtg areaPtg = new AreaPtg(areaReference);
                            resultList.add(areaPtg.toFormulaString());
                            //-----跨表数组公式解析结束
                            strContact = "";
                        } else {
                            if (!strContact.contains(":")) {
                                strContact += ":";
                            }
                        }
                    }
                }
                tempRefList = new ArrayList<String>(2);
            }
        }
        return resultList;
    }

    /**
     * 搜索公式带方法体
     *
     * @param arr_ptg     ptg数组
     * @param formulaList 公式集合
     * @return 字符串
     */
    public List<String> searchQuoteFunc(Ptg[] arr_ptg, LinkedList<KeyValueFormula> formulaList) {
        List<String> resultList = new ArrayList<>();
        List<String> tempRefList = new ArrayList<>(2);
        int saveFlag = 1;
        for (int i = 0; i < arr_ptg.length; i++) {
            Ptg ptg = arr_ptg[i];
            if (ptg instanceof RefPtg) {
                RefPtg refPtg = (RefPtg) ptg;
                resultList.add(refPtg.toFormulaString());
            } else if (ptg instanceof AreaPtg) {
                if (arrFlag) {
                    AreaPtg areaPtg = (AreaPtg) ptg;
                    resultList.add(areaPtg.toFormulaString());
                }
            } else if (ptg instanceof IntPtg) {
                IntPtg intPtg = (IntPtg) ptg;
                resultList.add(String.valueOf(intPtg.getValue()));
            } else if (ptg instanceof BoolPtg) {
                BoolPtg boolPtg = (BoolPtg) ptg;
                resultList.add(String.valueOf(boolPtg.getValue()));
            } else if (ptg instanceof StringPtg) {
                StringPtg stringPtg = (StringPtg) ptg;
                resultList.add(String.valueOf(stringPtg.getValue()));
            } else if (ptg instanceof MemAreaPtg) {
                saveFlag = 2;
            } else if (ptg instanceof Ref3DPxg) {
                // 引用类型《跨表数组时只能得到参数，无法得到完整的数组，需要进入RangePtg处理》
                Ref3DPxg ref3DPxg = (Ref3DPxg) ptg;
                resultList.add(ref3DPxg.toFormulaString());
            }else if(ptg instanceof Area3DPxg){
                Area3DPxg area3DPxg = (Area3DPxg) ptg;
                resultList.add(area3DPxg.toFormulaString());
            } else if (ptg instanceof OperationPtg) {
                resultList.add(formulaList.getLast().getResult());
            }
        }
        return resultList;
    }
}
