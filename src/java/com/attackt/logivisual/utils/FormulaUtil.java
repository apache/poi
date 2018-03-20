package com.attackt.logivisual.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.attackt.logivisual.model.FormulaSplitInfo;
import com.attackt.logivisual.model.retry.KeyValueFormula;
import com.attackt.logivisual.model.service.SheetIndirectData;
import com.attackt.logivisual.model.newfunctions.*;
import com.attackt.logivisual.mysql.OperationUtils;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.ptg.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * 公式支持通用类
 */
public class FormulaUtil {
    Workbook workbook = null;
    FormulaEvaluator evaluator;

    public FormulaUtil(Workbook workbook) {
        this.evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        this.workbook = workbook;
    }

    /**
     * 取得LOOKUPCell函数的实际引用
     *
     * @param cell
     * @return cell
     */
    public Cell getLOOKUPCell(Cell cell) throws Exception {
        Ptg[] arr_ptg = null;
        if (workbook instanceof XSSFWorkbook) {
            arr_ptg = FormulaParser.parse(cell.getCellFormula(), XSSFEvaluationWorkbook.create((XSSFWorkbook) workbook), FormulaType.CELL, 0);
        } else {
            arr_ptg = FormulaParser.parse(cell.getCellFormula(), HSSFEvaluationWorkbook.create((HSSFWorkbook) workbook), FormulaType.CELL, 0);
        }
        // 定义所有需要提取的变量
        String lookupValue = null;
        String tableArray = null;
        Integer colIndexNum = null;
        Sheet findSheet = null;
        Boolean rangeLookup = true;
        //临时存储拆分数据跨sheet数组使用
        List<FormulaSplitInfo> tempSplitInfo = new ArrayList<FormulaSplitInfo>();
        List<String> cellAreaList = new ArrayList<String>();
        // 遍历保存结果
        for (int index = 0; index < arr_ptg.length; index++) {
            Ptg ptg = arr_ptg[index];
            if (ptg instanceof RefPtg) {
                // lookup_value 要查找的值 必
                RefPtg refPtg = (RefPtg) ptg;
                lookupValue = refPtg.toFormulaString();
            } else if (ptg instanceof AreaPtg) {
                // table_array 查找区域 必
                AreaPtg areaPtg = (AreaPtg) ptg;
                tableArray = areaPtg.toFormulaString();
                // 拆分数组结构拿到所有的单元格
                int firstRow = areaPtg.getFirstRow();
                int firstColumn = areaPtg.getFirstColumn();
                int lastRow = areaPtg.getLastRow();
                int lastColumn = areaPtg.getLastColumn();
                findSheet = cell.getSheet();
                for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
                    for (int columnIndex = firstColumn; columnIndex <= lastColumn; columnIndex++) {
                        CellReference cellReference = new CellReference(rowIndex, columnIndex, false,
                                false);
                        String showValue = cellReference.formatAsString();
                        String factValue = showValue;
                        factValue = factValue.replace("$", "");
                        cellAreaList.add(factValue);
                    }
                }
            } else if (ptg instanceof IntPtg) {
                // col_index_num 返回值的单元格的编号 必
                IntPtg intPtg = (IntPtg) ptg;
                colIndexNum = intPtg.getValue();
            } else if (ptg instanceof BoolPtg) {
                // range_lookup  查找近似匹配还是精确匹配 非
                BoolPtg boolPtg = (BoolPtg) ptg;
                rangeLookup = boolPtg.getValue();
            } else if (ptg instanceof Ref3DPxg) {
                // table_array 查找区域其它sheet 必
                Ref3DPxg ref3DPxg = (Ref3DPxg) ptg;
                String showValue = ref3DPxg.toFormulaString();
                String factValue = ref3DPxg.format2DRefAsString();
                factValue = factValue.replace("$", "");
                FormulaSplitInfo formulaSplitInfo = new FormulaSplitInfo(
                        workbook.getSheetIndex(ref3DPxg.getSheetName()) + ":" + cell.getRowIndex()
                                + ":" + cell.getColumnIndex(),
                        showValue, factValue, 3, ref3DPxg.getSheetName());
                tempSplitInfo.add(formulaSplitInfo);
            } else if (ptg instanceof RangePtg) {
                // 跨表数组结束
                FormulaSplitInfo formulaSplitInfoCell1 = tempSplitInfo.get(tempSplitInfo.size() - 2);
                FormulaSplitInfo formulaSplitInfoCell2 = tempSplitInfo.get(tempSplitInfo.size() - 1);
                tableArray = formulaSplitInfoCell1.getFactValue() + ":" + formulaSplitInfoCell2.getFactValue();
                findSheet = workbook.getSheet(formulaSplitInfoCell1.getSheet());
                AreaReference areaReference = new AreaReference(tableArray, workbook.getSpreadsheetVersion());
                AreaPtg areaPtg = new AreaPtg(areaReference);
                int firstRow = areaPtg.getFirstRow();
                int firstColumn = areaPtg.getFirstColumn();
                int lastRow = areaPtg.getLastRow();
                int lastColumn = areaPtg.getLastColumn();
                for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
                    for (int columnIndex = firstColumn; columnIndex <= lastColumn; columnIndex++) {
                        CellReference cellReference = new CellReference(rowIndex, columnIndex, false, false);
                        String showValue = cellReference.formatAsString();
                        String factValue = showValue;
                        factValue = factValue.replace("$", "");
                        cellAreaList.add(factValue);
                    }
                }
            } else if (ptg instanceof FuncVarPtg) {
                // 方法名称
                FuncVarPtg funcVarPtg = (FuncVarPtg) ptg;
                String funcName = funcVarPtg.getName();
            }
        }
        // 实现函数
        if (lookupValue != null && tableArray != null && colIndexNum != null) {
            CellAddress cellAddress = new CellAddress(lookupValue);
            Cell findCell = cell.getSheet().getRow(cellAddress.getRow()).getCell(cellAddress.getColumn());
            String findCellValue = findCell.toString();
            for (String cellAreaStr : cellAreaList) {
                CellAddress tempCellAddress = new CellAddress(cellAreaStr);
                Cell tempCell = findSheet.getRow(tempCellAddress.getRow()).getCell(tempCellAddress.getColumn());
                if (tempCell.toString().equals(findCellValue)) {
                    Cell resultCell = findSheet.getRow(tempCellAddress.getRow()).getCell(colIndexNum - 1);
                    return resultCell;
                }
            }
        }
        return null;
    }

    /**
     * 取得INDIRECT函数的实际引用
     *
     * @param cell
     * @return cell
     */
    public List<FormulaSplitInfo> getINDIRECT(Cell cell) {
        Ptg[] arr_ptg = null;
        List<FormulaSplitInfo> splitInfo = new ArrayList<FormulaSplitInfo>();
        SheetIndirectData sheetIndirectData = null;
        if (workbook instanceof XSSFWorkbook) {
            arr_ptg = FormulaParser.parse(cell.getCellFormula(), XSSFEvaluationWorkbook.create((XSSFWorkbook) workbook), FormulaType.CELL, 0);
        } else {
            arr_ptg = FormulaParser.parse(cell.getCellFormula(), HSSFEvaluationWorkbook.create((HSSFWorkbook) workbook), FormulaType.CELL, 0);
        }
        for (int index = 0; index < arr_ptg.length; index++) {
            Ptg ptg = arr_ptg[index];
            if (ptg instanceof StringPtg) {
                //INDIRECT 支持处理
                StringPtg stringPtg = (StringPtg) ptg;
                String sheetName1 = null;
                CellReference cellReference = null;
                //cellReference是带sheet的，cellReference1是不带sheet的
                try {
                    cellReference = new CellReference(stringPtg.getValue());
                } catch (Exception e) {
                    System.out.println(e);
                }
                if (cellReference != null) {
                    CellReference cellReference1 = new CellReference(cellReference.getRow(), cellReference.getCol());
                    if (cellReference.getSheetName() != null) {
                        //含 sheet
                        sheetName1 = cellReference.getSheetName();
                    } else {
                        //不含 sheet
                        sheetName1 = cell.getSheet().getSheetName();
                    }
                    // 不含row，含row
                    if (cellReference.getRow() != -1) {
                        String showValue = cellReference1.formatAsString();
                        String factValue = showValue;
                        factValue = factValue.replace("$", "");
                        FormulaSplitInfo formulaSplitInfo = null;
                        if (cellReference.getSheetName() != null) {
                            formulaSplitInfo = new FormulaSplitInfo(
                                    workbook.getSheetIndex(sheetName1) + ":" + cellReference.getRow()
                                            + ":" + cellReference.getCol(),
                                    showValue, factValue, 3, sheetName1);

                        } else {
                            formulaSplitInfo = new FormulaSplitInfo(
                                    workbook.getSheetIndex(sheetName1) + ":" + cellReference.getRow()
                                            + ":" + cellReference.getCol(),
                                    showValue, factValue, 1, sheetName1);
                        }
                        splitInfo.add(formulaSplitInfo);
                    } else {
                        sheetIndirectData = new SheetIndirectData(sheetName1, cellReference1.formatAsString());
                    }
                }
            } else if (ptg instanceof Ref3DPxg) {
                // 引用类型《跨表数组时只能得到参数，无法得到完整的数组，需要进入RangePtg处理》
                Ref3DPxg temp = (Ref3DPxg) ptg;
                String showValue = temp.toFormulaString();
                String factValue = temp.format2DRefAsString();
                factValue = factValue.replace("$", "");
                CellReference cellReference = new CellReference(showValue);
                FormulaSplitInfo formulaSplitInfo = new FormulaSplitInfo(
                        workbook.getSheetIndex(temp.getSheetName()) + ":" + cellReference.getRow()
                                + ":" + cellReference.getCol(),
                        showValue, factValue, 3, temp.getSheetName());
                splitInfo.add(formulaSplitInfo);
            } else if (ptg instanceof IntPtg) {
                IntPtg intPtg = (IntPtg) ptg;
                if (sheetIndirectData != null) {
                    // 链接
                    String infoStr = sheetIndirectData.getInfoStr();
                    sheetIndirectData.setInfoStr(infoStr.concat(intPtg.getValue() + ""));
                }
            } else if (ptg instanceof ConcatPtg) {
                if (sheetIndirectData != null) {
                    // 取值
                    String showValue = sheetIndirectData.getInfoStr();
                    String factValue = showValue;
                    factValue = factValue.replace("$", "");
                    CellReference cellReference = new CellReference(factValue);
                    if (cellReference.getRow() != -1) {
                        FormulaSplitInfo formulaSplitInfo = null;
                        formulaSplitInfo = new FormulaSplitInfo(
                                workbook.getSheetIndex(sheetIndirectData.getSheetName()) + ":" + cellReference.getRow()
                                        + ":" + cellReference.getCol(),
                                showValue, factValue, 1, sheetIndirectData.getSheetName());
                        splitInfo.add(formulaSplitInfo);
                    }
                    sheetIndirectData = null;
                }
            }
        }
        return splitInfo;
    }

    /**
     * 得到公式名称
     *
     * @param arr_ptg 单元格ptg
     * @return 公式名称
     */
    public String getFomulaName(Ptg[] arr_ptg) {
        try {

            for (int index = arr_ptg.length - 1; index > -1; index--) {
                Ptg ptg = arr_ptg[index];
                if (ptg instanceof OperationPtg) {
                    OperationPtg operationPtg = (OperationPtg) ptg;
                    String funcName = "";
                    if (ptg instanceof FuncVarPtg) {
                        FuncVarPtg funcVarPtg = (FuncVarPtg) ptg;
                        funcName = funcVarPtg.getName();
                    } else if (ptg instanceof FuncPtg) {
                        FuncPtg funcPtg = (FuncPtg) ptg;
                        funcName = funcPtg.getName();
                    } else {
                        funcName = operationPtg.getClass().getSimpleName();
                    }
                    if (funcName.lastIndexOf("Ptg") != -1) {
                        funcName = funcName.substring(0, funcName.lastIndexOf("Ptg"));
                    }
                    return funcName;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return "";
    }

    /**
     * 根据单元格获得ptg数组
     *
     * @param cell
     * @return
     */
    public Ptg[] getPtgs(Cell cell) {
        Ptg[] arr_ptg = null;
        try {
            if (workbook instanceof XSSFWorkbook) {
                arr_ptg = FormulaParser.parse(cell.getCellFormula(), XSSFEvaluationWorkbook.create((XSSFWorkbook) workbook), FormulaType.CELL, 0);
            } else {
                arr_ptg = FormulaParser.parse(cell.getCellFormula(), HSSFEvaluationWorkbook.create((HSSFWorkbook) workbook), FormulaType.CELL, 0);
            }
        } catch (Exception e) {
            System.out.println("-------begin 公式转为ptg出错-------");
            System.out.println(e);
            System.out.println("-------end  公式转为ptg出错-------");
        }
        return arr_ptg;
    }

    /**
     * 绑定数据
     *
     * @param cellIndexList
     * @param sourceExcelInfo
     */
    public void bindData(List<CellIndex> cellIndexList, SourceExcelInfo sourceExcelInfo) {
        List<ParaInfo> paraInfos = new ArrayList<ParaInfo>();
        for (CellIndex cellIndex : cellIndexList) {
            CellReference cellReference = new CellReference(cellIndex.getRowIndex(), cellIndex.getColumnIndex(), false, false);
            ParaInfo paraInfo = new ParaInfo(0, cellReference.formatAsString(), Integer.parseInt(SourceNodeType.valueOf("RefPtg").toString()), cellIndex.getSheetIndex());
            paraInfos.add(paraInfo);
        }
        if (paraInfos.size() > 0) {
            sourceExcelInfo.setParaInfos(paraInfos);
        }
    }

    /**
     * 数据套壳
     *
     * @return
     */
    public CellJsonObject dataHousing(String uid, int srcSheetIndex, int srcRowIndex, int srcColumnIndex, String bolanStr, List<SourceExcelInfo> sourceExcelInfoList, String cellFormulaStr, int status, List<String> sheetNames, String formulaType) {
        // 套壳
        CellJsonObject cellJsonObject = new CellJsonObject();
        cellJsonObject.setCellId(uid + ":" + srcSheetIndex + ":" + srcRowIndex + ":" + srcColumnIndex);
        cellJsonObject.setBolanStr(bolanStr);
        cellJsonObject.setData(sourceExcelInfoList);
        cellJsonObject.setStatus(status);
        cellJsonObject.setFormula(cellFormulaStr);
        cellJsonObject.setSheetNames(sheetNames);
        cellJsonObject.setFormulaType(formulaType);
        return cellJsonObject;
    }

    /**
     * 保存数据
     *
     * @param operationUtils 操作utils
     * @param cellJsonObject cellJsonObject
     * @param uid            excelId
     * @param isRetry        是否重算 true为重算
     * @param cell           单元格
     */
    public void saveData(OperationUtils operationUtils, CellJsonObject cellJsonObject, String uid, boolean isRetry, Cell cell) {
        // 保存数据
        operationUtils.saveData(uid, cellJsonObject);
        if (isRetry == true) {
            try {
                evaluator.evaluateInCell(cell);
            } catch (NotImplementedException notImplementedException) {
            } catch (Exception e) {
                System.out.println("------数据公式重算失败begin---------");
                System.out.println(e);
                System.out.println("------数据公式重算失败end---------");
            }

        }
    }

    /**
     * 保存数据
     *
     * @param operationUtils 操作utils
     * @param cellJsonObject cellJsonObject
     * @param uid            excelId
     * @param isRetry        是否重算 true为重算
     * @param cell           单元格
     * @param status         状态
     */
    public void saveData(OperationUtils operationUtils, CellJsonObject cellJsonObject, String uid, boolean isRetry, Cell cell, int status) {
        // 保存数据
        operationUtils.saveData(uid, cellJsonObject);
        if (isRetry == true) {
            // 执行重算
            try {
                evaluator.evaluateInCell(cell);
            } catch (NotImplementedException notImplementedException) {
            } catch (Exception e) {
                System.out.println("------通用数据公式重算失败begin---------");
                System.out.println(e);
                System.out.println("------通用数据公式重算失败end---------");
            }
            // 取得结果
            if (status == 2) {
                try {
                    String excelId = null;
                    int funcValueType = 0;
                    String funcValue = null;
                    Map<String, Object> map = null;
                    if(cell.getCellType().getCode() == CellType.NUMERIC.getCode())
                    {
                        excelId = new ThreadUtil().getExcelUid();
                        funcValueType = Integer.parseInt(SourceValueType.valueOf(NumberEval.class.getSimpleName()).toString());
                        funcValue = String.valueOf(cell.getNumericCellValue());
                        // 查找对应的记录
                        map = operationUtils.findData(excelId);
                        if (map.size() > 0) {
                            String text = map.get("content").toString();
                            Integer recordId = Integer.valueOf(map.get("id").toString());
                            JSONArray jsonArray = JSONArray.parseArray(text);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            jsonObject.put("funcValueType", funcValueType);
                            jsonObject.put("funcValue", funcValue);
                            // 更改有效性数据
                            operationUtils.updateData(recordId, jsonArray.toJSONString());
                        }
                    }else if(cell.getCellType().getCode() == CellType.STRING.getCode())
                    {
                        excelId = new ThreadUtil().getExcelUid();
                        funcValueType = Integer.parseInt(SourceValueType.valueOf(StringEval.class.getSimpleName()).toString());
                        funcValue = cell.getStringCellValue();
                        // 查找对应的记录
                        map = operationUtils.findData(excelId);
                        if (map.size() > 0) {
                            String text = map.get("content").toString();
                            Integer recordId = Integer.valueOf(map.get("id").toString());
                            JSONArray jsonArray = JSONArray.parseArray(text);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            jsonObject.put("funcValueType", funcValueType);
                            jsonObject.put("funcValue", funcValue);
                            // 更改有效性数据
                            operationUtils.updateData(recordId, jsonArray.toJSONString());
                        }
                    }else if(cell.getCellType().getCode() == CellType.BOOLEAN.getCode())
                    {
                        excelId = new ThreadUtil().getExcelUid();
                        funcValueType = Integer.parseInt(SourceValueType.valueOf(BoolEval.class.getSimpleName()).toString());
                        funcValue = String.valueOf(cell.getBooleanCellValue());
                        // 查找对应的记录
                        map = operationUtils.findData(excelId);
                        if (map.size() > 0) {
                            String text = map.get("content").toString();
                            Integer recordId = Integer.valueOf(map.get("id").toString());
                            JSONArray jsonArray = JSONArray.parseArray(text);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            jsonObject.put("funcValueType", funcValueType);
                            jsonObject.put("funcValue", funcValue);
                            // 更改有效性数据
                            operationUtils.updateData(recordId, jsonArray.toJSONString());
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }

            }
        }
    }

    /**
     * 通用的解析公式方法
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
    public void commonSave(List<SourceExcelInfo> sourceExcelInfoList, SourceExcelInfo sourceExcelInfo, int status, Ptg[] arr_ptg, int srcCellSheetIndex, String srcCellSheetName, Util util, Stack<String> operatorStack, Stack<String> elementStack, String uid, int srcCellRowIndex, int srcCellColumnIndex, String cellFormulaStr, OperationUtils operationUtils, Cell cell, List<String> sheetNames, boolean isRetry,String formulaType) {
        // 添加进excelInfo集合
        sourceExcelInfoList.add(sourceExcelInfo);
        // 完成状态
        status = 2;
        // 不是搜索类函数
        OtherFormulaUtil otherFomulaUtil = new OtherFormulaUtil(workbook);
        List<CellIndex> cellIndexList = otherFomulaUtil.searchQuote(arr_ptg, srcCellSheetIndex, srcCellSheetName);
        // 绑定cellIndexList 到 sourceExcelInfo
        bindData(cellIndexList, sourceExcelInfo);
        // 获得逆波兰表达式
        String bolanStr = util.getBolanStr(operatorStack, elementStack);
        // 套壳
        CellJsonObject cellJsonObject = dataHousing(String.valueOf(uid), srcCellSheetIndex, srcCellRowIndex, srcCellColumnIndex, bolanStr, sourceExcelInfoList, cellFormulaStr, status, sheetNames,formulaType);
        // 保存数据
        saveData(operationUtils, cellJsonObject, uid, isRetry, cell, status);
    }


    /**
     * 生成json对象
     * @param operationUtils 数据库操作类
     * @param uid uid
     * @param file file
     */
    public void saveJSON( OperationUtils operationUtils, String uid,File file) {
        // 统一合并文件
        List<Map<String, Object>> mapList = operationUtils.findUidQueryAll(uid);
        JSONArray jsonArrayBase = new JSONArray();
        for (Map<String, Object> map : mapList) {
            String text = map.get("content").toString();
            JSONArray jsonArray = JSONArray.parseArray(text);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            JSONArray sheetNameArray = JSONArray.parseArray(map.get("sheet_names").toString());
            // 准备数据
            String dataId = jsonObject.getIntValue("sourceSheetIndex") + ":" + jsonObject.getIntValue("sourceRowIndex") + ":" + jsonObject.getIntValue("sourceColumnIndex");
            String dataFormula = map.get("formula").toString();
            CellReference cellReference = new CellReference(jsonObject.getIntValue("sourceRowIndex"), jsonObject.getIntValue("sourceColumnIndex"));
            String dataCellLocation = cellReference.formatAsString();
            String dataSheetName = sheetNameArray.getString(jsonObject.getIntValue("sourceSheetIndex"));
            // para_info
            JSONArray jsonArrayParaInfo = jsonObject.getJSONArray("paraInfos");
            JSONArray jsonArrayInfo = new JSONArray();
            if (jsonArrayParaInfo != null) {
                for (int i = 0; i < jsonArrayParaInfo.size(); i++) {
                    JSONObject jsonObjectParse = jsonArrayParaInfo.getJSONObject(i);
                    CellReference cellReferenceTemp = new CellReference(jsonObjectParse.getString("nodeAttr"));
                    JSONObject jsonObjectTemp = new JSONObject();
                    jsonObjectTemp.put("factValue", jsonObjectParse.getString("nodeAttr").replace("$", ""));
                    jsonObjectTemp.put("id", jsonObjectParse.getIntValue("sheetIndex") + ":" + cellReferenceTemp.getRow() + ":" + cellReferenceTemp.getCol());
                    jsonObjectTemp.put("sheet", sheetNameArray.getString(jsonObjectParse.getIntValue("sheetIndex")));
                    jsonObjectTemp.put("showValue", jsonObjectParse.getString("nodeAttr"));
                    jsonArrayInfo.add(jsonObjectTemp);
                }
            }
            // 生成对象
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("id", dataId);
            jsonObject1.put("cellContent", dataFormula);
            jsonObject1.put("cellLocation", dataCellLocation);
            jsonObject1.put("sheetName", dataSheetName);
            jsonObject1.put("splitInfo", jsonArrayInfo);
            jsonArrayBase.add(jsonObject1);
        }
        //更改整个excel在数据库中的状态
        operationUtils.updateAllStatus(uid, 3);
        String resultJsonStr = jsonArrayBase.toJSONString();
        FileWriter writer;
        String fileJSONPath = null;
        try {
            fileJSONPath = file.getParent() + "/" + Util.splitFile(file.getName())[0] + "-a.json";
            writer = new FileWriter(fileJSONPath);
            writer.write(resultJsonStr);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(fileJSONPath);
    }
    /**
     * 生成json对象
     * @param operationUtils 数据库操作类
     * @param uid uid
     * @param file file
     * @return json字符串
     */
    public String returnJSON(OperationUtils operationUtils, String uid, File file) {
        // 统一合并文件
        List<Map<String, Object>> mapList = operationUtils.findUidQueryAll(uid);
        JSONArray jsonArrayBase = new JSONArray();
        for (Map<String, Object> map : mapList) {
            String text = map.get("content").toString();
            JSONArray jsonArray = JSONArray.parseArray(text);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            JSONArray sheetNameArray = JSONArray.parseArray(map.get("sheet_names").toString());
            // 准备数据
            String dataId = jsonObject.getIntValue("sourceSheetIndex") + ":" + jsonObject.getIntValue("sourceRowIndex") + ":" + jsonObject.getIntValue("sourceColumnIndex");
            String dataFormula = map.get("formula").toString();
            CellReference cellReference = new CellReference(jsonObject.getIntValue("sourceRowIndex"), jsonObject.getIntValue("sourceColumnIndex"));
            String dataCellLocation = cellReference.formatAsString();
            String dataSheetName = sheetNameArray.getString(jsonObject.getIntValue("sourceSheetIndex"));
            JSONArray jsonArrayParaInfo = jsonObject.getJSONArray("paraInfos");
            JSONArray jsonArrayInfo = new JSONArray();
            if (jsonArrayParaInfo != null) {
                for (int i = 0; i < jsonArrayParaInfo.size(); i++) {
                    JSONObject jsonObjectParse = jsonArrayParaInfo.getJSONObject(i);
                    CellReference cellReferenceTemp = new CellReference(jsonObjectParse.getString("nodeAttr"));
                    JSONObject jsonObjectTemp = new JSONObject();
                    jsonObjectTemp.put("factValue", jsonObjectParse.getString("nodeAttr").replace("$", ""));
                    jsonObjectTemp.put("id", jsonObjectParse.getIntValue("sheetIndex") + ":" + cellReferenceTemp.getRow() + ":" + cellReferenceTemp.getCol());
                    jsonObjectTemp.put("sheet", sheetNameArray.getString(jsonObjectParse.getIntValue("sheetIndex")));
                    jsonObjectTemp.put("showValue", jsonObjectParse.getString("nodeAttr"));
                    jsonArrayInfo.add(jsonObjectTemp);
                }
            }
            // 生成对象
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("id", dataId);
            jsonObject1.put("cellContent", dataFormula);
            jsonObject1.put("cellLocation", dataCellLocation);
            jsonObject1.put("sheetName", dataSheetName);
            jsonObject1.put("splitInfo", jsonArrayInfo);
            jsonArrayBase.add(jsonObject1);
        }
        //更改整个excel在数据库中的状态
        operationUtils.updateAllStatus(uid, 3);
        String resultJsonStr = jsonArrayBase.toJSONString();
        return resultJsonStr;
    }

    /**
     * 保存文件并返回路径
     * @param operationUtils 操作类
     * @param uid uid
     * @param file 文件
     * @param fileName 文件名称
     * @return 文件路径
     */
    public String saveFileInfo(OperationUtils operationUtils,String uid,File file,String fileName){
        // 写入文件
        String resultJsonStr = null;
        try {
            // 获取json串
            resultJsonStr = this.returnJSON(operationUtils, uid, file);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        FileWriter writer;
        String fileJSONPath = null;
        if(resultJsonStr != null)
        {
            try {
                fileJSONPath = file.getParent() + "/" + Util.splitFile(fileName)[0] + "-quote.json";
                writer = new FileWriter(fileJSONPath);
                writer.write(resultJsonStr);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileJSONPath;
    }

    /**
     * 得到公式
     * @param cell 单元格
     * @param otherFomulaUtil 数组搜索对象
     * @return 拆分的结果
     */
    public LinkedList<KeyValueFormula> getFormula(Cell cell, OtherFormulaUtil otherFomulaUtil){
        Ptg[] arrPtg = this.getPtgs(cell);
        // 改为list
        List<Ptg> listPtg = new ArrayList<>();
        for (int j = 0; j < arrPtg.length; j++) {
            listPtg.add(arrPtg[j]);
        }
        LinkedList<KeyValueFormula> formulaList = new LinkedList<>();
        // 对于数组公式不重算
        if (cell.isPartOfArrayFormulaGroup()) {
            KeyValueFormula keyValueFormula = new KeyValueFormula(cell.getCellFormula(),2);
            formulaList.add(keyValueFormula);
            return formulaList;
        }
        for (int index = 0; index < listPtg.size(); index++) {
            Ptg ptg = listPtg.get(index);
            if (ptg instanceof OperationPtg|| ptg instanceof AttrPtg) {
                int numberOperands = 0;
                if (ptg instanceof AttrPtg) {
                    AttrPtg attrPtg = (AttrPtg) ptg;
                    numberOperands = attrPtg.getNumberOfOperands();
                } else if (ptg instanceof OperationPtg) {
                    OperationPtg operationPtg = (OperationPtg) ptg;
                    numberOperands = operationPtg.getNumberOfOperands();
                }
                int variableNumber = numberOperands;
                int stepIndex = 0;
                Ptg[] ptgArr = new Ptg[numberOperands];
                // 传入参数
                while (variableNumber > 0) {
                    try {
                        int currentIndex = index - variableNumber;
                        Ptg variablePtg;
                        if (currentIndex < listPtg.size()) {
                            variablePtg = listPtg.get(currentIndex);

                        } else {
                            // 引用多个情况
                            variablePtg = formulaList.getLast().getPtg();
                        }
                        ptgArr[stepIndex] = variablePtg;
                    } catch (Exception e) {
                        System.out.println("嵌套公式拆分出现问题:" + e);
                    }
                    stepIndex++;
                    variableNumber--;
                }
                // 去除掉使用过的，同时变换下标
                listPtg.removeAll(Arrays.asList(ptgArr));
                index -= ptgArr.length;
                // 拿到正确的参数
                List<String> listStirngStr = otherFomulaUtil.searchQuoteFunc(ptgArr, formulaList);
                String[] operands = new String[numberOperands];
                for (int index1 = 0; index1 < operands.length; index1++) {
                    if (index1 < listStirngStr.size()) {
                        operands[index1] = listStirngStr.get(index1);
                    }
                }
                // 生成公式
                String formulaStr = "";
                if (ptg instanceof AttrPtg) {
                    formulaStr = ((AttrPtg) ptg).toFormulaString(operands);
                } else if (ptg instanceof OperationPtg) {
                    formulaStr = ((OperationPtg) ptg).toFormulaString(operands);
                }
                // 将生成的公式写回cell
                cell.setCellFormula(formulaStr);
                // cell重算拿到结果
                String evaluateValue = evaluator.evaluateInCell(cell).toString();
                // 清理公式缓存
                evaluator.clearAllCachedResultValues();
                // 放入公式组中
                KeyValueFormula keyValueFormula = new KeyValueFormula(formulaStr, evaluateValue, ptg);
                formulaList.add(keyValueFormula);
            }
        }
        return formulaList;
    }
}
