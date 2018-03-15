package org.apache.poi.ss.formula.functions;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.attackt.logivisual.model.newfunctions.SourceNodeType;
import com.attackt.logivisual.model.newfunctions.SourceValueType;
import com.attackt.logivisual.mysql.OperationUtils;
import com.attackt.logivisual.utils.ThreadUtil;
import org.apache.poi.ss.formula.LazyAreaEval;
import org.apache.poi.ss.formula.LazyRefEval;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.formula.ptg.NumberPtg;
import org.apache.poi.ss.util.CellReference;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Loopbing
 */
public final class Areas implements Function {

    @Override
    public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        if (args.length == 0) {
            throw new RuntimeException("这个Area函数中没有参数");
        }
        try {
            ValueEval valueEval = args[0];
            int result = 1;
            if (valueEval instanceof RefListEval) {
                RefListEval refListEval = (RefListEval) valueEval;
                result = refListEval.getList().size();
            }
            NumberEval numberEval = new NumberEval(new NumberPtg(result));
            //-------处理数据开始---------
            try {
                String excelId = new ThreadUtil().getExcelUid();
                int funcValueType = Integer.parseInt(SourceValueType.valueOf(numberEval.getClass().getSimpleName()).toString());
                String funcValue = String.valueOf(numberEval.getNumberValue());
                // 查找对应的记录
                OperationUtils operationUtils = new OperationUtils();
                Map<String, Object> map = operationUtils.findData(excelId);
                if (map.size() > 0) {
                    String text = map.get("content").toString();
                    Integer recordId = Integer.valueOf(map.get("id").toString());
                    Integer sheetIndex = 0;
                    JSONArray jsonArray = JSONArray.parseArray(text);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    jsonObject.put("funcValueType", funcValueType);
                    jsonObject.put("funcValue", funcValue);
                    JSONArray jsonArray1 = new JSONArray();
                    if (valueEval instanceof LazyRefEval) {
                        LazyRefEval lazyRefEval = (LazyRefEval) valueEval;
                        CellReference cr = new CellReference(lazyRefEval.getRow(), lazyRefEval.getColumn());
                        // 找到对应的sheetIndex
                        sheetIndex = lazyRefEval.getFirstSheetIndex();
                        // 添加新的
                        JSONObject newJsonObject = new JSONObject();
                        newJsonObject.put("nodeType", Integer.parseInt(SourceNodeType.valueOf("RefPtg").toString()));
                        newJsonObject.put("nodeAttr", cr.formatAsString());
                        newJsonObject.put("numArgs", 0);

                        newJsonObject.put("sheetIndex", sheetIndex);
                        // 连接旧的
                        jsonArray1.add(newJsonObject);
                    } else if (valueEval instanceof LazyAreaEval) {
                        LazyAreaEval lazyAreaEval = (LazyAreaEval) valueEval;
                        int firstRow = lazyAreaEval.getFirstRow();
                        int firstColumn = lazyAreaEval.getFirstColumn();
                        int lastRow = lazyAreaEval.getLastRow();
                        int lastColumn = lazyAreaEval.getLastColumn();
                        for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
                            for (int columnIndex = firstColumn; columnIndex <= lastColumn; columnIndex++) {
                                CellReference cellReference = new CellReference(rowIndex, columnIndex);
                                // 添加新的
                                JSONObject newJsonObject = new JSONObject();
                                newJsonObject.put("nodeType", Integer.parseInt(SourceNodeType.valueOf("RefPtg").toString()));
                                newJsonObject.put("nodeAttr", cellReference.formatAsString());
                                newJsonObject.put("numArgs", 0);
                                newJsonObject.put("sheetIndex", lazyAreaEval.getFirstSheetIndex());
                                // 连接旧的
                                jsonArray1.add(newJsonObject);
                            }
                        }
                    }else if(valueEval instanceof RefListEval)
                    {
                        RefListEval refListEval = (RefListEval) valueEval;
                        jsonArray1 = coverRefListEval(refListEval,jsonArray1);
                    }
                    // 存储数据
                    jsonObject.put("para_info", jsonArray1);
                    // 更改有效性数据
                    operationUtils.updateData(recordId, jsonArray.toJSONString());
                }
            } catch (Exception e) {
                System.out.println("函数内部重算出错" + e);
            }
            //-------处理数据结束---------
            return numberEval;
        } catch (Exception e) {
            return ErrorEval.VALUE_INVALID;
        }

    }

    /**
     * 转换valueList
     * @param refListEval
     * @param jsonArray1
     * @return
     */
    public JSONArray coverRefListEval(RefListEval refListEval,JSONArray jsonArray1){
        for (ValueEval valueEval:refListEval.getList()) {
            if (valueEval instanceof LazyRefEval) {
                LazyRefEval lazyRefEval = (LazyRefEval) valueEval;
                CellReference cr = new CellReference(lazyRefEval.getRow(), lazyRefEval.getColumn());
                // 找到对应的sheetIndex
                // 添加新的
                JSONObject newJsonObject = new JSONObject();
                newJsonObject.put("nodeType", Integer.parseInt(SourceNodeType.valueOf("RefPtg").toString()));
                newJsonObject.put("nodeAttr", cr.formatAsString());
                newJsonObject.put("numArgs", 0);

                newJsonObject.put("sheetIndex", lazyRefEval.getFirstSheetIndex());
                // 连接旧的
                jsonArray1.add(newJsonObject);
            } else if (valueEval instanceof LazyAreaEval) {
                LazyAreaEval lazyAreaEval = (LazyAreaEval) valueEval;
                int firstRow = lazyAreaEval.getFirstRow();
                int firstColumn = lazyAreaEval.getFirstColumn();
                int lastRow = lazyAreaEval.getLastRow();
                int lastColumn = lazyAreaEval.getLastColumn();
                for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
                    for (int columnIndex = firstColumn; columnIndex <= lastColumn; columnIndex++) {
                        CellReference cellReference = new CellReference(rowIndex, columnIndex);
                        // 添加新的
                        JSONObject newJsonObject = new JSONObject();
                        newJsonObject.put("nodeType", Integer.parseInt(SourceNodeType.valueOf("RefPtg").toString()));
                        newJsonObject.put("nodeAttr", cellReference.formatAsString());
                        newJsonObject.put("numArgs", 0);
                        newJsonObject.put("sheetIndex", lazyAreaEval.getFirstSheetIndex());
                        // 连接旧的
                        jsonArray1.add(newJsonObject);
                    }
                }
            }
        }
        return  jsonArray1;
    }
}
