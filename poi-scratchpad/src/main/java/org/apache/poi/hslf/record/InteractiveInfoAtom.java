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

package org.apache.poi.hslf.record;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.util.LittleEndian;

/**
 * Tne atom that holds metadata on Links in the document.
 * (The actual link is held Document.ExObjList.ExHyperlink)
 *
 * @author Nick Burch
 * @author Yegor Kozlov
 */
public class InteractiveInfoAtom extends RecordAtom {

    /**
     * Action Table
     */
    public static final byte ACTION_NONE = 0;
    public static final byte ACTION_MACRO = 1;
    public static final byte ACTION_RUNPROGRAM = 2;
    public static final byte ACTION_JUMP = 3;
    public static final byte ACTION_HYPERLINK = 4;
    public static final byte ACTION_OLE = 5;
    public static final byte ACTION_MEDIA = 6;
    public static final byte ACTION_CUSTOMSHOW = 7;

    /**
     *  Jump Table
     */
    public static final byte JUMP_NONE = 0;
    public static final byte JUMP_NEXTSLIDE = 1;
    public static final byte JUMP_PREVIOUSSLIDE = 2;
    public static final byte JUMP_FIRSTSLIDE = 3;
    public static final byte JUMP_LASTSLIDE = 4;
    public static final byte JUMP_LASTSLIDEVIEWED = 5;
    public static final byte JUMP_ENDSHOW = 6;

    /**
     * Types of hyperlinks
     */
    public static final byte LINK_NextSlide = 0x00;
    public static final byte LINK_PreviousSlide = 0x01;
    public static final byte LINK_FirstSlide = 0x02;
    public static final byte LINK_LastSlide = 0x03;
    public static final byte LINK_CustomShow = 0x06;
    public static final byte LINK_SlideNumber = 0x07;
    public static final byte LINK_Url = 0x08;
    public static final byte LINK_OtherPresentation = 0x09;
    public static final byte LINK_OtherFile = 0x0A;
    public static final byte LINK_NULL = (byte)0xFF;

    /**
     * Record header.
     */
    private byte[] _header;

    /**
     * Record data.
     */
    private byte[] _data;

    /**
     * Constructs a brand new link related atom record.
     */
    protected InteractiveInfoAtom() {
        _header = new byte[8];
        _data = new byte[16];

        LittleEndian.putShort(_header, 2, (short)getRecordType());
        LittleEndian.putInt(_header, 4, _data.length);

        // It is fine for the other values to be zero
    }

    /**
     * Constructs the link related atom record from its
     *  source data.
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected InteractiveInfoAtom(byte[] source, int start, int len) {
        // Get the header.
        _header = new byte[8];
        System.arraycopy(source,start,_header,0,8);

        // Get the record data.
        _data = new byte[len-8];
        System.arraycopy(source,start+8,_data,0,len-8);

        // Must be at least 16 bytes long
        if(_data.length < 16) {
        	throw new IllegalArgumentException("The length of the data for a InteractiveInfoAtom must be at least 16 bytes, but was only " + _data.length);
        }

        // First 4 bytes - no idea, normally 0
        // Second 4 bytes - the id of the link (from 1 onwards)
        // Third 4 bytes - no idea, normally 4
        // Fourth 4 bytes - no idea, normally 8
    }

    /**
     * Gets the link number. You will normally look the
     *  ExHyperlink with this number to get the details.
     * @return the link number
     */
    public int getHyperlinkID() {
        return LittleEndian.getInt(_data,4);
    }

    /**
     * Sets the persistent unique identifier of the link
     *
     * @param number the persistent unique identifier of the link
     */
    public void setHyperlinkID(int number) {
        LittleEndian.putInt(_data,4,number);
    }

    /**
     * a reference to a sound in the sound collection.
     */
    public int getSoundRef() {
        return LittleEndian.getInt(_data,0);
    }
    /**
     * a reference to a sound in the sound collection.
     *
     * @param val a reference to a sound in the sound collection
     */
    public void setSoundRef(int val) {
    	LittleEndian.putInt(_data, 0, val);
    }

    /**
     * Hyperlink Action.
     * <p>
     * see <code>ACTION_*</code> constants for the list of actions
     * </p>
     *
     * @return hyperlink action.
     */
    public byte getAction() {
        return _data[8];
    }

    /**
     * Hyperlink Action
     * <p>
     * see <code>ACTION_*</code> constants for the list of actions
     * </p>
     *
     * @param val hyperlink action.
     */
    public void setAction(byte val) {
    	_data[8] = val;
    }

    /**
     * Only valid when action == OLEAction. OLE verb to use, 0 = first verb, 1 = second verb, etc.
     */
    public byte getOleVerb() {
        return _data[9];
    }

    /**
     * Only valid when action == OLEAction. OLE verb to use, 0 = first verb, 1 = second verb, etc.
     */
    public void setOleVerb(byte val) {
    	_data[9] = val;
    }

    /**
     * Jump
     * <p>
     * see <code>JUMP_*</code> constants for the list of actions
     * </p>
     *
     * @return jump
     */
    public byte getJump() {
        return _data[10];
    }

    /**
     * Jump
     * <p>
     * see <code>JUMP_*</code> constants for the list of actions
     * </p>
     *
     * @param val jump
     */
    public void setJump(byte val) {
    	_data[10] = val;
    }

    /**
     * Flags
     * <p>
     * <li> Bit 1: Animated. If 1, then button is animated
     * <li> Bit 2: Stop sound. If 1, then stop current sound when button is pressed.
     * <li> Bit 3: CustomShowReturn. If 1, and this is a jump to custom show,
     *   then return to this slide after custom show.
     * </p>
     */
    public byte getFlags() {
        return _data[11];
    }

    /**
     * Flags
     * <p>
     * <li> Bit 1: Animated. If 1, then button is animated
     * <li> Bit 2: Stop sound. If 1, then stop current sound when button is pressed.
     * <li> Bit 3: CustomShowReturn. If 1, and this is a jump to custom show,
     *   then return to this slide after custom show.
     * </p>
     */
    public void setFlags(byte val) {
    	_data[11] = val;
    }

    /**
     * hyperlink type
     *
     * @return hyperlink type
     */
    public byte getHyperlinkType() {
        return _data[12];
    }

    /**
     * hyperlink type
     *
     * @param val hyperlink type
     */
    public void setHyperlinkType(byte val) {
    	_data[12] = val;
    }

    /**
     * Gets the record type.
     * @return the record type.
     */
    public long getRecordType() { return RecordTypes.InteractiveInfoAtom.typeID; }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     *
     * @param out the output stream to write to.
     * @throws IOException if an error occurs.
     */
    public void writeOut(OutputStream out) throws IOException {
        out.write(_header);
        out.write(_data);
    }
}
