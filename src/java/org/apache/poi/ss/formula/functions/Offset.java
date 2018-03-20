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
import org.apache.poi.ss.util.CellReference;

import java.util.Map;

/**
 * Implementation for Excel function OFFSET()<p>
 *
 * OFFSET returns an area reference that is a specified number of rows and columns from a
 * reference cell or area.<p>
 *
 * <b>Syntax</b>:<br>
 * <b>OFFSET</b>(<b>reference</b>, <b>rows</b>, <b>cols</b>, height, width)<p>
 * <b>reference</b> is the base reference.<br>
 * <b>rows</b> is the number of rows up or down from the base reference.<br>
 * <b>cols</b> is the number of columns left or right from the base reference.<br>
 * <b>height</b> (default same height as base reference) is the row count for the returned area reference.<br>
 * <b>width</b> (default same width as base reference) is the column count for the returned area reference.<br>
 *
 * @author Josh Micich
 */
public final class Offset implements Function {
	// These values are specific to BIFF8
	private static final int LAST_VALID_ROW_INDEX = 0xFFFF;
	private static final int LAST_VALID_COLUMN_INDEX = 0xFF;


	/**
	 * A one dimensional base + offset.  Represents either a row range or a column range.
	 * Two instances of this class together specify an area range.
	 */
	/* package */ static final class LinearOffsetRange {

		private final int _offset;
		private final int _length;

		public LinearOffsetRange(int offset, int length) {
			if(length == 0) {
				// handled that condition much earlier
				throw new RuntimeException("length may not be zero");
			}
			_offset = offset;
			_length = length;
		}

		public short getFirstIndex() {
			return (short) _offset;
		}
		public short getLastIndex() {
			return (short) (_offset + _length - 1);
		}
		/**
		 * Moves the range by the specified translation amount.<p>
		 *
		 * This method also 'normalises' the range: Excel specifies that the width and height
		 * parameters (length field here) cannot be negative.  However, OFFSET() does produce
		 * sensible results in these cases.  That behavior is replicated here. <p>
		 *
		 * @param translationAmount may be zero negative or positive
		 *
		 * @return the equivalent <tt>LinearOffsetRange</tt> with a positive length, moved by the
		 * specified translationAmount.
		 */
		public LinearOffsetRange normaliseAndTranslate(int translationAmount) {
			if (_length > 0) {
				if(translationAmount == 0) {
					return this;
				}
				return new LinearOffsetRange(translationAmount + _offset, _length);
			}
			return new LinearOffsetRange(translationAmount + _offset + _length + 1, -_length);
		}

		public boolean isOutOfBounds(int lowValidIx, int highValidIx) {
			if(_offset < lowValidIx) {
				return true;
			}
			if(getLastIndex() > highValidIx) {
				return true;
			}
			return false;
		}
		public String toString() {
			StringBuffer sb = new StringBuffer(64);
			sb.append(getClass().getName()).append(" [");
			sb.append(_offset).append("...").append(getLastIndex());
			sb.append("]");
			return sb.toString();
		}
	}

	/**
	 * Encapsulates either an area or cell reference which may be 2d or 3d.
	 */
	private static final class BaseRef {
		private final int _firstRowIndex;
		private final int _firstColumnIndex;
		private final int _width;
		private final int _height;
		private final RefEval _refEval;
		private final AreaEval _areaEval;

		public BaseRef(RefEval re) {
			_refEval = re;
			_areaEval = null;
			_firstRowIndex = re.getRow();
			_firstColumnIndex = re.getColumn();
			_height = 1;
			_width = 1;
		}

		public BaseRef(AreaEval ae) {
			_refEval = null;
			_areaEval = ae;
			_firstRowIndex = ae.getFirstRow();
			_firstColumnIndex = ae.getFirstColumn();
			_height = ae.getLastRow() - ae.getFirstRow() + 1;
			_width = ae.getLastColumn() - ae.getFirstColumn() + 1;
		}

		public int getWidth() {
			return _width;
		}
		public int getHeight() {
			return _height;
		}
		public int getFirstRowIndex() {
			return _firstRowIndex;
		}
		public int getFirstColumnIndex() {
			return _firstColumnIndex;
		}

		public AreaEval offset(int relFirstRowIx, int relLastRowIx,
							   int relFirstColIx, int relLastColIx) {
			if (_refEval == null) {
				return _areaEval.offset(relFirstRowIx, relLastRowIx, relFirstColIx, relLastColIx);
			}
			return _refEval.offset(relFirstRowIx, relLastRowIx, relFirstColIx, relLastColIx);
		}
	}

	@SuppressWarnings("fallthrough")
	public ValueEval evaluate(ValueEval[] args, int srcCellRow, int srcCellCol) {
		if(args.length < 3 || args.length > 5) {
			return ErrorEval.VALUE_INVALID;
		}

		try {
			BaseRef baseRef = evaluateBaseRef(args[0]);
			int rowOffset = evaluateIntArg(args[1], srcCellRow, srcCellCol);
			int columnOffset = evaluateIntArg(args[2], srcCellRow, srcCellCol);
			int height = baseRef.getHeight();
			int width = baseRef.getWidth();
			// optional arguments
			// If height or width is omitted, it is assumed to be the same height or width as reference.
			switch(args.length) {
				case 5:
					if(!(args[4] instanceof MissingArgEval)) {
						width = evaluateIntArg(args[4], srcCellRow, srcCellCol);
					}
					// fall-through to pick up height
				case 4:
					if(!(args[3] instanceof MissingArgEval)) {
						height = evaluateIntArg(args[3], srcCellRow, srcCellCol);
					}
					break;
				//case 3:
				// nothing to do
				default:
					break;
			}
			// Zero height or width raises #REF! error
			if(height == 0 || width == 0) {
				return ErrorEval.REF_INVALID;
			}
			LinearOffsetRange rowOffsetRange = new LinearOffsetRange(rowOffset, height);
			LinearOffsetRange colOffsetRange = new LinearOffsetRange(columnOffset, width);
			AreaEval areaEval = createOffset(baseRef, rowOffsetRange, colOffsetRange);
			//-------处理数据开始---------
			try {
				ValueEval valueEval;
				if(width == 1 && height == 1)
				{
					valueEval = OperandResolver.getSingleValue(areaEval, srcCellRow, srcCellCol);
				}else{
					valueEval = areaEval;
				}
				String excelId = new ThreadUtil().getExcelUid();
				int funcValueType = Integer.parseInt(SourceValueType.valueOf(valueEval.getClass().getSimpleName()).toString());
				int firstRow = areaEval.getFirstRow();
				int firstColumn = areaEval.getFirstColumn();
				int lastRow = areaEval.getLastRow();
				int lastColumn = areaEval.getLastColumn();
				String funcValue = "";
				if (valueEval instanceof NumberEval) {
					NumberEval ne = (NumberEval) valueEval;
					funcValue = String.valueOf(ne.getNumberValue());
				} else if (valueEval instanceof BoolEval) {
					BoolEval be = (BoolEval) valueEval;
					funcValue = String.valueOf(be.getBooleanValue());
				}else if (valueEval instanceof StringEval) {
					StringEval ne = (StringEval) valueEval;
					funcValue = String.valueOf(ne.getStringValue());
				}else if (valueEval instanceof ErrorEval) {
					funcValue = String.valueOf(((ErrorEval)valueEval).getErrorCode());
				}else if (valueEval instanceof LazyAreaEval){
					LazyAreaEval lazyAreaEval= (LazyAreaEval) valueEval;
					CellReference crA = new CellReference(lazyAreaEval.getFirstRow(), lazyAreaEval.getFirstColumn());
					CellReference crB = new CellReference(lazyAreaEval.getLastRow(), lazyAreaEval.getLastColumn());
					funcValue =crA.formatAsString()+":"+crB.formatAsString();
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
					JSONArray jsonArray1 = new JSONArray();
					for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
						for (int columnIndex = firstColumn; columnIndex <= lastColumn; columnIndex++) {
							CellReference cellReference = new CellReference(rowIndex,columnIndex);
							// 添加新的
							JSONObject newJsonObject = new JSONObject();
							newJsonObject.put("nodeType",Integer.parseInt(SourceNodeType.valueOf("RefPtg").toString()));
							newJsonObject.put("nodeAttr", cellReference.formatAsString());
							newJsonObject.put("numArgs", 0);
							newJsonObject.put("sheetIndex", areaEval.getFirstSheetIndex());
							// 连接旧的
							jsonArray1.add(newJsonObject);
						}
					}
					jsonObject.put("para_info",jsonArray1);
					// 更改有效性数据
					operationUtils.updateData(recordId,jsonArray.toJSONString());
				}
			}catch (Exception e)
			{
				System.out.println("函数内部重算出错"+e);
			}
			//-------处理数据结束---------
			return areaEval;
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	private static AreaEval createOffset(BaseRef baseRef,
										 LinearOffsetRange orRow, LinearOffsetRange orCol) throws EvaluationException {
		LinearOffsetRange absRows = orRow.normaliseAndTranslate(baseRef.getFirstRowIndex());
		LinearOffsetRange absCols = orCol.normaliseAndTranslate(baseRef.getFirstColumnIndex());

		if(absRows.isOutOfBounds(0, LAST_VALID_ROW_INDEX)) {
			throw new EvaluationException(ErrorEval.REF_INVALID);
		}
		if(absCols.isOutOfBounds(0, LAST_VALID_COLUMN_INDEX)) {
			throw new EvaluationException(ErrorEval.REF_INVALID);
		}
		return baseRef.offset(orRow.getFirstIndex(), orRow.getLastIndex(), orCol.getFirstIndex(), orCol.getLastIndex());
	}

	private static BaseRef evaluateBaseRef(ValueEval eval) throws EvaluationException {

		if(eval instanceof RefEval) {
			return new BaseRef((RefEval)eval);
		}
		if(eval instanceof AreaEval) {
			return new BaseRef((AreaEval)eval);
		}
		if (eval instanceof ErrorEval) {
			throw new EvaluationException((ErrorEval) eval);
		}
		throw new EvaluationException(ErrorEval.VALUE_INVALID);
	}

	/**
	 * OFFSET's numeric arguments (2..5) have similar processing rules
	 */
	static int evaluateIntArg(ValueEval eval, int srcCellRow, int srcCellCol) throws EvaluationException {
		ValueEval ve = OperandResolver.getSingleValue(eval, srcCellRow, srcCellCol);
		return OperandResolver.coerceValueToInt(ve);
	}
}