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


import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

/**
 * Describes the frozen and unfozen panes.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class PaneRecord extends Record {
    public final static short      sid                             = 0x41;
    private  short      field_1_x;
    private  short      field_2_y;
    private  short      field_3_topRow;
    private  short      field_4_leftColumn;
    private  short      field_5_activePane;
    public final static short       ACTIVE_PANE_LOWER_RIGHT        = 0;
    public final static short       ACTIVE_PANE_UPPER_RIGHT        = 1;
    public final static short       ACTIVE_PANE_LOWER_LEFT         = 2;
    // TODO - remove obsolete field (it was deprecated May-2008 v3.1)
    /** @deprecated use ACTIVE_PANE_UPPER_LEFT */
    public final static short       ACTIVE_PANE_UPER_LEFT          = 3;
    public final static short       ACTIVE_PANE_UPPER_LEFT         = 3;


    public PaneRecord()
    {

    }

    /**
     * Constructs a Pane record and sets its fields appropriately.
     *
     * @param in the RecordInputstream to read the record from
     */

    public PaneRecord(RecordInputStream in)
    {
        super(in);
    
    }

    /**
     * Checks the sid matches the expected side for this record
     *
     * @param id   the expected sid.
     */
    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("Not a Pane record");
        }
    }

    protected void fillFields(RecordInputStream in)
    {
        field_1_x                      = in.readShort();
        field_2_y                      = in.readShort();
        field_3_topRow                 = in.readShort();
        field_4_leftColumn             = in.readShort();
        field_5_activePane             = in.readShort();
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[PANE]\n");
        buffer.append("    .x                    = ")
            .append("0x").append(HexDump.toHex(  getX ()))
            .append(" (").append( getX() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .y                    = ")
            .append("0x").append(HexDump.toHex(  getY ()))
            .append(" (").append( getY() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .topRow               = ")
            .append("0x").append(HexDump.toHex(  getTopRow ()))
            .append(" (").append( getTopRow() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .leftColumn           = ")
            .append("0x").append(HexDump.toHex(  getLeftColumn ()))
            .append(" (").append( getLeftColumn() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .activePane           = ")
            .append("0x").append(HexDump.toHex(  getActivePane ()))
            .append(" (").append( getActivePane() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/PANE]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset + pos, field_1_x);
        LittleEndian.putShort(data, 6 + offset + pos, field_2_y);
        LittleEndian.putShort(data, 8 + offset + pos, field_3_topRow);
        LittleEndian.putShort(data, 10 + offset + pos, field_4_leftColumn);
        LittleEndian.putShort(data, 12 + offset + pos, field_5_activePane);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4  + 2 + 2 + 2 + 2 + 2;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        PaneRecord rec = new PaneRecord();
    
        rec.field_1_x = field_1_x;
        rec.field_2_y = field_2_y;
        rec.field_3_topRow = field_3_topRow;
        rec.field_4_leftColumn = field_4_leftColumn;
        rec.field_5_activePane = field_5_activePane;
        return rec;
    }




    /**
     * Get the x field for the Pane record.
     */
    public short getX()
    {
        return field_1_x;
    }

    /**
     * Set the x field for the Pane record.
     */
    public void setX(short field_1_x)
    {
        this.field_1_x = field_1_x;
    }

    /**
     * Get the y field for the Pane record.
     */
    public short getY()
    {
        return field_2_y;
    }

    /**
     * Set the y field for the Pane record.
     */
    public void setY(short field_2_y)
    {
        this.field_2_y = field_2_y;
    }

    /**
     * Get the top row field for the Pane record.
     */
    public short getTopRow()
    {
        return field_3_topRow;
    }

    /**
     * Set the top row field for the Pane record.
     */
    public void setTopRow(short field_3_topRow)
    {
        this.field_3_topRow = field_3_topRow;
    }

    /**
     * Get the left column field for the Pane record.
     */
    public short getLeftColumn()
    {
        return field_4_leftColumn;
    }

    /**
     * Set the left column field for the Pane record.
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
}
