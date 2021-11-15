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

package org.apache.poi.ss.formula.ptg;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * ReferencePtgBase - handles references (such as A1, A2, IA4)
 */
public abstract class RefPtgBase extends OperandPtg {

    /**
     * YK: subclasses of RefPtgBase are used by the FormulaParser and FormulaEvaluator across HSSF and XSSF.
     * The bit mask should accommodate the maximum number of available columns, i.e. 0x3FFF.
     *
     * @see org.apache.poi.ss.SpreadsheetVersion
     */
    private static final BitField column = BitFieldFactory.getInstance(0x3FFF);

    private static final BitField rowRelative = BitFieldFactory.getInstance(0x8000);
    private static final BitField colRelative = BitFieldFactory.getInstance(0x4000);


    /** The row index - zero based unsigned 16 bit value */
    private int field_1_row;
    /**
     * Field 2 - lower 8 bits is the zero based unsigned byte column index - bit
     * 16 - isRowRelative - bit 15 - isColumnRelative
     */
    private int field_2_col;

    protected RefPtgBase() {}

    protected RefPtgBase(RefPtgBase other) {
        super(other);
        field_1_row = other.field_1_row;
        field_2_col = other.field_2_col;
    }

    protected RefPtgBase(CellReference c) {
        setRow(c.getRow());
        setColumn(c.getCol());
        setColRelative(!c.isColAbsolute());
        setRowRelative(!c.isRowAbsolute());
    }

    protected final void readCoordinates(LittleEndianInput in) {
        field_1_row = in.readUShort();
        field_2_col = in.readUShort();
    }

    protected final void writeCoordinates(LittleEndianOutput out) {
        out.writeShort(field_1_row);
        out.writeShort(field_2_col);
    }

    public final void setRow(int rowIndex) {
        field_1_row = rowIndex;
    }

    /**
     * @return the row number as an int
     */
    public final int getRow() {
        return field_1_row;
    }

    public final boolean isRowRelative() {
        return rowRelative.isSet(field_2_col);
    }

    public final void setRowRelative(boolean rel) {
        field_2_col = rowRelative.setBoolean(field_2_col, rel);
    }

    public final boolean isColRelative() {
        return colRelative.isSet(field_2_col);
    }

    public final void setColRelative(boolean rel) {
        field_2_col = colRelative.setBoolean(field_2_col, rel);
    }

    public final void setColumn(int col) {
        field_2_col = column.setValue(field_2_col, col);
    }

    public final int getColumn() {
        return column.getValue(field_2_col);
    }

    protected String formatReferenceAsString() {
        // Only make cell references as needed. Memory is an issue
        CellReference cr = new CellReference(getRow(), getColumn(), !isRowRelative(), !isColRelative());
        return cr.formatAsString();
    }

    @Override
    public final byte getDefaultOperandClass() {
        return Ptg.CLASS_REF;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "row", this::getRow,
            "rowRelative", this::isRowRelative,
            "column", this::getColumn,
            "colRelative", this::isColRelative,
            "formatReference", this::formatReferenceAsString
        );
    }
}
