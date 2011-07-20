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
package org.apache.poi.ss.util;

import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;

/**
 * Class that provides useful sheet build capabilities. It can be used
 * in test cases to improve readability or in Swing applications with
 * tables.
 *
 * @author Roman Kashitsyn
 */
public class SheetBuilder {

    private Workbook workbook;
    private Object[][] cells;
    private boolean shouldCreateEmptyCells = false;

    public SheetBuilder(Workbook workbook, Object[][] cells) {
	this.workbook = workbook;
	this.cells = cells;
    }

    /**
     * @return true if null objects should be trated as empty cells
     *         false otherwise
     */
    public boolean getCreateEmptyCells() {
	return shouldCreateEmptyCells;
    }

    /**
     * @param shouldCreateEmptyCells true if null array elements should be
     *        trated as empty cells
     * @return this
     */
    public SheetBuilder setCreateEmptyCells(boolean shouldCreateEmptyCells) {
	this.shouldCreateEmptyCells = shouldCreateEmptyCells;
	return this;
    }

    /**
     * Builds sheet from parent workbook and 2D array with cell
     * values. Creates rows anyway (even if row contains only null
     * cells), creates cells only if corresponding property is true.
     * The conversion is performed in the following way:
     *
     * <ul>
     * <li>Numbers become numeric cells.</li>
     * <li><code>java.util.Date</code> or <code>java.util.Calendar</code>
     *     instances become date cells.</li>
     * <li>String with leading '=' char become formulas (leading '='
     *     trancated).</li>
     * <li>Other objects become strings via <code>Object.toString()</code>
     *     method.</li>
     * </ul>
     *
     * @return newly created sheet
     */
    public Sheet build() {
	Sheet sheet = workbook.createSheet();
	Row currentRow = null;
	Cell currentCell = null;

	for (int rowIndex = 0; rowIndex < cells.length; ++rowIndex) {
	    Object[] rowArray = cells[rowIndex];
	    currentRow = sheet.createRow(rowIndex);

	    for (int cellIndex = 0; cellIndex < rowArray.length; ++cellIndex) {
		Object cellValue = rowArray[cellIndex];
		if (cellValue != null || shouldCreateEmptyCells) {
		    currentCell = currentRow.createCell(cellIndex);
		    setCellValue(currentCell, cellValue);
		}
	    }
	}
	return sheet;
    }

    /**
     * Sets the cell value using object type information.
     * @param cell cell to change
     * @param value value to set
     */
    public void setCellValue(Cell cell, Object value) {
	if (value == null || cell == null) {
	    return;
	} else if (value instanceof Number) {
	    double doubleValue = ((Number) value).doubleValue();
	    cell.setCellValue(doubleValue);
	} else if (value instanceof Date) {
	    cell.setCellValue((Date) value);
	} else if (value instanceof Calendar) {
	    cell.setCellValue((Calendar) value);
	} else if (isFormulaDefinition(value)) {
	    cell.setCellFormula(getFormula(value));
	} else {
	    cell.setCellValue(value.toString());
	}
    }

    private boolean isFormulaDefinition(Object obj) {
	if (obj instanceof String) {
	    String str = (String) obj;
	    if (str.length() < 2) {
		return false;
	    } else {
		return ((String) obj).charAt(0) == '=';
	    }
	} else {
	    return false;
	}
    }

    private String getFormula(Object obj) {
	return ((String) obj).substring(1);
    }
}