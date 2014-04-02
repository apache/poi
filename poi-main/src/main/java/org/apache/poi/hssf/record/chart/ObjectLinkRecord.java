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
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Links text to an object on the chart or identifies it as the title.<p/>
 * 
 * @author Andrew C. Oliver (acoliver at apache.org)
 */
public final class ObjectLinkRecord extends StandardRecord {
    public final static short      sid                             = 0x1027;
    private  short      field_1_anchorId;
    public final static short       ANCHOR_ID_CHART_TITLE          = 1;
    public final static short       ANCHOR_ID_Y_AXIS               = 2;
    public final static short       ANCHOR_ID_X_AXIS               = 3;
    public final static short       ANCHOR_ID_SERIES_OR_POINT      = 4;
    public final static short       ANCHOR_ID_Z_AXIS               = 7;
    private  short      field_2_link1;
    private  short      field_3_link2;


    public ObjectLinkRecord()
    {

    }

    public ObjectLinkRecord(RecordInputStream in)
    {
        field_1_anchorId               = in.readShort();
        field_2_link1                  = in.readShort();
        field_3_link2                  = in.readShort();

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[OBJECTLINK]\n");
        buffer.append("    .anchorId             = ")
            .append("0x").append(HexDump.toHex(  getAnchorId ()))
            .append(" (").append( getAnchorId() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .link1                = ")
            .append("0x").append(HexDump.toHex(  getLink1 ()))
            .append(" (").append( getLink1() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .link2                = ")
            .append("0x").append(HexDump.toHex(  getLink2 ()))
            .append(" (").append( getLink2() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/OBJECTLINK]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_anchorId);
        out.writeShort(field_2_link1);
        out.writeShort(field_3_link2);
    }

    protected int getDataSize() {
        return 2 + 2 + 2;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        ObjectLinkRecord rec = new ObjectLinkRecord();
    
        rec.field_1_anchorId = field_1_anchorId;
        rec.field_2_link1 = field_2_link1;
        rec.field_3_link2 = field_3_link2;
        return rec;
    }




    /**
     * Get the anchor id field for the ObjectLink record.
     *
     * @return  One of 
     *        ANCHOR_ID_CHART_TITLE
     *        ANCHOR_ID_Y_AXIS
     *        ANCHOR_ID_X_AXIS
     *        ANCHOR_ID_SERIES_OR_POINT
     *        ANCHOR_ID_Z_AXIS
     */
    public short getAnchorId()
    {
        return field_1_anchorId;
    }

    /**
     * Set the anchor id field for the ObjectLink record.
     *
     * @param field_1_anchorId
     *        One of 
     *        ANCHOR_ID_CHART_TITLE
     *        ANCHOR_ID_Y_AXIS
     *        ANCHOR_ID_X_AXIS
     *        ANCHOR_ID_SERIES_OR_POINT
     *        ANCHOR_ID_Z_AXIS
     */
    public void setAnchorId(short field_1_anchorId)
    {
        this.field_1_anchorId = field_1_anchorId;
    }

    /**
     * Get the link 1 field for the ObjectLink record.
     */
    public short getLink1()
    {
        return field_2_link1;
    }

    /**
     * Set the link 1 field for the ObjectLink record.
     */
    public void setLink1(short field_2_link1)
    {
        this.field_2_link1 = field_2_link1;
    }

    /**
     * Get the link 2 field for the ObjectLink record.
     */
    public short getLink2()
    {
        return field_3_link2;
    }

    /**
     * Set the link 2 field for the ObjectLink record.
     */
    public void setLink2(short field_3_link2)
    {
        this.field_3_link2 = field_3_link2;
    }
}
