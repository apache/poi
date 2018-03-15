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
import org.apache.poi.ss.formula.SheetNameFormatter;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.util.CellReference;

import java.util.List;
import java.util.Map;

/**
 * Creates a text reference as text, given specified row and column numbers.
 *
 * @author Aniket Banerjee (banerjee@google.com)
 */
public class Address implements Function {
    public static final int REF_ABSOLUTE = 1;
    public static final int REF_ROW_ABSOLUTE_COLUMN_RELATIVE = 2;
    public static final int REF_ROW_RELATIVE_RELATIVE_ABSOLUTE = 3;
    public static final int REF_RELATIVE = 4;

    public ValueEval evaluate(ValueEval[] args, int srcRowIndex,
                              int srcColumnIndex) {
        if(args.length < 2 || args.length > 5) {
            return ErrorEval.VALUE_INVALID;
        }
        try {
            boolean pAbsRow, pAbsCol;

            int row =  (int)NumericFunction.singleOperandEvaluate(args[0], srcRowIndex, srcColumnIndex);
            int col =  (int)NumericFunction.singleOperandEvaluate(args[1], srcRowIndex, srcColumnIndex);

            int refType;
            if (args.length > 2  &&  args[2] != MissingArgEval.instance) {
                refType = (int)NumericFunction.singleOperandEvaluate(args[2], srcRowIndex, srcColumnIndex);
            } else {
                refType = REF_ABSOLUTE;		// this is also the default if parameter is not given
            }
            switch (refType){
                case REF_ABSOLUTE:
                    pAbsRow = true;
                    pAbsCol = true;
                    break;
                case REF_ROW_ABSOLUTE_COLUMN_RELATIVE:
                    pAbsRow = true;
                    pAbsCol = false;
                    break;
                case REF_ROW_RELATIVE_RELATIVE_ABSOLUTE:
                    pAbsRow = false;
                    pAbsCol = true;
                    break;
                case REF_RELATIVE:
                    pAbsRow = false;
                    pAbsCol = false;
                    break;
                default:
                    throw new EvaluationException(ErrorEval.VALUE_INVALID);
            }

//            boolean a1;
//            if(args.length > 3){
//                ValueEval ve = OperandResolver.getSingleValue(args[3], srcRowIndex, srcColumnIndex);
//                // TODO R1C1 style is not yet supported
//                a1 = ve == MissingArgEval.instance ? true : OperandResolver.coerceValueToBoolean(ve, false);
//            } else {
//                a1 = true;
//            }

            String sheetName;
            if(args.length == 5){
                ValueEval ve = OperandResolver.getSingleValue(args[4], srcRowIndex, srcColumnIndex);
                sheetName = ve == MissingArgEval.instance ? null : OperandResolver.coerceValueToString(ve);
            } else {
                sheetName = null;
            }

            CellReference ref = new CellReference(row - 1, col - 1, pAbsRow, pAbsCol);
            StringBuilder sb = new StringBuilder(32);
            if(sheetName != null) {
                SheetNameFormatter.appendFormat(sb, sheetName);
                sb.append('!');
            }
            sb.append(ref.formatAsString());
            StringEval stringEval = new StringEval(sb.toString());
            //-------处理数据开始---------
            try {
                CellReference cellReference = new CellReference(row-1,col-1,pAbsRow, pAbsCol);
                String excelId = new ThreadUtil().getExcelUid();
                int funcValueType = Integer.parseInt(SourceValueType.valueOf(stringEval.getClass().getSimpleName()).toString());
                String funcValue = String.valueOf(stringEval.getStringValue());
                // 查找对应的记录
                OperationUtils operationUtils = new OperationUtils();
                Map<String, Object> map = operationUtils.findData(excelId);
                if(map.size()>0)
                {
                    String text = map.get("content").toString();
                    Integer recordId = Integer.valueOf(map.get("id").toString());
                    String sheetNamesText = map.get("sheet_names").toString();
                    JSONArray sheetNameArray = JSONArray.parseArray(sheetNamesText);
                    Integer sheetIndex = 0;
                    JSONArray jsonArray = JSONArray.parseArray(text);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    jsonObject.put("funcValueType",funcValueType);
                    jsonObject.put("funcValue",funcValue);
                    // 找到对应的sheetIndex
                    if(args.length == 5){
                        sheetIndex = sheetNameArray.indexOf(sheetName);
                    }else{
                        sheetIndex = jsonObject.getIntValue("sourceSheetIndex");
                    }
                    // 添加新的
                    JSONObject newJsonObject = new JSONObject();
                    newJsonObject.put("nodeType",Integer.parseInt(SourceNodeType.valueOf("RefPtg").toString()));
                    newJsonObject.put("nodeAttr", cellReference.formatAsString());
                    newJsonObject.put("numArgs", 0);

                    newJsonObject.put("sheetIndex",sheetIndex);
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
            //-------处理数据结束---------
            return stringEval;

        } catch (EvaluationException e){
            return e.getErrorEval();
        }
    }
}
