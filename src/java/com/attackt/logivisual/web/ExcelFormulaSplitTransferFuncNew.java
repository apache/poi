package com.attackt.logivisual.web;

import com.attackt.logivisual.model.newfunctions.*;
import com.attackt.logivisual.mysql.OperationUtils;
import com.attackt.logivisual.utils.*;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Logger;

/**
 * 采用新方法，结合实际需求，进行拆分
 *
 * @author loopbing
 */
public class ExcelFormulaSplitTransferFuncNew {
    private final Logger logger = Logger.getLogger(ExcelFormulaSplitTransferFuncNew.class.getName());
    XSSFRow row;

    public String converFile(String filePath, String password, int accees, String uid) throws IOException {
        File file = null;
        String fileName = "";
        if (accees == 1) {
            // 本地文件
            file = new File(filePath);
            fileName = file.getName();
        } else {
            // oss文件
            fileName = Util.pathToFileName(filePath);
            file = new OSSUtil().downloadFile("./resource/tmp/" + fileName);
        }
        FileInputStream fis = new FileInputStream(file);
        Workbook workbook = null;
        logger.info("读取文件开始");

        if (password.equals("")) {
            // 有密码的文档
            if (filePath.toLowerCase().endsWith("xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (filePath.toLowerCase().endsWith("xls")) {
                workbook = new HSSFWorkbook(fis);
            }
        } else {
            // 没有密码的文档
            NPOIFSFileSystem fs = new NPOIFSFileSystem(fis);
            if (filePath.toLowerCase().endsWith("xlsx")) {
                EncryptionInfo info = new EncryptionInfo(fs);
                Decryptor d = Decryptor.getInstance(info);
                try {
                    if (!d.verifyPassword(password)) {
                        throw new RuntimeException("你提供的密码不正确");
                    }
                    InputStream dataStream = d.getDataStream(fs);
                    workbook = new XSSFWorkbook(dataStream);
                } catch (GeneralSecurityException ex) {
                    throw new RuntimeException("无法处理加密文档", ex);
                }
            } else if (filePath.toLowerCase().endsWith("xls")) {
                Biff8EncryptionKey.setCurrentUserPassword(password);
                workbook = new HSSFWorkbook(fs.getRoot(), true);
                Biff8EncryptionKey.setCurrentUserPassword(null);
            }
        }
        logger.info("读取文件结束");
        logger.info("拆分数据开始");
        Sheet existSheet = workbook.getSheet("__FDSCACHE__");
        if (existSheet != null) {
            workbook.removeSheetAt(workbook.getSheetIndex(existSheet));
        }
        //-------通用类
        AreaOperateUtil areaOperateUtil = new AreaOperateUtil(workbook);
        FormulaUtil formulaUtils = new FormulaUtil(workbook);
        Util util = new Util();
        OperationUtils operationUtils = new OperationUtils();
        int numberOfSheets = workbook.getNumberOfSheets();
        List<String> sheetNames = util.getSheetNames(workbook, numberOfSheets);
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet spreadsheet = workbook.getSheetAt(i);
            Iterator<Row> rowIterator = spreadsheet.iterator();
            while (rowIterator.hasNext()) {
                row = (XSSFRow) rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_FORMULA:
                            try {
                                // 公式基本信息
                                int srcCellRowIndex = cell.getRowIndex();
                                int srcCellColumnIndex = cell.getColumnIndex();
                                String srcCellSheetName = cell.getSheet().getSheetName();
                                int srcCellSheetIndex = workbook.getSheetIndex(cell.getSheet());
                                // 是否进入重算
                                int status = 1;
                                // 内sourceInfo
                                SourceExcelInfo sourceExcelInfo = new SourceExcelInfo(uid + "", srcCellSheetIndex, cell.getRowIndex(), cell.getColumnIndex());
                                // 公式
                                String cellFormulaStr = cell.getCellFormula();
                                if (cell.isPartOfArrayFormulaGroup()) {
                                    cellFormulaStr = "{=" + cellFormulaStr + "}";
                                }
                                //存放运算符的堆栈
                                Stack<String> operatorStack = new Stack<String>();
                                //存放元素的堆栈
                                Stack<String> elementStack = new Stack<String>();
                                // 获得ptg数组
                                Ptg[] arr_ptg = formulaUtils.getPtgs(cell);
                                // 循环ptg，生成波兰表达式和设置存储对象属性
                                util.dataMachining(workbook, sourceExcelInfo, arr_ptg, operatorStack, elementStack);
                                // 初始化集合信息
                                List<SourceExcelInfo> sourceExcelInfoList = new ArrayList<SourceExcelInfo>();
                                if (arr_ptg != null) {
                                    // 得到公式名称
                                    String formulaNameStr = formulaUtils.getFomulaName(arr_ptg);
                                    // 判断是否属于数组函数
                                    if (cell.isPartOfArrayFormulaGroup()) {
                                        // 判断是否属于函数A
                                        if (FormulaTypeA.isContainStr(formulaNameStr)) {
                                            // 取结果的Area
                                            String areaResultPositon = cell.getArrayFormulaRange().formatAsString();
                                            // 一些必须参数
                                            List<String> areaBlock = areaOperateUtil.searchAreaBlock(arr_ptg);
                                            Map<String, List<CellIndex>> areaBlockMap = areaOperateUtil.returnIndex(srcCellSheetName, areaBlock);
                                            // 搜索AreaIndex 数组中实际所对应的引用(list无记录为N/A)
                                            List<CellIndex> cellIndexList = areaOperateUtil.searchAreaOfSrcIndex(srcCellSheetName, areaResultPositon, areaBlock, areaBlockMap, srcCellRowIndex, srcCellColumnIndex);
                                            // 绑定cellIndexList 到 sourceExcelInfo
                                            formulaUtils.bindData(cellIndexList, sourceExcelInfo);
                                            // 完成状态
                                            status = 2;
                                            // 添加进excelInfo集合
                                            sourceExcelInfoList.add(sourceExcelInfo);
                                            // 获得逆波兰表达式
                                            String bolanStr = util.getBolanStr(operatorStack, elementStack);
                                            // 套壳
                                            CellJsonObject cellJsonObject = formulaUtils.dataHousing(String.valueOf(uid), srcCellSheetIndex, srcCellRowIndex, srcCellColumnIndex, bolanStr, sourceExcelInfoList, cellFormulaStr, status, sheetNames, "A");
                                            // 保存数据
                                            formulaUtils.saveData(operationUtils, cellJsonObject, uid, false, cell);
                                        } else if (FormulaTypeB2.isContainStr(formulaNameStr)) {
                                            // 判断是否是B2类函数
                                            if (formulaNameStr.equals("TRANSPOSE")) {
                                                // 取结果的Area
                                                String areaResultPositon = cell.getArrayFormulaRange().formatAsString();
                                                new TransposeFormula(workbook).save(formulaUtils, sourceExcelInfoList, sourceExcelInfo, 1, arr_ptg, srcCellSheetIndex, srcCellSheetName, util, operatorStack, elementStack, uid, srcCellRowIndex, srcCellColumnIndex, cellFormulaStr, operationUtils, cell, sheetNames, areaResultPositon, true, "B2");
                                            } else {
                                                // 判断是否是B2类函数
                                                formulaUtils.commonSave(sourceExcelInfoList, sourceExcelInfo, 1, arr_ptg, srcCellSheetIndex, srcCellSheetName, util, operatorStack, elementStack, uid, srcCellRowIndex, srcCellColumnIndex, cellFormulaStr, operationUtils, cell, sheetNames, true, "B2");
                                            }
                                        } else {
                                            // 对于其他类别函数，筛选出数值类型单元格，作为该函数的引用
                                            formulaUtils.commonSave(sourceExcelInfoList, sourceExcelInfo, 1, arr_ptg, srcCellSheetIndex, srcCellSheetName, util, operatorStack, elementStack, uid, srcCellRowIndex, srcCellColumnIndex, cellFormulaStr, operationUtils, cell, sheetNames, true, "Other");
                                        }
                                    } else {
                                        if (FormulaTypeSearch.isContainStr(formulaNameStr)) {
                                            // 判断是否是搜索类函数
                                            // 添加进excelInfo集合
                                            sourceExcelInfoList.add(sourceExcelInfo);
                                            // 完成状态
                                            status = 1;
                                            // 获得逆波兰表达式
                                            String bolanStr = util.getBolanStr(operatorStack, elementStack);
                                            // 套壳
                                            CellJsonObject cellJsonObject = formulaUtils.dataHousing(String.valueOf(uid), srcCellSheetIndex, srcCellRowIndex, srcCellColumnIndex, bolanStr, sourceExcelInfoList, cellFormulaStr, status, sheetNames, "Search");
                                            // 保存数据
                                            formulaUtils.saveData(operationUtils, cellJsonObject, uid, true, cell);
                                        } else if (FormulaTypeB1.isContainStr(formulaNameStr)) {
                                            // 判断函数是函数类型B1
                                            formulaUtils.commonSave(sourceExcelInfoList, sourceExcelInfo, 1, arr_ptg, srcCellSheetIndex, srcCellSheetName, util, operatorStack, elementStack, uid, srcCellRowIndex, srcCellColumnIndex, cellFormulaStr, operationUtils, cell, sheetNames, true, "B1");
                                        } else {
                                            //对于其他类别函数，筛选出数值类型单元格，作为该函数的引用
                                            formulaUtils.commonSave(sourceExcelInfoList, sourceExcelInfo, 1, arr_ptg, srcCellSheetIndex, srcCellSheetName, util, operatorStack, elementStack, uid, srcCellRowIndex, srcCellColumnIndex, cellFormulaStr, operationUtils, cell, sheetNames, true, "Other");
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                            break;
                    }
                }
            }
        }
        logger.info("拆分数据结束");
        logger.info("写入文件开始");
        String fileJSONPath = formulaUtils.saveFileInfo(operationUtils, uid, file, fileName);
        logger.info("写入文件结束");
        if (accees == 2) {
            // 删除文件
            file.delete();
        }
        logger.info(fileJSONPath);
        return fileJSONPath;
    }
}
