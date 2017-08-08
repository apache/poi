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

package org.apache.poi.xddf.usermodel;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Beta;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrData;

/**
 * Class {@code XDDFDataSourcesFactory} is a factory for {@link XDDFDataSource} instances.
 */
@Beta
public class XDDFDataSourcesFactory {

    private XDDFDataSourcesFactory() {
    }

    public static XDDFCategoryDataSource fromDataSource(final CTAxDataSource categoryDS) {
        return new XDDFCategoryDataSource() {
            private CTStrData category = (CTStrData) categoryDS.getStrRef().getStrCache().copy();

            @Override
            public boolean isNumeric() {
                return false;
            }

            @Override
            public boolean isReference() {
                return true;
            }

            @Override
            public int getPointCount() {
                return (int) category.getPtCount().getVal();
            }

            @Override
            public String getPointAt(int index) {
                return category.getPtArray(index).getV();
            }

            @Override
            public String getDataRangeReference() {
                return categoryDS.getStrRef().getF();
            }
        };
    }

    public static XDDFNumericalDataSource<Double> fromDataSource(final CTNumDataSource valuesDS) {
        return new XDDFNumericalDataSource<Double>() {
            private CTNumData values = (CTNumData) valuesDS.getNumRef().getNumCache().copy();
            private String formatCode = values.isSetFormatCode() ? values.getFormatCode() : null;

            @Override
            public String getFormatCode() {
                return formatCode;
            }

            @Override
            public void setFormatCode(String formatCode) {
                this.formatCode = formatCode;
            }

            @Override
            public boolean isNumeric() {
                return true;
            }

            @Override
            public boolean isReference() {
                return true;
            }

            @Override
            public int getPointCount() {
                return (int) values.getPtCount().getVal();
            }

            @Override
            public Double getPointAt(int index) {
                return Double.valueOf(values.getPtArray(index).getV());
            }

            @Override
            public String getDataRangeReference() {
                return valuesDS.getNumRef().getF();
            }
        };
    }

    public static <T extends Number> XDDFNumericalDataSource<T> fromArray(T[] elements, String dataRange) {
        return new NumericalArrayDataSource<T>(elements, dataRange);
    }

    public static XDDFCategoryDataSource fromArray(String[] elements, String dataRange) {
        return new StringArrayDataSource(elements, dataRange);
    }

    public static XDDFNumericalDataSource<Double> fromNumericCellRange(XSSFSheet sheet,
            CellRangeAddress cellRangeAddress) {
        return new NumericalCellRangeDataSource(sheet, cellRangeAddress);
    }

    public static XDDFCategoryDataSource fromStringCellRange(XSSFSheet sheet, CellRangeAddress cellRangeAddress) {
        return new StringCellRangeDataSource(sheet, cellRangeAddress);
    }

    private abstract static class AbstractArrayDataSource<T> implements XDDFDataSource<T> {
        private final T[] elements;
        private final String dataRange;

        public AbstractArrayDataSource(T[] elements, String dataRange) {
            this.elements = elements.clone();
            this.dataRange = dataRange;
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
            return dataRange != null;
        }

        @Override
        public boolean isNumeric() {
            Class<?> arrayComponentType = elements.getClass().getComponentType();
            return (Number.class.isAssignableFrom(arrayComponentType));
        }

        @Override
        public String getDataRangeReference() {
            if (dataRange == null) {
                throw new UnsupportedOperationException("Literal data source can not be expressed by reference.");
            } else {
                return dataRange;
            }
        }
    }

    private static class NumericalArrayDataSource<T extends Number> extends AbstractArrayDataSource<T>
            implements XDDFNumericalDataSource<T> {
        private String formatCode;

        public NumericalArrayDataSource(T[] elements, String dataRange) {
            super(elements, dataRange);
        }

        @Override
        public String getFormatCode() {
            return formatCode;
        }

        @Override
        public void setFormatCode(String formatCode) {
            this.formatCode = formatCode;
        }
    }

    private static class StringArrayDataSource extends AbstractArrayDataSource<String>
            implements XDDFCategoryDataSource {
        public StringArrayDataSource(String[] elements, String dataRange) {
            super(elements, dataRange);
        }
    }

    private abstract static class AbstractCellRangeDataSource<T> implements XDDFDataSource<T> {
        private final XSSFSheet sheet;
        private final CellRangeAddress cellRangeAddress;
        private final int numOfCells;
        private XSSFFormulaEvaluator evaluator;

        protected AbstractCellRangeDataSource(XSSFSheet sheet, CellRangeAddress cellRangeAddress) {
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
        public String getDataRangeReference() {
            return cellRangeAddress.formatAsString(sheet.getSheetName(), true);
        }

        protected CellValue getCellValueAt(int index) {
            if (index < 0 || index >= numOfCells) {
                throw new IndexOutOfBoundsException(
                        "Index must be between 0 and " + (numOfCells - 1) + " (inclusive), given: " + index);
            }
            int firstRow = cellRangeAddress.getFirstRow();
            int firstCol = cellRangeAddress.getFirstColumn();
            int lastCol = cellRangeAddress.getLastColumn();
            int width = lastCol - firstCol + 1;
            int rowIndex = firstRow + index / width;
            int cellIndex = firstCol + index % width;
            XSSFRow row = sheet.getRow(rowIndex);
            return (row == null) ? null : evaluator.evaluate(row.getCell(cellIndex));
        }
    }

    private static class NumericalCellRangeDataSource extends AbstractCellRangeDataSource<Double>
            implements XDDFNumericalDataSource<Double> {
        protected NumericalCellRangeDataSource(XSSFSheet sheet, CellRangeAddress cellRangeAddress) {
            super(sheet, cellRangeAddress);
        }

        private String formatCode;

        @Override
        public String getFormatCode() {
            return formatCode;
        }

        @Override
        public void setFormatCode(String formatCode) {
            this.formatCode = formatCode;
        }

        @Override
        public Double getPointAt(int index) {
            CellValue cellValue = getCellValueAt(index);
            if (cellValue != null && cellValue.getCellTypeEnum() == CellType.NUMERIC) {
                return Double.valueOf(cellValue.getNumberValue());
            } else {
                return null;
            }
        }

        @Override
        public boolean isNumeric() {
            return true;
        }
    }

    private static class StringCellRangeDataSource extends AbstractCellRangeDataSource<String>
            implements XDDFCategoryDataSource {
        protected StringCellRangeDataSource(XSSFSheet sheet, CellRangeAddress cellRangeAddress) {
            super(sheet, cellRangeAddress);
        }

        @Override
        public String getPointAt(int index) {
            CellValue cellValue = getCellValueAt(index);
            if (cellValue != null && cellValue.getCellTypeEnum() == CellType.STRING) {
                return cellValue.getStringValue();
            } else {
                return null;
            }
        }

        @Override
        public boolean isNumeric() {
            return false;
        }
    }
}
