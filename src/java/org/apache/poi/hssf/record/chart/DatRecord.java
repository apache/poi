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
 * The dat record is used to store options for the chart.
 */
public final class DatRecord extends StandardRecord implements Cloneable {
    public final static short sid = 0x1063;

    private static final BitField horizontalBorder = BitFieldFactory.getInstance(0x1);
    private static final BitField verticalBorder   = BitFieldFactory.getInstance(0x2);
    private static final BitField border           = BitFieldFactory.getInstance(0x4);
    private static final BitField showSeriesKey    = BitFieldFactory.getInstance(0x8);

    private  short      field_1_options;


    public DatRecord()
    {

    }

    public DatRecord(RecordInputStream in)
    {
        field_1_options                = in.readShort();
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[DAT]\n");
        buffer.append("    .options              = ")
            .append("0x").append(HexDump.toHex(  getOptions ()))
            .append(" (").append( getOptions() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("         .horizontalBorder         = ").append(isHorizontalBorder()).append('\n'); 
        buffer.append("         .verticalBorder           = ").append(isVerticalBorder()).append('\n'); 
        buffer.append("         .border                   = ").append(isBorder()).append('\n'); 
        buffer.append("         .showSeriesKey            = ").append(isShowSeriesKey()).append('\n'); 

        buffer.append("[/DAT]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_options);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public DatRecord clone() {
        DatRecord rec = new DatRecord();
        rec.field_1_options = field_1_options;
        return rec;
    }




    /**
     * Get the options field for the Dat record.
     */
    public short getOptions()
    {
        return field_1_options;
    }

    /**
     * Set the options field for the Dat record.
     */
    public void setOptions(short field_1_options)
    {
        this.field_1_options = field_1_options;
    }

    /**
     * Sets the horizontal border field value.
     * has a horizontal border
     */
    public void setHorizontalBorder(boolean value)
    {
        field_1_options = horizontalBorder.setShortBoolean(field_1_options, value);
    }

    /**
     * has a horizontal border
     * @return  the horizontal border field value.
     */
    public boolean isHorizontalBorder()
    {
        return horizontalBorder.isSet(field_1_options);
    }

    /**
     * Sets the vertical border field value.
     * has vertical border
     */
    public void setVerticalBorder(boolean value)
    {
        field_1_options = verticalBorder.setShortBoolean(field_1_options, value);
    }

    /**
     * has vertical border
     * @return  the vertical border field value.
     */
    public boolean isVerticalBorder()
    {
        return verticalBorder.isSet(field_1_options);
    }

    /**
     * Sets the border field value.
     * data table has a border
     */
    public void setBorder(boolean value)
    {
        field_1_options = border.setShortBoolean(field_1_options, value);
    }

    /**
     * data table has a border
     * @return  the border field value.
     */
    public boolean isBorder()
    {
        return border.isSet(field_1_options);
    }

    /**
     * Sets the show series key field value.
     * shows the series key
     */
    public void setShowSeriesKey(boolean value)
    {
        field_1_options = showSeriesKey.setShortBoolean(field_1_options, value);
    }

    /**
     * shows the series key
     * @return  the show series key field value.
     */
    public boolean isShowSeriesKey()
    {
        return showSeriesKey.isSet(field_1_options);
    }
}
