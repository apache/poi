
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


package org.apache.poi.hssf.record;



import org.apache.poi.util.*;

/**
 * Describes the frozen and unfozen panes.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class PaneRecord
    extends Record
{
    public final static short      sid                             = 0x41;
    private  short      field_1_x;
    private  short      field_2_y;
    private  short      field_3_topRow;
    private  short      field_4_leftColumn;
    private  short      field_5_activePane;
    public final static short       ACTIVE_PANE_LOWER_RIGHT        = 0;
    public final static short       ACTIVE_PANE_UPPER_RIGHT        = 1;
    public final static short       ACTIVE_PANE_LOWER_LEFT         = 2;
    public final static short       ACTIVE_PANE_UPER_LEFT          = 3;


    public PaneRecord()
    {

    }

    /**
     * Constructs a Pane record and sets its fields appropriately.
     *
     * @param id    id must be 0x41 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public PaneRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    
    }

    /**
     * Constructs a Pane record and sets its fields appropriately.
     *
     * @param id    id must be 0x41 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public PaneRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    
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

    protected void fillFields(byte [] data, short size, int offset)
    {

        int pos = 0;
        field_1_x                      = LittleEndian.getShort(data, pos + 0x0 + offset);
        field_2_y                      = LittleEndian.getShort(data, pos + 0x2 + offset);
        field_3_topRow                 = LittleEndian.getShort(data, pos + 0x4 + offset);
        field_4_leftColumn             = LittleEndian.getShort(data, pos + 0x6 + offset);
        field_5_activePane             = LittleEndian.getShort(data, pos + 0x8 + offset);

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
        return this.sid;
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
     *        ACTIVE_PANE_UPER_LEFT
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
     *        ACTIVE_PANE_UPER_LEFT
     */
    public void setActivePane(short field_5_activePane)
    {
        this.field_5_activePane = field_5_activePane;
    }


}  // END OF CLASS




