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

package org.apache.poi.hssf.record;


import static org.apache.poi.util.GenericRecordUtil.getEnumBitsAsString;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Describes the frozen and unfrozen panes.
 */
public final class PaneRecord extends StandardRecord {
    public static final short sid                     = 0x41;
    public static final short ACTIVE_PANE_LOWER_RIGHT = 0;
    public static final short ACTIVE_PANE_UPPER_RIGHT = 1;
    public static final short ACTIVE_PANE_LOWER_LEFT  = 2;
    public static final short ACTIVE_PANE_UPPER_LEFT  = 3;

    private short field_1_x;
    private short field_2_y;
    private short field_3_topRow;
    private short field_4_leftColumn;
    private short field_5_activePane;

    public PaneRecord() {}

    public PaneRecord(PaneRecord other) {
        super(other);
        field_1_x          = other.field_1_x;
        field_2_y          = other.field_2_y;
        field_3_topRow     = other.field_3_topRow;
        field_4_leftColumn = other.field_4_leftColumn;
        field_5_activePane = other.field_5_activePane;
    }

    public PaneRecord(RecordInputStream in) {
        field_1_x          = in.readShort();
        field_2_y          = in.readShort();
        field_3_topRow     = in.readShort();
        field_4_leftColumn = in.readShort();
        field_5_activePane = in.readShort();
    }

    @Override
    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_x);
        out.writeShort(field_2_y);
        out.writeShort(field_3_topRow);
        out.writeShort(field_4_leftColumn);
        out.writeShort(field_5_activePane);
    }

    @Override
    protected int getDataSize() {
        return 2 + 2 + 2 + 2 + 2;
    }

    @Override
    public short getSid()
    {
        return sid;
    }

    @Override
    public PaneRecord copy() {
        return new PaneRecord(this);
    }

    /**
     * Get the x field for the Pane record.
     *
     * @return the x value
     */
    public short getX()
    {
        return field_1_x;
    }

    /**
     * Set the x field for the Pane record.
     *
     * @param field_1_x the x value
     */
    public void setX(short field_1_x)
    {
        this.field_1_x = field_1_x;
    }

    /**
     * Get the y field for the Pane record.
     *
     * @return the y value
     */
    public short getY()
    {
        return field_2_y;
    }

    /**
     * Set the y field for the Pane record.
     *
     * @param field_2_y the y value
     */
    public void setY(short field_2_y)
    {
        this.field_2_y = field_2_y;
    }

    /**
     * Get the top row field for the Pane record.
     *
     * @return the top row
     */
    public short getTopRow()
    {
        return field_3_topRow;
    }

    /**
     * Set the top row field for the Pane record.
     *
     * @param field_3_topRow the top row
     */
    public void setTopRow(short field_3_topRow)
    {
        this.field_3_topRow = field_3_topRow;
    }

    /**
     * Get the left column field for the Pane record.
     *
     * @return the left column
     */
    public short getLeftColumn()
    {
        return field_4_leftColumn;
    }

    /**
     * Set the left column field for the Pane record.
     *
     * @param field_4_leftColumn the left column
     */
    public void setLeftColumn(short field_4_leftColumn)
    {
        this.field_4_leftColumn = field_4_leftColumn;
    }

    /**
     * Get the active pane field for the Pane record.
     *
     * @return  One of
     *        ACTIVE_PANE_LOWER_RIGHT
     *        ACTIVE_PANE_UPPER_RIGHT
     *        ACTIVE_PANE_LOWER_LEFT
     *        ACTIVE_PANE_UPPER_LEFT
     */
    public short getActivePane()
    {
        return field_5_activePane;
    }

    /**
     * Set the active pane field for the Pane record.
     *
     * @param field_5_activePane
     *        One of
     *        ACTIVE_PANE_LOWER_RIGHT
     *        ACTIVE_PANE_UPPER_RIGHT
     *        ACTIVE_PANE_LOWER_LEFT
     *        ACTIVE_PANE_UPPER_LEFT
     */
    public void setActivePane(short field_5_activePane)
    {
        this.field_5_activePane = field_5_activePane;
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.PANE;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "x", this::getX,
            "y", this::getY,
            "topRow", this::getTopRow,
            "leftColumn", this::getLeftColumn,
            "activePane", getEnumBitsAsString(this::getActivePane,
                new int[]{ACTIVE_PANE_LOWER_RIGHT, ACTIVE_PANE_UPPER_RIGHT, ACTIVE_PANE_LOWER_LEFT, ACTIVE_PANE_UPPER_LEFT},
                new String[]{"LOWER_RIGHT","UPPER_RIGHT","LOWER_LEFT","UPPER_LEFT"})
        );
    }
}
