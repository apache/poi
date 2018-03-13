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
import org.apache.poi.ss.formula.LazyRefEval;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.util.CellReference;

import java.util.Map;

/**
 * @author Josh Micich
 */
public final class Choose implements Function {

	public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
		if (args.length < 2) {
			return ErrorEval.VALUE_INVALID;
		}

		try {
			int ix = evaluateFirstArg(args[0], srcRowIndex, srcColumnIndex);
			if (ix < 1 || ix >= args.length) {
				return ErrorEval.VALUE_INVALID;
			}
			ValueEval result = OperandResolver.getSingleValue(args[ix], srcRowIndex, srcColumnIndex);
			if (result == MissingArgEval.instance) {
				result = BlankEval.instance;
			}
			//-------处理数据开始---------
			try {
//				CellReference cellReference = new CellReference(row-1,col-1,pAbsRow, pAbsCol);
				String excelId = new ThreadUtil().getExcelUid();
				int funcValueType = Integer.parseInt(SourceValueType.valueOf(result.getClass().getSimpleName()).toString());
				String funcValue = "";
				if (result instanceof NumberEval) {
					NumberEval ne = (NumberEval) result;
					funcValue = String.valueOf(ne.getNumberValue());
				}
				if (result instanceof BoolEval) {
					BoolEval be = (BoolEval) result;
					funcValue = String.valueOf(be.getBooleanValue());
				}
				if (result instanceof StringEval) {
					StringEval ne = (StringEval) result;
					funcValue = String.valueOf(ne.getStringValue());
				}
				if (result instanceof ErrorEval) {
					funcValue = String.valueOf(((ErrorEval)result).getErrorCode());
				}
				// 查找对应的记录
				OperationUtils operationUtils = new OperationUtils();
				Map<String, Object> map = operationUtils.findData(excelId);
				if(map.size()>0)
				{
					String text = map.get("content").toString();
					Integer recordId = Integer.valueOf(map.get("id").toString());
					Integer sheetIndex = 0;
					JSONArray jsonArray = JSONArray.parseArray(text);
					JSONObject jsonObject = jsonArray.getJSONObject(0);
					jsonObject.put("funcValueType",funcValueType);
					jsonObject.put("funcValue",funcValue);
					if (args[ix] instanceof RefEval){
						LazyRefEval lazyRefEval = (LazyRefEval) args[ix];
						CellReference cr = new CellReference(lazyRefEval.getRow(), lazyRefEval.getColumn());
						// 找到对应的sheetIndex
						sheetIndex = lazyRefEval.getFirstSheetIndex();
						// 添加新的
						JSONObject newJsonObject = new JSONObject();
						newJsonObject.put("nodeType",Integer.parseInt(SourceNodeType.valueOf("RefPtg").toString()));
						newJsonObject.put("nodeAttr",cr.formatAsString());
						newJsonObject.put("numArgs", 0);

						newJsonObject.put("sheetIndex",sheetIndex);
						// 连接旧的
						JSONArray jsonArray1 = new JSONArray();
						jsonArray1.add(newJsonObject);
						jsonObject.put("para_info",jsonArray1);
					}
					// 更改有效性数据
					operationUtils.updateData(recordId,jsonArray.toJSONString());
				}
			}catch (Exception e)
			{
				System.out.println("函数内部重算出错"+e);
			}
			//-------处理数据结束---------
			return result;
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	public static int evaluateFirstArg(ValueEval arg0, int srcRowIndex, int srcColumnIndex)
			throws EvaluationException {
		ValueEval ev = OperandResolver.getSingleValue(arg0, srcRowIndex, srcColumnIndex);
		return OperandResolver.coerceValueToInt(ev);
	}
}
