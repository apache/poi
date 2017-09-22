/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.usermodel.charts;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Removal;

/**
 * Class {@code DataSources} is a factory for {@link ChartDataSource} instances.
 *
 *@deprecated use XDDFDataSourcesFactory instead
 */
@Deprecated
@Removal(version="4.2")
public class DataSources {

    private DataSources() {
    }

    public static <T> ChartDataSource<T> fromArray(T[] elements) {
        return new ArrayDataSource<>(elements);
    }

    public static ChartDataSource<Number> fromNumericCellRange(Sheet sheet, CellRangeAddress cellRangeAddress) {
        return new AbstractCellRangeDataSource<Number>(sheet, cellRangeAddress) {
            @Override
            public Number getPointAt(int index) {
                CellValue cellValue = getCellValueAt(index);
                if (cellValue != null && cellValue.getCellType() == CellType.NUMERIC) {
                    return Double.valueOf(cellValue.getNumberValue());
                } else {
                    return null;
                }
            }

            @Override
            public boolean isNumeric() {
                return true;
            }
        };
    }

    public static ChartDataSource<String> fromStringCellRange(Sheet sheet, CellRangeAddress cellRangeAddress) {
        return new AbstractCellRangeDataSource<String>(sheet, cellRangeAddress) {
            @Override
            public String getPointAt(int index) {
                CellValue cellValue = getCellValueAt(index);
                if (cellValue != null && cellValue.getCellType() == CellType.STRING) {
                    return cellValue.getStringValue();
                } else {
                    return null;
                }
            }

            @Override
            public boolean isNumeric() {
                return false;
            }
        };
    }

    private static class ArrayDataSource<T> implements ChartDataSource<T> {

        private final T[] elements;

        public ArrayDataSource(T[] elements) {
            this.elements = elements.clone();
        }

        @Override
        public int getPointCount() {
            return elements.length;
        }

        @Override
        public T getPointAt(int index) {
            return elements[index];
        }

        @Override
        public boolean isReference() {
            return false;
        }

        @Override
        public boolean isNumeric() {
            Class<?> arrayComponentType = elements.getClass().getComponentType();
            return (Number.class.isAssignableFrom(arrayComponentType));
        }

        @Override
        public String getFormulaString() {
            throw new UnsupportedOperationException("Literal data source can not be expressed by reference.");
        }
    }

    private abstract static class AbstractCellRangeDataSource<T> implements ChartDataSource<T> {
        private final Sheet sheet;
        private final CellRangeAddress cellRangeAddress;
        private final int numOfCells;
        private FormulaEvaluator evaluator;

        protected AbstractCellRangeDataSource(Sheet sheet, CellRangeAddress cellRangeAddress) {
            this.sheet = sheet;
            // Make copy since CellRangeAddress is mutable.
            this.cellRangeAddress = cellRangeAddress.copy();
            this.numOfCells = this.cellRangeAddress.getNumberOfCells();
            this.evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        }

        @Override
        public int getPointCount() {
            return numOfCells;
        }

        @Override
        public boolean isReference() {
            return true;
        }

        @Override
        public String getFormulaString() {
            return cellRangeAddress.formatAsString(sheet.getSheetName(), true);
        }

        protected CellValue getCellValueAt(int index) {
            if (index < 0 || index >= numOfCells) {
                throw new IndexOutOfBoundsException("Index must be between 0 and " +
                        (numOfCells - 1) + " (inclusive), given: " + index);
            }
            int firstRow = cellRangeAddress.getFirstRow();
            int firstCol = cellRangeAddress.getFirstColumn();
            int lastCol = cellRangeAddress.getLastColumn();
            int width = lastCol - firstCol + 1;
            int rowIndex = firstRow + index / width;
            int cellIndex = firstCol + index % width;
            Row row = sheet.getRow(rowIndex);
            return (row == null) ? null : evaluator.evaluate(row.getCell(cellIndex));
        }
    }
}
