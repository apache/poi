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
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.RecordFormatException;


/**
 * This structure appears as part of an Obj record that represents image display properties.
 */
public final class FtPioGrbitSubRecord extends SubRecord implements Cloneable {
    public final static short sid = 0x08;
    public final static short length = 0x02;
    
    /**
     * A bit that specifies whether the picture's aspect ratio is preserved when rendered in 
     * different views (Normal view, Page Break Preview view, Page Layout view and printing).
     */
    public static final int AUTO_PICT_BIT    = 1 << 0;

    /**
     * A bit that specifies whether the pictFmla field of the Obj record that contains 
     * this FtPioGrbit specifies a DDE reference.
     */
    public static final int DDE_BIT          = 1 << 1;
    
    /**
     * A bit that specifies whether this object is expected to be updated on print to
     * reflect the values in the cell associated with the object.
     */
    public static final int PRINT_CALC_BIT   = 1 << 2;

    /**
     * A bit that specifies whether the picture is displayed as an icon.
     */
    public static final int ICON_BIT         = 1 << 3;
    
    /**
     * A bit that specifies whether this object is an ActiveX control.
     * It MUST NOT be the case that both fCtl and fDde are equal to 1.
     */
    public static final int CTL_BIT          = 1 << 4;
    
    /**
     * A bit that specifies whether the object data are stored in an
     * embedding storage (= 0) or in the controls stream (ctls) (= 1).
     */
    public static final int PRSTM_BIT        = 1 << 5;
    
    /**
     * A bit that specifies whether this is a camera picture.
     */
    public static final int CAMERA_BIT       = 1 << 7;
    
    /**
     * A bit that specifies whether this picture's size has been explicitly set.
     * 0 = picture size has been explicitly set, 1 = has not been set
     */
    public static final int DEFAULT_SIZE_BIT = 1 << 8;
    
    /**
     * A bit that specifies whether the OLE server for the object is called
     * to load the object's data automatically when the parent workbook is opened.
     */
    public static final int AUTO_LOAD_BIT    = 1 << 9;

    
    private short flags;

    /**
     * Construct a new <code>FtPioGrbitSubRecord</code> and
     * fill its data with the default values
     */
    public FtPioGrbitSubRecord() {
    }

    public FtPioGrbitSubRecord(LittleEndianInput in, int size) {
        if (size != length) {
            throw new RecordFormatException("Unexpected size (" + size + ")");
        }
        flags = in.readShort();
    }

    /**
     * Use one of the bitmasks MANUAL_ADVANCE_BIT ... CURSOR_VISIBLE_BIT
     * @param bitmask the bitmask to apply
     * @param enabled if true, the bitmask will be or-ed, otherwise the bits set in the mask will be removed from the flags
     */
    public void setFlagByBit(int bitmask, boolean enabled) {
        if (enabled) {
            flags |= bitmask;
        } else {
            flags &= (0xFFFF ^ bitmask);
        }
    }    
    
    public boolean getFlagByBit(int bitmask) {
        return ((flags & bitmask) != 0);
    }
    
    /**
     * Convert this record to string.
     * Used by BiffViewer and other utilities.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[FtPioGrbit ]\n");
        buffer.append("  size     = ").append(length).append("\n");
        buffer.append("  flags    = ").append(HexDump.toHex(flags)).append("\n");
        buffer.append("[/FtPioGrbit ]\n");
        return buffer.toString();
    }

    /**
     * Serialize the record data into the supplied array of bytes
     *
     * @param out the stream to serialize into
     */
    public void serialize(LittleEndianOutput out) {
        out.writeShort(sid);
        out.writeShort(length);
        out.writeShort(flags);
    }

 protected int getDataSize() {
        return length;
    }

    /**
     * @return id of this record.
     */
    public short getSid()
    {
        return sid;
    }

    @Override
    public FtPioGrbitSubRecord clone() {
        FtPioGrbitSubRecord rec = new FtPioGrbitSubRecord();
        rec.flags = this.flags;
        return rec;
    }

 public short getFlags() {
   return flags;
 }

 public void setFlags(short flags) {
   this.flags = flags;
 }
}