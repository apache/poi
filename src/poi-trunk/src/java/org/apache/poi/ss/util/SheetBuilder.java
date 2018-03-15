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
 * Class {@code SheetBuilder} provides an easy way of building workbook sheets
 * from 2D array of Objects. It can be used in test cases to improve code
 * readability or in Swing applications with tables.
 *
 * @author Roman Kashitsyn
 */
public class SheetBuilder {

    private final Workbook workbook;
    private final Object[][] cells;
    private boolean shouldCreateEmptyCells;
    private String sheetName;

    public SheetBuilder(Workbook workbook, Object[][] cells) {
        this.workbook = workbook;
        this.cells = cells.clone();
    }

    /**
     * Returns {@code true} if null array elements should be treated as empty
     * cells.
     *
     * @return {@code true} if null objects should be treated as empty cells
     *         and {@code false} otherwise
     */
    public boolean getCreateEmptyCells() {
        return shouldCreateEmptyCells;
    }

    /**
     * Specifies if null array elements should be treated as empty cells.
     *
     * @param shouldCreateEmptyCells {@code true} if null array elements should be
     *                               treated as empty cells
     * @return {@code this}
     */
    public SheetBuilder setCreateEmptyCells(boolean shouldCreateEmptyCells) {
        this.shouldCreateEmptyCells = shouldCreateEmptyCells;
        return this;
    }

    /**
     * Specifies name of the sheet to build. If not specified, default name (provided by
     * workbook) will be used instead.
     * @param sheetName sheet name to use
     * @return {@code this}
     */
    public SheetBuilder setSheetName(String sheetName) {
        this.sheetName = sheetName;
        return this;
    }

    /**
     * Builds sheet from parent workbook and 2D array with cell
     * values. Creates rows anyway (even if row contains only null
     * cells), creates cells if either corresponding array value is not
     * null or createEmptyCells property is true.
     * The conversion is performed in the following way:
     * <p>
     * <ul>
     * <li>Numbers become numeric cells.</li>
     * <li><code>java.util.Date</code> or <code>java.util.Calendar</code>
     * instances become date cells.</li>
     * <li>String with leading '=' char become formulas (leading '='
     * will be truncated).</li>
     * <li>Other objects become strings via <code>Object.toString()</code>
     * method call.</li>
     * </ul>
     *
     * @return newly created sheet
     */
    public Sheet build() {
        Sheet sheet = (sheetName == null) ? workbook.createSheet() : workbook.createSheet(sheetName);
        Row currentRow;
        Cell currentCell;

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
     *
     * @param cell  cell to change
     * @param value value to set
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null || cell == null) {
            return;
        }

        if (value instanceof Number) {
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
            return str.length() >= 2 && str.charAt(0) == '=';
        } else {
            return false;
        }
    }

    private String getFormula(Object obj) {
        return ((String) obj).substring(1);
    }
}
