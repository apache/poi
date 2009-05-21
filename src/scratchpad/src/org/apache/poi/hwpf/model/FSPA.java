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

package org.apache.poi.hwpf.model;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;

/**
 * File Shape Address structure
 *
 * @author Squeeself
 */
public final class FSPA
{
    public static final int FSPA_SIZE = 26;
    private int spid; // Shape identifier. Used to get data position
    private int xaLeft; // Enclosing rectangle
    private int yaTop; // Enclosing rectangle
    private int xaRight; // Enclosing rectangle
    private int yaBottom; // Enclosing rectangle
    private short options;
        private static BitField fHdr = BitFieldFactory.getInstance(0x0001); // 1 in undo when in header
        private static BitField bx = BitFieldFactory.getInstance(0x0006); // x pos relative to anchor CP: 0 - page margin, 1 - top of page, 2 - text, 3 - reserved
        private static BitField by = BitFieldFactory.getInstance(0x0018); // y pos relative to anchor CP: ditto
        private static BitField wr = BitFieldFactory.getInstance(0x01E0); // Text wrapping mode: 0 - like 2 w/o absolute, 1 - no text next to shape, 2 - wrap around absolute object, 3 - wrap as if no object, 4 - wrap tightly around object, 5 - wrap tightly, allow holes, 6-15 - reserved
        private static BitField wrk = BitFieldFactory.getInstance(0x1E00); // Text wrapping mode type (for modes 2&4): 0 - wrap both sides, 1 - wrap only left, 2 - wrap only right, 3 - wrap largest side
        private static BitField fRcaSimple = BitFieldFactory.getInstance(0x2000); // Overwrites bx if set, forcing rectangle to be page relative
        private static BitField fBelowText = BitFieldFactory.getInstance(0x4000); // if true, shape is below text, otherwise above
        private static BitField fAnchorLock = BitFieldFactory.getInstance(0x8000); // if true, anchor is locked
    private int cTxbx; // Count of textboxes in shape (undo doc only)

    public FSPA()
    {
    }

    public FSPA(byte[] bytes, int offset)
    {
        spid = LittleEndian.getInt(bytes, offset);
        offset += LittleEndian.INT_SIZE;
        xaLeft = LittleEndian.getInt(bytes, offset);
        offset += LittleEndian.INT_SIZE;
        yaTop = LittleEndian.getInt(bytes, offset);
        offset += LittleEndian.INT_SIZE;
        xaRight = LittleEndian.getInt(bytes, offset);
        offset += LittleEndian.INT_SIZE;
        yaBottom = LittleEndian.getInt(bytes, offset);
        offset += LittleEndian.INT_SIZE;
        options = LittleEndian.getShort(bytes, offset);
        offset += LittleEndian.SHORT_SIZE;
        cTxbx = LittleEndian.getInt(bytes, offset);
    }

    public int getSpid()
    {
        return spid;
    }

    public int getXaLeft()
    {
        return xaLeft;
    }

    public int getYaTop()
    {
        return yaTop;
    }

    public int getXaRight()
    {
        return xaRight;
    }

    public int getYaBottom()
    {
        return yaBottom;
    }

    public boolean isFHdr()
    {
        return fHdr.isSet(options);
    }

    public short getBx()
    {
        return bx.getShortValue(options);
    }

    public short getBy()
    {
        return by.getShortValue(options);
    }

    public short getWr()
    {
        return wr.getShortValue(options);
    }

    public short getWrk()
    {
        return wrk.getShortValue(options);
    }

    public boolean isFRcaSimple()
    {
        return fRcaSimple.isSet(options);
    }

    public boolean isFBelowText()
    {
        return fBelowText.isSet(options);
    }

    public boolean isFAnchorLock()
    {
        return fAnchorLock.isSet(options);
    }

    public int getCTxbx()
    {
        return cTxbx;
    }

    public byte[] toByteArray()
    {
        int offset = 0;
        byte[] buf = new byte[FSPA_SIZE];

        LittleEndian.putInt(buf, offset, spid);
        offset += LittleEndian.INT_SIZE;
        LittleEndian.putInt(buf, offset, xaLeft);
        offset += LittleEndian.INT_SIZE;
        LittleEndian.putInt(buf, offset, yaTop);
        offset += LittleEndian.INT_SIZE;
        LittleEndian.putInt(buf, offset, xaRight);
        offset += LittleEndian.INT_SIZE;
        LittleEndian.putInt(buf, offset, yaBottom);
        offset += LittleEndian.INT_SIZE;
        LittleEndian.putShort(buf, offset, options);
        offset += LittleEndian.SHORT_SIZE;
        LittleEndian.putInt(buf, offset, cTxbx);
        offset += LittleEndian.INT_SIZE;

        return buf;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("spid: ").append(spid);
        buf.append(", xaLeft: ").append(xaLeft);
        buf.append(", yaTop: ").append(yaTop);
        buf.append(", xaRight: ").append(xaRight);
        buf.append(", yaBottom: ").append(yaBottom);
        buf.append(", options: ").append(options);
            buf.append(" (fHdr: ").append(isFHdr());
            buf.append(", bx: ").append(getBx());
            buf.append(", by: ").append(getBy());
            buf.append(", wr: ").append(getWr());
            buf.append(", wrk: ").append(getWrk());
            buf.append(", fRcaSimple: ").append(isFRcaSimple());
            buf.append(", fBelowText: ").append(isFBelowText());
            buf.append(", fAnchorLock: ").append(isFAnchorLock());
        buf.append("), cTxbx: ").append(cTxbx);
        return buf.toString();
    }
}
