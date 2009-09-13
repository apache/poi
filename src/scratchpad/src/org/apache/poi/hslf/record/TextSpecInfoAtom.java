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

import org.apache.poi.util.LittleEndian;

import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * The special info runs contained in this text.
 * Special info runs consist of character properties which don?t follow styles.
 *
 * @author Yegor Kozlov
 */
public final class TextSpecInfoAtom extends RecordAtom {
    /**
     * Record header.
     */
    private byte[] _header;

    /**
     * Record data.
     */
    private byte[] _data;

    /**
     * Constructs the link related atom record from its
     *  source data.
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected TextSpecInfoAtom(byte[] source, int start, int len) {
        // Get the header.
        _header = new byte[8];
        System.arraycopy(source,start,_header,0,8);

        // Get the record data.
        _data = new byte[len-8];
        System.arraycopy(source,start+8,_data,0,len-8);

    }
    /**
     * Gets the record type.
     * @return the record type.
     */
    public long getRecordType() { return RecordTypes.TextSpecInfoAtom.typeID; }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     *
     * @param out the output stream to write to.
     * @throws java.io.IOException if an error occurs.
     */
    public void writeOut(OutputStream out) throws IOException {
        out.write(_header);
        out.write(_data);
    }

    /**
     * Update the text length
     *
     * @param size the text length
     */
    public void setTextSize(int size){
        LittleEndian.putInt(_data, 0, size);
    }

    /**
     * Reset the content to one info run with the default values
     * @param size  the site of parent text
     */
    public void reset(int size){
        _data = new byte[10];
        // 01 00 00 00
        LittleEndian.putInt(_data, 0, size);
        // 01 00 00 00
        LittleEndian.putInt(_data, 4, 1); //mask
        // 00 00
        LittleEndian.putShort(_data, 8, (short)0); //langId

        // Update the size (header bytes 5-8)
        LittleEndian.putInt(_header, 4, _data.length);
    }

    /**
     * Get the number of characters covered by this records
     *
     * @return the number of characters covered by this records
     */
    public int getCharactersCovered(){
        int covered = 0;
        TextSpecInfoRun[] runs = getTextSpecInfoRuns();
        for (int i = 0; i < runs.length; i++) covered += runs[i].len;
        return covered;
    }

    public TextSpecInfoRun[] getTextSpecInfoRuns(){
        ArrayList lst = new ArrayList();
        int pos = 0;
        int[] bits = {1, 0, 2};
        while(pos < _data.length) {
            TextSpecInfoRun run = new TextSpecInfoRun();
            run.len = LittleEndian.getInt(_data, pos); pos += 4;
            run.mask = LittleEndian.getInt(_data, pos); pos += 4;
            for (int i = 0; i < bits.length; i++) {
                if((run.mask & 1 << bits[i]) != 0){
                    switch (bits[i]){
                        case 0:
                            run.spellInfo = LittleEndian.getShort(_data, pos); pos += 2;
                            break;
                        case 1:
                            run.langId = LittleEndian.getShort(_data, pos); pos += 2;
                            break;
                        case 2:
                            run.altLangId = LittleEndian.getShort(_data, pos); pos += 2;
                            break;
                    }
                }
            }
            lst.add(run);
        }
        return (TextSpecInfoRun[])lst.toArray(new TextSpecInfoRun[lst.size()]);

    }

    public static class TextSpecInfoRun {
        //Length of special info run.
        protected int len;

        //Special info mask of this run;
        protected int mask;

        // info fields as indicated by the mask.
        // -1 means the bit is not set
        protected short spellInfo = -1;
        protected short langId = -1;
        protected short altLangId = -1;

        /**
         * Spelling status of this text. See Spell Info table below.
         *
         * <p>Spell Info Types:</p>
         * <li>0    Unchecked
         * <li>1    Previously incorrect, needs rechecking
         * <li>2    Correct
         * <li>3    Incorrect
         *
         * @return Spelling status of this text
         */
        public short getSpellInfo(){
            return spellInfo;
        }

        /**
         * Windows LANGID for this text.
         *
         * @return Windows LANGID for this text.
         */
        public short getLangId(){
            return spellInfo;
        }

        /**
         * Alternate Windows LANGID of this text;
         * must be a valid non-East Asian LANGID if the text has an East Asian language,
         * otherwise may be an East Asian LANGID or language neutral (zero).
         *
         * @return  Alternate Windows LANGID of this text
         */
        public short getAltLangId(){
            return altLangId;
        }

        /**
         * @return Length of special info run.
         */
        public int length(){
            return len;
        }
    }
}
