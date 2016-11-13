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

package org.apache.poi.hssf.record.chart;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Describes a chart sheet properties record. SHTPROPS (0x1044)<p>
 * 
 * (As with all chart related records, documentation is lacking.
 * See {@link ChartRecord} for more details)
 */
public final class SheetPropertiesRecord extends StandardRecord {
    public final static short sid = 0x1044;
    
    private static final BitField chartTypeManuallyFormatted = BitFieldFactory.getInstance(0x01);
    private static final BitField plotVisibleOnly            = BitFieldFactory.getInstance(0x02);
    private static final BitField doNotSizeWithWindow        = BitFieldFactory.getInstance(0x04);
    private static final BitField defaultPlotDimensions      = BitFieldFactory.getInstance(0x08);
    private static final BitField autoPlotArea               = BitFieldFactory.getInstance(0x10);
    
    private int field_1_flags;
    private int field_2_empty;
    public final static byte        EMPTY_NOT_PLOTTED              = 0;
    public final static byte        EMPTY_ZERO                     = 1;
    public final static byte        EMPTY_INTERPOLATED             = 2;


    public SheetPropertiesRecord() {
        // fields uninitialised
    }

    public SheetPropertiesRecord(RecordInputStream in) {
        field_1_flags = in.readUShort();
        field_2_empty = in.readUShort();
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[SHTPROPS]\n");
        buffer.append("    .flags                = ").append(HexDump.shortToHex(field_1_flags)).append('\n');
        buffer.append("         .chartTypeManuallyFormatted= ").append(isChartTypeManuallyFormatted()).append('\n'); 
        buffer.append("         .plotVisibleOnly           = ").append(isPlotVisibleOnly()).append('\n'); 
        buffer.append("         .doNotSizeWithWindow       = ").append(isDoNotSizeWithWindow()).append('\n'); 
        buffer.append("         .defaultPlotDimensions     = ").append(isDefaultPlotDimensions()).append('\n'); 
        buffer.append("         .autoPlotArea              = ").append(isAutoPlotArea()).append('\n'); 
        buffer.append("    .empty                = ").append(HexDump.shortToHex(field_2_empty)).append('\n'); 

        buffer.append("[/SHTPROPS]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_flags);
        out.writeShort(field_2_empty);
    }

    protected int getDataSize() {
        return 2 + 2;
    }

    public short getSid() {
        return sid;
    }

    public Object clone() {
        SheetPropertiesRecord rec = new SheetPropertiesRecord();
    
        rec.field_1_flags = field_1_flags;
        rec.field_2_empty = field_2_empty;
        return rec;
    }

    /**
     * Get the flags field for the SheetProperties record.
     */
    public int getFlags() {
        return field_1_flags;
    }

    /**
     * Get the empty field for the SheetProperties record.
     *
     * @return  One of 
     *        EMPTY_NOT_PLOTTED
     *        EMPTY_ZERO
     *        EMPTY_INTERPOLATED
     */
    public int getEmpty() {
        return field_2_empty;
    }

    /**
     * Set the empty field for the SheetProperties record.
     *
     * @param empty
     *        One of 
     *        EMPTY_NOT_PLOTTED
     *        EMPTY_ZERO
     *        EMPTY_INTERPOLATED
     */
    public void setEmpty(byte empty) {
        this.field_2_empty = empty;
    }

    /**
     * Sets the chart type manually formatted field value.
     * Has the chart type been manually formatted?
     */
    public void setChartTypeManuallyFormatted(boolean value) {
        field_1_flags = chartTypeManuallyFormatted.setBoolean(field_1_flags, value);
    }

    /**
     * Has the chart type been manually formatted?
     * @return  the chart type manually formatted field value.
     */
    public boolean isChartTypeManuallyFormatted() {
        return chartTypeManuallyFormatted.isSet(field_1_flags);
    }

    /**
     * Sets the plot visible only field value.
     * Only show visible cells on the chart.
     */
    public void setPlotVisibleOnly(boolean value) {
        field_1_flags = plotVisibleOnly.setBoolean(field_1_flags, value);
    }

    /**
     * Only show visible cells on the chart.
     * @return  the plot visible only field value.
     */
    public boolean isPlotVisibleOnly() {
        return plotVisibleOnly.isSet(field_1_flags);
    }

    /**
     * Sets the do not size with window field value.
     * Do not size the chart when the window changes size
     */
    public void setDoNotSizeWithWindow(boolean value) {
        field_1_flags = doNotSizeWithWindow.setBoolean(field_1_flags, value);
    }

    /**
     * Do not size the chart when the window changes size
     * @return  the do not size with window field value.
     */
    public boolean isDoNotSizeWithWindow() {
        return doNotSizeWithWindow.isSet(field_1_flags);
    }

    /**
     * Sets the default plot dimensions field value.
     * Indicates that the default area dimensions should be used.
     */
    public void setDefaultPlotDimensions(boolean value) {
        field_1_flags = defaultPlotDimensions.setBoolean(field_1_flags, value);
    }

    /**
     * Indicates that the default area dimensions should be used.
     * @return  the default plot dimensions field value.
     */
    public boolean isDefaultPlotDimensions() {
        return defaultPlotDimensions.isSet(field_1_flags);
    }

    /**
     * Sets the auto plot area field value.
     * ??
     */
    public void setAutoPlotArea(boolean value)  {
        field_1_flags = autoPlotArea.setBoolean(field_1_flags, value);
    }

    /**
     * ??
     * @return  the auto plot area field value.
     */
    public boolean isAutoPlotArea() {
        return autoPlotArea.isSet(field_1_flags);
    }
}
