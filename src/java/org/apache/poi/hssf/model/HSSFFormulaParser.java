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

import java.util.List;

import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;

/**
 * HSSF wrapper for the {@link FormulaParser}
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

	public static Ptg[] parse(String formula, HSSFWorkbook workbook) {
		return FormulaParser.parse(formula, createParsingWorkbook(workbook));
	}

	public static Ptg[] parse(String formula, HSSFWorkbook workbook, int formulaType) {
		return FormulaParser.parse(formula, createParsingWorkbook(workbook), formulaType);
	}

	public static String toFormulaString(HSSFWorkbook book, List lptgs) {
		return toFormulaString(HSSFEvaluationWorkbook.create(book), lptgs);
	}
	/**
	 * Convenience method which takes in a list then passes it to the
	 *  other toFormulaString signature.
	 * @param book   workbook for 3D and named references
	 * @param lptgs  list of Ptg, can be null or empty
	 * @return a human readable String
	 */
	public static String toFormulaString(FormulaRenderingWorkbook book, List lptgs) {
		Ptg[] ptgs = new Ptg[lptgs.size()];
		lptgs.toArray(ptgs);
		return FormulaRenderer.toFormulaString(book, ptgs);
	}
    
	public static String toFormulaString(HSSFWorkbook book, Ptg[] ptgs) {
		return FormulaRenderer.toFormulaString(HSSFEvaluationWorkbook.create(book), ptgs);
	}
}
