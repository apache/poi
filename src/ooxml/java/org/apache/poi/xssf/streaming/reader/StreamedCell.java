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
package org.apache.poi.xssf.streaming.reader;

import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.NotImplemented;

/**
 * Represents cell in a row Value of cell is represented as a string.
 *
 */
public class StreamedCell implements Cell {
    private final Row row;
    private String value;
    private int columnIndex;
    private CellType cellType = CellType._NONE;

    StreamedCell(Row row) {
        this.row = row;
    }
    
    /**
     * <pre>
     * Return cell value
     * </pre>
     * 
     * Return the value of a cell in String format. Value will be same as how it
     * is represented in excel.
     * 
     * @return String
     */
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    /**
     * <pre>
     * Returns the column index of the cell.
     * </pre>
     * 
     * @return int that represents cell number
     */
    @Override
    public int getColumnIndex() {
        return this.columnIndex;
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public int getRowIndex() {
        throw new UnsupportedOperationException("Not implememted yet.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Sheet getSheet() {
        throw new UnsupportedOperationException("Not implememted yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row getRow() {
        return row;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCellType(int cellType) {
        setCellType(CellType.forInt(cellType));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCellType(CellType cellType) {
        // to avoid setting this by user
        if (this.cellType == null) {
            this.cellType = cellType;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCellType() {
        return cellType.getCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotImplemented
    public CellType getCellTypeEnum() {
        return cellType;
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public int getCachedFormulaResultType() {
        throw new UnsupportedOperationException("Not implememted yet.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public CellType getCachedFormulaResultTypeEnum() {
        throw new UnsupportedOperationException("Not implememted yet.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @param value
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setCellValue(double value) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @param value
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setCellValue(Date value) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setCellValue(Calendar value) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setCellValue(RichTextString value) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setCellValue(String value) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setCellFormula(String formula) throws FormulaParseException {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory foot print.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public String getCellFormula() {
        throw new UnsupportedOperationException("Not implememted yet.");
    }

    /**
     * <pre>
     * Not supported due to memory foot print.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public double getNumericCellValue() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory foot print.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Date getDateCellValue() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public RichTextString getRichStringCellValue() {
        throw new UnsupportedOperationException("Not implememted yet.");
    }

    /**
     * <pre>
     *  Returns the String value of cell content
     * </pre>
     * 
     * @return String, representing the value of cell
     */
    @Override
    public String getStringCellValue() {
        return value;
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void setCellValue(boolean value) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setCellErrorValue(byte value) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory foot print.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean getBooleanCellValue() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public byte getErrorCellValue() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setCellStyle(CellStyle style) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public CellStyle getCellStyle() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setAsActiveCell() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellAddress getAddress() {
        return new CellAddress(getRow().getRowNum(), columnIndex);
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void setCellComment(Comment comment) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported due to memory footprint
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Comment getCellComment() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void removeCellComment() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Hyperlink getHyperlink() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public void setHyperlink(Hyperlink link) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void removeHyperlink() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public CellRangeAddress getArrayFormulaRange() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean isPartOfArrayFormulaGroup() {
        throw new UnsupportedOperationException("Operation not supported.");
    }
}