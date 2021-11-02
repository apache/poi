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

import static org.apache.poi.util.GenericRecordUtil.getBitsAsString;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordJsonWriter;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * An atom record that specifies information about external audio or video data.
 */
public final class ExMediaAtom extends RecordAtom {

    /**
     * A bit that specifies whether the audio or video data is repeated continuously during playback.
     */
    public static final int fLoop = 1;
    /**
     * A bit that specifies whether the audio or video data is rewound after playing.
     */
    public static final int fRewind = 2;
    /**
     * A bit that specifies whether the audio data is recorded narration for the slide show. It MUST be FALSE if this ExMediaAtom record is contained by an ExVideoContainer record.
     */
    public static final int fNarration = 4;


    private static final int[] FLAG_MASKS = { fLoop, fRewind, fNarration };

    private static final String[] FLAG_NAMES = { "LOOP", "REWIND", "NARRATION" };


    /**
     * Record header.
     */
    private byte[] _header;

    /**
     * record data
     */
    private byte[] _recdata;

    /**
     * Constructs a brand new link related atom record.
     */
    protected ExMediaAtom() {
        _recdata = new byte[8];

        _header = new byte[8];
        LittleEndian.putShort(_header, 2, (short)getRecordType());
        LittleEndian.putInt(_header, 4, _recdata.length);
    }

    /**
     * Constructs the link related atom record from its
     *  source data.
     *
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected ExMediaAtom(byte[] source, int start, int len) {
        // Get the header
        _header = Arrays.copyOfRange(source, start, start+8);

        // Grab the record data
        _recdata = IOUtils.safelyClone(source,start+8, len-8, getMaxRecordLength());
    }

    /**
     * Gets the record type.
     * @return the record type.
     */
    public long getRecordType() { return RecordTypes.ExMediaAtom.typeID; }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     *
     * @param out the output stream to write to.
     * @throws java.io.IOException if an error occurs.
     */
    public void writeOut(OutputStream out) throws IOException {
        out.write(_header);
        out.write(_recdata);
    }

    /**
     * A 4-byte unsigned integer that specifies an ID for an external object.
     *
     * @return  A 4-byte unsigned integer that specifies an ID for an external object.
     */
    public int getObjectId(){
        return LittleEndian.getInt(_recdata, 0);
    }

    /**
     * A 4-byte unsigned integer that specifies an ID for an external object.
     *
     * @param id  A 4-byte unsigned integer that specifies an ID for an external object.
     */
    public void setObjectId(int id){
         LittleEndian.putInt(_recdata, 0, id);
    }

    /**
     *  A bit mask specifying options for displaying headers and footers
     *
     * @return A bit mask specifying options for displaying headers and footers
     */
    public int getMask(){
        return LittleEndian.getInt(_recdata, 4);
    }

    /**
     *  A bit mask specifying options for displaying video
     *
     * @param mask A bit mask specifying options for displaying video
     */
    public void setMask(int mask){
        LittleEndian.putInt(_recdata, 4, mask);
    }

    /**
     * @param bit the bit to check
     * @return whether the specified flag is set
     */
    public boolean getFlag(int bit){
        return (getMask() & bit) != 0;
    }

    /**
     * @param  bit the bit to set
     * @param  value whether the specified bit is set
     */
    public void setFlag(int bit, boolean value){
        int mask = getMask();
        if(value) mask |= bit;
        else mask &= ~bit;
        setMask(mask);
    }

    public String toString() {
        return GenericRecordJsonWriter.marshal(this);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "objectId", this::getObjectId,
            "flags", getBitsAsString(this::getMask, FLAG_MASKS, FLAG_NAMES)
        );
    }
}
