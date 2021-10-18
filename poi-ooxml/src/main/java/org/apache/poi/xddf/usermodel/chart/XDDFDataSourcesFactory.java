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

package org.apache.poi.xddf.usermodel.chart;

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
 * Class {@code XDDFDataSourcesFactory} is a factory for {@link XDDFDataSource}
 * instances.
 */
@Beta
public class XDDFDataSourcesFactory {

    private XDDFDataSourcesFactory() {
    }

    public static XDDFCategoryDataSource fromDataSource(final CTAxDataSource categoryDS) {
        if (categoryDS == null) {
            return null;
        }
        if (categoryDS.getNumRef() != null && categoryDS.getNumRef().getNumCache() != null) {
            return new XDDFCategoryDataSource() {
                private final CTNumData category = (CTNumData) categoryDS.getNumRef().getNumCache().copy();
                private final String formatCode = category.isSetFormatCode() ? category.getFormatCode() : null;

                @Override
                public boolean isCellRange() {
                    return true;
                }

                @Override
                public boolean isNumeric() {
                    return true;
                }

                @Override
                public String getDataRangeReference() {
                    return categoryDS.getNumRef().getF();
                }

                @Override
                public int getPointCount() {
                    return (int) category.getPtCount().getVal();
                }

                @Override
                public String getPointAt(int index) {
                    if (category.sizeOfPtArray() <= index) {
                        throw new IllegalArgumentException("Cannot access 0-based index " + index +
                                " in point-array with " + category.sizeOfPtArray() + " items");
                    }
                    return category.getPtArray(index).getV();
                }

                @Override
                public String getFormatCode() { return formatCode; }
            };
        } else if (categoryDS.getStrRef() != null && categoryDS.getStrRef().getStrCache() != null) {
            return new XDDFCategoryDataSource() {
                private final CTStrData category = (CTStrData) categoryDS.getStrRef().getStrCache().copy();

                @Override
                public boolean isCellRange() {
                    return true;
                }

                @Override
                public String getDataRangeReference() {
                    return categoryDS.getStrRef().getF();
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
                public String getFormatCode() { return null; }
            };
        } else if (categoryDS.getNumLit() != null) {
            return new XDDFCategoryDataSource() {
                private final CTNumData category = (CTNumData) categoryDS.getNumLit().copy();
                private final String formatCode = category.isSetFormatCode() ? category.getFormatCode() : null;

                @Override
                public boolean isCellRange() {
                    return false;
                }

                @Override
                public boolean isLiteral() {
                    return true;
                }

                @Override
                public boolean isNumeric() {
                    return true;
                }

                @Override
                public boolean isReference() {
                    return false;
                }

                @Override
                public String getDataRangeReference() {
                    return null;
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
                public String getFormatCode() { return formatCode; }
            };
        } else if (categoryDS.getStrLit() != null) {
            return new XDDFCategoryDataSource() {
                private final CTStrData category = (CTStrData) categoryDS.getStrLit().copy();

                @Override
                public boolean isCellRange() {
                    return false;
                }

                @Override
                public boolean isLiteral() {
                    return true;
                }

                @Override
                public boolean isReference() {
                    return false;
                }

                @Override
                public String getDataRangeReference() {
                    return null;
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
                public String getFormatCode() { return null; }
            };
        } else {
            return null; // in some weird cases the element is empty
        }
    }

    public static XDDFNumericalDataSource<Double> fromDataSource(final CTNumDataSource valuesDS) {
        if (valuesDS == null) {
            return null;
        }
        if (valuesDS.getNumRef() != null && valuesDS.getNumRef().getNumCache() != null) {
            return new XDDFNumericalDataSource<Double>() {
                private final CTNumData values = (CTNumData) valuesDS.getNumRef().getNumCache().copy();
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
                public boolean isCellRange() {
                    return true;
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

                @Override
                public int getColIndex() {
                    return 0;
                }
            };
        } else if (valuesDS.getNumLit() != null) {
            return new XDDFNumericalDataSource<Double>() {
                private final CTNumData values = (CTNumData) valuesDS.getNumLit().copy();
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
                public boolean isCellRange() {
                    return false;
                }

                @Override
                public boolean isLiteral() {
                    return true;
                }

                @Override
                public boolean isNumeric() {
                    return true;
                }

                @Override
                public boolean isReference() {
                    return false;
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
                    return null;
                }

                @Override
                public int getColIndex() {
                    return 0;
                }
            };
        } else {
            return null; // in some weird cases the element is empty
        }
    }

    public static <T extends Number> XDDFNumericalDataSource<T> fromArray(T[] elements) {
        return new LiteralNumericalArrayDataSource<>(elements);
    }

    public static XDDFCategoryDataSource fromArray(String[] elements) {
        return new LiteralStringArrayDataSource(elements);
    }

    public static <T extends Number> XDDFNumericalDataSource<T> fromArray(T[] elements, String dataRange) {
        return new NumericalArrayDataSource<>(elements, dataRange);
    }

    public static XDDFCategoryDataSource fromArray(String[] elements, String dataRange) {
        return new StringArrayDataSource(elements, dataRange);
    }

    public static <T extends Number> XDDFNumericalDataSource<T> fromArray(T[] elements, String dataRange, int col) {
        return new NumericalArrayDataSource<>(elements, dataRange, col);
    }

    public static XDDFCategoryDataSource fromArray(String[] elements, String dataRange, int col) {
        return new StringArrayDataSource(elements, dataRange, col);
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
        private int col = 0;

        public AbstractArrayDataSource(T[] elements, String dataRange) {
            this.elements = elements.clone();
            this.dataRange = dataRange;
        }

        public AbstractArrayDataSource(T[] elements, String dataRange, int col) {
            this.elements = elements.clone();
            this.dataRange = dataRange;
            this.col = col;
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
        public boolean isCellRange() {
            return false;
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

        @Override
        public int getColIndex() {
            return col;
        }
    }

    private static class NumericalArrayDataSource<T extends Number> extends AbstractArrayDataSource<T>
            implements XDDFNumericalDataSource<T> {
        private String formatCode;

        public NumericalArrayDataSource(T[] elements, String dataRange) {
            super(elements, dataRange);
        }

        public NumericalArrayDataSource(T[] elements, String dataRange, int col) {
            super(elements, dataRange, col);
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

        public StringArrayDataSource(String[] elements, String dataRange, int col) {
            super(elements, dataRange, col);
        }

        @Override
        public String getFormatCode() { return null; }
    }

    private static class LiteralNumericalArrayDataSource<T extends Number> extends NumericalArrayDataSource<T> {
        public LiteralNumericalArrayDataSource(T[] elements) {
            super(elements, null, 0);
        }

        @Override
        public boolean isLiteral() {
            return true;
        }
    }

    private static class LiteralStringArrayDataSource extends StringArrayDataSource {
        public LiteralStringArrayDataSource(String[] elements) {
            super(elements, null, 0);
        }

        @Override
        public boolean isLiteral() {
            return true;
        }
    }

    private abstract static class AbstractCellRangeDataSource<T> implements XDDFDataSource<T> {
        private final XSSFSheet sheet;
        private final CellRangeAddress cellRangeAddress;
        private final int numOfCells;
        private final XSSFFormulaEvaluator evaluator;

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
        public boolean isCellRange() {
            return true;
        }

        @Override
        public boolean isReference() {
            return true;
        }

        @Override
        public int getColIndex() {
            return cellRangeAddress.getFirstColumn();
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
            if (cellValue != null && cellValue.getCellType() == CellType.NUMERIC) {
                return cellValue.getNumberValue();
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

        @Override
        public String getFormatCode() { return null; }
    }
}
