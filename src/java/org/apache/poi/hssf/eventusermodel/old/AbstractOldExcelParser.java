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
package org.apache.poi.hssf.eventusermodel.old;

import org.apache.poi.hssf.record.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser base
 *
 * @param <C> column type
 * @param <E> exception type thrown by data parsing errors
 */
public abstract class AbstractOldExcelParser<C extends AbstractColumn, E extends Exception> implements OldHSSFListener {

    private static final DoubleGetter DOUBLE_GETTER = new DoubleGetter();
    private static final LongGetter LONG_GETTER = new LongGetter();
    private static final StringGetter STRING_GETTER = new StringGetter();


    // Sheet scope
    /**
     * Current sheet name
     */
    private String sheetName;
    /**
     * Columns to extract data
     */
    private final Map<Short, C> columns = new HashMap<Short, C>();
    /**
     * Flag of successfull table structure
     */
    private boolean headerFound;
    /**
     * Current row num
     */
    private int currentRowNum;

    /**
     * @return current sheet name
     */
    protected String getSheetName() {
        return sheetName;
    }

    /**
     * @return current row num
     */
    protected int getCurrentRowNum() {
        return currentRowNum;
    }

    private void onRowEnd(int rowNum) {
        if (currentRowNum >= 0) {
            if (headerFound) {
                onDataRowEnd();
                for (Map.Entry<Short, C> e : columns.entrySet()) {
                    e.getValue().setRecordValue(null);
                }
            } else {
                if (columns.size() > 0) {
                    if (tryHeaderEnd(columns)) {
                        headerFound = true;
                    } else {
                        columns.clear();
                    }
                }
            }
        }
        this.currentRowNum = rowNum;
    }

    /**
     * Proves that header structure
     *
     * @param header header filled by {@link #tryHeaderRecord(short, String, Map)} method
     * @return {@code true} when header structure is ok
     */
    protected abstract boolean tryHeaderEnd(Map<Short, C> header);

    /**
     * Called when a row ends
     */
    protected abstract void onDataRowEnd();

    @Override
    public void onOldSheetRecord(OldSheetRecord shr) {
        onRowEnd(-1);
        sheetName = shr.getSheetname();
        resetHeader();
    }

    /**
     * Resets header and prevents data handling
     */
    protected void resetHeader() {
        columns.clear();
        headerFound = false;
    }

    @Override
    public void onBOFRecord(BOFRecord record, int biffVersion) {
        // stub
    }

    private void checkNextRow(int rowNum) {
        if (rowNum > currentRowNum) {
            onRowEnd(rowNum);
        }
    }

    private void onStringData(short colNum, String value) {
        C column = columns.get(colNum);
        if (column != null) {
            column.setRecordValue(new StringValue(value));
        }
    }

    private void onNumericData(short column, double value) {
        C columnMeta = columns.get(column);
        if (columnMeta != null) {
            columnMeta.setRecordValue(new DoubleValue(value));
        }
    }

    /**
     * Appends to {@code header} parameter a column when it matches with column number and column value
     *
     * @param colNum column number
     * @param value value of header cell
     * @param header map of columns by their numbers
     */
    protected abstract void tryHeaderRecord(short colNum, String value, Map<Short, C> header);


    @Override
    public void onOldLabelRecord(OldLabelRecord lr) {
        checkNextRow(lr.getRow());
        if (headerFound) {
            onStringData(lr.getColumn(), lr.getValue());
        } else {
            tryHeaderRecord(lr.getColumn(), lr.getValue(), columns);
        }
    }

    @Override
    public void onOldStringRecord(OldStringRecord sr) {
        // record doesn't contain column and row info, let's skip her
    }

    @Override
    public void onNumberRecord(NumberRecord nr) {
        checkNextRow(nr.getRow());
        if (headerFound) {
            onNumericData(nr.getColumn(), nr.getValue());
        }
    }

    @Override
    public void onFormulaRecord(FormulaRecord fr) {
        checkNextRow(fr.getRow());
        if (headerFound) {
            onNumericData(fr.getColumn(), fr.getValue());
        }
    }

    @Override
    public void onOldFormulaRecord(OldFormulaRecord fr) {
        checkNextRow(fr.getRow());
        if (headerFound) {
            onNumericData(fr.getColumn(), fr.getValue());
        }
    }

    @Override
    public void onRKRecord(RKRecord rr) {
        checkNextRow(rr.getRow());
        if (headerFound) {
            onNumericData(rr.getColumn(), rr.getRKNumber());
        }
    }

    @Override
    public void onMulRKRecord(MulRKRecord mrr) {
        int rowNum = mrr.getRow();
        checkNextRow(rowNum);
        if (headerFound) {
            final short offset = mrr.getFirstColumn();
            final int columns = mrr.getNumColumns();
            for (int i = 0; i < columns; i++) {
                onNumericData((short) (offset + i), mrr.getRKNumberAt(i));
            }
        }
    }

    @Override
    public void onBookEnd() {
        onRowEnd(Integer.MAX_VALUE - 1);
    }

    /**
     * Called when the column has incorrect value
     * @param column column
     * @param message error message
     * @return exception instance
     */
    protected abstract E createException(C column, String message);

    /**
     * @param column column
     * @param getter instance to extract data in certain format
     * @param <T> data type
     * @return data value
     * @throws E exception prepared in {@link #createException(AbstractColumn, String)} method
     */
    protected final <T> T get(C column, IRecordValueGetter<T> getter) throws E {
        IRecordValue recordValue = column.getRecordValue();
        if (recordValue == null) {
            if (column.isRequired()) {
                throw createException(column, null);
            } else {
                return null;
            }
        }
        try {
            return getter.get(recordValue);
        } catch (Exception e) {
            if (column.isRequired()) {
                throw createException(column, e.getMessage());
            }
        }
        return null;
    }

    protected Double getDouble(C column) throws E {
        return get(column, DOUBLE_GETTER);
    }

    protected Long getLong(C column) throws E {
        return get(column, LONG_GETTER);
    }

    protected String getString(C column) throws E {
        return get(column, STRING_GETTER);
    }

}
