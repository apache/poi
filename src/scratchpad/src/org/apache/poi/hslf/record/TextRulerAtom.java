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
import org.apache.poi.util.POILogger;

/**
 * Ruler of a text as it differs from the style's ruler settings.
 *
 * @author Yegor Kozlov
 */
public final class TextRulerAtom extends RecordAtom {

    /**
     * Record header.
     */
    private byte[] _header;

    /**
     * Record data.
     */
    private byte[] _data;

    //ruler internals
    private int defaultTabSize;
    private int numLevels;
    private int[] tabStops;
    private int[] bulletOffsets = new int[5];
    private int[] textOffsets = new int[5];

    /**
     * Constructs a new empty ruler atom.
     */
    public TextRulerAtom() {
        _header = new byte[8];
        _data = new byte[0];

        LittleEndian.putShort(_header, 2, (short)getRecordType());
        LittleEndian.putInt(_header, 4, _data.length);
    }

    /**
     * Constructs the ruler atom record from its
     *  source data.
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected TextRulerAtom(byte[] source, int start, int len) {
        // Get the header.
        _header = new byte[8];
        System.arraycopy(source,start,_header,0,8);

        // Get the record data.
        _data = new byte[len-8];
        System.arraycopy(source,start+8,_data,0,len-8);

        try {
            read();
        } catch (Exception e){
            logger.log(POILogger.ERROR, "Failed to parse TextRulerAtom: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets the record type.
     *
     * @return the record type.
     */
    public long getRecordType() {
        return RecordTypes.TextRulerAtom.typeID;
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk.
     *
     * @param out the output stream to write to.
     * @throws java.io.IOException if an error occurs.
     */
    public void writeOut(OutputStream out) throws IOException {
        out.write(_header);
        out.write(_data);
    }

    /**
     * Read the record bytes and initialize the internal variables
     */
    private void read(){
        int pos = 0;
        short mask = LittleEndian.getShort(_data);  pos += 4;
        short val;
        int[] bits = {1, 0, 2, 3, 8, 4, 9, 5, 10, 6, 11, 7, 12};
        for (int i = 0; i < bits.length; i++) {
            if((mask & 1 << bits[i]) != 0){
                switch (bits[i]){
                    case 0:
                        //defaultTabSize
                        defaultTabSize = LittleEndian.getShort(_data, pos); pos += 2;
                        break;
                    case 1:
                        //numLevels
                        numLevels = LittleEndian.getShort(_data, pos); pos += 2;
                        break;
                    case 2:
                        //tabStops
                        val = LittleEndian.getShort(_data, pos); pos += 2;
                        tabStops = new int[val*2];
                        for (int j = 0; j < tabStops.length; j++) {
                            tabStops[j] = LittleEndian.getUShort(_data, pos); pos += 2;
                        }
                        break;
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        //bullet.offset
                        val = LittleEndian.getShort(_data, pos); pos += 2;
                        bulletOffsets[bits[i]-3] = val;
                        break;
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                        //text.offset
                        val = LittleEndian.getShort(_data, pos); pos += 2;
                        textOffsets[bits[i]-8] = val;
                        break;
                }
            }
        }
    }

    /**
     * Default distance between tab stops, in master coordinates (576 dpi).
     */
    public int getDefaultTabSize(){
        return defaultTabSize;
    }

    /**
     * Number of indent levels (maximum 5).
     */
    public int getNumberOfLevels(){
        return numLevels;
    }

    /**
     * Default distance between tab stops, in master coordinates (576 dpi).
     */
    public int[] getTabStops(){
        return tabStops;
    }

    /**
     * Paragraph's distance from shape's left margin, in master coordinates (576 dpi).
     */
    public int[] getTextOffsets(){
        return textOffsets;
    }

    /**
     * First line of paragraph's distance from shape's left margin, in master coordinates (576 dpi).
     */
    public int[] getBulletOffsets(){
        return bulletOffsets;
    }

    public static TextRulerAtom getParagraphInstance(){
        byte[] data = new byte[] {
            0x00, 0x00, (byte)0xA6, 0x0F, 0x0A, 0x00, 0x00, 0x00,
            0x10, 0x03, 0x00, 0x00, (byte)0xF9, 0x00, 0x41, 0x01, 0x41, 0x01
        };
        TextRulerAtom ruler = new TextRulerAtom(data, 0, data.length);
        return ruler;
    }

    public void setParagraphIndent(short tetxOffset, short bulletOffset){
        LittleEndian.putShort(_data, 4, tetxOffset);
        LittleEndian.putShort(_data, 6, bulletOffset);
        LittleEndian.putShort(_data, 8, bulletOffset);
    }
}
