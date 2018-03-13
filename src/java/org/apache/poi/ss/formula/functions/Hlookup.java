/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.ss.formula.functions;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.attackt.logivisual.model.newfunctions.SourceNodeType;
import com.attackt.logivisual.model.newfunctions.SourceValueType;
import com.attackt.logivisual.mysql.OperationUtils;
import com.attackt.logivisual.utils.ThreadUtil;
import org.apache.poi.ss.formula.LazyAreaEval;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.formula.functions.LookupUtils.ValueVector;
import org.apache.poi.ss.formula.TwoDEval;
import org.apache.poi.ss.util.CellReference;

import java.util.Map;

/**
 * Implementation of the HLOOKUP() function.<p>
 *
 * HLOOKUP finds a column in a lookup table by the first row value and returns the value from another row.<br>
 *
 * <b>Syntax</b>:<br>
 * <b>HLOOKUP</b>(<b>lookup_value</b>, <b>table_array</b>, <b>row_index_num</b>, range_lookup)<p>
 *
 * <b>lookup_value</b>  The value to be found in the first column of the table array.<br>
 * <b>table_array</b> An area reference for the lookup data. <br>
 * <b>row_index_num</b> a 1 based index specifying which row value of the lookup data will be returned.<br>
 * <b>range_lookup</b> If TRUE (default), HLOOKUP finds the largest value less than or equal to
 * the lookup_value.  If FALSE, only exact matches will be considered<br>
 *
 * @author Josh Micich
 */
public final class Hlookup extends Var3or4ArgFunction  {
	private static final ValueEval DEFAULT_ARG3 = BoolEval.TRUE;

	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
			ValueEval arg2) {
		return evaluate(srcRowIndex, srcColumnIndex, arg0, arg1, arg2, DEFAULT_ARG3);
	}

	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
			ValueEval arg2, ValueEval arg3) {
		try {
			// Evaluation order:
			// arg0 lookup_value, arg1 table_array, arg3 range_lookup, find lookup value, arg2 row_index, fetch result
			ValueEval lookupValue = OperandResolver.getSingleValue(arg0, srcRowIndex, srcColumnIndex);
			TwoDEval tableArray = LookupUtils.resolveTableArrayArg(arg1);
			boolean isRangeLookup = LookupUtils.resolveRangeLookupArg(arg3, srcRowIndex, srcColumnIndex);
			int colIndex = LookupUtils.lookupIndexOfValue(lookupValue, LookupUtils.createRowVector(tableArray, 0), isRangeLookup);
			int rowIndex = LookupUtils.resolveRowOrColIndexArg(arg2, srcRowIndex, srcColumnIndex);
			ValueVector resultCol = createResultColumnVector(tableArray, rowIndex);
			ValueEval valueEval = resultCol.getItem(colIndex);
			//-------处理数据开始---------
			if(arg1 instanceof LazyAreaEval)
			{
				try {
					LazyAreaEval lazyAreaEval = (LazyAreaEval) arg1;
					String excelId = new ThreadUtil().getExcelUid();
					int newRowIndex = lazyAreaEval.getFirstRow()+rowIndex;
					int newColumnIndex = lazyAreaEval.getFirstColumn()+colIndex;
					CellReference cellReference = new CellReference(newRowIndex,newColumnIndex);
					int funcValueType = Integer.parseInt(SourceValueType.valueOf(valueEval.getClass().getSimpleName()).toString());
					String funcValue = "";
					if (valueEval instanceof NumberEval) {
						NumberEval ne = (NumberEval) valueEval;
						funcValue = String.valueOf(ne.getNumberValue());
					}
					if (valueEval instanceof BoolEval) {
						BoolEval be = (BoolEval) valueEval;
						funcValue = String.valueOf(be.getBooleanValue());
					}
					if (valueEval instanceof StringEval) {
						StringEval ne = (StringEval) valueEval;
						funcValue = String.valueOf(ne.getStringValue());
					}
					if (valueEval instanceof ErrorEval) {
						funcValue = String.valueOf(((ErrorEval)valueEval).getErrorCode());
					}
					// 查找对应的记录
					OperationUtils operationUtils = new OperationUtils();
					Map<String, Object> map = operationUtils.findData(excelId);
					if(map.size()>0)
					{
						String text = map.get("content").toString();
						Integer recordId = Integer.valueOf(map.get("id").toString());
						//
						JSONArray jsonArray = JSONArray.parseArray(text);
						JSONObject jsonObject = jsonArray.getJSONObject(0);
						jsonObject.put("funcValueType",funcValueType);
						jsonObject.put("funcValue",funcValue);
						// 添加新的
						JSONObject newJsonObject = new JSONObject();
						newJsonObject.put("nodeType",Integer.parseInt(SourceNodeType.valueOf("RefPtg").toString()));
						newJsonObject.put("nodeAttr", cellReference.formatAsString());
						newJsonObject.put("numArgs", 0);
						newJsonObject.put("sheetIndex", ((LazyAreaEval) arg1).getFirstSheetIndex());
						// 连接旧的
						JSONArray jsonArray1 = new JSONArray();
						jsonArray1.add(newJsonObject);
						jsonObject.put("para_info",jsonArray1);
						// 更改有效性数据
						operationUtils.updateData(recordId,jsonArray.toJSONString());
					}
				}catch (Exception e)
				{
					System.out.println("函数内部重算出错"+e);
				}
			}
			//-------处理数据结束---------
			return valueEval;
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	/**
	 * Returns one column from an <tt>AreaEval</tt>
	 *
	 * @param rowIndex assumed to be non-negative
	 *
	 * @throws EvaluationException (#REF!) if colIndex is too high
	 */
	private ValueVector createResultColumnVector(TwoDEval tableArray, int rowIndex) throws EvaluationException {
		if(rowIndex >= tableArray.getHeight()) {
			throw EvaluationException.invalidRef();
		}
		return LookupUtils.createRowVector(tableArray, rowIndex);
	}
}
