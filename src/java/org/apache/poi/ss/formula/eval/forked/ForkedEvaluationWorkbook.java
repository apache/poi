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

package org.apache.poi.ss.formula.eval.forked;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.record.formula.NamePtg;
import org.apache.poi.hssf.record.formula.NameXPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationName;
import org.apache.poi.ss.formula.EvaluationSheet;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Represents a workbook being used for forked evaluation. Most operations are delegated to the
 * shared master workbook, except those that potentially involve cell values that may have been
 * updated after a call to {@link #getOrCreateUpdatableCell(String, int, int)}.
 *
 * @author Josh Micich
 */
final class ForkedEvaluationWorkbook implements EvaluationWorkbook {

	private final EvaluationWorkbook _masterBook;
	private final Map<String, ForkedEvaluationSheet> _sharedSheetsByName;

	public ForkedEvaluationWorkbook(EvaluationWorkbook master) {
		_masterBook = master;
		_sharedSheetsByName = new HashMap<String, ForkedEvaluationSheet>();
	}

	public ForkedEvaluationCell getOrCreateUpdatableCell(String sheetName, int rowIndex,
			int columnIndex) {
		ForkedEvaluationSheet sheet = getSharedSheet(sheetName);
		return sheet.getOrCreateUpdatableCell(rowIndex, columnIndex);
	}

	public EvaluationCell getEvaluationCell(String sheetName, int rowIndex, int columnIndex) {
		ForkedEvaluationSheet sheet = getSharedSheet(sheetName);
		return sheet.getCell(rowIndex, columnIndex);
	}

	private ForkedEvaluationSheet getSharedSheet(String sheetName) {
		ForkedEvaluationSheet result = _sharedSheetsByName.get(sheetName);
		if (result == null) {
			result = new ForkedEvaluationSheet(_masterBook.getSheet(_masterBook
					.getSheetIndex(sheetName)));
			_sharedSheetsByName.put(sheetName, result);
		}
		return result;
	}

	public void copyUpdatedCells(Workbook workbook) {
		String[] sheetNames = new String[_sharedSheetsByName.size()];
		_sharedSheetsByName.keySet().toArray(sheetNames);
		OrderedSheet[] oss = new OrderedSheet[sheetNames.length];
		for (int i = 0; i < sheetNames.length; i++) {
			String sheetName = sheetNames[i];
			oss[i] = new OrderedSheet(sheetName, _masterBook.getSheetIndex(sheetName));
		}
		for (int i = 0; i < oss.length; i++) {
			String sheetName = oss[i].getSheetName();
			ForkedEvaluationSheet sheet = _sharedSheetsByName.get(sheetName);
			sheet.copyUpdatedCells(workbook.getSheet(sheetName));
		}
	}

	public int convertFromExternSheetIndex(int externSheetIndex) {
		return _masterBook.convertFromExternSheetIndex(externSheetIndex);
	}

	public ExternalSheet getExternalSheet(int externSheetIndex) {
		return _masterBook.getExternalSheet(externSheetIndex);
	}

	public Ptg[] getFormulaTokens(EvaluationCell cell) {
		if (cell instanceof ForkedEvaluationCell) {
			// doesn't happen yet because formulas cannot be modified from the master workbook
			throw new RuntimeException("Updated formulas not supported yet");
		}
		return _masterBook.getFormulaTokens(cell);
	}

	public EvaluationName getName(NamePtg namePtg) {
		return _masterBook.getName(namePtg);
	}

	public EvaluationSheet getSheet(int sheetIndex) {
		return getSharedSheet(getSheetName(sheetIndex));
	}
	
	public ExternalName getExternalName(int externSheetIndex, int externNameIndex) {
	   return _masterBook.getExternalName(externSheetIndex, externNameIndex);
	}

	public int getSheetIndex(EvaluationSheet sheet) {
		if (sheet instanceof ForkedEvaluationSheet) {
			ForkedEvaluationSheet mes = (ForkedEvaluationSheet) sheet;
			return mes.getSheetIndex(_masterBook);
		}
		return _masterBook.getSheetIndex(sheet);
	}

	public int getSheetIndex(String sheetName) {
		return _masterBook.getSheetIndex(sheetName);
	}

	public String getSheetName(int sheetIndex) {
		return _masterBook.getSheetName(sheetIndex);
	}

	public String resolveNameXText(NameXPtg ptg) {
		return _masterBook.resolveNameXText(ptg);
	}

	private static final class OrderedSheet implements Comparable<OrderedSheet> {
		private final String _sheetName;
		private final int _index;

		public OrderedSheet(String sheetName, int index) {
			_sheetName = sheetName;
			_index = index;
		}
		public String getSheetName() {
			return _sheetName;
		}
		public int compareTo(OrderedSheet o) {
			return _index - o._index;
		}
	}
}
