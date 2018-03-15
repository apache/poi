package com.attackt.logivisual.utils;

import com.alibaba.fastjson.JSON;
import com.attackt.logivisual.model.newfunctions.CellJsonObject;
import com.attackt.logivisual.model.newfunctions.SourceExcelInfo;
import com.attackt.logivisual.model.newfunctions.SourceNodeType;
import com.attackt.logivisual.mysql.OperationUtils;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.ptg.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Util {
	/**
	 * 返回文件名的拆分结果
	 * 
	 * @param filename
	 *            文件名
	 * @return 按点拆分的结果
	 */
	public synchronized static String[] splitFile(String filename) {
		String[] data = filename.split("\\.");
		return data;
	}

	/**
	 * 线上文件路径处理
	 * @param path 路径
	 * @return
	 */
	public synchronized static String pathToFileName(String path) {
		String fileName = path.substring(path.lastIndexOf("/")+1);
		return fileName;
	}
    /**
     * 得到别名所对应的单元格
     * @param workbook
     * @param name
     * @return
     */
	public Cell getFormulaCell(Workbook workbook, String name) {
		int namedCellIdx = workbook.getNameIndex(name);
		Name aNamedCell = workbook.getNameAt(namedCellIdx);
		AreaReference[] arefs = AreaReference.generateContiguous(aNamedCell.getRefersToFormula());
		for (int i = 0; i < arefs.length; i++) {
			CellReference[] crefs = arefs[i].getAllReferencedCells();
			for (int j = 0; j < crefs.length; j++) {
				Sheet s = workbook.getSheet(crefs[j].getSheetName());
				if( s == null)
				{
					return null;
				}
				Row r = s.getRow(crefs[j].getRow());
				if( r == null)
				{
					return null;
				}
				Cell c = r.getCell(crefs[j].getCol());
				return c;
			}
		}
		return null;
	}
	/**
	 * 得到逆波兰字符串
	 * @param operatorStack 操作stack
	 * @param elementStack 元素Stack
	 * @return 逆波兰字符创
	 */
	public String getBolanStr(Stack<String> operatorStack, Stack<String> elementStack)
	{
		while (!operatorStack.empty()){
			elementStack.push(operatorStack.pop());
		}
		// 转换list结构
		ArrayList<String> str2List = new ArrayList<String>();
		Stack<String> swapStack = new Stack<String>();

		while(!elementStack.empty()){
			swapStack.push(elementStack.pop());
		}
		while (!swapStack.empty()){
			str2List.add(swapStack.pop());
		}
		StringBuilder sb = new StringBuilder();
		for (String str:str2List) {
			sb.append(str+",");
		}
		if(sb.lastIndexOf(",")==sb.length()-1)
		{
			return sb.substring(0,sb.lastIndexOf(","));
		}else{
			return sb.toString();
		}
	}

	/**
	 * 循环ptg，生成波兰表达式和设置存储对象属性
	 * @param sourceExcelInfo
	 * @param arr_ptg
	 * @param operatorStack
	 * @param elementStack
	 */
	public void dataMachining(Workbook workbook,SourceExcelInfo sourceExcelInfo, Ptg[] arr_ptg, Stack<String> operatorStack, Stack<String> elementStack)
	{
		for (int index = 0; index < arr_ptg.length; index++) {
			Ptg ptg = arr_ptg[index];
			if (ptg instanceof OperationPtg) {
				// 操作函数
				OperationPtg operationPtg = (OperationPtg) ptg;
				if (ptg instanceof FuncVarPtg) {
					FuncVarPtg funcVarPtg = (FuncVarPtg) ptg;
					sourceExcelInfo.setNodeAttr(funcVarPtg.getName());
					sourceExcelInfo.setNumArgs(funcVarPtg.getSize());
					sourceExcelInfo.setNodeType(Integer.parseInt(SourceNodeType.valueOf(ptg.getClass().getSimpleName()).toString()));
					operatorStack.push(funcVarPtg.getName());
				}else if (ptg instanceof FuncPtg){
					FuncPtg funcPtg= (FuncPtg) ptg;
					sourceExcelInfo.setNodeAttr(funcPtg.getName());
					sourceExcelInfo.setNumArgs(funcPtg.getSize());
					sourceExcelInfo.setNodeType(Integer.parseInt(SourceNodeType.valueOf(ptg.getClass().getSimpleName()).toString()));
					operatorStack.push(funcPtg.getName());
				} else {
					sourceExcelInfo.setNodeType(Integer.parseInt(SourceNodeType.valueOf(ptg.getClass().getSimpleName()).toString()));
					sourceExcelInfo.setNodeAttr(operationPtg.getClass().getSimpleName());
					operatorStack.push(operationPtg.getClass().getSimpleName());
				}
			} else {
				String nodeAttr = "";
				if(ptg instanceof NamePtg)
				{
					NamePtg namePtg = (NamePtg) ptg;
					String nameStr = null;
					if (workbook instanceof XSSFWorkbook) {
						nameStr = namePtg
								.toFormulaString(XSSFEvaluationWorkbook.create((XSSFWorkbook) workbook));
					} else {
						nameStr = namePtg
								.toFormulaString(HSSFEvaluationWorkbook.create((HSSFWorkbook) workbook));
					}
					Cell cell = getFormulaCell(workbook,nameStr);
					if(cell != null)
					{
						nodeAttr = cell.getAddress().formatAsString();
					}else{
						nodeAttr = nameStr;
					}

				}
				if("".equals(nodeAttr))
				{
					nodeAttr = ptg.toFormulaString();
				}
				// 去除一些空对象
				if(!"".equals(nodeAttr))
				{
					// 非操作函数
					sourceExcelInfo.setNodeAttr(nodeAttr);
					elementStack.push(nodeAttr);
				}
			}
		}
	}

	/**
	 * 取得sheetName数组
	 * @param workbook excel
	 * @param numberOfSheets sheet总数
	 * @return sheetName集合
	 */
	public List<String> getSheetNames(Workbook workbook,int numberOfSheets){
		List<String> sheetNames = new ArrayList<>();
		for(int i=0;i<numberOfSheets;i++)
		{
			sheetNames.add(workbook.getSheetName(i));
		}
		return sheetNames;
	}
}
