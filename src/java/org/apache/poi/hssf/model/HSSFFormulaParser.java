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

package org.apache.poi.hssf.model;

import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;

/**
 * HSSF wrapper for the {@link FormulaParser} and {@link FormulaRenderer}
 *
 * @author Josh Micich
 */
public final class HSSFFormulaParser {

	private static FormulaParsingWorkbook createParsingWorkbook(HSSFWorkbook book) {
		return HSSFEvaluationWorkbook.create(book);
	}

	private HSSFFormulaParser() {
		// no instances of this class
	}

	/**
	 * Convenience method for parsing cell formulas. see {@link #parse(String, HSSFWorkbook, int, int)}
	 */
	public static Ptg[] parse(String formula, HSSFWorkbook workbook) throws FormulaParseException {
		return parse(formula, workbook, FormulaType.CELL);
	}

	/**
	 * @param formulaType a constant from {@link FormulaType}
	 * @return the parsed formula tokens
     * @throws FormulaParseException if the formula has incorrect syntax or is otherwise invalid
	 */
	public static Ptg[] parse(String formula, HSSFWorkbook workbook, int formulaType) throws FormulaParseException {
		return parse(formula, workbook, formulaType, -1);
	}

	/**
	 * @param formula     the formula to parse
	 * @param workbook    the parent workbook
	 * @param formulaType a constant from {@link FormulaType}
	 * @param sheetIndex  the 0-based index of the sheet this formula belongs to.
	 * The sheet index is required to resolve sheet-level names. <code>-1</code> means that
	 * the scope of the name will be ignored and  the parser will match named ranges only by name
	 *
	 * @return the parsed formula tokens
     * @throws FormulaParseException if the formula has incorrect syntax or is otherwise invalid
	 */
	public static Ptg[] parse(String formula, HSSFWorkbook workbook, int formulaType, int sheetIndex) throws FormulaParseException {
		return FormulaParser.parse(formula, createParsingWorkbook(workbook), formulaType, sheetIndex);
	}

	/**
	 * Static method to convert an array of {@link Ptg}s in RPN order
	 * to a human readable string format in infix mode.
	 * @param book  used for defined names and 3D references
	 * @param ptgs  must not be <code>null</code>
	 * @return a human readable String
	 */
	public static String toFormulaString(HSSFWorkbook book, Ptg[] ptgs) {
		return FormulaRenderer.toFormulaString(HSSFEvaluationWorkbook.create(book), ptgs);
	}
}
